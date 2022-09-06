package com.tagging.dto.videoInformation;

import lombok.Data;

@Data
public class VideoFromServerCreateReq {
    private String shootTime;
    private String shootPlace;
    private String sensorType;
    private String fileName;
    private String typeLinkId;
}
