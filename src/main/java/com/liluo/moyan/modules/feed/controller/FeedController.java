package com.liluo.moyan.modules.feed.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liluo.moyan.framework.common.Result;
import com.liluo.moyan.modules.feed.service.FeedService;
import com.liluo.moyan.modules.work.vo.WorkVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Feed流控制器
 */
@Tag(name = "Feed流")
@RestController
@RequestMapping("/api/feed")
public class FeedController {
    
    @Autowired
    private FeedService feedService;
    
    @Operation(summary = "获取关注Feed")
    @GetMapping("/follow")
    public Result<Page<WorkVO>> getFollowFeed(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<WorkVO> page = feedService.getFollowFeed(pageNum, pageSize);
        return Result.success(page);
    }
    
    @Operation(summary = "获取推荐Feed")
    @GetMapping("/recommend")
    public Result<Page<WorkVO>> getRecommendFeed(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<WorkVO> page = feedService.getRecommendFeed(pageNum, pageSize);
        return Result.success(page);
    }
}
