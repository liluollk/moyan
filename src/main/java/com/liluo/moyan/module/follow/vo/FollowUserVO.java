package com.liluo.moyan.module.follow.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowUserVO {

    private Long userId;
    private String nickname;
    private String avatar;
    private Boolean isFollowing;
}
