package com.tagging.enums;

import lombok.Getter;

@Getter
public enum DataStatusEnum {
    AVAILABLE("A", "正常"),
    EXCEPTION("E", "异常"),
    DELETED("D", "删除");

    private String state;
    private String message;

    DataStatusEnum(String state, String message) {
        this.state = state;
        this.message = message;
    }
}
