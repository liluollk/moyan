package com.liluo.moyan.modules.work.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 作品响应 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkVO {
    
    private Long id;
    
    private Long userId;
    
    private String nickname;  // 作者昵称
    
    private String avatar;    // 作者头像
    
    private String title;
    
    private String content;
    
    private List<String> images;
    
    private Integer likeCount;
    
    private Integer favoriteCount;
    
    private Integer commentCount;
    
    private Boolean isLiked;      // 当前用户是否已点赞

    private Boolean isFavorited;  // 当前用户是否已收藏

    private Boolean isFollowing;  // 当前用户是否已关注该作者

    private LocalDateTime createTime;
}
