package com.tagging.dto.visibleTagging;
import lombok.Data;

@Data

public class VisibleTaggingUploadRsp {
    private String videoId;
    private String videoFileUrl;
    private String imageUrl;
    private String height;
    private String width;
}
