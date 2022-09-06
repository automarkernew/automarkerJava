package com.tagging.dto.visibleTagging;
import lombok.Data;

@Data

public class VisibleTaggingLoadFileReq {
    private String objectType;
    private String objectModel;
    private String sensorType;
    private String shootTime;
    private String shootPlace;
}
