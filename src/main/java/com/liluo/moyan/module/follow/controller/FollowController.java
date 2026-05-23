package com.liluo.moyan.module.follow.controller;

import com.liluo.moyan.common.result.Result;
import com.liluo.moyan.module.follow.service.FollowService;
import com.liluo.moyan.module.follow.vo.FollowUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @Operation(summary = "获取粉丝列表")
    @GetMapping("/followers/{userId}")
    public Result<List<FollowUserVO>> getFollowers(@PathVariable Long userId) {
        return Result.success(followService.getFollowers(userId));
    }

    @Operation(summary = "获取关注列表")
    @GetMapping("/following/{userId}")
    public Result<List<FollowUserVO>> getFollowing(@PathVariable Long userId) {
        return Result.success(followService.getFollowing(userId));
    }
}
