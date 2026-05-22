package com.liluo.moyan.modules.file.controller;

import com.liluo.moyan.framework.common.Result;
import com.liluo.moyan.modules.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 */
@Slf4j
@Tag(name = "文件管理")
@RestController
@RequestMapping("/api/files")
public class FileController {
    
    @Autowired
    private FileService fileService;
    
    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String url = fileService.uploadFile(file);
            return Result.success(url);
        } catch (Exception e) {
            // 记录详细错误日志（包含异常信息）
            log.error("文件上传失败: {}", e.getMessage(), e);
            // 返回通用错误消息给前端（不暴露内部细节）
            return Result.error("文件上传失败");
        }
    }
}
