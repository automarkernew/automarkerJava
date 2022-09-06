package com.tagging.service;

import com.github.wujun234.uid.UidGenerator;
import com.tagging.common.Constants;
import com.tagging.dao.mapper.FrameInformationDao;
import com.tagging.dao.mapper.TargetTrackDao;
import com.tagging.dao.mapper.VideoInformationDao;
import com.tagging.dto.yoloTagging.YoloTaggingFinishReq;
import com.tagging.dto.yoloTagging.YoloTaggingSaveReq;
import com.tagging.dto.yoloTagging.*;
import com.tagging.entity.FrameInformation;
import com.tagging.entity.LineReplaceEntity;
import com.tagging.entity.TargetTrackT;
import com.tagging.entity.VideoInformation;
import com.tagging.enums.DataStatusEnum;
import com.tagging.enums.VideoInformation.IsMotedEnum;
import com.tagging.enums.VideoInformation.IsTaggedEnum;
import com.tagging.enums.VideoInformation.SensorTypeEnum;
import com.tagging.enums.VideoInformation.TagStatusEnum;
import com.tagging.exception.CMSException;
import com.tagging.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.tagging.utils.FileUtils.consumeInputStream;
import static com.tagging.utils.FileUtils.rpFileContentByLineNo;


@Service
@Slf4j
public class YoloTaggingService {
    @Resource
    VideoInformationDao videoInformationDao;

    @Resource
    FrameInformationDao frameInformationDao;

    @Resource
    TargetTrackDao targetTrackDao;

    @Resource
    UidGenerator cachedUidGenerator;

    @Value("${minio.localUrl}")
    private String minioLocalUrl;

    @Value("${minio.txtBuket}")
    private String txtBuket;

    @Value("${minio.motimgBuket}")
    private String motimgBuket;

    @Value("${minio.imgBuket}")
    private String imgBuket;

    @Value("${yolo5.localUrl}")
    private String yoloUrl;

    @Value("${yolo5.executeUrl}")
    private String executeUrl;

    @Value("${yolo5.pythonName}")
    private String pythonName;

    //TODO 修改文件保存方式 yolo 和 mot
    //标注
    public void tagging(YoloTaggingTagReq req){
        VideoInformation videoInformation = videoInformationDao.queryByVideoId(req.getVideoId());
        if (videoInformation.getIsTagged().equals(IsTaggedEnum.EXECUTED.getState())){
            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE, "此视频进行过标注");
        }

        //保存txt文件地址
        String txtUrl = minioLocalUrl + "/" + txtBuket + "/" + videoInformation.getVideoId();

        String command = "cd "
                + executeUrl
                + " && " + pythonName  +  " " + yoloUrl + "/detect.py --source "
                + minioLocalUrl + "/" + videoInformation.getVideoFileUrl() + " --project "
                + txtUrl  + "/";

