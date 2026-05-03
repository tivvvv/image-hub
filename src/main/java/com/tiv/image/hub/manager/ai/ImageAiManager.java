package com.tiv.image.hub.manager.ai;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.tiv.image.hub.common.BusinessCodeEnum;
import com.tiv.image.hub.exception.BusinessException;
import com.tiv.image.hub.model.dto.image.request.ImageExpandTaskCreateRequest;
import com.tiv.image.hub.model.vo.ImageExpandTaskCreateVO;
import com.tiv.image.hub.model.vo.ImageExpandTaskStatusQueryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 阿里云百炼 AI 图像处理服务
 */
@Slf4j
@Component
public class ImageAiManager {

    @Value("${aliyun.apiKey}")
    private String apiKey;

    /**
     * 扩图任务创建地址
     */
    public static final String IMAGE_EXPAND_TASK_CREATE_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";

    /**
     * 扩图任务状态查询地址
     */
    public static final String IMAGE_EXPAND_TASK_STATUS_QUERY_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";

    /**
     * 创建扩图任务
     *
     * @param imageExpandTaskCreateRequest
     * @return
     */
    public ImageExpandTaskCreateVO createImageExpandTask(ImageExpandTaskCreateRequest imageExpandTaskCreateRequest) {
        if (imageExpandTaskCreateRequest == null) {
            throw new BusinessException(BusinessCodeEnum.PARAMS_ERROR, "扩图参数为空");
        }

        // 构造请求
        HttpRequest httpRequest = HttpRequest.post(IMAGE_EXPAND_TASK_CREATE_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                // 必须开启异步处理
                .header("X-DashScope-Async", "enable")
                .body(JSONUtil.toJsonStr(imageExpandTaskCreateRequest));

        // 处理响应
        try (HttpResponse httpResponse = httpRequest.execute()) {
            if (!httpResponse.isOk()) {
                log.error("扩图任务创建请求异常: {}", httpResponse.body());
                throw new BusinessException(BusinessCodeEnum.OPERATION_ERROR, "AI 扩图失败");
            }

            ImageExpandTaskCreateVO imageExpandTaskCreateVO = JSONUtil.toBean(httpResponse.body(), ImageExpandTaskCreateVO.class);
            if (imageExpandTaskCreateVO.getCode() != null) {
                String errorMessage = imageExpandTaskCreateVO.getMessage();
                log.error("扩图任务创建失败: {}", errorMessage);
                throw new BusinessException(BusinessCodeEnum.OPERATION_ERROR, "AI 扩图失败, " + errorMessage);
            }
            return imageExpandTaskCreateVO;
        }
    }

    /**
     * 查询扩图任务状态
     *
     * @param taskId
     * @return
     */
    public ImageExpandTaskStatusQueryVO queryImageExpandTaskStatus(String taskId) {
        if (StrUtil.isBlank(taskId)) {
            throw new BusinessException(BusinessCodeEnum.PARAMS_ERROR, "任务 id 为空");
        }

        String url = String.format(IMAGE_EXPAND_TASK_STATUS_QUERY_URL, taskId);
        HttpRequest httpRequest = HttpRequest.get(url)
                .header("Authorization", "Bearer " + apiKey);

        // 处理响应
        try (HttpResponse httpResponse = httpRequest.execute()) {
            if (!httpResponse.isOk()) {
                log.error("查询扩图任务状态请求异常: {}", httpResponse.body());
                throw new BusinessException(BusinessCodeEnum.OPERATION_ERROR, "查询扩图任务结果失败");
            }
            return JSONUtil.toBean(httpResponse.body(), ImageExpandTaskStatusQueryVO.class);
        }
    }

}