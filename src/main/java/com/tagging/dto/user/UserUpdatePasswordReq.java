package com.tagging.dto.user;

import lombok.Data;

@Data
public class UserUpdatePasswordReq {
    private String userId;
    private String userPassword;
    private String newPassword;
}
