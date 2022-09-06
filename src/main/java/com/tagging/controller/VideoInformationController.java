package com.tagging.controller;

import com.alibaba.fastjson.JSON;
import com.tagging.dto.QueryPagingParamsReq;
import com.tagging.dto.QuerySummaryRsp;
import com.tagging.dto.videoInformation.*;
import com.tagging.service.VideoInformationService;
import com.tagging.utils.R;
//import jdk.vm.ci.code.site.Mark;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("videoInformation")
public class VideoInformationController {
    @Autowired
    VideoInformationService videoInformationService;

    @Autowired
    MinioController minioController;

    @RequestMapping("queryById")
    public R queryById(@RequestBody Map<String,Object> req){
        VideoInformationQueryByIdReq videoInformationQueryByIdReq = JSON.parseObject(JSON.toJSONString(req.get("VideoInformationQueryByIdReq")),VideoInformationQueryByIdReq.class);
        VideoInformationQueryByIdRsp videoInformationQueryByIdRsp = videoInformationService.queryByVideoId(videoInformationQueryByIdReq);
        return R.ok().data("VideoInformationQueryByIdRsp",videoInformationQueryByIdRsp);
    }

    @RequestMapping("query")
    public R query(@RequestBody Map<String, Object> req){
        VideoInformationQueryReq videoInformationQueryReq = JSON.parseObject(JSON.toJSONString(req.get("VideoInformationQueryReq")), VideoInformationQueryReq.class);
        QueryPagingParamsReq queryPagingParamsReq = JSON.parseObject(JSON.toJSONString(req.get("QueryPagingParamsReq")), QueryPagingParamsReq.class);
        List<VideoInformationQueryRsp> videoInformationQueryRsps = videoInformationService.query(queryPagingParamsReq.getOffset(), queryPagingParamsReq.getQueryCount(), videoInformationQueryReq);
        QuerySummaryRsp querySummaryRsp = videoInformationService.count(videoInformationQueryReq);
        return R.ok().data("VideoInformationQueryRsp", videoInformationQueryRsps).data("QuerySummaryRsp", querySummaryRsp);
    }

    @RequestMapping ("markquery")
    public R markquery(@RequestBody Map<String, Object> req){
        MarkInformationQueryReq markInformationQueryReq = JSON.parseObject(JSON.toJSONString(req.get("MarkInformationQueryReq")), MarkInformationQueryReq.class);
        QueryPagingParamsReq queryPagingParamsReq = JSON.parseObject(JSON.toJSONString(req.get("QueryPagingParamsReq")), QueryPagingParamsReq.class);
        List<VideoInformationQueryRsp> videoInformationQueryRsps = videoInformationService.markquery(queryPagingParamsReq.getOffset(), queryPagingParamsReq.getQueryCount(), markInformationQueryReq);
        QuerySummaryRsp querySummaryRsp = videoInformationService.markcount(markInformationQueryReq);
        return R.ok().data("VideoInformationQueryRsp", videoInformationQueryRsps).data("QuerySummaryRsp", querySummaryRsp);
    }

    @RequestMapping("create")
    public R create(@RequestPart("file") List<MultipartFile> files, @RequestPart("VideoInformationCreateReq") VideoInformationCreateReq videoInformationCreateReq) {
        videoInformationService.create(files, videoInformationCreateReq);
        return R.ok();
    }

    @RequestMapping("serverVideoCreate")
    public R create(@RequestBody Map<String, Object> req) throws Exception {
        VideoFromServerCreateReq videoFromServerCreateReq = JSON.parseObject(JSON.toJSONString(req.get("VideoFromServerCreateReq")), VideoFromServerCreateReq.class);
        videoInformationService.createFromServer(videoFromServerCreateReq);
        return R.ok();
    }

    @RequestMapping("delete")
    public R delete(@RequestBody Map<String, Object> req) {
        VideoInformationDeleteReq videoInformationDeleteReq = JSON.parseObject(JSON.toJSONString(req.get("VideoInformationDeleteReq")), VideoInformationDeleteReq.class);
        videoInformationService.delete(videoInformationDeleteReq);
        return R.ok();
    }

    @RequestMapping("download")
    public R download(@RequestParam("VideoId") String videoId, HttpServletResponse httpResponse) throws IOException {
        String fileUrl = videoInformationService.download(videoId);
        return minioController.getDownloadUrl(fileUrl);
    }

    @RequestMapping("queryShootPlace")
    public R queryShootPlace() {
        List<VideoInformationQueryShootPlaceRsp> videoInformationQueryShootPlaceRsps = videoInformationService.queryShootPlace();
        return R.ok().data("VideoInformationQueryShootPlaceRsp", videoInformationQueryShootPlaceRsps);
    }

    @RequestMapping("queryVideoLength")
    public R queryVideoLength(@RequestBody Map<String, Object> req){
        VideoLengthReq videoLengthReq = JSON.parseObject(JSON.toJSONString(req.get("VideoLengthReq")), VideoLengthReq.class);
        return R.ok().data("VideoLengthRsp", videoInformationService.queryVideoLength(videoLengthReq));
    }

    @RequestMapping("getDataListFromServer")
    public R getDataListFromServer(@RequestBody Map<String, Object> req){
        GetDataListFromServerReq getDataListFromServerReq = JSON.parseObject(JSON.toJSONString(req.get("GetDataListFromServerReq")), GetDataListFromServerReq.class);
        return R.ok().data("GetDataListFromServerRsp", videoInformationService.getDataListFromServer(getDataListFromServerReq));
    }
}
