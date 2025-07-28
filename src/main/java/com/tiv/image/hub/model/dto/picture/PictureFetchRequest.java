package com.tiv.image.hub.model.dto.picture;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 图片抓取请求
 */
@Data
public class PictureFetchRequest implements Serializable {

    /**
     * 搜索词
     */
    @NotBlank(message = "搜索词不能为空")
    private String searchText;

    /**
     * 数量
     */
    @Max(value = 30, message = "单次请求最多抓取30张图片")
    private int fetchSize = 20;

    /**
     * 图片名称前缀
     */
    private String picNamePrefix;

    private static final long serialVersionUID = 1L;

}
