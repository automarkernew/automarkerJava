package com.tagging.dto.targetTrackT;

import lombok.Data;

import java.util.List;

@Data
public class TargetTrackGetListRsp {

    private String videoId;
    private String videoName;
    private String sensorType;
    private String shootTime;
    private List<String> objectTypeList;
    private List<target> targetList;

    public static class target{
        public String objectType;
        public String trackId;
    }

}

