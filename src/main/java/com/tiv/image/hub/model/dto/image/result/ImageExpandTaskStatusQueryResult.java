package com.tiv.image.hub.model.dto.image.result;

import lombok.Data;

/**
 * 扩图任务状态查询结果
 */
public class ImageExpandTaskStatusQueryResult {

    /**
     * 请求唯一标识
     */
    private String requestId;

    /**
     * 输出信息
     */
    private Output output;

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

        /**
         * 提交时间
         */
        private String submitTime;

        /**
         * 调度时间
         */
        private String scheduledTime;

        /**
         * 结束时间
         */
        private String endTime;

        /**
         * 输出图像的 url
         */
        private String outputImageUrl;

        /**
         * 错误码, 仅在请求失败时返回
         */
        private String code;

        /**
         * 错误信息, 仅在请求失败时返回
         */
        private String message;

        /**
         * 任务指标信息
         */
        private TaskMetrics taskMetrics;

    }

    @Data
    public static class TaskMetrics {

        /**
         * 总任务数
         */
        private Integer total;

        /**
         * 成功任务数
         */
        private Integer succeeded;

        /**
         * 失败任务数
         */
        private Integer failed;

    }

}