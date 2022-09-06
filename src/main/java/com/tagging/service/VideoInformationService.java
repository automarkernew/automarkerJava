package com.tagging.service;

import com.github.wujun234.uid.UidGenerator;
import com.tagging.common.Constants;
import com.tagging.controller.MinioController;
import com.tagging.dao.mapper.VideoInformationDao;
import com.tagging.dto.QuerySummaryRsp;
import com.tagging.dto.videoInformation.*;
import com.tagging.entity.VideoInformation;
import com.tagging.enums.DataStatusEnum;
import com.tagging.enums.VideoInformation.IsMotedEnum;
import com.tagging.enums.VideoInformation.IsTaggedEnum;
import com.tagging.enums.VideoInformation.SensorTypeEnum;
import com.tagging.enums.VideoInformation.TagStatusEnum;
import com.tagging.exception.CMSException;
import com.tagging.utils.DataUtils;
import com.tagging.utils.MinioUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.tagging.utils.FileUtils.consumeInputStream;
import static com.tagging.utils.FileUtils.copyDir;

@Service
@Slf4j
public class VideoInformationService {
    @Resource
    VideoInformationDao videoInformationDao;

    @Resource
    UidGenerator cachedUidGenerator;

    @Resource
    MinioUtils minioUtils;

    @Resource
    MinioController minioController;

    @Value("${minio.videoBucket}")
    private String minioBucketName;

    @Value("${minio.serverFilePath}")
    private String serverFilePath;

    @Value("${minio.localUrl}")
    private String minioLocalUrl;

    @Value("${minio.motimgBuket}")
    private String motimgBuket;

    @Value("${minio.videoBucket}")
    private String videoBucket;

    @Value("${minio.imgBuket}")
    private String imgBuket;

    @Value("${yolo5.localUrl}")
    private String yoloUrl;

    @Value("${yolo5.executeUrl}")
    private String executeUrl;

    @Value("${yolo5.pythonName}")
    private String pythonName;
    
    public VideoInformationQueryByIdRsp queryByVideoId(VideoInformationQueryByIdReq req){
        VideoInformation videoInformation = videoInformationDao.queryByVideoId(req.getVideoId());
        VideoInformationQueryByIdRsp videoInformationQueryByIdRsp = new VideoInformationQueryByIdRsp();
        videoInformationQueryByIdRsp.setVideoId(videoInformation.getVideoId());
        videoInformationQueryByIdRsp.setVideoName(videoInformation.getVideoName());
        videoInformationQueryByIdRsp.setVideoFileUrl(videoInformation.getVideoFileUrl());
        videoInformationQueryByIdRsp.setShootTime(videoInformation.getShootTime());
        videoInformationQueryByIdRsp.setShootPlace(videoInformation.getShootPlace());
        videoInformationQueryByIdRsp.setLength(videoInformation.getLength());
        videoInformationQueryByIdRsp.setSensorType(videoInformation.getSensorType());
        videoInformationQueryByIdRsp.setHeight(videoInformation.getHeight());
        videoInformationQueryByIdRsp.setWidth(videoInformation.getWidth());
        videoInformationQueryByIdRsp.setTagUserId(videoInformation.getTagUserId());
        videoInformationQueryByIdRsp.setMotUserId(videoInformation.getMotUserId());
        videoInformationQueryByIdRsp.setTagImgUrl(videoInformation.getTagImgUrl());
        videoInformationQueryByIdRsp.setMotImgUrl(videoInformation.getMotImgUrl());
        videoInformationQueryByIdRsp.setTagTime(videoInformation.getTagTime());
        videoInformationQueryByIdRsp.setTagStatus(videoInformation.getTagStatus());
        videoInformationQueryByIdRsp.setIsTagged(videoInformation.getIsTagged());
        videoInformationQueryByIdRsp.setIsMoted(videoInformation.getIsMoted());
        return videoInformationQueryByIdRsp;
    }

