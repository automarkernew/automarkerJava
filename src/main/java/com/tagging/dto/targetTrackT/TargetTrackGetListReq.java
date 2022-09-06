package com.tagging.dto.targetTrackT;

import lombok.Data;
@Data
public class TargetTrackGetListReq {
    private String videoName;
    private String sensorType;
    private String objectType;
    private String shootTimeBegin;
    private String shootTimeEnd;
    private String shootPlace;

}
