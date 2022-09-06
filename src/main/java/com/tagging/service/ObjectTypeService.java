package com.tagging.service;

import com.github.wujun234.uid.UidGenerator;
import com.tagging.common.Constants;
import com.tagging.dao.mapper.FrameInformationDao;
import com.tagging.dao.mapper.ObjectTypeDao;
import com.tagging.dao.mapper.TargetTrackDao;
import com.tagging.dao.mapper.VideoInformationDao;
import com.tagging.dto.ObjectType.*;
import com.tagging.dto.targetTrackT.TargetTrackUpdateReq;
import com.tagging.entity.TargetTrackT;
import com.tagging.entity.TargetTypeT;
import com.tagging.entity.VideoInformation;
import com.tagging.exception.CMSException;
import com.tagging.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class ObjectTypeService {

    @Resource
    ObjectTypeDao objectTypeDao;

    @Resource
    TargetTrackDao targetTrackDao;

    @Resource
    FrameInformationDao frameInformationDao;

    @Resource
    VideoInformationDao videoInformationDao;

    @Resource
    UidGenerator cachedUidGenerator;
    // 新建类型跟型号
    public ObjectTypeRsp creat(ObjectTypeReq req){
        if (objectTypeDao.queryIs(req.getObjectType(), req.getObjectModel()) == null) {
            VideoInformation videoInformation = videoInformationDao.queryByVideoId(req.getVideoId());
            if (videoInformation == null){
                throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE,"视频ID不存在");
            }
            if(objectTypeDao.queryIs(req.getObjectType(), req.getObjectModel()) != null){
                throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE,"该种类型号已存在");
            }
            TargetTypeT targetTypeT = new TargetTypeT();
            ObjectTypeRsp objectTypeRsp = new ObjectTypeRsp();
            targetTypeT.setUniqueId(String.valueOf(cachedUidGenerator.getUID()));
            if (req.getMorT() == 0) {
                targetTypeT.setTargetTypeId(String.valueOf(objectTypeDao.queryMaxTargetId()==null? 0 : objectTypeDao.queryMaxTargetId() + 1));
                targetTypeT.setTrackTypeId(String.valueOf(objectTypeDao.queryMaxTrackId()==null? 0 : objectTypeDao.queryMaxTrackId() + 1));
            } else {
                targetTypeT.setTargetTypeId(String.valueOf(objectTypeDao.queryMaxTargetId()==null? 0 : objectTypeDao.queryMaxTargetId() + 1));
                targetTypeT.setTrackTypeId(objectTypeDao.getTrackTypeId(req.getObjectType()));
            }
            targetTypeT.setLinkId(videoInformation.getShootPlace() +"-"+ req.getObjectType() +"-"+ req.getObjectModel() +"-"+ targetTypeT.getTargetTypeId());
            targetTypeT.setObjectType(req.getObjectType());
            targetTypeT.setObjectModel(req.getObjectModel());
            targetTypeT.setDataStatus("A");
            targetTypeT.setSpecCode01(" ");
            targetTypeT.setSpecCode02(" ");
            targetTypeT.setSpecCode03(" ");
            targetTypeT.setCreateTimestamp(DataUtils.getSysTimeByFormat());
            targetTypeT.setUpdateTimestamp(DataUtils.getSysTimeByFormat());
            targetTypeT.setVersion(1);
            targetTypeT.setRemarks(" ");
            targetTypeT.setReserve(" ");
            objectTypeDao.insert(targetTypeT);
            objectTypeRsp.setLinkId(targetTypeT.getLinkId());
            return objectTypeRsp;
        }
        else{
            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE,"该类别型号已经存在");
        }
    }

    // 查询所有类型
    public List<QueryAllTypeRsp> queryAllType(){
        return objectTypeDao.queryAllType();
    }

    // 查询某个类型下的所有的型号
    public List<ObjectTypeModelRsp> queryModelByType(ObjectTypeModelReq req){
        return objectTypeDao.queryModelByType(req.getObjectType());
    }

    //根据LinkId查询类别型号
    public LinkIdRsp queryByLinkId(LinkIdReq req){
        LinkIdRsp linkIdRsp= objectTypeDao.queryByLinkId(req.getLinkId());
        if (linkIdRsp==null) {
            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE,"不存在改关联ID");
        }
        return objectTypeDao.queryByLinkId(req.getLinkId());
    }

    //查询所有LinkId
    public List<QueryLinkIdRsp> queryAllLinkId(){return objectTypeDao.queryLinkId();}
}
