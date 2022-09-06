package com.tagging.dto.visibleTagging;

import lombok.Data;

@Data
public class VisibleTaggingMotCreateReq {
    private String videoId;
    private Integer startFrame;
    private Integer endFrame;
    private Integer frame;
    private String leftUpperCornerAbscissa;
    private String leftUpperCornerOrdinate;
    private String rightLowerQuarterAbscissa;
    private String rightLowerQuarterOrdinate;
}
