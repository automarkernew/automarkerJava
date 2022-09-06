package com.tagging.dto.user;

import lombok.Data;

@Data
public class RegisterReq {
    private String userName;
    private String userPassword;
    private String userPhone;
    private String userEmail;
}
