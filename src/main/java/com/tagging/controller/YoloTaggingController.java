package com.tagging.controller;

import com.alibaba.fastjson.JSON;
import com.tagging.dto.yoloTagging.*;
import com.tagging.service.YoloTaggingService;
import com.tagging.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("yoloTagging")
public class YoloTaggingController {

    @Autowired
    YoloTaggingService yoloTaggingService;

    @RequestMapping("tag")
    public R tag(@RequestBody Map<String, Object> req){
        YoloTaggingTagReq yoloTaggingTagReq = JSON.parseObject(JSON.toJSONString(req.get("YoloTaggingTagReq")), YoloTaggingTagReq.class);
        yoloTaggingService.tagging(yoloTaggingTagReq);
        return R.ok();
    }

    @RequestMapping("getCoordinate")
    public R getCoordinate(@RequestBody Map<String, Object> req){
        YoloTaggingGetCoordinateReq yoloTaggingGetCoordinateReq = JSON.parseObject(JSON.toJSONString(req.get("YoloTaggingGetCoordinateReq")), YoloTaggingGetCoordinateReq.class);
        List<YoloTaggingGetCoordinateRsp> yoloTaggingGetCoordinateRsps = yoloTaggingService.getCoordinateRsps(yoloTaggingGetCoordinateReq);
        return R.ok().data("YoloTaggingGetCoordinateRsp", yoloTaggingGetCoordinateRsps);
    }

    @RequestMapping("create")
    public R create(@RequestBody Map<String, Object> req) {
        YoloTaggingCreateReq yoloTaggingCreateReq = JSON.parseObject(JSON.toJSONString(req.get("YoloTaggingCreateReq")), YoloTaggingCreateReq.class);
        YoloTaggingCreateRsp yoloTaggingCreateRsp = yoloTaggingService.create(yoloTaggingCreateReq);
        return R.ok().data("YoloTaggingCreateRsp", yoloTaggingCreateRsp);
    }

    @RequestMapping("delete")
    public R delete(@RequestBody Map<String, Object> req) {
        YoloTaggingDeleteReq yoloTaggingDeleteReq = JSON.parseObject(JSON.toJSONString(req.get("YoloTaggingDeleteReq")), YoloTaggingDeleteReq.class);
        yoloTaggingService.delete(yoloTaggingDeleteReq);
        return R.ok();
    }

    @RequestMapping("update")
    public R update(@RequestBody Map<String, Object> req) {
        YoloTaggingUpdateReq yoloTaggingUpdateReq = JSON.parseObject(JSON.toJSONString(req.get("YoloTaggingUpdateReq")), YoloTaggingUpdateReq.class);
        yoloTaggingService.update(yoloTaggingUpdateReq);
        return R.ok();
    }

    @RequestMapping("save")
    public R save(@RequestBody Map<String, Object> req) throws IOException {
        YoloTaggingSaveReq yoloTaggingSaveReq = JSON.parseObject(JSON.toJSONString(req.get("YoloTaggingSaveReq")), YoloTaggingSaveReq.class);
        yoloTaggingService.save(yoloTaggingSaveReq);
        return R.ok();
    }

    @RequestMapping("finish")
    public R finish(@RequestBody Map<String, Object> req) throws IOException {
        YoloTaggingFinishReq yoloTaggingFinishReq = JSON.parseObject(JSON.toJSONString(req.get("YoloTaggingFinishReq")), YoloTaggingFinishReq.class);
        yoloTaggingService.finish(yoloTaggingFinishReq);
        return R.ok();
    }

    @RequestMapping("getThumbnail")
    public R getThumbnail(@RequestBody Map<String, Object> req) throws IOException {
        YoloTaggingGetThumbnailReq yoloTaggingGetThumbnailReq = JSON.parseObject(JSON.toJSONString(req.get("YoloTaggingGetThumbnailReq")), YoloTaggingGetThumbnailReq.class);
        List<YoloTaggingGetThumbnailRsp> yoloTaggingGetThumbnailRsps =  yoloTaggingService.getThumbnail(yoloTaggingGetThumbnailReq);
        return R.ok().data("YoloTaggingGetThumbnailRsp", yoloTaggingGetThumbnailRsps);
    }
}
