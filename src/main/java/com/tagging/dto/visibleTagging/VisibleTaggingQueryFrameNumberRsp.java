package com.tagging.dto.visibleTagging;
import lombok.Data;

import java.util.List;

@Data

public class VisibleTaggingQueryFrameNumberRsp {
    private String frameCount;
    private List<VisibleTaggingObjectCountRsp> visibleTaggingObjectCountRsps;
}
