package com.tagging.dto.user;

import lombok.Data;

@Data
public class UserQueryReq {
    private String userName;
    private String userPhone;
    private String userEmail;
}