        log.info(command);
        try {
            //调用算法
            Process p = null;
            p = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", command});
            String inStr = consumeInputStream(p.getInputStream());
            String errStr = consumeInputStream(p.getErrorStream());
            p.waitFor();

            //文件地址
            String filePath = txtUrl + "/detect.txt";

            //存入数据库
            String encoding="GBK";
            File file=new File(filePath);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while((lineTxt = bufferedReader.readLine()) != null){
                    String[] message = lineTxt.split(" ");
                    FrameInformation frameInformation = new FrameInformation();
                    frameInformation.setFrameInformationId(String.valueOf(cachedUidGenerator.getUID()));
                    frameInformation.setVideoId(videoInformation.getVideoId());

                    //非可见红外,设为1
                    if(!videoInformation.getSensorType().equals(SensorTypeEnum.VISIBLE.getState()) &&
                            !videoInformation.getSensorType().equals(SensorTypeEnum.INFRARED.getState())) {
                        long numbers = targetTrackDao.queryTrackNumbers(req.getVideoId());
                        TargetTrackT targetTrackT = new TargetTrackT();
                        targetTrackT.setUniqueId(String.valueOf(cachedUidGenerator.getUID()));
                        targetTrackT.setTrackId(String.valueOf(numbers + 1));
                        frameInformation.setTrackId(String.valueOf(numbers + 1));
                        targetTrackT.setVideoId(req.getVideoId());
                        targetTrackT.setLinkId(" ");
                        targetTrackT.setTargetTypeId(" ");
                        targetTrackT.setTrackTypeId(" ");
                        targetTrackT.setAppearFrame("1");
                        targetTrackT.setCoordinateFileUrl(minioLocalUrl + "/" + txtBuket + "/" + videoInformation.getVideoId() + "/detect.txt");
                        targetTrackT.setMotImgUrl(minioLocalUrl + "/" + motimgBuket + "/" + videoInformation.getVideoId());
                        targetTrackT.setMarkInformationSaveTime(" ");
                        targetTrackT.setSpecCode01("");
                        targetTrackT.setSpecCode02("");
                        targetTrackT.setSpecCode03("");
                        targetTrackT.setCreateTimestamp(DataUtils.getSysTimeByFormat());
                        targetTrackT.setUpdateTimestamp(DataUtils.getSysTimeByFormat());
                        targetTrackT.setDataStatus(DataStatusEnum.AVAILABLE.getState());
                        targetTrackT.setVersion(1);
                        targetTrackT.setRemarks("");
                        targetTrackT.setReserve("");
                        targetTrackDao.insert(targetTrackT);
                        frameInformation.setFrameType("1");
                    }
                    else {
                        frameInformation.setFrameType("0");
                        frameInformation.setTrackId("");
                    }
                    frameInformation.setFrame(Integer.valueOf(message[0]));
                    frameInformation.setLeftUpperCornerAbscissa(message[1]);
                    frameInformation.setLeftUpperCornerOrdinate(message[2]);
                    frameInformation.setRightLowerQuarterAbscissa(message[3]);
                    frameInformation.setRightLowerQuarterOrdinate(message[4]);
                    frameInformation.setSpecCode01("");
                    frameInformation.setSpecCode02("");
                    frameInformation.setSpecCode03("");
                    frameInformation.setCreateTimestamp(DataUtils.getSysTimeByFormat());
                    frameInformation.setUpdateTimestamp(DataUtils.getSysTimeByFormat());
                    frameInformation.setDataStatus(DataStatusEnum.AVAILABLE.getState());
                    frameInformation.setVersion(1);
                    frameInformation.setRemarks("");
                    frameInformation.setReserve("");
                    frameInformationDao.insert(frameInformation);
                }
                read.close();
            }else{
                System.out.println("找不到指定的文件");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        videoInformation.setIsTagged(IsTaggedEnum.EXECUTED.getState());
        if(videoInformation.getSensorType().equals(SensorTypeEnum.HYPERSPECTRAL_IMAGE.getState()) ||
        videoInformation.getSensorType().equals(SensorTypeEnum.HIGH_RESOLUTION_VISIBLE.getState()))
            videoInformation.setIsMoted(IsMotedEnum.EXECUTED.getState());
        videoInformation.setUpdateTimestamp(DataUtils.getSysTimeByFormat("yyyyMMddhhmmss"));
        videoInformation.setVersion(videoInformation.getVersion() + 1);
        videoInformationDao.updateByPrimaryKeySelective(videoInformation);
    }

    //获取坐标信息
    public List<YoloTaggingGetCoordinateRsp> getCoordinateRsps(YoloTaggingGetCoordinateReq req){
        return frameInformationDao.getCoordinateRsp(
                req.getVideoId(),
                req.getFrameType(),
                req.getFrame(),
                req.getTrackId()
        );
    }

