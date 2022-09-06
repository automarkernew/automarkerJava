package com.tagging.controller;

import com.tagging.utils.MinioUtils;
import com.tagging.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;


@Controller
@RequestMapping("minio")
public class MinioController {
    @Autowired
    private MinioUtils minioUtils;



    /**
     * 上传文件
     * @param file 前端传入文件流
     */
    @PostMapping("/upload")
    @ResponseBody
    public R upload(@RequestParam(name = "file", required = false) List<MultipartFile> file) {
        // 判断上传文件是否为空
        if (null == file || 0 == file.size()) {
            return R.error().message("上传文件不能为空");
        }
        try {
            for (MultipartFile fileList : file) {
                String fileUrl = minioUtils.putObject("tag", fileList, "test/" + fileList.getOriginalFilename(), fileList.getContentType());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return R.error().message("上传失败");
        }
        return R.ok().message("上传成功");

    }

    /**
     * 下载文件
     * @param fileUrl  上传时返回的fileUrl
     */
    @GetMapping("/download")
    @ResponseBody
    public void download(HttpServletResponse httpResponse, @RequestParam(name = "fileUrl", required = false) String fileUrl) throws IOException {
        fileUrl = fileUrl.substring(1);
        String bucketName = fileUrl.substring(0, fileUrl.indexOf('/'));
        String objectName = fileUrl.substring(fileUrl.indexOf('/')+1);
        if(minioUtils.doesObjectExist(bucketName, objectName)){
            InputStream object = minioUtils.getObject(bucketName, objectName);
            httpResponse.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(objectName.substring(objectName.indexOf('/', -1)+1), "UTF-8"));
            httpResponse.setContentType("application/octet-stream");
            httpResponse.setCharacterEncoding("utf-8");
            OutputStream outputStream = httpResponse.getOutputStream();
            int length = 0;
            byte[] tmpBuf = new byte[1024];
            while ((length = object.read(tmpBuf)) > 0) {
                outputStream.write(tmpBuf, 0, length);
            }
            outputStream.close();
        }
    }

    @GetMapping("/preview")
    @ResponseBody
    public R preview(String fileUrl){
        fileUrl = fileUrl.substring(1);
        String bucketName = fileUrl.substring(0, fileUrl.indexOf('/'));
        String objectName = fileUrl.substring(fileUrl.indexOf('/')+1);
        if(minioUtils.doesObjectExist(bucketName, objectName)){
            String previewUrl = minioUtils.getPresignedObjectUrl(bucketName, objectName, 24*60*60);
            return R.ok().data("previewUrl", previewUrl);
        }else{
            return R.error().message("找不到文件");
        }
    }

    @GetMapping("/getDownloadUrl")
    @ResponseBody
    public R getDownloadUrl(String fileUrl){
        fileUrl = fileUrl.substring(1);
        String bucketName = fileUrl.substring(0, fileUrl.indexOf('/'));
        String objectName = fileUrl.substring(fileUrl.indexOf('/')+1);
        if(minioUtils.doesObjectExist(bucketName, objectName)){
            String downloadUrl = minioUtils.getDownloadUrl(bucketName, objectName, 5 * 60);
            return R.ok().data("downloadUrl", downloadUrl);
        } else {
            return R.error().message("找不到文件");
        }
    }
}
