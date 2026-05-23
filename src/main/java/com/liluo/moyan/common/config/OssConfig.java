package com.liluo.moyan.common.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云 OSS 配置
 * 仅当 aliyun.oss.enabled=true 时创建 OSS 客户端，否则使用本地文件存储
 */
@Configuration
public class OssConfig {

    @Value("${aliyun.oss.endpoint:}")
    private String endpoint;

    @Value("${aliyun.oss.access-key-id:}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret:}")
    private String accessKeySecret;

    @Getter
    @Value("${aliyun.oss.bucket-name:}")
    private String bucketName;

    @Getter
    @Value("${aliyun.oss.url-prefix:}")
    private String urlPrefix;

    @Bean
    @ConditionalOnProperty(name = "aliyun.oss.enabled", havingValue = "true")
    public OSS ossClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

}
