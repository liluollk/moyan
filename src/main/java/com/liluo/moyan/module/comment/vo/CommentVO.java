package com.liluo.moyan.module.comment.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评论响应 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentVO {

    private Long id;

    private Long userId;

    private String nickname;  // 评论者昵称

    private String avatar;    // 评论者头像

    private Long workId;

    private String content;

    private LocalDateTime createTime;
}
