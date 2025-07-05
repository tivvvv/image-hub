package com.tiv.image.hub.controller;

import com.tiv.image.hub.annotation.AuthCheck;
import com.tiv.image.hub.common.BusinessCodeEnum;
import com.tiv.image.hub.common.BusinessResponse;
import com.tiv.image.hub.exception.BusinessException;
import com.tiv.image.hub.manager.CosManager;
import com.tiv.image.hub.util.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

/**
 * 文件controller
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private CosManager cosManager;

    /**
     * 上传文件
     *
     * @param multipartFile
     * @return
     */
    @PostMapping("/upload")
    @AuthCheck(mustRole = "admin")
    public BusinessResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile) {
        String filename = multipartFile.getOriginalFilename();
        String filePath = String.format("/image-hub/%s", filename);
        File file = null;
        try {
            // MultipartFile转File
            file = File.createTempFile(filePath, null);
            multipartFile.transferTo(file);
            // 上传文件
            cosManager.putObject(filePath, file);
            // 返回可访问地址
            return ResultUtils.success(filePath);
        } catch (IOException e) {
            log.error("file upload error, file path = {}", filePath, e);
            throw new BusinessException(BusinessCodeEnum.OPERATION_ERROR, "文件上传异常");
        } finally {
            // 删除临时文件
            if (file != null) {
                boolean deleted = file.delete();
                if (!deleted) {
                    log.error("temporary file delete error, file path = {}", filePath);
                }
            }
        }
    }

}
