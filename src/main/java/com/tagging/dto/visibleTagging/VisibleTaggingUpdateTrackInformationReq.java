package com.tagging.dto.visibleTagging;

import lombok.Data;

@Data
public class VisibleTaggingUpdateTrackInformationReq {
    private String leftUpperCornerAbscissa;
    private String leftUpperCornerOrdinate;
    private String rightLowerQuarterAbscissa;
    private String rightLowerQuarterOrdinate;
    private String objectType;
    private String objectModel;
    private String trackTypeId;
    private String videoId;
    private String trackId;
    private String frameInformationId;

}
