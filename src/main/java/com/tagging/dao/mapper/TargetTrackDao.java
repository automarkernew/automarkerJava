package com.tagging.dao.mapper;

import com.tagging.dao.base.BaseDao;
import com.tagging.dto.targetTrackT.TargetTrackGetListRsp;
import com.tagging.dto.targetTrackT.TargetTrackGetListRsp.target;
import com.tagging.dto.targetTrackT.TargetTrackGetTrackRsp;
import com.tagging.dto.targetTrackT.TargetTrackGetTrackRsp.relatedInformation;
import com.tagging.dto.visibleTagging.VisibleTaggingQueryTrackInformationRsp;
import com.tagging.entity.TargetTrackT;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TargetTrackDao extends BaseDao<TargetTrackT> {

    //目标轨迹查询
    public List<TargetTrackGetTrackRsp> targetTrackGetTrack(
            @Param("videoId") String videoId,
            @Param("trackId") String trackId
    );

    public String getImageUrl(
            @Param("videoId") String videoId,
            @Param("trackId") String trackId
    );

    public List<relatedInformation> getRelatedInformation(
            @Param("linkId") String linkId
    );

    //目标库的查询
    public List<TargetTrackGetListRsp> targetTrackGetList(
            @Param("offset") long offset,
            @Param("queryCount") int queryCount,
            @Param("videoName") String videoName,
            @Param("sensorType") String sensorType,
            @Param("objectType") String objectType,
            @Param("shootTimeBegin") String shootTimeBegin,
            @Param("shootTimeEnd") String shootTimeEnd,
            @Param("shootPlace") String shootPlace
    );

    public long count(
            @Param("videoName") String videoName,
            @Param("sensorType") String sensorType,
            @Param("objectType") String objectType,
            @Param("shootTimeBegin") String shootTimeBegin,
            @Param("shootTimeEnd") String shootTimeEnd,
            @Param("shootPlace") String shootPlace
    );

    List<String> getObjectType(
            @Param("videoId") String videoId
    );

    List<target> getTarget(
            @Param("videoId") String videoId
    );//目标库查询结束

    public List<VisibleTaggingQueryTrackInformationRsp> queryTrackInformation(
            @Param("videoId") String videoId,
            @Param("trackId") String trackId,
            @Param("frameInformationId") String frameInformationId);

    public void updateTrackInformation(
            @Param("objectType") String objectType,
            @Param("objectModel") String objectModel,
            @Param("trackTypeId") String trackTypeId,
            @Param("videoId") String videoId,
            @Param("trackId") String trackId
    );

    public void updateFrameInformation(
            @Param("leftUpperCornerAbscissa") String leftUpperCornerAbscissa,
            @Param("leftUpperCornerOrdinate") String leftUpperCornerOrdinate,
            @Param("rightLowerQuarterAbscissa") String rightLowerQuarterAbscissa,
            @Param("rightLowerQuarterOrdinate") String rightLowerQuarterOrdinate,
            @Param("frameInformationId") String frameInformationId

    );

    TargetTrackT queryByVideoIdAndTrackId(@Param("videoId") String videoId,
                                                @Param("trackId") String trackId);

    TargetTrackT queryById(@Param("uniqueId") String uniqueId);

    List<String> queryTypeByVideoId(@Param("videoId") String videoId);

    void deleteByVideoIdAndTrackId(@Param("videoId") String videoId,
                                    @Param("trackId") String trackId);

    void deleteByVideoId(@Param("videoId") String videoId);

    List<TargetTrackT> queryTrack(@Param("videoId") String videoId,
                                    @Param("targetTypeId") String targetTypeId);

    List<TargetTrackT> queryByVideoId(@Param("videoId") String videoId);

    public TargetTrackT queryByTrack(
            @Param("videoId") String videoId,
            @Param("trackId") String trackId);

    public List<TargetTrackT> queryTrackIdByLoadFile(
            @Param("targetTypeId") String targetTypeId,
            @Param("videoId") String videoId
    );

    long queryTrackNumbers(@Param("videoId") String videoId);

    List<TargetTrackT> queryByLinkId(@Param("linkId") String linkId);

}
