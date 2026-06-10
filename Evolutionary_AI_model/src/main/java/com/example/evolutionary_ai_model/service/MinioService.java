package com.example.evolutionary_ai_model.service;

import com.example.evolutionary_ai_model.config.MinioProperties;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 用法：MinIO服务类，负责对象存储的核心业务逻辑。
 * 提供文件上传、下载、删除、预览URL生成等常用功能，依赖MinioClient进行数据持久化操作。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    /**
     * 检查存储桶是否存在，不存在则创建
     *
     * @param bucketName 存储桶名称
     * @return 是否创建成功
     */
    public boolean ensureBucketExists(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("创建存储桶成功: {}", bucketName);
            }
            return true;
        } catch (Exception e) {
            log.error("检查或创建存储桶失败: {}", bucketName, e);
            return false;
        }
    }

    /**
     * 上传文件
     *
     * @param file 文件
     * @param objectName 对象名称（文件在MinIO中的路径）
     * @return 文件访问路径
     */
    public String uploadFile(MultipartFile file, String objectName) {
        return uploadFile(file, objectName, minioProperties.getBucketName());
    }

    /**
     * 上传文件到指定存储桶
     *
     * @param file 文件
     * @param objectName 对象名称
     * @param bucketName 存储桶名称
     * @return 文件访问路径
     */
    public String uploadFile(MultipartFile file, String objectName, String bucketName) {
        try {
            ensureBucketExists(bucketName);
            
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            
            log.info("文件上传成功: {}/{}", bucketName, objectName);
            return getObjectUrl(objectName, bucketName);
        } catch (Exception e) {
            log.error("文件上传失败: {}", objectName, e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 通过输入流上传文件
     *
     * @param inputStream 输入流
     * @param objectName 对象名称
     * @param contentType 内容类型
     * @param size 文件大小
     * @return 文件访问路径
     */
    public String uploadFile(InputStream inputStream, String objectName, String contentType, long size) {
        try {
            String bucketName = minioProperties.getBucketName();
            ensureBucketExists(bucketName);
            
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build());
            
            log.info("文件上传成功: {}/{}", bucketName, objectName);
            return getObjectUrl(objectName, bucketName);
        } catch (Exception e) {
            log.error("文件上传失败: {}", objectName, e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     *
     * @param objectName 对象名称
     * @return 文件输入流
     */
    public InputStream downloadFile(String objectName) {
        return downloadFile(objectName, minioProperties.getBucketName());
    }

    /**
     * 从指定存储桶下载文件
     *
     * @param objectName 对象名称
     * @param bucketName 存储桶名称
     * @return 文件输入流
     */
    public InputStream downloadFile(String objectName, String bucketName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("文件下载失败: {}", objectName, e);
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     *
     * @param objectName 对象名称
     * @return 是否删除成功
     */
    public boolean deleteFile(String objectName) {
        return deleteFile(objectName, minioProperties.getBucketName());
    }

    /**
     * 从指定存储桶删除文件
     *
     * @param objectName 对象名称
     * @param bucketName 存储桶名称
     * @return 是否删除成功
     */
    public boolean deleteFile(String objectName, String bucketName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            log.info("文件删除成功: {}/{}", bucketName, objectName);
            return true;
        } catch (Exception e) {
            log.error("文件删除失败: {}", objectName, e);
            return false;
        }
    }

    /**
     * 获取文件预览URL（临时访问链接）
     *
     * @param objectName 对象名称
     * @param expiry 过期时间（秒）
     * @return 预览URL
     */
    public String getPresignedUrl(String objectName, int expiry) {
        return getPresignedUrl(objectName, minioProperties.getBucketName(), expiry);
    }

    /**
     * 获取文件预览URL
     *
     * @param objectName 对象名称
     * @param bucketName 存储桶名称
     * @param expiry 过期时间（秒）
     * @return 预览URL
     */
    public String getPresignedUrl(String objectName, String bucketName, int expiry) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(expiry, TimeUnit.SECONDS)
                    .build());
        } catch (Exception e) {
            log.error("获取预览URL失败: {}", objectName, e);
            throw new RuntimeException("获取预览URL失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件访问路径（永久访问链接，适用于公开桶）
     *
     * @param objectName 对象名称
     * @return 文件访问路径
     */
    public String getObjectUrl(String objectName) {
        return getObjectUrl(objectName, minioProperties.getBucketName());
    }

    /**
     * 获取文件访问路径
     *
     * @param objectName 对象名称
     * @param bucketName 存储桶名称
     * @return 文件访问路径
     */
    public String getObjectUrl(String objectName, String bucketName) {
        return String.format("%s/%s/%s", minioProperties.getEndpoint(), bucketName, objectName);
    }

    /**
     * 检查文件是否存在
     *
     * @param objectName 对象名称
     * @return 是否存在
     */
    public boolean fileExists(String objectName) {
        return fileExists(objectName, minioProperties.getBucketName());
    }

    /**
     * 检查文件是否存在
     *
     * @param objectName 对象名称
     * @param bucketName 存储桶名称
     * @return 是否存在
     */
    public boolean fileExists(String objectName, String bucketName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 列出存储桶中的所有文件
     *
     * @return 文件列表
     */
    public List<String> listFiles() {
        return listFiles(minioProperties.getBucketName());
    }

    /**
     * 列出指定存储桶中的所有文件
     *
     * @param bucketName 存储桶名称
     * @return 文件列表
     */
    public List<String> listFiles(String bucketName) {
        List<String> fileNames = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .recursive(true)
                    .build());
            
            for (Result<Item> result : results) {
                Item item = result.get();
                fileNames.add(item.objectName());
            }
        } catch (Exception e) {
            log.error("列出文件失败: {}", bucketName, e);
        }
        return fileNames;
    }

    /**
     * 列出所有存储桶
     *
     * @return 存储桶列表
     */
    public List<String> listBuckets() {
        List<String> bucketNames = new ArrayList<>();
        try {
            List<Bucket> buckets = minioClient.listBuckets();
            for (Bucket bucket : buckets) {
                bucketNames.add(bucket.name());
            }
        } catch (Exception e) {
            log.error("列出存储桶失败", e);
        }
        return bucketNames;
    }

    /**
     * 获取默认存储桶名称
     *
     * @return 默认存储桶名称
     */
    public String getDefaultBucketName() {
        return minioProperties.getBucketName();
    }
}