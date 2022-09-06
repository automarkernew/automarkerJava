package com.tagging.controller;

import com.alibaba.fastjson.JSON;
import com.tagging.dto.user.RegisterReq;
import com.tagging.dto.user.UserLoginReq;
import com.tagging.dto.user.UserLoginRsp;
import com.tagging.service.LoginService;
import com.tagging.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("login")
public class LoginController {
    @Autowired
    LoginService loginService;

    @RequestMapping("login")
    public R login(@RequestBody Map<String, Object> req) throws Exception {
        UserLoginReq userLoginReq = JSON.parseObject(JSON.toJSONString(req.get("UserLoginReq")), UserLoginReq.class);
        UserLoginRsp userLoginRsp = loginService.findUser(userLoginReq);
        return R.ok().data("UserLoginRsp", userLoginRsp);

    }

    @RequestMapping("register")
    public R register(@RequestBody Map<String, Object> req) throws Exception {
        RegisterReq registerReq = JSON.parseObject(JSON.toJSONString(req.get("RegisterReq")), RegisterReq.class);
        loginService.register(registerReq);
        return R.ok();

    }
}
