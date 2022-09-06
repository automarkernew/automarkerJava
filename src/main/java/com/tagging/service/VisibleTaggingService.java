package com.tagging.service;

import com.github.wujun234.uid.UidGenerator;
import com.tagging.common.Constants;
import com.tagging.dao.mapper.FrameInformationDao;
import com.tagging.dao.mapper.ObjectTypeDao;
import com.tagging.dao.mapper.TargetTrackDao;
import com.tagging.dao.mapper.VideoInformationDao;
import com.tagging.dto.visibleTagging.*;
import com.tagging.entity.FrameInformation;
import com.tagging.entity.TargetTrackT;
import com.tagging.entity.TargetTypeT;
import com.tagging.entity.VideoInformation;
import com.tagging.enums.DataStatusEnum;
import com.tagging.enums.VideoInformation.IsMotedEnum;
import com.tagging.enums.VideoInformation.TagStatusEnum;
import com.tagging.exception.CMSException;
import com.tagging.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

import static com.tagging.utils.FileUtils.consumeInputStream;
import static com.tagging.utils.FileUtils.numberOfFiles;

@Service
@Slf4j
public class VisibleTaggingService {

    @Resource
    VideoInformationDao videoInformationDao;

    @Resource
    FrameInformationDao frameInformationDao;

    @Resource
    TargetTrackDao targetTrackDao;

    @Resource
    ObjectTypeDao objectTypeDao;

    @Resource
    UidGenerator cachedUidGenerator;


    @Value("${minio.localUrl}")
    private String minioLocalUrl;

    @Value("${yolo5.localUrl}")
    private String yoloUrl;

    @Value("${minio.videoBucket}")
    private String minioBucketName;

    @Value("${minio.mottxtBuket}")
    private String mottxtBuket;

    @Value("${minio.txtBuket}")
    private String txtBuket;

    @Value("${minio.motimgBuket}")
    private String motimgBuket;

    @Value("${minio.imgBuket}")
    private String imgBuket;

    @Value("${yolo5.executeUrl}")
    private String executeUrl;

    @Value("${yolo5.pythonName}")
    private String pythonName;

    public List<VisibleTaggingQueryTrackInformationRsp> queryTrackInformation(VisibleTaggingQueryTrackInformationReq req){
        return targetTrackDao.queryTrackInformation(
                req.getVideoId(),
                req.getTrackId(),
                req.getFrameInformationId()
        );
    }

    public void updateTrackInformation(VisibleTaggingUpdateTrackInformationReq req){
        targetTrackDao.updateTrackInformation(
                req.getObjectType(),
                req.getObjectModel(),
                req.getTrackTypeId(),
                req.getVideoId(),
                req.getTrackId()
        );
    }

    public void updateFrameInformation(VisibleTaggingUpdateTrackInformationReq req){
        targetTrackDao.updateFrameInformation(
                req.getLeftUpperCornerAbscissa(),
                req.getLeftUpperCornerOrdinate(),
                req.getRightLowerQuarterAbscissa(),
                req.getRightLowerQuarterOrdinate(),
                req.getFrameInformationId()
        );
    }

    //轨迹跟踪
    public VisibleTaggingMotRsp mot(VisibleTaggingMotReq req) {
        VideoInformation videoInformation = videoInformationDao.queryByVideoId(req.getVideoId());
        if(videoInformation.getIsMoted().equals(IsMotedEnum.EXECUTED.getState())){
            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE, "此视频已进行过轨迹跟踪");
        }

        //各地址
        String cmdB = "cd"+' '+ executeUrl;
        String txtPath = minioLocalUrl + "/" + txtBuket + "/" + req.getVideoId() + "/";
        String txtMotPath = minioLocalUrl + "/" + mottxtBuket + "/" + req.getVideoId() + "/";
        String imgMotPath = minioLocalUrl + "/" + motimgBuket + "/" + req.getVideoId() + '/';

        String command1 = "cd "
                + executeUrl
                + " && " + pythonName  +  " "  + yoloUrl + "/mot.py "
                + minioLocalUrl +"/" + videoInformation.getVideoFileUrl() + ' '
                + txtPath + ' '
                + txtMotPath;

        String command2 = "cd "
                + executeUrl
                + " && " + pythonName  +  " " + yoloUrl + "/tracking_paint.py "
                + minioLocalUrl + '/' + videoInformation.getVideoFileUrl() + ' '
                + txtMotPath + "mot.txt "
                + imgMotPath;