    //新增坐标框
    public YoloTaggingCreateRsp create(YoloTaggingCreateReq req){
        VideoInformation videoInformation = videoInformationDao.queryByVideoId(req.getVideoId());


        //坐标新增
        FrameInformation frameInformation = new FrameInformation();
        frameInformation.setFrameInformationId(String.valueOf(cachedUidGenerator.getUID()));
        frameInformation.setVideoId(req.getVideoId());
        frameInformation.setFrame(req.getFrame());

        //非可见红外,设为1
        if(!videoInformation.getSensorType().equals(SensorTypeEnum.VISIBLE.getState()) &&
                !videoInformation.getSensorType().equals(SensorTypeEnum.INFRARED.getState()))
            frameInformation.setFrameType("1");
        else
            frameInformation.setFrameType("0");
        frameInformation.setTrackId("");
        frameInformation.setLeftUpperCornerAbscissa(req.getLeftUpperCornerAbscissa());
        frameInformation.setLeftUpperCornerOrdinate(req.getLeftUpperCornerOrdinate());
        frameInformation.setRightLowerQuarterAbscissa(req.getRightLowerQuarterAbscissa());
        frameInformation.setRightLowerQuarterOrdinate(req.getRightLowerQuarterOrdinate());
        frameInformation.setSpecCode01("");
        frameInformation.setSpecCode02("");
        frameInformation.setSpecCode03("");
        frameInformation.setCreateTimestamp(DataUtils.getSysTimeByFormat());
        frameInformation.setUpdateTimestamp(DataUtils.getSysTimeByFormat());
        frameInformation.setDataStatus(DataStatusEnum.AVAILABLE.getState());
        frameInformation.setVersion(1);
        frameInformation.setRemarks("");
        frameInformation.setReserve("");


        //非可将光或红外情况，在第一次新增时将video状态改为已标注、已跟踪
        if(!videoInformation.getSensorType().equals(SensorTypeEnum.VISIBLE.getState()) &&
                !videoInformation.getSensorType().equals(SensorTypeEnum.INFRARED.getState())){
            //第一次新增
            List<FrameInformation> frameInformations = frameInformationDao.queryAllFrames(req.getVideoId());
            if(frameInformations.size() == 0) {
                videoInformation.setIsTagged(IsTaggedEnum.EXECUTED.getState());
                videoInformation.setIsMoted(IsMotedEnum.EXECUTED.getState());
                videoInformation.setUpdateTimestamp(DataUtils.getSysTimeByFormat("yyyyMMddhhmmss"));
                videoInformation.setVersion(videoInformation.getVersion() + 1);
                videoInformationDao.updateByPrimaryKeySelective(videoInformation);

                //此时轨迹表必定没有数据,将Id设为1
                TargetTrackT targetTrackT = new TargetTrackT();
                targetTrackT.setUniqueId(String.valueOf(cachedUidGenerator.getUID()));
                targetTrackT.setTrackId(String.valueOf(1));
                frameInformation.setTrackId(String.valueOf(1));
                targetTrackT.setLinkId(" ");
                targetTrackT.setVideoId(req.getVideoId());
                targetTrackT.setTargetTypeId(" ");
                targetTrackT.setTrackTypeId(" ");
                targetTrackT.setAppearFrame("1");
                targetTrackT.setCoordinateFileUrl(minioLocalUrl + "/" + txtBuket + "/" + videoInformation.getVideoId() + "/detect.txt");
                targetTrackT.setMotImgUrl(minioLocalUrl + "/" + motimgBuket + "/" + videoInformation.getVideoId());
                targetTrackT.setMarkInformationSaveTime(" ");
                targetTrackT.setSpecCode01("");
                targetTrackT.setSpecCode02("");
                targetTrackT.setSpecCode03("");
                targetTrackT.setCreateTimestamp(DataUtils.getSysTimeByFormat());
                targetTrackT.setUpdateTimestamp(DataUtils.getSysTimeByFormat());
                targetTrackT.setDataStatus(DataStatusEnum.AVAILABLE.getState());
                targetTrackT.setVersion(1);
                targetTrackT.setRemarks("");
                targetTrackT.setReserve("");
                targetTrackDao.insert(targetTrackT);
            }else {
                long numbers = targetTrackDao.queryTrackNumbers(req.getVideoId());
                TargetTrackT targetTrackT = new TargetTrackT();
                targetTrackT.setUniqueId(String.valueOf(cachedUidGenerator.getUID()));
                targetTrackT.setTrackId(String.valueOf(numbers + 1));
                frameInformation.setTrackId(String.valueOf(numbers + 1));
                targetTrackT.setVideoId(req.getVideoId());
                targetTrackT.setLinkId(" ");
                targetTrackT.setTargetTypeId(" ");
                targetTrackT.setTrackTypeId(" ");
                targetTrackT.setAppearFrame("1");
                targetTrackT.setCoordinateFileUrl(minioLocalUrl + "/" + txtBuket + "/" + videoInformation.getVideoId() + "/detect.txt");
                targetTrackT.setMotImgUrl(minioLocalUrl + "/" + motimgBuket + "/" + videoInformation.getVideoId());
                targetTrackT.setMarkInformationSaveTime(" ");
                targetTrackT.setSpecCode01("");
                targetTrackT.setSpecCode02("");
                targetTrackT.setSpecCode03("");
                targetTrackT.setCreateTimestamp(DataUtils.getSysTimeByFormat());
                targetTrackT.setUpdateTimestamp(DataUtils.getSysTimeByFormat());
                targetTrackT.setDataStatus(DataStatusEnum.AVAILABLE.getState());
                targetTrackT.setVersion(1);
                targetTrackT.setRemarks("");
                targetTrackT.setReserve("");
                targetTrackDao.insert(targetTrackT);
            }
        }

        //文件地址
        String txtPath = minioLocalUrl + "/" + txtBuket + "/" + req.getVideoId() + "/detect.txt";

        //非可见光新增
        File file = new File(txtPath);
        if(!file.exists()){
            try{
                file.getParentFile().mkdir();
                file.createNewFile();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        String createContent = String.valueOf(req.getFrame()) + " " + req.getLeftUpperCornerAbscissa() + " "
                + req.getLeftUpperCornerOrdinate() + " " + req.getRightLowerQuarterAbscissa() + " "
                + req.getRightLowerQuarterOrdinate() + " ";

        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(txtPath, true)));
            out.write(createContent);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        frameInformationDao.insert(frameInformation);

        YoloTaggingCreateRsp yoloTaggingCreateRsp = new YoloTaggingCreateRsp();
        yoloTaggingCreateRsp.setFrameInformationId(frameInformation.getFrameInformationId());
        return yoloTaggingCreateRsp;
    }

