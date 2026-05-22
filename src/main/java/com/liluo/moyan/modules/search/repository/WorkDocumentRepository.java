package com.liluo.moyan.modules.search.repository;

import com.liluo.moyan.modules.search.document.WorkDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 作品文档 Repository
 */
@Repository
public interface WorkDocumentRepository extends ElasticsearchRepository<WorkDocument, Long> {
    
    /**
     * 根据关键词搜索作品（标题或内容）
     * 多字段匹配：
     * - title^2: 标题权重为2（更相关）
     * - content: 内容权重为1
     * 
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return 搜索结果
     * 注意：当前SearchService使用ElasticsearchClient实现更复杂的高亮搜索
     * 此方法可作为简单搜索的备选方案
     */
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^2\", \"content\"]}}")
    Page<WorkDocument> searchByKeyword(String keyword, Pageable pageable);
    
    /**
     * 根据用户ID查询作品
     * 可用于个人主页展示用户的所有作品
     * 
     * @param userId 用户ID
     * @return 作品列表
     */
    List<WorkDocument> findByUserId(Long userId);
}
