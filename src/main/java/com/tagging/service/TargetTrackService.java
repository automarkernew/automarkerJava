package com.tagging.service;

import com.tagging.common.Constants;
import com.tagging.dao.mapper.FrameInformationDao;
import com.tagging.dao.mapper.ObjectTypeDao;
import com.tagging.dao.mapper.TargetTrackDao;
import com.tagging.dao.mapper.VideoInformationDao;
import com.tagging.dto.QuerySummaryRsp;
import com.tagging.dto.targetTrackT.*;
import com.tagging.dto.targetTrackT.TargetTrackGetListRsp.target;
import com.tagging.dto.targetTrackT.TargetTrackGetTrackRsp.relatedInformation;
import com.tagging.entity.FrameInformation;
import com.tagging.entity.TargetTrackT;
import com.tagging.entity.TargetTypeT;
import com.tagging.entity.VideoInformation;
import com.tagging.exception.CMSException;
import com.tagging.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class TargetTrackService {
    @Resource
    TargetTrackDao targetTrackDao;
    @Resource
    ObjectTypeDao objectTypeDao;
    @Resource
    VideoInformationDao videoInformationDao;
    @Resource
    FrameInformationDao frameInformationDao;

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

    @Value("${yolo5.executeUrl}")
    private String executeUrl;


    //轨迹库查询
    public TargetTrackGetTrackRsp targetTrackGetTrack(TargetTrackGetTrackReq req){
        List<TargetTrackGetTrackRsp> targetTrackGetTrackRsps = targetTrackDao.targetTrackGetTrack(req.getVideoId(), req.getTrackId());
        if (targetTrackGetTrackRsps.size()>0){
            List<relatedInformation> relatedInformationList=getRelatedInformation(targetTrackGetTrackRsps.get(0).getLinkId());
            targetTrackGetTrackRsps.get(0).setRelatedInformationList(relatedInformationList);
            targetTrackGetTrackRsps.get(0).setImageUrlList(getImageUrl(req));
            targetTrackGetTrackRsps.get(0).setObjectCoordinate(getObjectCoordinate(req.getVideoId(),req.getTrackId()));
        }
        return targetTrackGetTrackRsps.get(0);
    }

    public String[][] getObjectCoordinate(String videoId,String trackId) {
        List<FrameInformation> frameInformationList = frameInformationDao.queryByVideoIdAndTrackId(videoId,trackId);
        String[][] objectCoordinate = new String[frameInformationList.size()][6];
        for (int i = 0;i < frameInformationList.size();i++){
            objectCoordinate[i][0] = String.valueOf(frameInformationList.get(frameInformationList.size() - i - 1).getFrame());
            objectCoordinate[i][1] = String.valueOf(frameInformationList.get(frameInformationList.size() - i - 1).getTrackId());
            objectCoordinate[i][2] = String.valueOf(frameInformationList.get(frameInformationList.size() - i - 1).getLeftUpperCornerAbscissa());
            objectCoordinate[i][3] = String.valueOf(frameInformationList.get(frameInformationList.size() - i - 1).getLeftUpperCornerOrdinate());
            objectCoordinate[i][4] = String.valueOf(frameInformationList.get(frameInformationList.size() - i - 1).getRightLowerQuarterAbscissa());
            objectCoordinate[i][5] = String.valueOf(frameInformationList.get(frameInformationList.size() - i - 1).getRightLowerQuarterOrdinate());
        }
        //截取前九帧的坐标作为输出
        if (frameInformationList.size()>8)
        {
            String[][] output = new String[9][6];
            for (int i = 0;i < 9;i++){
                for (int j = 0;j<6;j++){
                    output[i][j] = objectCoordinate[i][j];
                }
            }
            return output;

        }else {
            return objectCoordinate;
        }
    }

    public List<String> getImageUrl(TargetTrackGetTrackReq req)
    {
        String imageUrlNum = targetTrackDao.getImageUrl(req.getVideoId(),req.getTrackId()); //出现的帧号
        List<String> imageUrlList = new ArrayList<>();
        String string1 = ""; //用于中转的字符变量

        //该循环完成后imageUrlList为倒序的文件路径
        for(int i = 0;i<imageUrlNum.length();i++){
            String string2 = String.valueOf(imageUrlNum.charAt(i));
            if(!string2.equals(" ")){
                string1 = string1 + string2;
            }
            else {
                if(!string1.equals("")) {
                    string1 = "/" + motimgBuket + "/" + req.getVideoId() + "/" + string1 + ".jpg";
                    imageUrlList.add(string1);
                    string1 = "";
                }
            }
        }
        if(!string1.equals(""))
        {
            string1 = "/" + motimgBuket + "/" + req.getVideoId() + "/" + string1 + ".jpg";
            imageUrlList.add(string1);
        }

        //从倒序的文件路径中截取前九个作为输出
        List<String> output = new ArrayList<>();
        if(imageUrlList.size()<9){
            for(int i = imageUrlList.size() - 1;i >= 0;i--){
                output.add(imageUrlList.get(i));
            }
        }
        else
        {
            for(int i = imageUrlList.size() - 1;i > imageUrlList.size() - 10;i--){
                output.add(imageUrlList.get(i));
            }
        }
        return output;
    }

    public List<relatedInformation> getRelatedInformation(String linkId){
        return targetTrackDao.getRelatedInformation(
                linkId
        );
    }
    //以上均为轨迹库查询部分所需的函数


    //目标库的查询
    public List<TargetTrackGetListRsp> targetTrackGetList(long offset, int queryCount,TargetTrackGetListReq req){
        return targetTrackDao.targetTrackGetList(
                offset,
                queryCount,
                req.getVideoName(),
                req.getSensorType(),
                req.getObjectType(),
                req.getShootTimeBegin(),
                req.getShootTimeEnd(),
                req.getShootPlace()
        );
    }

    public QuerySummaryRsp count(TargetTrackGetListReq req){
        QuerySummaryRsp querySummaryRsp = new QuerySummaryRsp();
        long num = targetTrackDao.count(
                req.getVideoName(),
                req.getSensorType(),
                req.getObjectType(),
                req.getShootTimeBegin(),
                req.getShootTimeEnd(),
                req.getShootPlace()
        );
        querySummaryRsp.setDataAmount(num);
        return querySummaryRsp;
    }

    //获得物体类型
    public List<String> getObjectType(String videoId){
        return targetTrackDao.getObjectType(videoId);
    }

    //获得与目标绑定的唯一id
    public List<target> getTarget(String videoId){
        return targetTrackDao.getTarget(videoId);
    }


    //新增目标轨迹
    public void updateTrack(TargetTrackUpdateReq req){
        TargetTypeT targetTypeT = objectTypeDao.queryIs(req.getObjectType(), req.getObjectModel());
        VideoInformation videoInformation = videoInformationDao.queryByVideoId(req.getVideoId());
        if(frameInformationDao.queryByVideoIdAndTrackId(req.getVideoId(),req.getTrackId()).size() == 0){
            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE,"轨迹不存在");
        }
        TargetTrackT targetTrackT = targetTrackDao.queryByVideoIdAndTrackId(req.getVideoId(),req.getTrackId());
        if(targetTrackT == null){
            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE,"轨迹不存在");
        }
        if ( targetTypeT != null && videoInformation!= null) {
//            TargetTrackT targetTrackT = targetTrackDao.queryById(targetTrackTs.getUniqueId());
            targetTrackT.setTrackId(req.getTrackId());
            targetTrackT.setVideoId(req.getVideoId());
            targetTrackT.setLinkId(targetTypeT.getLinkId());
            targetTrackT.setTrackTypeId(targetTypeT.getTrackTypeId());
            targetTrackT.setTargetTypeId(targetTypeT.getTargetTypeId());
            targetTrackT.setMotImgUrl(videoInformation.getMotImgUrl());
            targetTrackT.setMarkInformationSaveTime(DataUtils.getSysTimeByFormat());
            targetTrackT.setUpdateTimestamp(DataUtils.getSysTimeByFormat("yyyyMMddhhmmss"));
            targetTrackT.setVersion(targetTrackT.getVersion() + 1);

            targetTrackDao.updateByPrimaryKeySelective(targetTrackT);
        }
        else {
            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE,"新增失败");
        }
    }

    //查找某个videoId下已经标注好的所有类别
    public List<QueryTypeByVideoIdRsp> queryTypeByVideoId(QueryTypeByVideoIdReq req){
        List<String> TrackTypeIdList = targetTrackDao.queryTypeByVideoId(req.getVideoId());
//        if (targetTypeIdList.size() == 0){
//            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE,"Video不存在或者暂无标注类别型号");
//        }
        List<QueryTypeByVideoIdRsp> queryTypeByVideoIdRspList = new ArrayList<>();
        for (String trackTypeId : TrackTypeIdList) {
            QueryTypeByVideoIdRsp queryTypeByVideoIdRsp = objectTypeDao.queryTypeByTrackTypeId(trackTypeId);
            if(queryTypeByVideoIdRsp != null && !queryTypeByVideoIdRspList.contains(queryTypeByVideoIdRsp)) {
                queryTypeByVideoIdRspList.add(queryTypeByVideoIdRsp);
            }
        }
        return queryTypeByVideoIdRspList;
    }

    //查找某个VideoId下属于某个ObjectType的所有TrackId及基本信息
    public List<QueryTrackRsp> queryTrack(QueryTrackReq req){
        List<TargetTypeT> targetTypeTList = objectTypeDao.queryTrackTypeIdByObjectType(req.getObjectType());
        List<QueryTrackRsp> queryTrackRspList = new ArrayList<>();
        for (TargetTypeT targetTypeT : targetTypeTList) {
            List<TargetTrackT> targetTrackTList = targetTrackDao.queryTrack(req.getVideoId(),targetTypeT.getTargetTypeId());
            for (TargetTrackT targetTrackT : targetTrackTList) {
                QueryTrackRsp queryTrackRsp = new QueryTrackRsp();
                queryTrackRsp.setTrackId(targetTrackT.getTrackId());
                queryTrackRsp.setObjectModel(targetTypeT.getObjectModel());
                queryTrackRsp.setFrameList(targetTrackT.getAppearFrame());
                queryTrackRspList.add(queryTrackRsp);
            }
        }
        return queryTrackRspList;
    }
}
