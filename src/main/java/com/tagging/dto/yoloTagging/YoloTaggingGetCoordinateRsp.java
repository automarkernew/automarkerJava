package com.tagging.dto.yoloTagging;
import lombok.Data;

@Data

public class YoloTaggingGetCoordinateRsp {
    private String frameInformationId;
    private String videoId;
    private Integer frame;
    private String frameType;
    private String trackId;
    private String leftUpperCornerAbscissa;
    private String leftUpperCornerOrdinate;
    private String rightLowerQuarterAbscissa;
    private String rightLowerQuarterOrdinate;
}