    public List<VideoInformationQueryRsp> query(long offset, int queryCount, VideoInformationQueryReq req){
        return videoInformationDao.query(offset, queryCount,
                req.getVideoName(),
                req.getShootTimeBegin(),
                req.getShootTimeEnd(),
                req.getShootPlace(),
                req.getSensorType(),
                req.getTagStatus());

    }

    public List<VideoInformationQueryRsp> markquery(long offset, int queryCount, MarkInformationQueryReq req){
        return videoInformationDao.markquery(offset, queryCount,
                req.getTagStatus(),
                req.getShootTime(),
                req.getShootPlace(),
                req.getSensorType());
    }

    public QuerySummaryRsp count(VideoInformationQueryReq req){
        QuerySummaryRsp querySummaryRsp = new QuerySummaryRsp();
        long num = videoInformationDao.count(
                req.getVideoName(),
                req.getShootTimeBegin(),
                req.getShootTimeEnd(),
                req.getShootPlace(),
                req.getSensorType(),
                req.getTagStatus()
        );
        querySummaryRsp.setDataAmount(num);
        return querySummaryRsp;
    }

    public QuerySummaryRsp markcount(MarkInformationQueryReq req){
        QuerySummaryRsp querySummaryRsp = new QuerySummaryRsp();
        long num = videoInformationDao.markcount(
                req.getTagStatus(),
                req.getShootTime(),
                req.getShootPlace(),
                req.getSensorType()
        );
        querySummaryRsp.setDataAmount(num);
        return querySummaryRsp;
    }

    // 上传之前检测红外对应的可见光是否存在（如果是红外）
    // 设置上传要更新的 VideoInformation
    public VideoInformation setVideoInformation(VideoInformationCreateReq req){
        VideoInformation videoInformation = new VideoInformation();
        videoInformation.setVideoId(String.valueOf(cachedUidGenerator.getUID()));
        videoInformation.setShootTime(req.getShootTime());
        videoInformation.setShootPlace(req.getShootPlace());
        videoInformation.setSensorType(req.getSensorType());
        if(videoInformation.getSensorType().equals(SensorTypeEnum.INFRARED.getState())){
            VideoInformation visibleVideo = videoInformationDao.queryByVideoId(req.getTypeLinkId());
            if(visibleVideo == null){
                throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE,"要关联的可见光视频不存在");
            }
            videoInformation.setTypeLinkId(req.getTypeLinkId());
            visibleVideo.setTypeLinkId(videoInformation.getVideoId());
            visibleVideo.setUpdateTimestamp(DataUtils.getSysTimeByFormat("yyyyMMddHHmmss"));
            visibleVideo.setVersion(visibleVideo.getVersion() + 1);
            videoInformationDao.updateByPrimaryKeySelective(visibleVideo);
        }else {videoInformation.setTypeLinkId(" ");}

