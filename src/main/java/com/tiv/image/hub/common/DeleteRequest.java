package com.tiv.image.hub.common;

import lombok.Data;

import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * 通用删除请求类
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    @Min(value = 1, message = "id不能小于1")
    private Long id;

    private static final long serialVersionUID = 1L;

}