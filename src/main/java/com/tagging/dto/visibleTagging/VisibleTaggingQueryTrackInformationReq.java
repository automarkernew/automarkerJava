package com.tagging.dto.visibleTagging;

import lombok.Data;

@Data
public class VisibleTaggingQueryTrackInformationReq {

    private String videoId;
    private String trackId;
    private String frameInformationId;

}
