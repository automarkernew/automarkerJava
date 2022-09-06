package com.tagging.enums.VideoInformation;

import lombok.Getter;


@Getter
public enum IsTaggedEnum {
    UNEXECUTED("0", "未进行"),
    EXECUTED("1", "已进行");

    private String state;
    private String message;

    IsTaggedEnum(String state, String message){
        this.state = state;
        this.message = message;
    }
}
