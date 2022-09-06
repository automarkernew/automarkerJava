package com.tagging.dto.yoloTagging;
import lombok.Data;

@Data

public class YoloTaggingGetThumbnailRsp {
    private String imageUrl;
    private String height;
    private String width;
    private Integer frame;
    private String videoId;
    private String trackId;
}
