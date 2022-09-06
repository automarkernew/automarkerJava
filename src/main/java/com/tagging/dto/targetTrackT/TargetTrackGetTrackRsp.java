package com.tagging.dto.targetTrackT;

import lombok.Data;

import java.util.List;

@Data
public class TargetTrackGetTrackRsp {
    private String motImgUrl;
    private String shootTime;
    private String shootPlace;
    private String sensorType;
    private String objectType;
    private String objectModel;
    private String trackId;
    private String targetTypeId;
    private String linkId;
    private String[][] objectCoordinate;
    private List<String> imageUrlList;
    private List<relatedInformation> relatedInformationList;
    public static class relatedInformation{
        private String videoId;
        private String videoName;
        private String trackId;
    }
}
