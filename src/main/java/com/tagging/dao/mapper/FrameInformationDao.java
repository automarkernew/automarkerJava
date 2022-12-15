package com.tagging.dao.mapper;

import com.tagging.dao.base.BaseDao;
import com.tagging.dto.visibleTagging.VisibleTaggingMotQueryAllTrackIdRsp;
import com.tagging.dto.yoloTagging.YoloTaggingGetCoordinateRsp;
import com.tagging.entity.FrameInformation;
import com.tagging.entity.VideoInformation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FrameInformationDao extends BaseDao<FrameInformation> {
    public List<YoloTaggingGetCoordinateRsp> getCoordinateRsp(
            @Param("videoId") String videoId,
            @Param("frameType") String frameType,
            @Param("frame") Integer frame,
            @Param("trackId") String trackId);

    public FrameInformation queryByFrameId(@Param("frameInformationId") String frameInformationId);

    List<VisibleTaggingMotQueryAllTrackIdRsp> queryAllTrackId(@Param("videoId") String videoId);

    void updateFrameInformation(@Param("frameInformationId") String frameInformationId,
                                @Param("leftUpperCornerAbscissa") String leftUpperCornerAbscissa,
                                @Param("leftUpperCornerOrdinate") String leftUpperCornerOrdinate,
                                @Param("rightLowerQuarterAbscissa") String rightLowerQuarterAbscissa,
                                @Param("rightLowerQuarterOrdinate") String rightLowerQuarterOrdinate,
                                @Param("updateTimestamp") String updateTimestamp);

    void deleteFrameInformation(@Param("frameInformationId") String frameInformationId,
                                @Param("updateTimestamp") String updateTimestamp);

    void deleteAllFrameInformation(@Param("videoId") String videoId,
                                   @Param("trackId") String trackId,
                                   @Param("updateTimestamp") String updateTimestamp);

    List<FrameInformation> queryToCv2(@Param("videoId") String videoId);

    VideoInformation queryVideoName(@Param("videoId") String videoId);

    List<FrameInformation> queryByVideoIdAndTrackId(@Param("videoId") String videoId,
                                                    @Param("trackId") String trackId);

    public long getObjectCount(@Param("frame") Integer frame,
                               @Param("videoId") String videoId,
                               @Param("frameType") String frameType);

    List<FrameInformation> queryByVideoIdAndTrackIdAndType(@Param("videoId") String videoId,
                                                    @Param("trackId") String trackId,
                                                    @Param("frameType") String frameType);

    List<FrameInformation> queryAllFrames(@Param("videoId") String videoId);

    void deleteByVideoId(@Param("videoId") String videoId);

    public Integer queryFrameMaxByVideoId(@Param("videoId") String videoId);

    List<FrameInformation> queryByFramesAndVideoId(@Param("videoId") String videoId,
                                                   @Param("frame") Integer frame);

}
