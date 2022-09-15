package com.tagging.service;

import com.github.wujun234.uid.UidGenerator;
import com.tagging.common.Constants;
import com.tagging.dao.mapper.FrameInformationDao;
import com.tagging.dao.mapper.ObjectTypeDao;
import com.tagging.dao.mapper.TargetTrackDao;
import com.tagging.dao.mapper.VideoInformationDao;
import com.tagging.dto.infraredImageTagging.Coordinate;
import com.tagging.dto.infraredImageTagging.ImageRegisterReq;
import com.tagging.dto.infraredImageTagging.InfraredImageTaggingTagReq;
import com.tagging.dto.infraredImageTagging.InfraredImageTaggingTagRsp;
import com.tagging.entity.FrameInformation;
import com.tagging.entity.TargetTrackT;
import com.tagging.entity.VideoInformation;
import com.tagging.enums.DataStatusEnum;
import com.tagging.enums.VideoInformation.IsMotedEnum;
import com.tagging.exception.CMSException;
import com.tagging.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static com.tagging.utils.FileUtils.consumeInputStream;
import static com.tagging.utils.FileUtils.copyDir;

@Service
@Slf4j
public class InfraredImageTaggingService {
    @Resource
    TargetTrackDao targetTrackDao;

    @Resource
    ObjectTypeDao objectTypeDao;

    @Resource
    VideoInformationDao videoInformationDao;

    @Resource
    FrameInformationDao frameInformationDao;

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

    @Value("${minio.motimgBuket}")
    private String motimgBuket;

    @Value("${minio.imgBuket}")
    private String imgBuket;

    @Value("${minio.txtBuket}")
    private String txtBuket;

    @Value("${yolo5.executeUrl}")
    private String executeUrl;

    @Value("${yolo5.pythonName}")
    private String pythonName;

    private static final Logger logger = LoggerFactory.getLogger(InfraredImageTaggingService.class);

    public InfraredImageTaggingTagRsp tag(InfraredImageTaggingTagReq req) throws IOException{
        VideoInformation video1 = videoInformationDao.queryByVideoId(req.getVideoID());//红外视频
        VideoInformation video2 = videoInformationDao.queryByVideoId(req.getLinkedVideoId());//可见光视频

        if(!video2.getIsMoted().equals(IsMotedEnum.EXECUTED.getState())){
            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE, "可见光视频还未进行轨迹跟踪");
        }


        //同步轨迹表的信息
        List<TargetTrackT> targetTrackTList = targetTrackDao.queryByVideoId(req.getLinkedVideoId());
        for(int i=0;i<targetTrackTList.size();i++){
            TargetTrackT targetTrackT = targetTrackTList.get(i);
            targetTrackT.setUniqueId(String.valueOf(cachedUidGenerator.getUID()));
            targetTrackT.setVideoId(req.getVideoID());
            targetTrackT.setCoordinateFileUrl("/" + mottxtBuket + "/"
                    + String.valueOf(video1.getVideoId())
                    + "/object"
                    + String.valueOf(targetTrackT.getTrackId())
                    + ".txt"
                    );
            targetTrackDao.insert(targetTrackT);
        }

        //同步标注信息表(理论上需要修改坐标)
        List<FrameInformation> frameInformationList = frameInformationDao.queryToCv2(req.getLinkedVideoId());
        for (int i=0;i<frameInformationList.size();i++){
            FrameInformation frameInformation = frameInformationList.get(i);
            frameInformation.setFrameInformationId(String.valueOf(cachedUidGenerator.getUID()));
            frameInformation.setVideoId(req.getVideoID());
            frameInformation.setUpdateTimestamp(DataUtils.getSysTimeByFormat());
            frameInformationDao.insert(frameInformation);
        }

