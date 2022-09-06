package com.tagging.controller;

import com.alibaba.fastjson.JSON;
import com.tagging.dto.visibleTagging.*;
import com.tagging.dto.yoloTagging.YoloTaggingFinishReq;
import com.tagging.dto.yoloTagging.YoloTaggingSaveReq;
import com.tagging.service.VisibleTaggingService;
import com.tagging.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("visibleTagging")
public class VisibleTaggingController {

    @Autowired
    VisibleTaggingService visibleTaggingService;

    @RequestMapping("updateTrackInformation")
    public R updateTrackInformation(@RequestBody Map<String,Object> req){
        VisibleTaggingUpdateTrackInformationReq visibleTaggingUpdateTrackInformationReq = JSON.parseObject(JSON.toJSONString(req.get("VisibleTaggingUpdateTrackInformationReq")),VisibleTaggingUpdateTrackInformationReq.class);
        visibleTaggingService.updateTrackInformation(visibleTaggingUpdateTrackInformationReq);
        visibleTaggingService.updateFrameInformation(visibleTaggingUpdateTrackInformationReq);
        return R.ok();
    }

    @RequestMapping("queryTrackInformation")
    public R queryTrackInformation(@RequestBody Map<String,Object> req){
        VisibleTaggingQueryTrackInformationReq visibleTaggingQueryTrackInformationReq =JSON.parseObject(JSON.toJSONString(req.get("VisibleTaggingQueryTrackInformationReq")),VisibleTaggingQueryTrackInformationReq.class);
        List<VisibleTaggingQueryTrackInformationRsp> visibleTaggingQueryTrackInformationRsps= visibleTaggingService.queryTrackInformation(visibleTaggingQueryTrackInformationReq);
        return R.ok().data("VisibleTaggingQueryTrackInformationRsp",visibleTaggingQueryTrackInformationRsps);
    }

    @RequestMapping("mot")
    public R mot(@RequestBody Map<String, Object> req) {
        VisibleTaggingMotReq visibleTaggingMotReq = JSON.parseObject(JSON.toJSONString(req.get("VisibleTaggingMotReq")), VisibleTaggingMotReq.class);
        VisibleTaggingMotRsp visibleTaggingMotRsp = visibleTaggingService.mot(visibleTaggingMotReq);
        return R.ok().data("VisibleTaggingMotRsp", visibleTaggingMotRsp);
    }

    @RequestMapping("motCreate")
    public R motCreate(@RequestBody Map<String, Object> req) throws IOException {
        VisibleTaggingMotCreateReq visibleTaggingMotCreateReq = JSON.parseObject(JSON.toJSONString(req.get("VisibleTaggingMotCreateReq")), VisibleTaggingMotCreateReq.class);
        VisibleTaggingMotCreateRsp visibleTaggingMotCreateRsp = visibleTaggingService.motCreate(visibleTaggingMotCreateReq);
        return R.ok().data("VisibleTaggingMotRsp", visibleTaggingMotCreateRsp);
    }

    @RequestMapping("motDelete")
    public R motDelete(@RequestBody Map<String, Object> req) throws IOException {
        VisibleTaggingMotDeleteReq visibleTaggingMotDeleteReq = JSON.parseObject(JSON.toJSONString(req.get("VisibleTaggingMotDeleteReq")), VisibleTaggingMotDeleteReq.class);
        VisibleTaggingMotRsp visibleTaggingMotRsp = visibleTaggingService.motDelete(visibleTaggingMotDeleteReq);
        return R.ok().data("VisibleTaggingMotRsp", visibleTaggingMotRsp);
    }

    @RequestMapping("motDeleteAll")
    public R motDeleteAll(@RequestBody Map<String, Object> req) throws IOException {
        VisibleTaggingMotDeleteAllReq visibleTaggingMotDeleteAllReq = JSON.parseObject(JSON.toJSONString(req.get("VisibleTaggingMotDeleteAllReq")), VisibleTaggingMotDeleteAllReq.class);
        VisibleTaggingMotRsp visibleTaggingMotRsp = visibleTaggingService.motDeleteAll(visibleTaggingMotDeleteAllReq);
        return R.ok().data("VisibleTaggingMotRsp", visibleTaggingMotRsp);
    }

    @RequestMapping("motUpdate")
    public R motUpdate(@RequestBody Map<String, Object> req) throws IOException {
        VisibleTaggingMotUpdateReq visibleTaggingMotUpdateReq = JSON.parseObject(JSON.toJSONString(req.get("VisibleTaggingMotUpdateReq")), VisibleTaggingMotUpdateReq.class);
        VisibleTaggingMotRsp visibleTaggingMotRsp = visibleTaggingService.motUpdate(visibleTaggingMotUpdateReq);
        return R.ok().data("VisibleTaggingMotRsp", visibleTaggingMotRsp);
    }

    @RequestMapping("queryAllTrackId")
    public R queryAllTrackId(@RequestBody Map<String, Object> req){
        VisibleTaggingMotQueryAllTrackIdReq visibleTaggingMotQueryAllTrackIdReq = JSON.parseObject(JSON.toJSONString(req.get("VisibleTaggingMotQueryAllTrackIdReq")), VisibleTaggingMotQueryAllTrackIdReq.class);
        List<VisibleTaggingMotQueryAllTrackIdRsp> visibleTaggingMotQueryAllTrackIdRspList = visibleTaggingService.queryAllTrackId(visibleTaggingMotQueryAllTrackIdReq);
        return R.ok().data("VisibleTaggingMotQueryAllTrackIdRsp",visibleTaggingMotQueryAllTrackIdRspList);
    }

    @RequestMapping("queryFrameNumber")
    public R queryFrameNumber(@RequestBody Map<String, Object> req){
        VisibleTaggingQueryFrameNumberReq visibleTaggingQueryFrameNumberReq = JSON.parseObject(JSON.toJSONString(req.get("VisibleTaggingQueryFrameNumberReq")), VisibleTaggingQueryFrameNumberReq.class);
        VisibleTaggingQueryFrameNumberRsp visibleTaggingQueryFrameNumberRsp = visibleTaggingService.queryFrameNumber(visibleTaggingQueryFrameNumberReq);
        return R.ok().data("VisibleTaggingQueryFrameNumberRsp",visibleTaggingQueryFrameNumberRsp);
    }

    @RequestMapping("updateTrackId")
    public R updateTrackId(@RequestBody Map<String, Object> req) throws IOException {
        VisibleTaggingUpdateTrackIdReq visibleTaggingUpdateTrackIdReq = JSON.parseObject(JSON.toJSONString(req.get("VisibleTaggingUpdateTrackIdReq")), VisibleTaggingUpdateTrackIdReq.class);
        VisibleTaggingMotRsp visibleTaggingMotRsp = visibleTaggingService.updateTrackId(visibleTaggingUpdateTrackIdReq);
        return R.ok().data("VisibleTaggingMotRsp",visibleTaggingMotRsp);
    }


    @RequestMapping("loadFile")
    public R loadFile(@RequestBody Map<String, Object> req) throws IOException {
        VisibleTaggingLoadFileReq visibleTaggingLoadFileReq = JSON.parseObject(JSON.toJSONString(req.get("VisibleTaggingLoadFileReq")), VisibleTaggingLoadFileReq.class);
        List<VisibleTaggingLoadFileRsp> visibleTaggingLoadFileRsps = visibleTaggingService.loadFile(visibleTaggingLoadFileReq);
        return R.ok().data("VisibleTaggingLoadFileRsp", visibleTaggingLoadFileRsps);
    }

}
