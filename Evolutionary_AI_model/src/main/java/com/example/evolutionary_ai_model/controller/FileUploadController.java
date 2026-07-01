package com.example.evolutionary_ai_model.controller;

import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * 用法：文件上传控制器，提供通用文件上传功能。
 * 依赖 MinioService 处理文件存储，返回文件访问URL。
 */
@Slf4j
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class FileUploadController {

    //MinIO文件存储服务，处理文件上传持久化
    private final MinioService minioService;

    /**
     * 上传头像
     * 支持jpg、png、gif格式，生成唯一文件名后上传至MinIO的avatar目录
     *
     * @param file 头像文件
     * @return 头像访问URL
     */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // 校验文件是否为空
            if (file.isEmpty()) {
                return Result.fail("上传文件不能为空");
            }

            // 校验文件类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                return Result.fail("文件名不能为空");
            }

            // 提取文件后缀
            String suffix = "";
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                suffix = originalFilename.substring(dotIndex);
            }

            // 生成唯一文件名，防止重名覆盖
            String objectName = "avatar/" + UUID.randomUUID().toString() + suffix;

            // 上传文件到MinIO并获取访问URL
            String url = minioService.uploadFile(file, objectName);
            log.info("头像上传成功: {}", url);

            return Result.success("上传成功", url);
        } catch (Exception e) {
            log.error("头像上传失败", e);
            return Result.fail("头像上传失败: " + e.getMessage());
        }
    }
}