        videoInformation.setTagUserId("");
        videoInformation.setMotUserId("");
        videoInformation.setMotImgUrl("");
        videoInformation.setTagTime("");
        videoInformation.setTagStatus(TagStatusEnum.W.getState());
        videoInformation.setIsTagged(IsTaggedEnum.UNEXECUTED.getState());
        videoInformation.setIsMoted(IsMotedEnum.UNEXECUTED.getState());
        videoInformation.setOffsetX("");
        videoInformation.setOffsetY("");
        videoInformation.setSpecCode03("");
        videoInformation.setCreateTimestamp(DataUtils.getSysTimeByFormat());
        videoInformation.setUpdateTimestamp(DataUtils.getSysTimeByFormat());
        videoInformation.setDataStatus(DataStatusEnum.AVAILABLE.getState());
        videoInformation.setVersion(1);
        videoInformation.setRemarks("");
        videoInformation.setReserve("");
        return videoInformation;
    }

    // MultipartFile类型的拷贝
    public VideoInformation setMultipartFile(VideoInformation videoInformation, List<MultipartFile> files) throws IOException {
        for(MultipartFile file : files) {
//                    String fileUrl = minioUtils.putObject(minioBucketName, file, videoInformation.getVideoId() + "/" + file.getOriginalFilename(),
//                            file.getContentType());
            File dest = new File(String.valueOf(Paths.get(minioLocalUrl, minioBucketName, videoInformation.getVideoId(), file.getOriginalFilename())));
            if (!dest.exists()) {
                dest.mkdirs();
            }
            file.transferTo(dest);
            videoInformation.setVideoName(file.getOriginalFilename());
            videoInformation.setVideoFileUrl(String.valueOf(Paths.get(minioBucketName, videoInformation.getVideoId(), file.getOriginalFilename())));
        }
        return videoInformation;
    }

    // File类型直接拷贝
    public VideoInformation setFile(VideoInformation videoInformation, List<File> files) throws IOException {
        for (File file : files) {
            File destDir = new File(String.valueOf(Paths.get(minioLocalUrl, minioBucketName, videoInformation.getVideoId())));
            if (!(destDir.exists() && destDir.isDirectory())) {
                destDir.mkdirs();
            }
            File dest = new File(String.valueOf(Paths.get(minioLocalUrl, minioBucketName, videoInformation.getVideoId(), file.getName())));
            Files.copy(file.toPath(), dest.toPath());
            videoInformation.setVideoName(file.getName());
            videoInformation.setVideoFileUrl(String.valueOf(Paths.get(minioBucketName, videoInformation.getVideoId(), file.getName())));
        }
        return videoInformation;
    }

    // 上传后续操作
    public VideoInformation doCreate(VideoInformation videoInformation) throws InterruptedException, IOException {
        //若为高光谱,转换为伪彩色
        if(videoInformation.getSensorType().equals(SensorTypeEnum.HYPERSPECTRAL_IMAGE.getState())){
            String videoUrl = videoBucket + "/" + videoInformation.getVideoId();

            String command = "cd "
                    + executeUrl
                    + " && " + pythonName  +  " " + yoloUrl + "/his.py "
                    + minioLocalUrl + "/" + videoInformation.getVideoFileUrl().replace(".hdr","") + ".hdr" + " "
                    + minioLocalUrl + "/" + videoUrl + "/";

            log.info(command);

            videoInformation.setVideoFileUrl(videoUrl + "/1.jpg");

            //运行py文件
            Process p;
            p = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", command});
            String inStr = consumeInputStream(p.getInputStream());
            String errStr = consumeInputStream(p.getErrorStream());
            p.waitFor();

        }

        //非可见和红外在motimg也保存
        if(!videoInformation.getSensorType().equals(SensorTypeEnum.VISIBLE.getState())
                && !videoInformation.getSensorType().equals(SensorTypeEnum.INFRARED.getState())){
            String imgUrl = "/" + motimgBuket + "/" + videoInformation.getVideoId();
            String command = "cd "
                    + executeUrl
                    + " && " + pythonName + " " + yoloUrl + "/video2img.py "
                    + minioLocalUrl + "/" + videoInformation.getVideoFileUrl() + " "
                    + minioLocalUrl + imgUrl + '/';

            log.info(command);

            //运行py文件
            Process p;
            p = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", command});
            String inStr = consumeInputStream(p.getInputStream());
            String errStr = consumeInputStream(p.getErrorStream());
            p.waitFor();
        }

        //转为成图片帧所保存的的地址
        String imgUrl = "/" + imgBuket + "/" + videoInformation.getVideoId();
        videoInformation.setTagImgUrl(imgUrl);

        //调用保存图片帧数的算法
        String command = "cd "
                + executeUrl
                + " && " + pythonName + " " + yoloUrl + "/video2img.py "
                + minioLocalUrl + "/" + videoInformation.getVideoFileUrl() + " "
                + minioLocalUrl + imgUrl + '/';
        log.info(command);

        //运行py文件
        Process p = null;
        p = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", command});
        String inStr = consumeInputStream(p.getInputStream());
        String errStr = consumeInputStream(p.getErrorStream());
        p.waitFor();

        if(videoInformation.getSensorType().equals(SensorTypeEnum.HYPERSPECTRAL_IMAGE.getState())){
            File file1 = new File(minioLocalUrl + "/" + videoBucket + "/" + videoInformation.getVideoId() + '/');
            File file2 = new File (minioLocalUrl + imgUrl + '/');
            copyDir(file1, file2);
        }

        String encoding = "GBK";
        File sizeFile = new File(minioLocalUrl + imgUrl + '/' + "size.txt");
        if (sizeFile.isFile() && sizeFile.exists()) { //判断文件是否存在
            InputStreamReader read = new InputStreamReader(Files.newInputStream(sizeFile.toPath()), encoding);//考虑到编码格式
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                String[] message = lineTxt.split(" ");
                videoInformation.setHeight(message[1]);
                videoInformation.setWidth(message[2]);
                videoInformation.setLength(message[3]);
                System.out.println("======"+ Arrays.toString(message));
            }
            read.close();
        } else {
            System.out.println("找不到指定的文件");
        }
        return videoInformation;
    }


    //视频新增
    @Transactional
    public void create(List<MultipartFile> files, VideoInformationCreateReq req) {
        VideoInformation videoInformation = setVideoInformation(req);
        if (files != null) {
            try {
                videoInformation = setMultipartFile(videoInformation, files);
                videoInformation = doCreate(videoInformation);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        videoInformationDao.insert(videoInformation);
    }

    public void delete(VideoInformationDeleteReq req){
        VideoInformation videoInformation = videoInformationDao.queryByVideoId(req.getVideoId());
        videoInformation.setDataStatus(DataStatusEnum.DELETED.getState());
        videoInformation.setUpdateTimestamp(DataUtils.getSysTimeByFormat("yyyyMMddHHmmss"));
        videoInformation.setVersion(videoInformation.getVersion() + 1);

        // TODO 删除minio中的文件,联表删除
        videoInformationDao.updateByPrimaryKeySelective(videoInformation);
    }

    public String download(String VideoId) throws IOException{
        VideoInformation videoInformation = videoInformationDao.queryByVideoId(VideoId);
        return videoInformation.getVideoFileUrl();
    }


    public List<VideoInformationQueryShootPlaceRsp> queryShootPlace(){
        return videoInformationDao.queryShootPlace();
    }

    public VideoLengthRsp queryVideoLength(VideoLengthReq req){
        try{
            VideoLengthRsp rsp = videoInformationDao.queryVideoLength(req.getVideoId());
            if (rsp == null){
                throw new CMSException(404, "VideoId can not find");
            }
            return rsp;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    // 查询指定文件夹下的文件夹或者文件
    public List<GetDataListFromServerRsp> getDataListFromServer(GetDataListFromServerReq req){
        List<GetDataListFromServerRsp> getDataListFromServerRsps = new ArrayList<>();
        Map<String, List<String>> dataMap = readDirectoryList(req.getFilePathNow(), req.getType());
        List<String> dataNames = dataMap.get("dataNames");
        List<String> dataTypes = dataMap.get("dataTypes");
        assert dataNames.size() == dataTypes.size();
        for (int i = 0; i < dataTypes.size(); i++) {
            GetDataListFromServerRsp Rsp = new GetDataListFromServerRsp();
            // 根据系统的路径分隔符号对文件或者文件夹路径分割，只取文件或文件夹名字
            String dataListName = String.valueOf(dataNames.get(i)).split(File.separator)[String.valueOf(dataNames.get(i)).split(File.separator).length - 1];
            Rsp.setDataListName(dataListName);
            Rsp.setType(dataTypes.get(i));
            getDataListFromServerRsps.add(Rsp);
        }
        return getDataListFromServerRsps;
    }


    // 根据queryType查询指定文件夹（filePath为空代表yaml配置的文件夹，否则为后端配置文件夹下的filePath文件夹(","隔开)）
    // queryType代表查询的是文件夹还是文件或者所有
    // 用Map<String, List<String>>返回类型（文件夹或文件）以及路径
    public Map<String, List<String>> readDirectoryList(String filePath, String queryType){
        String filePathNow = getSplitFileName(filePath, serverFilePath, ",");
        Map<String, List<String>> map = new HashMap<>();
        List<String> dataNames = new ArrayList<>();
        List<String> dataTypes = new ArrayList<>();
        File file = new File(filePathNow);
        File[] tempList = file.listFiles();
        if (tempList != null) {
            for (File value : tempList) {
                switch (queryType) {
                    case "0":
                        if (value.isDirectory()) {
                            dataNames.add(String.valueOf(value));
                            dataTypes.add("0");
                        }
                        break;
                    case "1":
                        if (value.isFile()) {
                            dataNames.add(String.valueOf(value));
                            dataTypes.add("1");
                        }
                        break;
                    case "2":
                        if (value.isDirectory()) {
                            dataNames.add(String.valueOf(value));
                            dataTypes.add("0");
                        } else if (value.isFile()){
                            dataNames.add(String.valueOf(value));
                            dataTypes.add("1");
                        }
                        break;
                    default:
                        throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE, queryType + "查询模式错误");
                }
            }
            map.put("dataNames", dataNames);
            map.put("dataTypes", dataTypes);
            return map;
        }else {
            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE, filePathNow + "文件夹错误");
        }
    }

    // 从服务器内部上传
    public void createFromServer(VideoFromServerCreateReq req) throws Exception {
        String filePath = req.getFileName().replace(" ", "").replace("\n", "");
        String filePathNow = getSplitFileName(filePath, null, "/");
        if (filePathNow != null) {
            File file = new File(filePathNow);
            List<File> files = new ArrayList<>();
            files.add(file);
            if(req.getSensorType().equals(SensorTypeEnum.HYPERSPECTRAL_IMAGE.getState())){
                files.add(new File(req.getTypeLinkId()));
            }
            if (file.exists()) {
                VideoInformationCreateReq videoInformationCreateReq = new VideoInformationCreateReq();
                videoInformationCreateReq.setSensorType(req.getSensorType());
                videoInformationCreateReq.setShootPlace(req.getShootPlace());
                videoInformationCreateReq.setShootTime(req.getShootTime());
                if (! req.getTypeLinkId().equals("")){
                    if (! (req.getSensorType().equals(SensorTypeEnum.HYPERSPECTRAL_IMAGE.getState()) || req.getSensorType().equals(SensorTypeEnum.INFRARED.getState()))){
                        throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE, "TypeLinkId: " + req.getTypeLinkId() + " 不为空, 但却不是高光谱或者红外光");
                    };
                }else {
                    if (req.getSensorType().equals(SensorTypeEnum.HYPERSPECTRAL_IMAGE.getState()) || req.getSensorType().equals(SensorTypeEnum.INFRARED.getState())){
                        throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE, "TypeLinkId: " + req.getTypeLinkId() + " 为空, 但却是高光谱或者红外光");
                    };
                }
                videoInformationCreateReq.setTypeLinkId(req.getTypeLinkId());
                VideoInformation videoInformation = setVideoInformation(videoInformationCreateReq);
                try {
                    videoInformation = setFile(videoInformation, files);
                    videoInformation = doCreate(videoInformation);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                videoInformationDao.insert(videoInformation);
                System.out.println("From " + filePathNow + " upload");
            }else {
                throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE, filePathNow + "文件路径错误");
            }
        }else {
            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE, "文件路径错误");
        }

    }

    // 将按","分隔的路径拼接起来
    public String getSplitFileName(String filePath, String defaultPath, String separator){
        String filePathNow = defaultPath;
        if(! filePath.replace(" ", "").replace("\n", "").equals("")) {
            StringBuilder filePathSplit = new StringBuilder();
            for (String split : filePath.split(separator)){
                filePathSplit.append(File.separator).append(split.replace(" ", "").replace("\n", ""));
            }
            filePathNow = serverFilePath + filePathSplit;
        }
        return filePathNow;
    }

}
