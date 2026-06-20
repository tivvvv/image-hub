package com.tiv.image.hub.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiv.image.hub.mapper.SpacePermissionMapper;
import com.tiv.image.hub.model.entity.SpacePermission;
import com.tiv.image.hub.service.SpacePermissionService;
import org.springframework.stereotype.Service;

@Service
public class SpacePermissionServiceImpl extends ServiceImpl<SpacePermissionMapper, SpacePermission>
        implements SpacePermissionService {

}