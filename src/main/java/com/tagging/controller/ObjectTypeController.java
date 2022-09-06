package com.tagging.controller;

import com.alibaba.fastjson.JSON;
import com.tagging.dto.ObjectType.LinkIdReq;
import com.tagging.dto.ObjectType.ObjectTypeModelReq;
import com.tagging.dto.ObjectType.ObjectTypeReq;
import com.tagging.dto.targetTrackT.TargetTrackUpdateReq;
import com.tagging.service.ObjectTypeService;
import com.tagging.utils.R;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("ObjectType")
public class ObjectTypeController {

    @Resource
    ObjectTypeService objectTypeService;

    @RequestMapping("creat")
    public R tag(@RequestBody Map<String, Object> req){
        ObjectTypeReq objectTypeReq = JSON.parseObject(JSON.toJSONString(req.get("ObjectTypeReq")), ObjectTypeReq.class);
        return R.ok().data("ObjectTypRsp", objectTypeService.creat(objectTypeReq));
    }

    @RequestMapping("queryType")
    public R queryType() {
        return R.ok().data("QueryAllTypeRsp", objectTypeService.queryAllType());
    }

    @RequestMapping("queryModel")
    public R queryModel(@RequestBody Map<String, Object> req){
        ObjectTypeModelReq objectTypeReq = JSON.parseObject(JSON.toJSONString(req.get("ObjectTypeModelReq")), ObjectTypeModelReq.class);
        return R.ok().data("ObjectTypeModelRsp", objectTypeService.queryModelByType(objectTypeReq));
    }

    @RequestMapping("queryByLinkId")
    public R queryByLinkId(@RequestBody Map<String, Object> req){
        LinkIdReq linkIdReq = JSON.parseObject(JSON.toJSONString(req.get("LinkIdReq")), LinkIdReq.class);
        return R.ok().data("LinkIdRsp", objectTypeService.queryByLinkId(linkIdReq));
    }

    @RequestMapping("queryAllLinkId")
    public R queryAllLinkId(){
        return R.ok().data("QueryLinkIdRsp", objectTypeService.queryAllLinkId());
    }
}
