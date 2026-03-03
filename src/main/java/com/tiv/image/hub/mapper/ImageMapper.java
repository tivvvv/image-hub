package com.tiv.image.hub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tiv.image.hub.model.entity.Image;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ImageMapper extends BaseMapper<Image> {

}