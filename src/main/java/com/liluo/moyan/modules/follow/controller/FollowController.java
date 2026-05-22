package com.liluo.moyan.modules.follow.controller;

import com.liluo.moyan.framework.common.Result;
import com.liluo.moyan.modules.follow.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 关注控制器
 */
@Tag(name = "关注管理")
@RestController
@RequestMapping("/api/follows")
public class FollowController {
    
    @Autowired
    private FollowService followService;
    
    @Operation(summary = "关注用户")
    @PostMapping("/{userId}")
    public Result<Void> followUser(@PathVariable Long userId) {
        followService.followUser(userId);
        return Result.success();
    }
    
    @Operation(summary = "取消关注")
    @DeleteMapping("/{userId}")
    public Result<Void> unfollowUser(@PathVariable Long userId) {
        followService.unfollowUser(userId);
        return Result.success();
    }
    
    @Operation(summary = "检查是否关注")
    @GetMapping("/check/{userId}")
    public Result<Boolean> isFollowing(@PathVariable Long userId) {
        boolean following = followService.isFollowing(userId);
        return Result.success(following);
    }
}
