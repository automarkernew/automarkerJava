package com.tagging.enums.VideoInformation;

import lombok.Getter;


@Getter
public enum TagStatusEnum {
    W("W", "未标注"),
    F("F", "已标注"),
    P("P", "部分标注");

    private String state;
    private String message;

    TagStatusEnum(String state, String message){
        this.state = state;
        this.message = message;
    }
}
