package com.liluo.moyan.modules.work.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liluo.moyan.framework.common.Result;
import com.liluo.moyan.modules.work.dto.PublishWorkRequest;
import com.liluo.moyan.modules.work.dto.UpdateWorkRequest;
import com.liluo.moyan.modules.work.service.WorkService;
import com.liluo.moyan.modules.work.vo.WorkVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 作品控制器
 */
@Tag(name = "作品管理")
@RestController
@RequestMapping("/api/works")
public class WorkController {
    
    @Autowired
    private WorkService workService;
    
    @Operation(summary = "发布作品")
    @PostMapping
    public Result<Long> publishWork(@Valid @RequestBody PublishWorkRequest request) {
        Long workId = workService.publishWork(request);
        return Result.success(workId);
    }
    
    @Operation(summary = "获取作品详情")
    @GetMapping("/{workId}")
    public Result<WorkVO> getWorkDetail(@PathVariable Long workId) {
        WorkVO workVO = workService.getWorkDetail(workId);
        return Result.success(workVO);
    }
    
    @Operation(summary = "更新作品")
    @PutMapping
    public Result<Void> updateWork(@Valid @RequestBody UpdateWorkRequest request) {
        workService.updateWork(request);
        return Result.success();
    }
    
    @Operation(summary = "删除作品")
    @DeleteMapping("/{workId}")
    public Result<Void> deleteWork(@PathVariable Long workId) {
        workService.deleteWork(workId);
        return Result.success();
    }
    
    @Operation(summary = "分页查询作品列表")
    @GetMapping
    public Result<Page<WorkVO>> getWorkList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<WorkVO> page = workService.getWorkList(pageNum, pageSize);
        return Result.success(page);
    }
    
    @Operation(summary = "查询用户的作品列表")
    @GetMapping("/user/{userId}")
    public Result<Page<WorkVO>> getUserWorks(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<WorkVO> page = workService.getUserWorks(userId, pageNum, pageSize);
        return Result.success(page);
    }
}
