package com.liluo.moyan.modules.comment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liluo.moyan.modules.comment.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
