package com.liluo.moyan.module.favorite.controller;

import com.liluo.moyan.common.result.Result;
import com.liluo.moyan.module.favorite.service.FavoriteService;
import com.liluo.moyan.module.work.vo.WorkVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收藏控制器
 */
@Tag(name = "收藏管理")
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Operation(summary = "收藏作品")
    @PostMapping("/{workId}")
    public Result<Void> favoriteWork(@PathVariable Long workId) {
        favoriteService.favoriteWork(workId);
        return Result.success();
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/{workId}")
    public Result<Void> unfavoriteWork(@PathVariable Long workId) {
        favoriteService.unfavoriteWork(workId);
        return Result.success();
    }

    @Operation(summary = "获取我的收藏列表")
    @GetMapping
    public Result<List<WorkVO>> getMyFavorites() {
        return Result.success(favoriteService.getMyFavorites());
    }
}