    //修改坐标框
    public void update(YoloTaggingUpdateReq req){
        FrameInformation frameInformation = frameInformationDao.queryByFrameId(req.getFrameInformationId());
        VideoInformation videoInformation = videoInformationDao.queryByVideoId(frameInformation.getVideoId());

        //文件地址
        String txtPath = minioLocalUrl + "/" + txtBuket + "/" + videoInformation.getVideoId() + "/detect.txt";

        //原信息
        String lineTxt = String.valueOf(frameInformation.getFrame()) + " " + frameInformation.getLeftUpperCornerAbscissa() + " "
                + frameInformation.getLeftUpperCornerOrdinate() + " " + frameInformation.getRightLowerQuarterAbscissa() + " "
                + frameInformation.getRightLowerQuarterOrdinate() + " ";

        //修改的信息
        String replaceStr = String.valueOf(req.getFrame()) + " " + req.getLeftUpperCornerAbscissa() + " "
                + req.getLeftUpperCornerOrdinate() + " " + req.getRightLowerQuarterAbscissa() + " "
                + req.getRightLowerQuarterOrdinate() + " ";

        List<LineReplaceEntity> list = new ArrayList<>();
        list.add(new LineReplaceEntity(lineTxt, replaceStr));
        rpFileContentByLineNo(txtPath,list);

        frameInformation.setFrame(req.getFrame());
        frameInformation.setLeftUpperCornerAbscissa(req.getLeftUpperCornerAbscissa());
        frameInformation.setLeftUpperCornerOrdinate(req.getLeftUpperCornerOrdinate());
        frameInformation.setRightLowerQuarterAbscissa(req.getRightLowerQuarterAbscissa());
        frameInformation.setRightLowerQuarterOrdinate(req.getRightLowerQuarterOrdinate());
        frameInformation.setUpdateTimestamp(DataUtils.getSysTimeByFormat("yyyyMMddhhmmss"));
        frameInformation.setVersion(frameInformation.getVersion() + 1);

        frameInformationDao.updateByPrimaryKeySelective(frameInformation);
    }

