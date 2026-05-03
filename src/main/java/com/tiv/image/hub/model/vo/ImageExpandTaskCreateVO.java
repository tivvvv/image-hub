package com.tiv.image.hub.model.vo;

import lombok.Data;

/**
 * 扩图任务创建视图
 */
@Data
public class ImageExpandTaskCreateVO {

    /**
     * 任务的输出信息
     */
    private Output output;

    /**
     * 错误码, 仅在请求失败时返回
     */
    private String code;

    /**
     * 错误信息, 仅在请求失败时返回
     */
    private String message;

    /**
     * 请求唯一标识
     */
    private String requestId;

    @Data
    public static class Output {

        /**
         * 任务 id
         */
        private String taskId;

        /**
         * 任务状态
         */
        private String taskStatus;

    }

}