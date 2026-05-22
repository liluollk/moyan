package com.liluo.moyan.modules.like.controller;

import com.liluo.moyan.framework.common.Result;
import com.liluo.moyan.modules.like.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 点赞控制器
 */
@Tag(name = "点赞管理")
@RestController
@RequestMapping("/api/likes")
public class LikeController {
    
    @Autowired
    private LikeService likeService;
    
    @Operation(summary = "点赞作品")
    @PostMapping("/{workId}")
    public Result<Void> like(@PathVariable Long workId) {
        likeService.like(workId);
        return Result.success();
    }
    
    @Operation(summary = "取消点赞")
    @DeleteMapping("/{workId}")
    public Result<Void> unlike(@PathVariable Long workId) {
        likeService.unlike(workId);
        return Result.success();
    }
    
    @Operation(summary = "查询作品点赞数")
    @GetMapping("/{workId}/count")
    public Result<Long> getLikeCount(@PathVariable Long workId) {
        return Result.success(likeService.getLikeCount(workId));
    }
    
    @Operation(summary = "查询当前用户是否已点赞")
    @GetMapping("/{workId}/status")
    public Result<Boolean> isLiked(@PathVariable Long workId) {
        return Result.success(likeService.isLiked(workId));
    }
}
