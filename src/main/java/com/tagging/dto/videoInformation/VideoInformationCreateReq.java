package com.tagging.dto.videoInformation;
import lombok.Data;

@Data

public class VideoInformationCreateReq {
    private String shootTime;
    private String shootPlace;
    private String sensorType;
    private String typeLinkId;
}