        // log.info(command2);

        try {
            Process p1, p2 = null;
            p1 = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", command1});
            String inStr1 = consumeInputStream(p1.getInputStream());
            String errStr1 = consumeInputStream(p1.getErrorStream());
            log.info(command1);
            p1.waitFor();

            p2 = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", command2});
            String inStr2 = consumeInputStream(p2.getInputStream());
            String errStr2 = consumeInputStream(p2.getErrorStream());
            log.info(command2);
            p2.waitFor();

            String filePath = txtMotPath + "mot.txt";

            //轨迹表
            Integer numbers = numberOfFiles(txtMotPath);
            for (int i = 1; i < numbers; i++){
                TargetTrackT targetTrackT = new TargetTrackT();
                targetTrackT.setUniqueId(String.valueOf(cachedUidGenerator.getUID()));
                targetTrackT.setTrackId(String.valueOf(i));
                targetTrackT.setVideoId(req.getVideoId());
                targetTrackT.setTargetTypeId(" ");
                targetTrackT.setTrackTypeId(" ");
                targetTrackT.setAppearFrame("");
                targetTrackT.setLinkId(" ");
                targetTrackT.setCoordinateFileUrl("/" + mottxtBuket + "/" + req.getVideoId() + '/' + "object" + String.valueOf(i) + ".txt");
                targetTrackT.setMotImgUrl(" ");
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
                    frameInformation.setFrame(Integer.valueOf(message[0]));
                    frameInformation.setTrackId(message[1]);

                    //同步更新appearFrame
                    TargetTrackT targetTrackT = targetTrackDao.queryByVideoIdAndTrackId(videoInformation.getVideoId(), message[1]);
                    targetTrackT.setAppearFrame(targetTrackT.getAppearFrame() + message[0] + ' ');
                    targetTrackDao.updateByPrimaryKeySelective(targetTrackT);

                    frameInformation.setLeftUpperCornerAbscissa(message[2]);
                    frameInformation.setLeftUpperCornerOrdinate(message[3]);
                    frameInformation.setRightLowerQuarterAbscissa(message[4]);
                    frameInformation.setRightLowerQuarterOrdinate(message[5]);
                    frameInformation.setFrameType("1");
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
        videoInformation.setIsMoted(IsMotedEnum.EXECUTED.getState());
        videoInformation.setUpdateTimestamp(DataUtils.getSysTimeByFormat("yyyyMMddhhmmss"));
        videoInformation.setVersion(videoInformation.getVersion() + 1);
        videoInformationDao.updateByPrimaryKeySelective(videoInformation);

        VisibleTaggingMotRsp rsp = new VisibleTaggingMotRsp();
        rsp.setImageUrl("/" + motimgBuket + "/" + req.getVideoId() + '/');
        rsp.setHeight(videoInformation.getHeight());
        rsp.setWidth(videoInformation.getWidth());
        return rsp;
    }

    public List<VisibleTaggingMotQueryAllTrackIdRsp> queryAllTrackId(VisibleTaggingMotQueryAllTrackIdReq req){
        List<VisibleTaggingMotQueryAllTrackIdRsp> visibleTaggingMotQueryAllTrackIdRspList = frameInformationDao.queryAllTrackId(req.getVideoId());
        return visibleTaggingMotQueryAllTrackIdRspList;
    }
    // 画图
    public VisibleTaggingMotRsp DrawByCv2(String videoId) throws IOException {
        List<FrameInformation> frameInformationArrayList = frameInformationDao.queryToCv2(videoId);
        String timeNow = DataUtils.getSysTimeByFormat();
        File file =new File(minioLocalUrl + '/' + mottxtBuket + '/' + videoId);
        if  (!file .exists()  && !file .isDirectory()) {
            file.mkdir();
        }
        String filenameTemp =  minioLocalUrl + '/' + mottxtBuket + '/' + videoId + '/' + timeNow + ".txt";
        File filename = new File(filenameTemp);
        if (filename.createNewFile()) {
            BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(filename, true)));
            for (FrameInformation frameInformation : frameInformationArrayList) {
                String word = frameInformation.getFrame().toString() + ' ' +
                        frameInformation.getTrackId().toString() + ' ' +
                        frameInformation.getLeftUpperCornerAbscissa() + ' ' +
                        frameInformation.getLeftUpperCornerOrdinate() + ' ' +
                        frameInformation.getRightLowerQuarterAbscissa() + ' ' +
                        frameInformation.getRightLowerQuarterOrdinate();
                out.write(word + "\r\n");
            }
            out.close();
        }
        try {
            VideoInformation videoInformation = frameInformationDao.queryVideoName(videoId);
            String cmdC = pythonName + " " + yoloUrl + "/tracking_paint.py" + " " +
                    minioLocalUrl + '/' + minioBucketName + '/' + videoId + '/' + videoInformation.getVideoName() + " " +
                    filenameTemp + " " +
                    minioLocalUrl + '/' + motimgBuket + '/' + videoId + '/';
            String cmdB = "cd"+' '+ executeUrl;

            Process p = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", cmdB +"&&"+ cmdC});
            String inStr = consumeInputStream(p.getInputStream());
            String errStr = consumeInputStream(p.getErrorStream());
            p.waitFor();

            VisibleTaggingMotRsp visibleTaggingMotRsp = new VisibleTaggingMotRsp();
            visibleTaggingMotRsp.setHeight(videoInformation.getHeight());
            visibleTaggingMotRsp.setWidth(videoInformation.getWidth());
            visibleTaggingMotRsp.setImageUrl(motimgBuket + '/' + videoId);
            videoInformation.setMotImgUrl(motimgBuket + '/' + videoId);
            videoInformation.setVersion(videoInformation.getVersion() + 1);
            videoInformationDao.updateByPrimaryKey(videoInformation);
            return visibleTaggingMotRsp;
        }catch (Exception e){
            e.printStackTrace();
            log.info("轨迹图生成失败");
            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE,"轨迹图生成失败");
        }
    }
    // 新增框坐标并且重新画图
    public VisibleTaggingMotCreateRsp motCreate(VisibleTaggingMotCreateReq req) throws IOException {
        VideoInformation videoInformation = videoInformationDao.queryByVideoId(req.getVideoId());

        String encoding="GBK";
        Integer number = numberOfFiles(minioLocalUrl + "/" + mottxtBuket + "/" + req.getVideoId());
        System.out.println(number);

        String command = "cd "
                + executeUrl
                + " && " + pythonName  +  " " + yoloUrl + "/object_tracking.py "
                + minioLocalUrl + "/" + videoInformation.getVideoFileUrl() + ' '
                + minioLocalUrl + "/" + mottxtBuket + "/" + req.getVideoId() + "/" + ' '
                + req.getLeftUpperCornerAbscissa() + ' '
                + req.getLeftUpperCornerOrdinate() + ' '
                + req.getRightLowerQuarterAbscissa() + ' '
                + req.getRightLowerQuarterOrdinate() + ' '
                + req.getStartFrame() + ' '
                + req.getEndFrame() + ' '
                + req.getFrame() + ' '
                + "object" + String.valueOf(number);

        log.info(command);

        try {
            Process p = null;
            p = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", command});
            String inStr = consumeInputStream(p.getInputStream());
            String errStr = consumeInputStream(p.getErrorStream());
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        TargetTrackT targetTrackT = new TargetTrackT();
        targetTrackT.setUniqueId(String.valueOf(cachedUidGenerator.getUID()));
        targetTrackT.setTrackId(String.valueOf(number));
        targetTrackT.setVideoId(req.getVideoId());
        targetTrackT.setLinkId(" ");
        targetTrackT.setTargetTypeId(" ");
        targetTrackT.setTrackTypeId(" ");
        targetTrackT.setCoordinateFileUrl("/" + mottxtBuket + "/" + req.getVideoId() + '/' + "new.txt");
        targetTrackT.setMotImgUrl(" ");
        targetTrackT.setAppearFrame("");
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


        File file=new File(minioLocalUrl + "/" + mottxtBuket + "/" + req.getVideoId() + "/object" + String.valueOf(number) + ".txt");
        if(file.isFile() && file.exists()){ //判断文件是否存在
            InputStreamReader read = new InputStreamReader(new FileInputStream(file),encoding);//考虑到编码格式
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt = null;
            while((lineTxt = bufferedReader.readLine()) != null){
                String[] message = lineTxt.split(" ");
                FrameInformation frameInformation = new FrameInformation();
                frameInformation.setFrameInformationId(String.valueOf(cachedUidGenerator.getUID()));
                frameInformation.setVideoId(videoInformation.getVideoId());
                frameInformation.setFrame(Integer.valueOf(message[0]));
                frameInformation.setTrackId(String.valueOf(number));

                //同步更新appearFrame
                TargetTrackT targetTrack = targetTrackDao.queryByVideoIdAndTrackId(videoInformation.getVideoId(), String.valueOf(number));
                targetTrackT.setAppearFrame(targetTrack.getAppearFrame() + message[0] + ' ');
                targetTrackDao.updateByPrimaryKeySelective(targetTrackT);

                frameInformation.setLeftUpperCornerAbscissa(message[1]);
                frameInformation.setLeftUpperCornerOrdinate(message[2]);
                frameInformation.setRightLowerQuarterAbscissa(message[3]);
                frameInformation.setRightLowerQuarterOrdinate(message[4]);
                frameInformation.setFrameType("1");
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


        VisibleTaggingMotRsp visibleTaggingMotRsp = DrawByCv2(req.getVideoId());
        VisibleTaggingMotCreateRsp rsp = new VisibleTaggingMotCreateRsp();
        rsp.setTrackId(String.valueOf(number));
        rsp.setImageUrl(visibleTaggingMotRsp.getImageUrl());
        rsp.setHeight(visibleTaggingMotRsp.getHeight());
        rsp.setWidth(visibleTaggingMotRsp.getWidth());


        return rsp;
    }
    // 修正框坐标并且重新画图
    public VisibleTaggingMotRsp motUpdate(VisibleTaggingMotUpdateReq req) throws IOException {
        if(frameInformationDao.queryByFrameId(req.getFrameInformationId())!=null) {
            frameInformationDao.updateFrameInformation(req.getFrameInformationId(),
                    req.getLeftUpperCornerAbscissa(),
                    req.getLeftUpperCornerOrdinate(),
                    req.getRightLowerQuarterAbscissa(),
                    req.getRightLowerQuarterOrdinate(),
                    DataUtils.getSysTimeByFormat());
            VisibleTaggingMotRsp visibleTaggingMotRsp = DrawByCv2(req.getVideoId());
            return visibleTaggingMotRsp;
        }
        else{
            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE,"FrameInformationId不存在");
        }
    }
    // 删除轨迹中的一个框并且画图
    public VisibleTaggingMotRsp motDelete(VisibleTaggingMotDeleteReq req) throws IOException {
        if(frameInformationDao.queryByFrameId(req.getFrameInformationId())!=null) {
            frameInformationDao.deleteFrameInformation(req.getFrameInformationId(), DataUtils.getSysTimeByFormat());
            VisibleTaggingMotRsp visibleTaggingMotRsp = DrawByCv2(req.getVideoId());
            return visibleTaggingMotRsp;
        }
        else {
            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE,"FrameInformationId不存在");
        }
    }
    //删除整个轨迹并且画图
    public VisibleTaggingMotRsp motDeleteAll(VisibleTaggingMotDeleteAllReq req) throws IOException {
        if(frameInformationDao.queryByVideoIdAndTrackId(req.getVideoId(),req.getTrackId()).size() != 0){
            frameInformationDao.deleteAllFrameInformation(req.getVideoId(), req.getTrackId(), DataUtils.getSysTimeByFormat());
            VisibleTaggingMotRsp visibleTaggingMotRsp = DrawByCv2(req.getVideoId());
            targetTrackDao.deleteByVideoIdAndTrackId(req.getVideoId(),req.getTrackId());
            return visibleTaggingMotRsp;
        }
        else {
            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE,"该轨迹不存在");
        }
    }

    //查询每帧有多少物体
    public VisibleTaggingQueryFrameNumberRsp queryFrameNumber(VisibleTaggingQueryFrameNumberReq req){
        VideoInformation videoInformation = videoInformationDao.queryByVideoId(req.getVideoId());

        //帧数
        Integer frameCount = numberOfFiles(minioLocalUrl + "/" + imgBuket + "/" + req.getVideoId()) - 1;
        VisibleTaggingQueryFrameNumberRsp rsp = new VisibleTaggingQueryFrameNumberRsp();
        List<VisibleTaggingObjectCountRsp> list = new LinkedList<>();
        rsp.setFrameCount(String.valueOf(frameCount));
        for(int i = 1; i < frameCount; i++){
            VisibleTaggingObjectCountRsp visibleTaggingObjectCountRsp = new VisibleTaggingObjectCountRsp();
            visibleTaggingObjectCountRsp.setFrame(i);
            visibleTaggingObjectCountRsp.setCount(frameInformationDao.getObjectCount(i, req.getVideoId(), req.getFrameType()));
            list.add(visibleTaggingObjectCountRsp);
        }
        rsp.setVisibleTaggingObjectCountRsps(list);
        return rsp;
    }

    //确认某些轨迹为同一轨迹后,修改轨迹编号
    public VisibleTaggingMotRsp updateTrackId(VisibleTaggingUpdateTrackIdReq req) throws IOException {
        FrameInformation frameInformationGetTrackId = frameInformationDao.queryByFrameId(req.getFrameInformationId());

        //查询所有该轨迹的标注框,修改轨迹编号
        List<FrameInformation> frameInformations = frameInformationDao.queryByVideoIdAndTrackIdAndType(req.getVideoId(), frameInformationGetTrackId.getTrackId(), "1");
        for(FrameInformation frameInformation : frameInformations){
            frameInformation.setTrackId(req.getTrackId());
            frameInformation.setUpdateTimestamp(DataUtils.getSysTimeByFormat("yyyyMMddhhmmss"));
            frameInformation.setVersion(frameInformation.getVersion() + 1);
            frameInformationDao.updateByPrimaryKeySelective(frameInformation);
        }

        //轨迹表删除此轨迹
        TargetTrackT targetTrackT = targetTrackDao.queryByTrack(req.getVideoId(), frameInformationGetTrackId.getTrackId());
        targetTrackT.setDataStatus(DataStatusEnum.DELETED.getState());
        targetTrackT.setUpdateTimestamp(DataUtils.getSysTimeByFormat("yyyyMMddhhmmss"));
        targetTrackT.setVersion(targetTrackT.getVersion() + 1);
        targetTrackDao.updateByPrimaryKeySelective(targetTrackT);

        return DrawByCv2(req.getVideoId());
    }

    //载入
    public List<VisibleTaggingLoadFileRsp> loadFile(VisibleTaggingLoadFileReq req){

        //第一次查询,只有已标注的视频能作为参考文件
        List<VideoInformation> videoInformations = videoInformationDao.loadFile(req.getSensorType(), req.getShootTime(),
                req.getShootPlace(), TagStatusEnum.F.getState());

        List<VisibleTaggingLoadFileRsp> visibleTaggingLoadFileRsps = new LinkedList<>();

        //通过videoId和类型、型号进行第二次查询
        for (VideoInformation videoInformation : videoInformations) {
            List<TargetTypeT> targetTypeTs = objectTypeDao.queryTypes(req.getObjectType(), req.getObjectModel());
            for(TargetTypeT targetTypeT : targetTypeTs){
                List<TargetTrackT> targetTrackTs = targetTrackDao.queryTrackIdByLoadFile(
                        targetTypeT.getTargetTypeId(),
                        videoInformation.getVideoId()
            );
                for (TargetTrackT targetTrackT : targetTrackTs) {
                    List<FrameInformation> frameInformations = frameInformationDao.queryByVideoIdAndTrackIdAndType(
                            videoInformation.getVideoId(),
                            targetTrackT.getTrackId(),
                            "1"
                    );
                    VisibleTaggingLoadFileRsp visibleTaggingLoadFileRsp = new VisibleTaggingLoadFileRsp();
                    visibleTaggingLoadFileRsp.setVideoId(videoInformation.getVideoId());
                    visibleTaggingLoadFileRsp.setFrame(frameInformations.get(0).getFrame());
                    visibleTaggingLoadFileRsp.setImageUrl("/" + motimgBuket + "/" + videoInformation.getVideoId() + '/');
                    visibleTaggingLoadFileRsp.setHeight(videoInformation.getHeight());
                    visibleTaggingLoadFileRsp.setWidth(videoInformation.getWidth());
                    visibleTaggingLoadFileRsp.setTrackId(frameInformations.get(0).getTrackId());
                    visibleTaggingLoadFileRsps.add(visibleTaggingLoadFileRsp);
                }
            }
        }
        return visibleTaggingLoadFileRsps;
    }

}
