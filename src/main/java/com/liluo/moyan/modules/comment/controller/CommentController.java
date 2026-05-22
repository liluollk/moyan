package com.liluo.moyan.modules.comment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liluo.moyan.framework.common.Result;
import com.liluo.moyan.modules.comment.dto.AddCommentRequest;
import com.liluo.moyan.modules.comment.service.CommentService;
import com.liluo.moyan.modules.comment.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 评论控制器
 */
@Tag(name = "评论管理")
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Operation(summary = "添加评论")
    @PostMapping
    public Result<CommentVO> addComment(@Valid @RequestBody AddCommentRequest request) {
        CommentVO commentVO = commentService.addComment(request);
        return Result.success(commentVO);
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return Result.success();
    }

    @Operation(summary = "获取作品评论列表")
    @GetMapping("/work/{workId}")
    public Result<Page<CommentVO>> getComments(
            @PathVariable Long workId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<CommentVO> page = commentService.getComments(workId, pageNum, pageSize);
        return Result.success(page);
    }
}
