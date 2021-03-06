package com.tagging.service;

import com.github.wujun234.uid.UidGenerator;
import com.tagging.common.Constants;
import com.tagging.dao.mapper.FrameInformationDao;
import com.tagging.dao.mapper.ObjectTypeDao;
import com.tagging.dao.mapper.VideoInformationDao;
import com.tagging.dto.infraredImageTagging.InfraredImageTaggingTagReq;
import com.tagging.dto.infraredImageTagging.InfraredImageTaggingTagRsp;
import com.tagging.dto.visibleTagging.VisibleTaggingMotRsp;
import com.tagging.entity.FrameInformation;
import com.tagging.entity.TargetTrackT;
import com.tagging.entity.VideoInformation;
import com.tagging.enums.VideoInformation.IsMotedEnum;
import com.tagging.exception.CMSException;
import com.tagging.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.tagging.dao.mapper.TargetTrackDao;

import java.io.*;


import javax.annotation.Resource;
import java.util.List;

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

    @Value("${minio.mottxtBucket}")
    private String mottxtBucket;

    @Value("${minio.motingBuket}")
    private String motingBuket;

    @Value("${yolo5.executeUrl}")
    private String executeUrl;

    @Value("${yolo5.pythonName}")
    private String pythonName;

    public InfraredImageTaggingTagRsp tag(InfraredImageTaggingTagReq req) throws IOException{
        VideoInformation video1 = videoInformationDao.queryByVideoId(req.getVideoID());//????????????
        VideoInformation video2 = videoInformationDao.queryByVideoId(req.getLinkedVideoId());//???????????????

        if(!video2.getIsMoted().equals(IsMotedEnum.EXECUTED.getState())){
            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE, "???????????????????????????????????????");
        }


        //????????????????????????
        List<TargetTrackT> targetTrackTList = targetTrackDao.queryByVideoId(req.getLinkedVideoId());
        for(int i=0;i<targetTrackTList.size();i++){
            TargetTrackT targetTrackT = targetTrackTList.get(i);
            targetTrackT.setUniqueId(String.valueOf(cachedUidGenerator.getUID()));
            targetTrackT.setVideoId(req.getVideoID());
            targetTrackT.setCoordinateFileUrl("/mottxt/"
                    + String.valueOf(video1.getVideoId())
                    + "/object"
                    + String.valueOf(targetTrackT.getTrackId())
                    + ".txt"
                    );
            targetTrackDao.insert(targetTrackT);
        }

        //?????????????????????(???????????????????????????)
        List<FrameInformation> frameInformationList = frameInformationDao.queryToCv2(req.getLinkedVideoId());
        for (int i=0;i<frameInformationList.size();i++){
            FrameInformation frameInformation = frameInformationList.get(i);
            frameInformation.setFrameInformationId(String.valueOf(cachedUidGenerator.getUID()));
            frameInformation.setVideoId(req.getVideoID());
            frameInformation.setUpdateTimestamp(DataUtils.getSysTimeByFormat());
            frameInformationDao.insert(frameInformation);
        }

        //?????????????????????????????????
        //??????yolo?????????detect.txt??????
        String yolopath1 = minioLocalUrl + "/txt/" + video1.getVideoId() + "/"; //????????????
        String yolopath2 = minioLocalUrl + "/txt/" + video2.getVideoId() + "/"; //???????????????
        File yolofile1 = new File(yolopath1);
        File yolofile2 = new File(yolopath2);
        copyDir(yolofile2,yolofile1);

        //??????mottxt???????????????????????????
        String motpath1 = minioLocalUrl + "/mottxt/" + video1.getVideoId() + "/"; //??????
        String motpath2 = minioLocalUrl + "/mottxt/" + video2.getVideoId() + "/"; //?????????
        File motfile1 = new File(motpath1);
        File motfile2 = new File(motpath2);
        copyDir(motfile2,motfile1);

        //??????
        InfraredImageTaggingTagRsp rsp = DrawByCv2(video1.getVideoId());


        //???????????????????????????
        video1.setIsTagged("1");
        video1.setIsMoted("1");
        videoInformationDao.updateByPrimaryKey(video1);

        return rsp;
    }
    //???????????????
    public InfraredImageTaggingTagRsp DrawByCv2(String videoId) throws IOException {
        List<FrameInformation> frameInformationArrayList = frameInformationDao.queryToCv2(videoId);
        String timeNow = DataUtils.getSysTimeByFormat();
        File file =new File(minioLocalUrl + '/' + mottxtBucket + '/' + videoId);
        if  (!file .exists()  && !file .isDirectory()) {
            file.mkdir();
        }
        String filenameTemp =  minioLocalUrl + '/' + mottxtBucket + '/' + videoId + '/' + timeNow + ".txt";
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
                    minioLocalUrl + '/' + motingBuket + '/' + videoId + '/';
            String cmdB = "cd"+' '+ executeUrl;
            Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", cmdB +"&&"+ cmdC});

            InfraredImageTaggingTagRsp infraredImageTaggingTagRsp =new InfraredImageTaggingTagRsp();
            infraredImageTaggingTagRsp.setHeight(videoInformation.getHeight());
            infraredImageTaggingTagRsp.setWidth(videoInformation.getWidth());
            infraredImageTaggingTagRsp.setImageUrl(motingBuket + '/' +videoId);
            videoInformation.setMotImgUrl(motingBuket + '/' + videoId);
            videoInformation.setVersion(videoInformation.getVersion()+1);
            videoInformationDao.updateByPrimaryKey(videoInformation);

            return infraredImageTaggingTagRsp;
        }catch (Exception e){
            e.printStackTrace();
            log.info("?????????????????????");
            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE,"?????????????????????");
        }
    }
}

