package com.tagging.controller;

import com.alibaba.fastjson.JSON;
import com.tagging.dto.infraredImageTagging.ImageRegisterReq;
import com.tagging.dto.infraredImageTagging.InfraredImageTaggingTagReq;
import com.tagging.dto.infraredImageTagging.InfraredImageTaggingTagRsp;
import com.tagging.service.InfraredImageTaggingService;
import com.tagging.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("InfraredImageTagging")
public class InfraredImageTaggingController {
    @Autowired
    InfraredImageTaggingService infraredImageTaggingService;

    @RequestMapping("tag")
    public R tag(@RequestBody Map<String,Object> req) throws IOException {
        InfraredImageTaggingTagReq infraredImageTagReq = JSON.parseObject(JSON.toJSONString(req.get("InfraredImageTaggingTagReq")),InfraredImageTaggingTagReq.class);
        InfraredImageTaggingTagRsp infraredImageTaggingTagRsp = infraredImageTaggingService.tag(infraredImageTagReq);
        return R.ok().data("InfraredImageTaggingTagRsp",infraredImageTaggingTagRsp);
    }

    @RequestMapping("imageRegister")
    public R imageRegister(@RequestBody Map<String,Object> req){
        ImageRegisterReq imageRegisterReq = JSON.parseObject(JSON.toJSONString(req.get("ImageRegisterReq")),ImageRegisterReq.class);
        infraredImageTaggingService.updateOffsetByVideoId(imageRegisterReq);
        return R.ok();
    }

    @RequestMapping("tagByVisible")
    private R tagByVisible(@RequestBody Map<String,Object> req) throws IOException {
        InfraredImageTaggingTagReq imageRegisterReq = JSON.parseObject(JSON.toJSONString(req.get("InfraredImageTaggingTagReq")), InfraredImageTaggingTagReq.class);
        infraredImageTaggingService.tagByVisibleId(imageRegisterReq);
        return R.ok();
    }

}
