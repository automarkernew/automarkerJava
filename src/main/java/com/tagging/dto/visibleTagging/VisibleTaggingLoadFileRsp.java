package com.tagging.dto.visibleTagging;
import lombok.Data;

@Data

public class VisibleTaggingLoadFileRsp {
    private String videoId;
    private String imageUrl;
    private Integer frame;
    private String height;
    private String width;
    private String trackId;
}
