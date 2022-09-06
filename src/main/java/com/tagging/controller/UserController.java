package com.tagging.controller;

import com.alibaba.fastjson.JSON;
import com.tagging.dto.QueryPagingParamsReq;
import com.tagging.dto.QuerySummaryRsp;
import com.tagging.dto.targetTrackT.TargetTrackGetTrackReq;
import com.tagging.dto.targetTrackT.TargetTrackGetTrackRsp;
import com.tagging.dto.user.*;
import com.tagging.service.UserService;
import com.tagging.utils.R;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("user")
public class UserController {
    @Resource
    UserService userService;

    @RequestMapping("insert")
    public R userInsert(@RequestBody Map<String,Object> req){
        UserInsertReq userInsertReq = JSON.parseObject(JSON.toJSONString(req.get("UserInsertReq")),UserInsertReq.class);
        userService.insert(userInsertReq);
        return R.ok();
    }

    @RequestMapping("query")
    public R userQuery(@RequestBody Map<String,Object> req){
        UserQueryReq userQueryReq = JSON.parseObject(JSON.toJSONString(req.get("UserQueryReq")),UserQueryReq.class);
        QueryPagingParamsReq queryPagingParamsReq = JSON.parseObject(JSON.toJSONString(req.get("QueryPagingParamsReq")), QueryPagingParamsReq.class);
        List<UserQueryRsp> userQueryRsps = userService.query(queryPagingParamsReq.getOffset() ,queryPagingParamsReq.getQueryCount(),userQueryReq);
        QuerySummaryRsp querySummaryRsp = userService.count(userQueryReq);
        return R.ok().data("UserQueryRsp",userQueryRsps).data("QuerySummaryRsp",querySummaryRsp);
    }

    @RequestMapping("delete")
    public R userDelete(@RequestBody Map<String,Object> req){
        UserDeleteReq userDeleteReq = JSON.parseObject(JSON.toJSONString(req.get("UserDeleteReq")),UserDeleteReq.class);
        userService.delete(userDeleteReq);
        return R.ok();
    }

    @RequestMapping("update")
    public R userUpdate(@RequestBody Map<String,Object> req){
        UserUpdateReq userUpdateReq = JSON.parseObject(JSON.toJSONString(req.get("UserUpdateReq")),UserUpdateReq.class);
        userService.update(userUpdateReq);
        return R.ok();
    }

    @RequestMapping("updatePassword")
    public R userUpdatePassword(@RequestBody Map<String,Object> req){
        UserUpdatePasswordReq userUpdatePasswordReq= JSON.parseObject(JSON.toJSONString(req.get("UserUpdatePasswordReq")),UserUpdatePasswordReq.class);
        userService.updatePassword(userUpdatePasswordReq);
        return R.ok();
    }

}
