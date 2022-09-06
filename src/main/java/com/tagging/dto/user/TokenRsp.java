package com.tagging.dto.user;

import lombok.Data;

import java.util.Date;

@Data
public class TokenRsp {
    private String token;
    private Date refresh;
    private Date expires;
}
