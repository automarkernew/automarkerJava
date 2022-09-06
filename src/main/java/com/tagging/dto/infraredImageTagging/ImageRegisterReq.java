package com.tagging.dto.infraredImageTagging;

import lombok.Data;

@Data
public class ImageRegisterReq {
    private String videoId;
    private String linkedVideoId;
    private String InfraredImageX;
    private String InfraredImageY;
    private String VisibleImageX;
    private String VisibleImageY;
}
