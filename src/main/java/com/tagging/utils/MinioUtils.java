package com.tagging.utils;

import com.alibaba.fastjson.JSONObject;
import com.tagging.config.MinioConfig;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@Component
public class MinioUtils {

    @Autowired
    private MinioClient client;
    @Autowired
    private MinioConfig minioConfig;

    /**
     * 创建bucket
     *
     * @param bucketName bucket名称
     */
    @SneakyThrows
    public void createBucket(String bucketName){
        if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    /**
     * 获取存储桶策略
     *
     * @param bucketName 存储桶名称
     * @return json
     */
    @SneakyThrows
    private JSONObject getBucketPolicy(String bucketName){
        String bucketPolicy = client
                .getBucketPolicy(GetBucketPolicyArgs.builder().bucket(bucketName).build());
        return JSONObject.parseObject(bucketPolicy);
    }
    /**
     * 获取全部bucket
     *
     */
    @SneakyThrows
    public List<Bucket> getAllBuckets(){
        return client.listBuckets();
    }

    /**
     * 根据bucketName获取信息
     *
     * @param bucketName bucket名称
     */
    @SneakyThrows
    public Optional<Bucket> getBucket(String bucketName) {
        return client.listBuckets().stream().filter(b -> b.name().equals(bucketName)).findFirst();
    }
    /**
     * 根据bucketName删除信息
     *
     * @param bucketName bucket名称
     */
    @SneakyThrows
    public void removeBucket(String bucketName){
        client.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
    }
    /**
     * 判断文件是否存在
     *
     * @param bucketName 存储桶
     * @param objectName 对象
     * @return true：存在
     */
    public boolean doesObjectExist(String bucketName, String objectName) {
        boolean exist = true;
        try {
            client.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (Exception e) {
            exist = false;
        }
        return exist;
    }
    /**
     * 判断文件夹是否存在
     *
     * @param bucketName 存储桶
     * @param objectName 文件夹名称（去掉/）
     * @return true：存在
     */
    public boolean doesFolderExist(String bucketName, String objectName) {
        boolean exist = false;
        try {
            Iterable<Result<Item>> results = client.listObjects(
                    ListObjectsArgs.builder().bucket(bucketName).prefix(objectName).recursive(false).build());
            for (Result<Item> result : results) {
                Item item = result.get();
                if (item.isDir() && objectName.equals(item.objectName())) {
                    exist = true;
                }
            }
        } catch (Exception e) {
            exist = false;
        }
        return exist;
    }

    /**
     * 根据文件前置查询文件
     *
     * @param bucketName bucket名称
     * @param prefix 前缀
     * @param recursive 是否递归查询
     * @return MinioItem 列表
     */
    @SneakyThrows
    public List<Item> getAllObjectsByPrefix(String bucketName, String prefix,
                                            boolean recursive){
        List<Item> list = new ArrayList<>();
        Iterable<Result<Item>> objectsIterator = client.listObjects(
                ListObjectsArgs.builder().bucket(bucketName).prefix(prefix).recursive(recursive).build());
        if (objectsIterator != null) {
            for (Result<Item> o : objectsIterator) {
                Item item = o.get();
                list.add(item);
            }
        }
        return list;
    }
    /**
     * 获取文件流
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @return 二进制流
     */
    @SneakyThrows
    public InputStream getObject(String bucketName, String objectName){
        return client.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
    }
    /**
     * 通过MultipartFile，上传文件
     *
     * @param bucketName 存储桶
     * @param file 文件
     * @param objectName 对象名
     */
    @SneakyThrows
    public String putObject(String bucketName, MultipartFile file,
                            String objectName, String contentType){
        // 判断存储桶是否存在
        createBucket(bucketName);
        client.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(contentType)
                .build());
        return '/' + bucketName + '/' + objectName;
    }
    /**
     * 获取文件外链
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @param expires 过期时间 <=7 秒级
     * @return url
     */
    @SneakyThrows
    public String getPresignedObjectUrl(String bucketName, String objectName,
                                        Integer expires) {
        Map<String, String> header = new HashMap<>();
        return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucketName)
                .object(objectName)
                .expiry(expires)
                .build());
    }

    @SneakyThrows
    public String getImgPresignedObjectUrl(String bucketName, String objectName,
                                           Integer expires) {
        Map<String, String> header = new HashMap<>();
        header.put("response-content-type", "application/image/x-png");
        return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucketName)
                .object(objectName)
                .expiry(expires).extraQueryParams(header)
                .build());
    }

    @SneakyThrows
    public String getDownloadUrl(String bucketName, String objectName,
                                 Integer expires) {
        Map<String, String> header = new HashMap<>();
        header.put("response-content-type", "application/octet-stream");
        return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucketName)
                .object(objectName)
                .expiry(expires).extraQueryParams(header)
                .build());
    }
    @SneakyThrows
    public List<String> listObjects(String bucketName){
        List<String> stringList = new ArrayList<>();
        try {
            // 列出'my-bucketname'里的对象
            Iterable<Result<Item>> myObjects = client.listObjects(bucketName);
            for (Result<Item> result : myObjects) {
                Item item = result.get();
                stringList.add(item.objectName());
            }
        } catch (MinioException e) {
            System.out.println("Error occurred: " + e);
        }
        return stringList;
    }

}
