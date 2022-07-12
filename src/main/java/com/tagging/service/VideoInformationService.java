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
//import jdk.vm.ci.code.site.Mark;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.util.List;

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

    @Value("${minio.localUrl}")
    private String minioLocalUrl;

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

    //视频新增
    @Transactional
    public void create(List<MultipartFile> files, VideoInformationCreateReq req) {
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
        videoInformation.setSpecCode01("");
        videoInformation.setSpecCode02("");
        videoInformation.setSpecCode03("");
        videoInformation.setCreateTimestamp(DataUtils.getSysTimeByFormat());
        videoInformation.setUpdateTimestamp(DataUtils.getSysTimeByFormat());
        videoInformation.setDataStatus(DataStatusEnum.AVAILABLE.getState());
        videoInformation.setVersion(1);
        videoInformation.setRemarks("");
        videoInformation.setReserve("");

        if (files != null) {
            try {
                for(MultipartFile file : files) {
                    String fileUrl = minioUtils.putObject(minioBucketName, file, videoInformation.getVideoId() + "/" + file.getOriginalFilename(),
                            file.getContentType());
                    videoInformation.setVideoName(file.getOriginalFilename());
                    videoInformation.setVideoFileUrl(fileUrl);
                }

                //若为高光谱,转换为伪彩色
                if(videoInformation.getSensorType().equals(SensorTypeEnum.HYPERSPECTRAL_IMAGE.getState())){
                    String videoUrl = "/video/" + videoInformation.getVideoId();

                    String command = "cd "
                            + executeUrl
                            + " && " + pythonName  +  " " + yoloUrl + "/his.py "
                            + minioLocalUrl + videoInformation.getVideoFileUrl() + " "
                            + minioLocalUrl + videoUrl + "/";

                    log.info(command);

                    videoInformation.setVideoFileUrl(videoUrl + "/rgb.jpg");

                    //运行py文件
                    Process p = null;
                    p = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", command});
                    String inStr = consumeInputStream(p.getInputStream());
                    String errStr = consumeInputStream(p.getErrorStream());
                    p.waitFor();
                    
                }

                //非可见和红外在motimg也保存
                if(videoInformation.getSensorType() != SensorTypeEnum.VISIBLE.getState()
                        && videoInformation.getSensorType() != SensorTypeEnum.INFRARED.getState()){
                    String imgUrl = "/motimg/" + videoInformation.getVideoId();
                    String command = "cd "
                            + executeUrl
                            + " && " + pythonName + " " + yoloUrl + "/video2img.py "
                            + minioLocalUrl + videoInformation.getVideoFileUrl() + " "
                            + minioLocalUrl + imgUrl + '/';

                    log.info(command);

                    //运行py文件
                    Process p = null;
                    p = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", command});
                    String inStr = consumeInputStream(p.getInputStream());
                    String errStr = consumeInputStream(p.getErrorStream());
                    p.waitFor();
                }

                //转为成图片帧所保存的的地址
                String imgUrl = "/img/" + videoInformation.getVideoId();
                videoInformation.setTagImgUrl(imgUrl);

                //调用保存图片帧数的算法
                String command = "cd "
                        + executeUrl
                        + " && " + pythonName + " " + yoloUrl + "/video2img.py "
                        + minioLocalUrl + videoInformation.getVideoFileUrl() + " "
                        + minioLocalUrl + imgUrl + '/';
                log.info(command);

                //运行py文件
                Process p = null;
                p = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", command});
                String inStr = consumeInputStream(p.getInputStream());
                String errStr = consumeInputStream(p.getErrorStream());
                p.waitFor();

                if(videoInformation.getSensorType().equals(SensorTypeEnum.HYPERSPECTRAL_IMAGE.getState())){
                    File file1 = new File(minioLocalUrl + "/video/" + videoInformation.getVideoId() + '/');
                    File file2 = new File (minioLocalUrl + imgUrl + '/');
                    copyDir(file1, file2);
                }

                String encoding = "GBK";
                File sizeFile = new File(minioLocalUrl + imgUrl + '/' + "size.txt");
                if (sizeFile.isFile() && sizeFile.exists()) { //判断文件是否存在
                    InputStreamReader read = new InputStreamReader(new FileInputStream(sizeFile), encoding);//考虑到编码格式
                    BufferedReader bufferedReader = new BufferedReader(read);
                    String lineTxt = null;
                    while ((lineTxt = bufferedReader.readLine()) != null) {
                        String[] message = lineTxt.split(" ");
                        videoInformation.setHeight(message[1]);
                        videoInformation.setWidth(message[2]);
                        videoInformation.setLength(message[0]);
                    }
                    read.close();
                } else {
                    System.out.println("找不到指定的文件");
                }
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
}
