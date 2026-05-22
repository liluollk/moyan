package com.liluo.moyan.modules.search.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Elasticsearch 作品文档
 */
@Data
@Document(indexName = "works")  // 索引名称
public class WorkDocument implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 作品ID
     */
    @Id
    private Long id;
    
    /**
     * 作者ID
     */
    @Field(type = FieldType.Long)
    private Long userId;
    
    /**
     * 作品标题（使用 IK 分词器）
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;
    
    /**
     * 作品内容（使用 IK 分词器）
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;
    
    /**
     * 图片URL数组（JSON字符串）
     */
    @Field(type = FieldType.Keyword)
    private String images;
    
    /**
     * 点赞数
     */
    @Field(type = FieldType.Integer)
    private Integer likeCount;
    
    /**
     * 收藏数
     */
    @Field(type = FieldType.Integer)
    private Integer favoriteCount;
    
    /**
     * 评论数
     */
    @Field(type = FieldType.Integer)
    private Integer commentCount;
    
    /**
     * 热度分数
     */
    @Field(type = FieldType.Double)
    private Double hotScore;
    
    /**
     * 创建时间
     */
    @Field(type = FieldType.Date)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @Field(type = FieldType.Date)
    private LocalDateTime updateTime;
}
