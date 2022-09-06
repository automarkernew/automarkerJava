package com.tagging.dto.yoloTagging;
import lombok.Data;

@Data

public class YoloTaggingGetCoordinateReq {
    private String videoId;
    private String frameType;
    private Integer frame;
    private String trackId;
}
