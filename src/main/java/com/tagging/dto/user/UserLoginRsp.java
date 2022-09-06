package com.tagging.dto.user;

import lombok.Data;

@Data
public class UserLoginRsp {
    private TokenRsp token;
    private TokenRsp refreshToken;

}
