package com.tagging.dto.user;

import lombok.Data;

@Data
public class UserLoginReq {
    private String userName;
    private String userPassword;
}
