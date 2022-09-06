package com.tagging.controller;

import afu.org.checkerframework.checker.oigj.qual.O;
import com.alibaba.fastjson.JSON;
import com.tagging.dto.QueryPagingParamsReq;
import com.tagging.dto.QuerySummaryRsp;
import com.tagging.dto.targetTrackT.*;
import com.tagging.service.TargetTrackService;
import com.tagging.utils.R;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("TargetTrack")
public class TargetTrackController {
    @Resource
    TargetTrackService targetTrackService;

    //轨迹库查询
    @RequestMapping("getTrack")
    public R targetTrackGetTrack(@RequestBody Map<String,Object> req){
        TargetTrackGetTrackReq targetTrackGetTrackReq = JSON.parseObject(JSON.toJSONString(req.get("TargetTrackGetTrackReq")),TargetTrackGetTrackReq.class);
        TargetTrackGetTrackRsp targetTrackGetTrackRsp = targetTrackService.targetTrackGetTrack(targetTrackGetTrackReq);
        return R.ok().data("TargetTrackGetTrackRsp",targetTrackGetTrackRsp);
    }

    //目标库的查询
    @RequestMapping("getList")
    public R targetTrackGetList(@RequestBody Map<String,Object> req){
        TargetTrackGetListReq targetTrackGetListReq = JSON.parseObject(JSON.toJSONString(req.get("TargetTrackGetListReq")),TargetTrackGetListReq.class);
        QueryPagingParamsReq queryPagingParamsReq = JSON.parseObject(JSON.toJSONString(req.get("QueryPagingParamsReq")), QueryPagingParamsReq.class);
        QuerySummaryRsp querySummaryRsp = targetTrackService.count(targetTrackGetListReq);
        List<TargetTrackGetListRsp> targetTrackGetListRsps = targetTrackService.targetTrackGetList(queryPagingParamsReq.getOffset(),queryPagingParamsReq.getQueryCount(),targetTrackGetListReq);
        for(int i=0;i<targetTrackGetListRsps.size();i++){
            List<String> ObjectList = targetTrackService.getObjectType(targetTrackGetListRsps.get(i).getVideoId());
            List<TargetTrackGetListRsp.target> TargetList =targetTrackService.getTarget(targetTrackGetListRsps.get(i).getVideoId());
            targetTrackGetListRsps.get(i).setObjectTypeList(ObjectList);
            targetTrackGetListRsps.get(i).setTargetList(TargetList);
        }
        return R.ok().data("TargetTrackGetListRsp",targetTrackGetListRsps).data("QuerySummaryRsp", querySummaryRsp);
    }

    @RequestMapping("updateTrack")
    public R updateTrack(@RequestBody Map<String, Object> req){
        TargetTrackUpdateReq targetTrackUpdateReq = JSON.parseObject(JSON.toJSONString(req.get("TargetTrackUpdateReq")), TargetTrackUpdateReq.class);
        targetTrackService.updateTrack(targetTrackUpdateReq);
        return R.ok();
    }

    @RequestMapping("queryType")
    public R queryTypeByVideoId(@RequestBody Map<String, Object> req){
        QueryTypeByVideoIdReq queryTypeByVideoIdReq = JSON.parseObject(JSON.toJSONString(req.get("QueryTypeByVideoIdReq")),  QueryTypeByVideoIdReq.class);
        return R.ok().data("QueryTypeByVideoIdRsp", targetTrackService.queryTypeByVideoId(queryTypeByVideoIdReq));
    }

    @RequestMapping("queryTrack")
    public R queryTrackByType(@RequestBody Map<String, Object> req){
        QueryTrackReq queryTrackReq = JSON.parseObject(JSON.toJSONString(req.get("QueryTrackReq")),  QueryTrackReq.class);
        return R.ok().data("QueryTrackRsp", targetTrackService.queryTrack(queryTrackReq));
    }
}
