package com.liluo.moyan.module.file.service;

import com.aliyun.oss.OSS;
import com.liluo.moyan.common.config.OssConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件上传服务
 * 支持阿里云 OSS 和本地文件存储两种模式
 */
@Slf4j
@Service
public class FileService {

    @Autowired(required = false)
    private OSS ossClient;

    @Autowired(required = false)
    private OssConfig ossConfig;

    @Value("${aliyun.oss.enabled:false}")
    private boolean ossEnabled;

    @Value("${file.storage.local-path:uploads}")
    private String localPath;

    @Value("${file.storage.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * 上传文件，自动选择 OSS 或本地存储
     */
    public String uploadFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID() + extension;

        if (ossEnabled && ossClient != null) {
            return uploadToOss(file, fileName);
        } else {
            return uploadToLocal(file, fileName);
        }
    }

    /**
     * 上传到阿里云 OSS
     */
    private String uploadToOss(MultipartFile file, String fileName) throws IOException {
        log.info("上传文件到 OSS: {}", file.getOriginalFilename());
        ossClient.putObject(ossConfig.getBucketName(), fileName, file.getInputStream());
        String url = ossConfig.getUrlPrefix() + fileName;
        log.info("OSS 上传成功: {}", url);
        return url;
    }

    /**
     * 上传到本地磁盘
     */
    private String uploadToLocal(MultipartFile file, String fileName) throws IOException {
        log.info("上传文件到本地存储: {}", file.getOriginalFilename());
        // 使用项目根目录下的绝对路径，避免 Tomcat 临时目录问题
        String userDir = System.getProperty("user.dir");
        Path uploadDir = Paths.get(userDir, localPath).toAbsolutePath();
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        Path filePath = uploadDir.resolve(fileName);
        file.transferTo(filePath.toFile());
        String url = baseUrl + "/uploads/" + fileName;
        log.info("本地上传成功: {}", url);
        return url;
    }
}
