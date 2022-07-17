package com.tagging.dao.mapper;

import com.tagging.dao.base.BaseDao;
import com.tagging.dto.videoInformation.VideoInformationQueryRsp;
import com.tagging.dto.videoInformation.VideoInformationQueryShootPlaceRsp;
import com.tagging.dto.videoInformation.VideoLengthRsp;
import com.tagging.entity.VideoInformation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface VideoInformationDao extends BaseDao<VideoInformation> {
    public List<VideoInformationQueryRsp> query(@Param("offset") long offset,
                                                @Param("queryCount") int queryCount,
                                                @Param("videoName") String videoName,
                                                @Param("shootTimeBegin") String shootTimeBegin,
                                                @Param("shootTimeEnd") String shootTimeEnd,
                                                @Param("shootPlace") String shootPlace,
                                                @Param("sensorType") String sensorType,
                                                @Param("tagStatus") String tagStatus
    );



    public List<VideoInformationQueryRsp> markquery(@Param("offset") long offset,
                                                    @Param("queryCount") int queryCount,
                                                    @Param("tagStatus") String tagStatus,
                                                    @Param("shootTime") String shootTime,
                                                    @Param("shootPlace") String shootPlace,
                                                    @Param("sensorType") String sensorType);

    public long count(@Param("videoName") String videoName,
                      @Param("shootTimeBegin") String shootTimeBegin,
                      @Param("shootTimeEnd") String shootTimeEnd,
                      @Param("shootPlace") String shootPlace,
                      @Param("sensorType") String sensorType,
                      @Param("tagStatus") String tagStatus
    );

    public long markcount(@Param("tagStatus") String tagStatus,
                      @Param("shootTime") String shootTime,
                      @Param("shootPlace") String shootPlace,
                      @Param("sensorType") String sensorType);

    public List<VideoInformation> loadFile(@Param("sensorType") String sensorType,
                                           @Param("shootTime") String shootTime,
                                           @Param("shootPlace") String shootPlace,
                                           @Param("tagStatus") String tagStatus);

    public VideoInformation queryByVideoId(@Param("videoId") String videoId);

    public List<VideoInformationQueryShootPlaceRsp> queryShootPlace();

    public VideoLengthRsp queryVideoLength(@Param("videoId") String videoId);

}
