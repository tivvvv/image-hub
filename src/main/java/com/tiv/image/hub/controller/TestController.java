package com.tiv.image.hub.controller;

import com.tiv.image.hub.common.BusinessResponse;
import com.tiv.image.hub.util.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试controller
 */
@RestController
@RequestMapping("/")
public class TestController {

    /**
     * 心跳检查
     */
    @GetMapping("/heartBeat")
    public BusinessResponse<String> heartBeat() {
        return ResultUtils.success("ok");
    }

}
