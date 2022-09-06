package com.tagging.dto.visibleTagging;
import lombok.Data;

@Data

public class VisibleTaggingUpdateTrackIdReq {
    private String frameInformationId;
    private String videoId;
    private String trackId;
}
