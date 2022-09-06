package com.tagging.dto.targetTrackT;

import lombok.Data;
import org.python.antlr.ast.List;

@Data
public class QueryTrackRsp {
    private String trackId;
    private String objectModel;
    private String frameList;
}
