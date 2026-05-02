package com.tiv.image.hub.model.dto.image.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 扩图请求
 */
@Data
public class ImageExpandRequest implements Serializable {

    /**
     * 图片id
     */
    @NotNull(message = "图片id不能为空")
    private Long id;

    /**
     * 扩图参数
     */
    @NotNull(message = "扩图参数不能为空")
    private ImageExpandTaskCreateRequest.Parameters parameters;

    private static final long serialVersionUID = 1L;

}