    //删除坐标框
    public void delete(YoloTaggingDeleteReq req){
        FrameInformation frameInformation = frameInformationDao.queryByFrameId(req.getFrameInformationId());
        VideoInformation videoInformation = videoInformationDao.queryByVideoId(frameInformation.getVideoId());

        if(!videoInformation.getSensorType().equals(SensorTypeEnum.INFRARED.getState()) &&
        !videoInformation.getSensorType().equals(SensorTypeEnum.VISIBLE.getState())) {
            TargetTrackT targetTrackT = targetTrackDao.queryByTrack(videoInformation.getVideoId(), frameInformation.getTrackId());
            targetTrackT.setDataStatus(DataStatusEnum.DELETED.getState());
            targetTrackT.setUpdateTimestamp(DataUtils.getSysTimeByFormat("yyyyMMddHHmmss"));
            targetTrackT.setVersion(targetTrackT.getVersion() + 1);

            targetTrackDao.updateByPrimaryKeySelective(targetTrackT);
        }

        String txtPath = minioLocalUrl + "/" + txtBuket + "/" + videoInformation.getVideoId() + "/detect.txt";

        //要删除的信息
        String lineTxt = String.valueOf(frameInformation.getFrame()) + " " + frameInformation.getLeftUpperCornerAbscissa() + " "
                + frameInformation.getLeftUpperCornerOrdinate() + " " + frameInformation.getRightLowerQuarterAbscissa() + " "
                + frameInformation.getRightLowerQuarterOrdinate() + " ";

        List<LineReplaceEntity> list = new ArrayList<>();
        list.add(new LineReplaceEntity(lineTxt, ""));
        rpFileContentByLineNo(txtPath,list);

        frameInformation.setDataStatus(DataStatusEnum.DELETED.getState());
        frameInformation.setUpdateTimestamp(DataUtils.getSysTimeByFormat("yyyyMMddHHmmss"));
        frameInformation.setVersion(frameInformation.getVersion() + 1);


        frameInformationDao.updateByPrimaryKeySelective(frameInformation);
    }

    //保存
    public void save(YoloTaggingSaveReq req){
        VideoInformation videoInformation = videoInformationDao.queryByVideoId(req.getVideoId());
        //只有为标注才可有此状态
        if(videoInformation.getTagStatus().equals(TagStatusEnum.W.getState())) {
            videoInformation.setTagStatus(TagStatusEnum.P.getState());
            videoInformation.setUpdateTimestamp(DataUtils.getSysTimeByFormat("yyyyMMddhhmmss"));
            videoInformation.setVersion(videoInformation.getVersion() + 1);
            videoInformationDao.updateByPrimaryKeySelective(videoInformation);

            log.info("保存成功");
        }
        else {
            return;
        }
    }

    //完成
    public void finish(YoloTaggingFinishReq req){
        VideoInformation videoInformation = videoInformationDao.queryByVideoId(req.getVideoId());
        videoInformation.setTagStatus(TagStatusEnum.F.getState());
        videoInformation.setUpdateTimestamp(DataUtils.getSysTimeByFormat("yyyyMMddhhmmss"));
        videoInformation.setVersion(videoInformation.getVersion() + 1);
        videoInformationDao.updateByPrimaryKeySelective(videoInformation);

        log.info("已完成");
    }

    public List<YoloTaggingGetThumbnailRsp> getThumbnail(YoloTaggingGetThumbnailReq req){
        //先查所有
        List<TargetTrackT> targetTrackTs = targetTrackDao.queryByLinkId(req.getLinkId());
        List<YoloTaggingGetThumbnailRsp> rsps = new LinkedList<>();
        for(TargetTrackT targetTrackT : targetTrackTs){
            VideoInformation videoInformation = videoInformationDao.queryByVideoId(targetTrackT.getVideoId());
            //只有已完成才能被用作缩略图
            if(videoInformation.getTagStatus().equals(TagStatusEnum.F.getState())) {
                List<FrameInformation> frameInformations = frameInformationDao.queryByVideoIdAndTrackIdAndType(
                        videoInformation.getVideoId(),
                        targetTrackT.getTrackId(),
                        "1");
                YoloTaggingGetThumbnailRsp yoloTaggingGetThumbnailRsp = new YoloTaggingGetThumbnailRsp();
                yoloTaggingGetThumbnailRsp.setFrame(frameInformations.get(0).getFrame());
                yoloTaggingGetThumbnailRsp.setVideoId(videoInformation.getVideoId());
                yoloTaggingGetThumbnailRsp.setTrackId(targetTrackT.getTrackId());
                yoloTaggingGetThumbnailRsp.setImageUrl("/" + imgBuket + "/" + videoInformation.getVideoId() + '/');
                yoloTaggingGetThumbnailRsp.setHeight(videoInformation.getHeight());
                yoloTaggingGetThumbnailRsp.setWidth(videoInformation.getWidth());
                rsps.add(yoloTaggingGetThumbnailRsp);
            }
            else
                continue;
        }
        return rsps;
    }

}
