package com.tagging.dto.user;

import lombok.Data;

@Data
public class UserUpdateReq {
    private String userId;
    private String userName;
    private String userPhone;
    private String userEmail;
}