        //将可见光生成的信息复制
        //复制yolo生成的detect.txt信息
        String yolopath1 = minioLocalUrl + "/" + txtBuket + "/" + video1.getVideoId() + "/"; //红外路径
        String yolopath2 = minioLocalUrl + "/" + txtBuket + "/" + video2.getVideoId() + "/"; //可见光路径
        File yolofile1 = new File(yolopath1);
        File yolofile2 = new File(yolopath2);
        copyDir(yolofile2,yolofile1);

        //复制mottxt文件夹下的所有信息
        String motpath1 = minioLocalUrl + "/" + mottxtBuket + "/" + video1.getVideoId() + "/"; //红外
        String motpath2 = minioLocalUrl + "/" + mottxtBuket + "/" + video2.getVideoId() + "/"; //可见光
        File motfile1 = new File(motpath1);
        File motfile2 = new File(motpath2);
        copyDir(motfile2,motfile1);

        //画图
        InfraredImageTaggingTagRsp rsp = DrawByCv2(video1.getVideoId());


        //视频的标注信息更新
        video1.setIsTagged("1");
        video1.setIsMoted("1");
        videoInformationDao.updateByPrimaryKey(video1);

        return rsp;
    }
    //抄来的画图
    public InfraredImageTaggingTagRsp DrawByCv2(String videoId) throws IOException {
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
            String cmdC = pythonName  + " " + yoloUrl + "/tracking_paint.py" + " " +
                    minioLocalUrl + '/' + minioBucketName + '/' + videoId + '/' + videoInformation.getVideoName() + " " +
                    filenameTemp + " " +
                    minioLocalUrl + '/' + motimgBuket + '/' + videoId + '/';
            String cmdB = "cd"+' '+ executeUrl;
            Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", cmdB +"&&"+ cmdC});

            InfraredImageTaggingTagRsp infraredImageTaggingTagRsp =new InfraredImageTaggingTagRsp();
            infraredImageTaggingTagRsp.setHeight(videoInformation.getHeight());
            infraredImageTaggingTagRsp.setWidth(videoInformation.getWidth());
            infraredImageTaggingTagRsp.setImageUrl(motimgBuket + '/' +videoId);
            videoInformation.setMotImgUrl(motimgBuket + '/' + videoId);
            videoInformation.setVersion(videoInformation.getVersion()+1);
            videoInformationDao.updateByPrimaryKey(videoInformation);

            return infraredImageTaggingTagRsp;
        }catch (Exception e){
            e.printStackTrace();
            log.info("轨迹图生成失败");
            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE,"轨迹图生成失败");
        }
    }

    public void motByVideoId(ImageRegisterReq req) throws IOException {
        VideoInformation InfraredVideoInformation = videoInformationDao.selectByPrimaryKey(req.getVideoId());
        List<Coordinate> coordinateInfrareds = req.getInfraredImagePoints();
        List<Coordinate> coordinateVisibles = req.getVisibleImagePoints();

        if (coordinateInfrareds.size() == 0 && coordinateVisibles.size() == 0){
            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE,"可见光或红外坐标点为0个");
        }
        if (coordinateInfrareds.size() != coordinateVisibles.size()){
            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE,"可见光或红外坐标点数量不相等");
        }
        if (InfraredVideoInformation.getIsMoted().equals("1")){
            logger.info("可见光视频已经完成一次轨迹跟踪，现在进行删除更新操作");
            List<FrameInformation> frameInformations = frameInformationDao.queryAllFrames(req.getVideoId());
            for (FrameInformation frameInformation : frameInformations) {
                frameInformation.setDataStatus(DataStatusEnum.DELETED.getState());
                frameInformation.setVersion(frameInformation.getVersion() + 1);
                frameInformation.setUpdateTimestamp(DataUtils.getSysTimeByFormat("yyyyMMddHHmmss"));
                frameInformationDao.updateByPrimaryKey(frameInformation);
            }
        }
        StringBuilder InfraredXY = new StringBuilder();
        StringBuilder VisibleXY = new StringBuilder();
        for (int i = 0; i < coordinateInfrareds.size(); i++) {
            Coordinate coordinateInfrared = coordinateInfrareds.get(i);
            Coordinate coordinateVisible = coordinateVisibles.get(i);
            if (i != coordinateInfrareds.size() - 1) {
                InfraredXY.append(coordinateInfrared.getX()).append(",").append(coordinateInfrared.getY()).append(",");
                VisibleXY.append(coordinateVisible.getX()).append(",").append(coordinateVisible.getY()).append(",");
            }
            else {
                InfraredXY.append(coordinateInfrared.getX()).append(",").append(coordinateInfrared.getY());
                VisibleXY.append(coordinateVisible.getX()).append(",").append(coordinateVisible.getY());
            }

        }

        String VisibleMotTxtPath = minioLocalUrl + File.separator + mottxtBuket + File.separator + req.getLinkedVideoId() + File.separator + "mot.txt";
        String InfraredMotTxtPath = minioLocalUrl + File.separator + mottxtBuket + File.separator + req.getVideoId() + File.separator + "mot.txt";
        String InfraredWeights = InfraredVideoInformation.getWidth();
        String InfraredHeight = InfraredVideoInformation.getHeight();
        File fileVisible = new File(VisibleMotTxtPath);
        if (!fileVisible.exists()){
            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE,"红外对应的可见光视频的mot.txt文件不存在");
        }
        try {
            String cmd = "cd" +' '+ executeUrl +  " " + "&&" + " " + pythonName  + ' ' + yoloUrl + "/calc.py" + " " +
                    VisibleXY + " " +
                    InfraredXY + " " +
                    VisibleMotTxtPath + " " +
                    InfraredMotTxtPath + " " +
                    InfraredWeights + " " +
                    InfraredHeight;
            Process p;
            p = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", cmd});
            String inStr = consumeInputStream(p.getInputStream());
            String errStr = consumeInputStream(p.getErrorStream());
            p.waitFor();
            log.info(cmd);
        }catch (Exception e){
            e.printStackTrace();
            log.info("红外轨迹框自动生成失败");
            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE,"红外轨迹框自动生成失败");
        }

        File fileInfrared = new File(InfraredMotTxtPath);
        String encoding = "utf-8";
        try (InputStreamReader read = new InputStreamReader(Files.newInputStream(fileInfrared.toPath()), encoding);
             BufferedReader bufferedReader = new BufferedReader(read)) {
            //判断文件是否存在
            if (fileInfrared.isFile() && fileInfrared.exists()) {
                String lineTxt;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    insertCor(lineTxt, InfraredVideoInformation);
                }
                try {
                    String cmd = "cd "
                            + executeUrl
                            + " && " + pythonName  + " " + yoloUrl + "/tracking_paint.py" + " " +
                            minioLocalUrl + '/' + minioBucketName + '/' + req.getVideoId() + '/' + InfraredVideoInformation.getVideoName() + " " +
                            InfraredMotTxtPath + " " +
                            minioLocalUrl + '/' + motimgBuket + '/' + req.getVideoId() + '/';
                    Process p;
                    p = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", cmd});
                    String inStr = consumeInputStream(p.getInputStream());
                    String errStr = consumeInputStream(p.getErrorStream());
                    p.waitFor();
                    log.info(cmd);

                    InfraredVideoInformation.setMotImgUrl(motimgBuket + '/' + req.getVideoId());
                    InfraredVideoInformation.setIsTagged(String.valueOf(1));
                    InfraredVideoInformation.setIsMoted(String.valueOf(1));
                    InfraredVideoInformation.setUpdateTimestamp(DataUtils.getSysTimeByFormat("yyyyMMddHHmmss"));
                    InfraredVideoInformation.setVersion(InfraredVideoInformation.getVersion() + 1);
                    videoInformationDao.updateByPrimaryKey(InfraredVideoInformation);
                }catch (Exception e){
                    e.printStackTrace();
                    log.info("轨迹图生成失败");
                    throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE,"轨迹图生成失败");
                }
            } else {
                throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE,"红外视频的mot.txt生成错误");
            }
        } catch (Exception e) {
            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE,"红外视频的mot.txt读取错误");
        }
    }

    public void insertCor(String lineTxt, VideoInformation InfraredVideoInformation){
        List<String> lines = Arrays.asList(lineTxt.split(" "));
        FrameInformation frameInformation = new FrameInformation();
        frameInformation.setFrameInformationId(String.valueOf(cachedUidGenerator.getUID()));
        frameInformation.setVideoId(InfraredVideoInformation.getVideoId());
        frameInformation.setFrame(Integer.valueOf(lines.get(0)));
        frameInformation.setFrameType(String.valueOf(1));
        frameInformation.setTrackId(lines.get(1));
        frameInformation.setLeftUpperCornerAbscissa(lines.get(2));
        frameInformation.setLeftUpperCornerOrdinate(lines.get(3));
        frameInformation.setRightLowerQuarterAbscissa(lines.get(4));
        frameInformation.setRightLowerQuarterOrdinate(lines.get(5));
        frameInformation.setCreateTimestamp(DataUtils.getSysTimeByFormat());
        frameInformation.setUpdateTimestamp(DataUtils.getSysTimeByFormat());
        frameInformation.setSpecCode01("");
        frameInformation.setSpecCode02("");
        frameInformation.setSpecCode03("");
        frameInformation.setVersion(1);
        frameInformation.setDataStatus(DataStatusEnum.AVAILABLE.getState());
        frameInformation.setRemarks("");
        frameInformation.setReserve("");
        frameInformationDao.insert(frameInformation);
    }
