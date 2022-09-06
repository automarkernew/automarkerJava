package com.tagging.dto.videoInformation;
import lombok.Data;

@Data

public class VideoInformationQueryRsp {
    private String videoId;
    private String videoName;
    private String typeLinkId;
    private String videoFileUrl;
    private String shootTime;
    private String shootPlace;
    private String length;
    private String sensorType;
    private String height;
    private String width;
    private String tagUserId;
    private String motUserId;
    private String tagImgUrl;
    private String motImgUrl;
    private String tagTime;
    private String tagStatus;
    private String isTagged;
    private String isMoted;
}
