package com.tagging.dto.videoInformation;
import lombok.Data;

@Data

public class VideoInformationQueryReq {
    private String videoName;
    private String shootTimeBegin;
    private String shootTimeEnd;
    private String shootPlace;
    private String sensorType;
    private String tagStatus;
}
