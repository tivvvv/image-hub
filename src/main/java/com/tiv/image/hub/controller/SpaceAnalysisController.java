package com.tiv.image.hub.controller;

import com.tiv.image.hub.service.SpaceAnalysisService;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;

@Controller
public class SpaceAnalysisController {

    @Resource
    private SpaceAnalysisService spaceAnalysisService;

}