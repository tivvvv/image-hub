package com.tiv.image.hub.model.dto.image.request;

import cn.hutool.core.annotation.Alias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 扩图任务创建请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageExpandTaskCreateRequest implements Serializable {

    /**
     * 模型
     */
    @Builder.Default
    private String model = "image-out-painting";

    /**
     * 输入图像信息
     */
    private Input input;

    /**
     * 图像处理参数
     */
    private Parameters parameters;

    private static final long serialVersionUID = 1L;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Input {

        /**
         * 图像 url
         */
        @Alias("image_url")
        private String imageUrl;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Parameters {

        /**
         * 可选, 逆时针旋转角度
         */
        private Integer angle;

        /**
         * 可选, 输出图像的宽高比 ["", "1:1", "3:4", "4:3", "9:16", "16:9"]
         */
        @Alias("output_ratio")
        private String outputRatio;

        /**
         * 可选, 图像居中, 在水平方向上按比例扩展
         */
        @Alias("x_scale")
        @JsonProperty("xScale")
        private Float xScale;

        /**
         * 可选, 图像居中, 在垂直方向上按比例扩展
         */
        @Alias("y_scale")
        @JsonProperty("yScale")
        private Float yScale;

        /**
         * 可选, 在图像上方添加像素
         */
        @Alias("top_offset")
        private Integer topOffset;

        /**
         * 可选, 在图像下方添加像素
         */
        @Alias("bottom_offset")
        private Integer bottomOffset;

        /**
         * 可选, 在图像左侧添加像素
         */
        @Alias("left_offset")
        private Integer leftOffset;

        /**
         * 可选, 在图像右侧添加像素
         */
        @Alias("right_offset")
        private Integer rightOffset;

        /**
         * 可选, 是否开启图像最佳质量模式
         */
        @Alias("best_quality")
        private Boolean bestQuality;

        /**
         * 可选, 是否限制生成的图像文件大小
         */
        @Alias("limit_image_size")
        private Boolean limitImageSize;

        /**
         * 可选, 是否添加水印
         */
        @Alias("add_watermark")
        private Boolean addWatermark = false;
    }

}