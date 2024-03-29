package com.tagging.controller;

import com.alibaba.fastjson.JSON;
import com.tagging.dto.QueryPagingParamsReq;
import com.tagging.dto.QuerySummaryRsp;
import com.tagging.dto.videoInformation.*;
import com.tagging.service.InfraredImageTaggingService;
import com.tagging.service.VideoInformationService;
import com.tagging.utils.R;
import com.tagging.utils.ZipUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
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

    private static final Logger logger = LoggerFactory.getLogger(VideoInformationController.class);

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
    public void download(@RequestBody Map<String, Object> req,
                      HttpServletResponse response) throws IOException {
        VideoInformationDownload videoInformationDownload = JSON.parseObject(JSON.toJSONString(req.get("VideoInformationDownload")), VideoInformationDownload.class);
        String fileUrl = videoInformationService.download(videoInformationDownload.getVideoId());
        response.setContentType("application/zip");
        response.setCharacterEncoding("utf-8");
        //xxx.zip是你压缩包文件名
        response.setHeader("Content-Disposition", "attachment;filename=" + fileUrl);
        ZipUtils.downloadZip(response, fileUrl);
        logger.info(fileUrl + "生成成功");
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
