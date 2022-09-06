package com.tagging.dto.videoInformation;

import lombok.Data;

@Data
public class GetDataListFromServerReq {
    private String filePathNow;
    private String type;
}
