package com.tagging.dto.visibleTagging;

import lombok.Data;

@Data
public class VisibleTaggingQueryTrackInformationRsp {
    private String leftUpperCornerAbscissa;
    private String leftUpperCornerOrdinate;
    private String rightLowerQuarterAbscissa;
    private String rightLowerQuarterOrdinate;
    private String objectType;
    private String objectModel;
    private String trackTypeId;
    private String linkId;
}
