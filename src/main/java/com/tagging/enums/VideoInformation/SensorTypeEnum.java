package com.tagging.enums.VideoInformation;

import lombok.Getter;


@Getter
public enum SensorTypeEnum {
    VISIBLE("VIS", "可见光"),
    INFRARED("IR", "红外光"),
    SAR("SAR","SAR图像"),
    HYPERSPECTRAL_IMAGE("HSI","高光谱"),
    HIGH_RESOLUTION_VISIBLE("HRV","高分辨率可见光");

    private String state;
    private String message;

    SensorTypeEnum(String state, String message){
        this.state = state;
        this.message = message;
    }

}
