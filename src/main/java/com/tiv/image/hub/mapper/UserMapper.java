package com.tiv.image.hub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tiv.image.hub.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

}