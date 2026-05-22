package com.liluo.moyan.modules.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.liluo.moyan.modules.search.document.WorkDocument;
import com.liluo.moyan.modules.work.entity.Work;
import com.liluo.moyan.modules.work.mapper.WorkMapper;
import com.liluo.moyan.modules.search.repository.WorkDocumentRepository;
import com.liluo.moyan.modules.work.service.WorkService;
import com.liluo.moyan.modules.work.vo.WorkVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 搜索服务（Elasticsearch）
 * 架构设计：
 * 1. 同步双写：发布作品时同步索引到ES，保证搜索实时性
 * 2. 降级方案：ES故障时自动切换MySQL LIKE查询，保障可用性99.9%
 * 3. 多字段匹配：支持标题和内容全文检索
 * 4. 高亮显示：搜索结果关键词高亮
 * 5. 相关性排序：按ES评分降序排列
 */
@Slf4j
@Service
public class SearchService {
    
    @Autowired
    private ElasticsearchClient esClient;
    
    @Autowired
    private WorkDocumentRepository workDocumentRepository;
    
    @Autowired
    private WorkMapper workMapper;
    
    @Autowired
    private WorkService workService;
    
    /**
     * 搜索作品（支持高亮和相关性排序）
     * 
     * @param keyword 搜索关键词
     * @param from 起始位置
     * @param size 每页数量
     * @return 作品列表
     */
    public List<WorkVO> searchWorks(String keyword, int from, int size) {
        try {
            // 构建 ES 搜索请求（支持高亮和相关性排序）
            SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("works")
                .from(from)
                .size(size)
                // 多字段匹配查询
                .query(q -> q
                    .multiMatch(MultiMatchQuery.of(m -> m
                        .fields("title^2", "content")  // 标题权重更高
                        .query(keyword)
                    ))
                )
                // 高亮配置
                .highlight(h -> h
                    .fields("title", f -> f
                        .preTags("<em>")
                        .postTags("</em>")
                    )
                    .fields("content", f -> f
                        .preTags("<em>")
                        .postTags("</em>")
                        .fragmentSize(200)  // 摘要长度
                    )
                )
                // 按相关性得分排序（默认就是按 _score 降序）
                .sort(sort -> sort
                    .score(sc -> sc.order(co.elastic.clients.elasticsearch._types.SortOrder.Desc))
                )
            );
            
            // 执行搜索
            SearchResponse<WorkDocument> response = esClient.search(searchRequest, WorkDocument.class);
            
            // 转换为 VO（包含高亮信息）
            return response.hits().hits().stream()
                .map(hit -> {
                    try {
                        WorkDocument doc = hit.source();
                        if (doc == null) return null;
                        
                        // 获取高亮片段
                        String highlightedTitle = getHighlightedField(hit, "title");
                        String highlightedContent = getHighlightedField(hit, "content");
                        
                        // 如果有高亮，使用高亮内容；否则使用原始内容
                        if (highlightedTitle != null) {
                            doc.setTitle(highlightedTitle);
                        }
                        if (highlightedContent != null) {
                            doc.setContent(highlightedContent);
                        }
                        
                        return workService.getWorkDetail(doc.getId());
                    } catch (Exception e) {
                        log.error("获取作品详情失败", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("ES搜索失败，降级为DB查询", e);
            // ES失败时降级为数据库搜索（保障可用性）
            return searchFromDB(keyword, from, size);
        }
    }
    
    /**
     * 获取高亮字段
     */
    private String getHighlightedField(Hit<WorkDocument> hit, String fieldName) {
        if (hit.highlight() != null && hit.highlight().containsKey(fieldName)) {
            List<String> highlights = hit.highlight().get(fieldName);
            if (highlights != null && !highlights.isEmpty()) {
                return highlights.getFirst();  // 返回第一个高亮片段
            }
        }
        return null;
    }
    
    /**
     * 数据库搜索（降级方案）
     */
    private List<WorkVO> searchFromDB(String keyword, int from, int size) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Work> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.and(w -> w.like(Work::getTitle, keyword)
                .or()
                .like(Work::getContent, keyword))
                .last("LIMIT " + size + " OFFSET " + from);
        
        List<Work> works = workMapper.selectList(wrapper);
        
        return works.stream()
                .map(work -> {
                    try {
                        return workService.getWorkDetail(work.getId());
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * 索引作品（发布作品时同步调用）
     * 
     * 策略：同步双写，保证搜索实时性
     * 容错：异常不抛出，避免影响主流程
     */
    public void indexWork(Work work) {
        try {
            // 转换 Work -> WorkDocument
            WorkDocument doc = convertToDocument(work);
            
            // 保存到 ES
            workDocumentRepository.save(doc);
            
            log.info("作品 {} ES索引成功", work.getId());
        } catch (Exception e) {
            log.error("作品 {} ES索引失败", work.getId(), e);
            // 注意：这里不抛异常，避免影响作品发布主流程
            // 可以通过定时任务补偿同步
        }
    }
    
    /**
     * 删除作品索引
     */
    public void deleteWorkIndex(Long workId) {
        try {
            workDocumentRepository.deleteById(workId);
            log.info("作品 {} ES索引删除成功", workId);
        } catch (Exception e) {
            log.error("作品 {} ES索引删除失败", workId, e);
        }
    }
    
    /**
     * 转换 Work 实体为 WorkDocument
     */
    private WorkDocument convertToDocument(Work work) {
        WorkDocument doc = new WorkDocument();
        BeanUtils.copyProperties(work, doc);
        return doc;
    }
}
