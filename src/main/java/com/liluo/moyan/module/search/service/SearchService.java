package com.liluo.moyan.module.search.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liluo.moyan.module.search.document.WorkDocument;
import com.liluo.moyan.module.work.entity.Work;
import com.liluo.moyan.module.work.mapper.WorkMapper;
import com.liluo.moyan.module.search.repository.WorkDocumentRepository;
import com.liluo.moyan.module.work.service.WorkService;
import com.liluo.moyan.module.work.vo.WorkVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    private RestClient restClient;

    @Autowired
    private WorkDocumentRepository workDocumentRepository;

    @Autowired
    private WorkMapper workMapper;

    @Autowired
    private WorkService workService;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
            // 构建查询 JSON
            String queryJson = """
                {
                    "from": %d,
                    "size": %d,
                    "query": {
                        "multi_match": {
                            "fields": ["title^2", "content"],
                            "query": "%s"
                        }
                    },
                    "highlight": {
                        "fields": {
                            "title": {"pre_tags": ["<em>"], "post_tags": ["</em>"]},
                            "content": {"pre_tags": ["<em>"], "post_tags": ["</em>"], "fragment_size": 200}
                        }
                    },
                    "sort": [{"_score": "desc"}]
                }
                """.formatted(from, size, keyword.replace("\"", "\\\""));

            Request request = new Request("POST", "/works/_search");
            request.setEntity(new NStringEntity(queryJson, ContentType.APPLICATION_JSON));

            Response response = restClient.performRequest(request);
            String responseBody = EntityUtils.toString(response.getEntity());

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode hits = root.path("hits").path("hits");

            log.info("ES搜索返回 {} 条结果", hits.size());

            List<WorkVO> results = new ArrayList<>();
            for (JsonNode hit : hits) {
                try {
                    JsonNode source = hit.path("_source");
                    Long docId = source.path("id").asLong();

                    // 获取高亮片段
                    JsonNode highlight = hit.path("highlight");
                    String highlightedTitle = getHighlightedField(highlight, "title");
                    String highlightedContent = getHighlightedField(highlight, "content");

                    log.debug("Hit docId={}, highlightedTitle={}, highlightedContent={}", docId, highlightedTitle, highlightedContent);

                    WorkVO vo = workService.getWorkDetail(docId);

                    if (highlightedTitle != null) {
                        vo.setTitle(highlightedTitle);
                    }
                    if (highlightedContent != null) {
                        vo.setContent(highlightedContent);
                    }

                    results.add(vo);
                } catch (Exception e) {
                    log.error("处理搜索结果失败", e);
                }
            }
            return results;

        } catch (Exception e) {
            log.error("ES搜索失败(异常类型: {})，降级为DB查询，原因: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            List<WorkVO> dbResults = searchFromDB(keyword, from, size);
            log.info("DB降级搜索返回 {} 条结果（无高亮）", dbResults.size());
            return dbResults;
        }
    }

    /**
     * 从 highlight JSON 中获取高亮字段
     */
    private String getHighlightedField(JsonNode highlight, String fieldName) {
        if (highlight.isMissingNode() || highlight.isEmpty()) {
            return null;
        }
        JsonNode field = highlight.path(fieldName);
        if (!field.isMissingNode() && field.isArray() && field.size() > 0) {
            return field.get(0).asText();
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
            WorkDocument doc = convertToDocument(work);
            workDocumentRepository.save(doc);
            log.info("作品 {} ES索引成功", work.getId());
        } catch (Exception e) {
            log.error("作品 {} ES索引失败", work.getId(), e);
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

    private WorkDocument convertToDocument(Work work) {
        WorkDocument doc = new WorkDocument();
        BeanUtils.copyProperties(work, doc);
        return doc;
    }
}
