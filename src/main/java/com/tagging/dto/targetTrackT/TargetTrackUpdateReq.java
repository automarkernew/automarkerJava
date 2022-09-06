package com.tagging.dto.targetTrackT;

import lombok.Data;

@Data
public class TargetTrackUpdateReq {
    private String videoId;
    private String trackId;
    private String objectType;
    private String objectModel;
}
