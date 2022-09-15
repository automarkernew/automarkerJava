package com.tagging.dto.infraredImageTagging;

import lombok.Data;

import java.util.List;

@Data
public class ImageRegisterReq {
    private String videoId;
    private String linkedVideoId;
    private List<Coordinate> InfraredImagePoints;
    private List<Coordinate> VisibleImagePoints;
}
