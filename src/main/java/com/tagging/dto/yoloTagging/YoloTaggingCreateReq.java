package com.tagging.dto.yoloTagging;
import lombok.Data;

@Data

public class YoloTaggingCreateReq {
    private String videoId;
    private Integer frame;
    private String leftUpperCornerAbscissa;
    private String leftUpperCornerOrdinate;
    private String rightLowerQuarterAbscissa;
    private String rightLowerQuarterOrdinate;
}
