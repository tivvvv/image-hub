package com.tiv.image.hub.model.dto.picture;

import com.tiv.image.hub.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 图片审核请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PictureReviewRequest extends PageRequest implements Serializable {

    /**
     * 图片id
     */
    @NotNull(message = "图片id不能为空")
    private Long id;

    /**
     * 审核状态 0:审核中,1:通过,2:驳回
     */
    @NotNull(message = "审核状态不能为空")
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    @NotNull(message = "审核信息不能为空")
    private String reviewMessage;

    private static final long serialVersionUID = 1L;

}
