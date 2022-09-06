package com.tagging.dto.visibleTagging;
import lombok.Data;

@Data

public class VisibleTaggingMotUpdateReq {
    private String frameInformationId;
    private String trackId;
    private String videoId;
    private Integer frame;
    private String leftUpperCornerAbscissa;
    private String leftUpperCornerOrdinate;
    private String rightLowerQuarterAbscissa;
    private String rightLowerQuarterOrdinate;
}