//    public void updateOffsetByVideoId(ImageRegisterReq req){
//        VideoInformation videoInformation = videoInformationDao.selectByPrimaryKey(req.getVideoId());
//        VideoInformation videoInformation1 = videoInformationDao.selectByPrimaryKey(req.getLinkedVideoId());
//        if (videoInformation1 == null){
//            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE,"未上传该视频对应的可见光视频");
//        }
//        if (videoInformation == null){
//            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE,"未上传该红外视频");
//        }
//        else if (! videoInformation.getSensorType().equals(SensorTypeEnum.INFRARED.getState())){
//            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE,"该视频不是红外光");
//        }
//        Float InfraredImageX = new Float(req.getInfraredImageX());
//        Float InfraredImageY = new Float(req.getInfraredImageY());
//        Float VisibleImageX = new Float(req.getVisibleImageX());
//        Float VisibleImageY = new Float(req.getVisibleImageY());
//        Float offsetX = VisibleImageX - InfraredImageX;
//        Float offsetY = VisibleImageY - InfraredImageY;
//        videoInformation.setOffsetX(String.valueOf(offsetX));
//        videoInformation.setOffsetY(String.valueOf(offsetY));
//        videoInformation.setUpdateTimestamp(DataUtils.getSysTimeByFormat("yyyyMMddHHmmss"));
//        videoInformation.setVersion(videoInformation.getVersion() + 1);
//        videoInformationDao.updateByPrimaryKeySelective(videoInformation);
//        logger.info("红外光 " + req.getVideoId() + "  与可见光 " + req.getLinkedVideoId()
//                + "  设置 Offset: (" + String.valueOf(offsetX) + ", " + String.valueOf(offsetY) + ") 成功");
//    }

