package com.tagging.dao.mapper;

import com.tagging.dao.base.BaseDao;
import com.tagging.dto.ObjectType.LinkIdRsp;
import com.tagging.dto.ObjectType.ObjectTypeModelRsp;
import com.tagging.dto.ObjectType.QueryAllTypeRsp;
import com.tagging.dto.ObjectType.QueryLinkIdRsp;
import com.tagging.dto.targetTrackT.QueryTypeByVideoIdRsp;
import com.tagging.entity.TargetTypeT;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ObjectTypeDao extends BaseDao<TargetTypeT> {
    List<QueryAllTypeRsp> queryAllType();

    List<ObjectTypeModelRsp> queryModelByType(@Param("objectType") String objectType);

    String getTrackTypeId(@Param("objectType") String objectType);

    Integer queryMaxTargetId();

    Integer queryMaxTrackId();

    TargetTypeT queryIs(@Param("objectType") String objectType,
                        @Param("objectModel") String objectModel);

    List<TargetTypeT> queryTypes(@Param("objectType") String objectType,
                        @Param("objectModel") String objectModel);

    QueryTypeByVideoIdRsp queryTypeByTrackTypeId(@Param("trackTypeId") String trackTypeId);

    List<TargetTypeT> queryTrackTypeIdByObjectType(@Param("objectType") String objectType);

    List<QueryLinkIdRsp> queryLinkId();

    LinkIdRsp queryByLinkId(@Param("linkId") String linkId);

    TargetTypeT queryByTrackTypeIdAndTargetTypeId(@Param("trackTypeId") String trackTypeId,
                                                  @Param("objectType") String objectType);

}