//    public void tagByVisibleId(InfraredImageTaggingTagReq req) throws IOException {
//        String VisibleVideoPath = String.valueOf(Paths.get(minioLocalUrl, imgBuket, req.getLinkedVideoId()));
//        String InfraredVideoPath = String.valueOf(Paths.get(minioLocalUrl, imgBuket, req.getVideoID()));
//        String InfraredMotTxtPath = String.valueOf(Paths.get(minioLocalUrl, mottxtBuket, req.getVideoID(), "mot.txt"));
//        File dir = new File(String.valueOf(Paths.get(minioLocalUrl, mottxtBuket, req.getVideoID())));
//        if (! dir.exists()) {
//            dir.mkdir();
//        }
//        File write = new File(InfraredMotTxtPath);
//        write.createNewFile(); // 创建新文件
//        BufferedWriter out = new BufferedWriter(new FileWriter(write));
//        if (!Objects.equals(getFileNum(VisibleVideoPath), getFileNum(InfraredVideoPath))){
//            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE,"红外与可见光帧数不同，无法自动匹配");
//        }
//        List<FrameInformation> frameInformations = frameInformationDao.queryAllFrames(req.getLinkedVideoId());
//        VideoInformation videoInformationVisible = videoInformationDao.selectByPrimaryKey(req.getLinkedVideoId());
//        VideoInformation videoInformationInfrared = videoInformationDao.selectByPrimaryKey(req.getVideoID());
//        Float height = new Float(videoInformationInfrared.getHeight());
//        Float width = new Float(videoInformationInfrared.getWidth());
//        if (videoInformationInfrared.getOffsetX().equals("") || videoInformationInfrared.getOffsetY().equals("")){
//            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE,"红外与可见光未设置offset");
//        }
//        Float offsetX = new Float(videoInformationInfrared.getOffsetX());
//        Float offsetY = new Float(videoInformationInfrared.getOffsetY());
//        if (! videoInformationVisible.getIsTagged().equals("1"))
//        {
//            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE,"对应的可见光未标注");
//        }
//        if (videoInformationInfrared.getIsTagged().equals("1"))
//        {
//            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE,"红外光已标注");
//        }
//
//        for (FrameInformation frameInformation : frameInformations) {
//            Map<String, Float> map = getInfraredCornerByVisible(new Float(frameInformation.getLeftUpperCornerAbscissa()),
//                    new Float(frameInformation.getLeftUpperCornerOrdinate()),
//                    new Float(frameInformation.getRightLowerQuarterAbscissa()),
//                    new Float(frameInformation.getRightLowerQuarterOrdinate()),
//                    height, width, offsetX, offsetY);
//            if (map.get("isIn").equals(1.0F)) {
//                FrameInformation frameInformation1;
//                frameInformation1 = frameInformation;
//                frameInformation1.setFrameInformationId(String.valueOf(cachedUidGenerator.getUID()));
//                frameInformation1.setVideoId(req.getVideoID());
//                frameInformation1.setLeftUpperCornerAbscissa(String.valueOf(map.get("leftUpperCornerAbscissaInfrared")));
//                frameInformation1.setLeftUpperCornerOrdinate(String.valueOf(map.get("leftUpperCornerOrdinateInfrared")));
//                frameInformation1.setRightLowerQuarterAbscissa(String.valueOf(map.get("rightLowerQuarterAbscissaInfrared")));
//                frameInformation1.setRightLowerQuarterOrdinate(String.valueOf(map.get("rightLowerQuarterOrdinateInfrared")));
//                frameInformation1.setCreateTimestamp(DataUtils.getSysTimeByFormat());
//                frameInformation1.setUpdateTimestamp(DataUtils.getSysTimeByFormat());
//
//                out.write(frameInformation1.getFrame() + " " +
//                              frameInformation1.getTrackId() + " " +
//                              frameInformation1.getLeftUpperCornerAbscissa() + " " +
//                              frameInformation1.getLeftUpperCornerOrdinate() + " " +
//                              frameInformation1.getRightLowerQuarterAbscissa() + " " +
//                              frameInformation1.getRightLowerQuarterOrdinate() + "\r\n"); // \r\n即为换行
//                out.flush(); // 把缓存区内容压入文件
//                frameInformationDao.insert(frameInformation1);
//            }
//        }
//        out.close(); // 关闭文件
//        videoInformationInfrared.setIsTagged(videoInformationVisible.getIsTagged());
//        log.info("红外 ID: " + req.getVideoID() + "标注成功");
//        try {
//            String cmd = "cd "
//                    + executeUrl
//                    + " && " + pythonName  + " " + yoloUrl + "/tracking_paint.py" + " " +
//                    minioLocalUrl + '/' + minioBucketName + '/' + req.getVideoID() + '/' + videoInformationInfrared.getVideoName() + " " +
//                    InfraredMotTxtPath + " " +
//                    minioLocalUrl + '/' + motimgBuket + '/' + req.getVideoID() + '/';
//            Process p;
//            p = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", cmd});
//            String inStr = consumeInputStream(p.getInputStream());
//            String errStr = consumeInputStream(p.getErrorStream());
//            p.waitFor();
//            log.info(cmd);
//
//            InfraredImageTaggingTagRsp infraredImageTaggingTagRsp =new InfraredImageTaggingTagRsp();
//            infraredImageTaggingTagRsp.setHeight(videoInformationInfrared.getHeight());
//            infraredImageTaggingTagRsp.setWidth(videoInformationInfrared.getWidth());
//            infraredImageTaggingTagRsp.setImageUrl(motimgBuket + '/' + req.getVideoID());
//            videoInformationInfrared.setMotImgUrl(motimgBuket + '/' + req.getVideoID());
//            videoInformationInfrared.setVersion(videoInformationInfrared.getVersion()+1);
//            videoInformationDao.updateByPrimaryKey(videoInformationInfrared);
//        }catch (Exception e){
//            e.printStackTrace();
//            log.info("轨迹图生成失败");
//            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE,"轨迹图生成失败");
//        }
//        videoInformationInfrared.setIsMoted(videoInformationVisible.getIsMoted());
//    }
//
//    public Integer getFileNum(String filePath){
//        File file = new File(filePath);
//        File[] files = file.listFiles();
//        assert files != null;
//        return files.length;
//    }
//
//    public Map<String, Float> getInfraredCornerByVisible(Float leftUpperCornerAbscissa,
//                                                         Float leftUpperCornerOrdinate,
//                                                         Float rightLowerQuarterAbscissa,
//                                                         Float rightLowerQuarterOrdinate,
//                                                         Float height, Float width,
//                                                         Float offsetX, Float offsetY){
//        float leftUpperCornerAbscissaInfrared = leftUpperCornerAbscissa - offsetX;
//        float leftUpperCornerOrdinateInfrared = leftUpperCornerOrdinate - offsetY;
//        float rightLowerQuarterAbscissaInfrared = rightLowerQuarterAbscissa - offsetX;
//        float rightLowerQuarterOrdinateInfrared = rightLowerQuarterOrdinate - offsetY;
//
//        Map<String, Float> map = new HashMap<>();
//
//
//        if (!(leftUpperCornerAbscissaInfrared <= width && leftUpperCornerOrdinateInfrared <= height)) {
//            map.put("isIn", 0.0F);
//        }else if (! (rightLowerQuarterAbscissaInfrared >= 0 && rightLowerQuarterOrdinateInfrared >= 0)){
//            map.put("isIn", 0.0F);
//        } else {
//            map.put("isIn", 1.0F);
//            map.put("leftUpperCornerAbscissaInfrared", leftUpperCornerAbscissaInfrared <= 0 ? 0 : leftUpperCornerAbscissaInfrared);
//            map.put("leftUpperCornerOrdinateInfrared", leftUpperCornerOrdinateInfrared <= 0 ? 0 : leftUpperCornerOrdinateInfrared);
//            map.put("rightLowerQuarterAbscissaInfrared", rightLowerQuarterAbscissaInfrared >= width ? width : rightLowerQuarterAbscissaInfrared);
//            map.put("rightLowerQuarterOrdinateInfrared", rightLowerQuarterOrdinateInfrared <= height ? height : rightLowerQuarterOrdinateInfrared);
//        }
//        return map;
//    }
}

