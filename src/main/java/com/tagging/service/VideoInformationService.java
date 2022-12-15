package com.tagging.service;

import com.github.wujun234.uid.UidGenerator;
import com.tagging.common.Constants;
import com.tagging.controller.MinioController;
import com.tagging.dao.mapper.FrameInformationDao;
import com.tagging.dao.mapper.ObjectTypeDao;
import com.tagging.dao.mapper.TargetTrackDao;
import com.tagging.dao.mapper.VideoInformationDao;
import com.tagging.dto.QuerySummaryRsp;
import com.tagging.dto.videoInformation.*;
import com.tagging.entity.FrameInformation;
import com.tagging.entity.TargetTrackT;
import com.tagging.entity.TargetTypeT;
import com.tagging.entity.VideoInformation;
import com.tagging.enums.DataStatusEnum;
import com.tagging.enums.VideoInformation.IsMotedEnum;
import com.tagging.enums.VideoInformation.IsTaggedEnum;
import com.tagging.enums.VideoInformation.SensorTypeEnum;
import com.tagging.enums.VideoInformation.TagStatusEnum;
import com.tagging.exception.CMSException;
import com.tagging.utils.DataUtils;
import com.tagging.utils.MinioUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.tagging.utils.FileUtils.consumeInputStream;
import static com.tagging.utils.FileUtils.copyDir;

@Service
@Slf4j
public class VideoInformationService {
    @Resource
    VideoInformationDao videoInformationDao;

    @Resource
    FrameInformationDao frameInformationDao;

    @Resource
    TargetTrackDao targetTrackDao;

    @Resource
    ObjectTypeDao objectTypeDao;

    @Resource
    UidGenerator cachedUidGenerator;

    @Resource
    MinioUtils minioUtils;

    @Resource
    MinioController minioController;

    @Value("${minio.videoBucket}")
    private String minioBucketName;

    @Value("${minio.xmlPath}")
    private String xml;

    @Value("${minio.serverFilePath}")
    private String serverFilePath;

    @Value("${minio.localUrl}")
    private String minioLocalUrl;

    @Value("${minio.motimgBuket}")
    private String motimgBuket;

    @Value("${minio.videoBucket}")
    private String videoBucket;

    @Value("${minio.imgBuket}")
    private String imgBuket;

    @Value("${yolo5.localUrl}")
    private String yoloUrl;

    @Value("${yolo5.executeUrl}")
    private String executeUrl;

    @Value("${yolo5.pythonName}")
    private String pythonName;
    
    public VideoInformationQueryByIdRsp queryByVideoId(VideoInformationQueryByIdReq req){
        VideoInformation videoInformation = videoInformationDao.queryByVideoId(req.getVideoId());
        VideoInformationQueryByIdRsp videoInformationQueryByIdRsp = new VideoInformationQueryByIdRsp();
        videoInformationQueryByIdRsp.setVideoId(videoInformation.getVideoId());
        videoInformationQueryByIdRsp.setVideoName(videoInformation.getVideoName());
        videoInformationQueryByIdRsp.setVideoFileUrl(videoInformation.getVideoFileUrl());
        videoInformationQueryByIdRsp.setShootTime(videoInformation.getShootTime());
        videoInformationQueryByIdRsp.setShootPlace(videoInformation.getShootPlace());
        videoInformationQueryByIdRsp.setLength(videoInformation.getLength());
        videoInformationQueryByIdRsp.setSensorType(videoInformation.getSensorType());
        videoInformationQueryByIdRsp.setHeight(videoInformation.getHeight());
        videoInformationQueryByIdRsp.setWidth(videoInformation.getWidth());
        videoInformationQueryByIdRsp.setTagUserId(videoInformation.getTagUserId());
        videoInformationQueryByIdRsp.setMotUserId(videoInformation.getMotUserId());
        videoInformationQueryByIdRsp.setTagImgUrl(videoInformation.getTagImgUrl());
        videoInformationQueryByIdRsp.setMotImgUrl(videoInformation.getMotImgUrl());
        videoInformationQueryByIdRsp.setTagTime(videoInformation.getTagTime());
        videoInformationQueryByIdRsp.setTagStatus(videoInformation.getTagStatus());
        videoInformationQueryByIdRsp.setIsTagged(videoInformation.getIsTagged());
        videoInformationQueryByIdRsp.setIsMoted(videoInformation.getIsMoted());
        return videoInformationQueryByIdRsp;
    }

    public List<VideoInformationQueryRsp> query(long offset, int queryCount, VideoInformationQueryReq req){
        return videoInformationDao.query(offset, queryCount,
                req.getVideoName(),
                req.getShootTimeBegin(),
                req.getShootTimeEnd(),
                req.getShootPlace(),
                req.getSensorType(),
                req.getTagStatus());

    }

    public List<VideoInformationQueryRsp> markquery(long offset, int queryCount, MarkInformationQueryReq req){
        return videoInformationDao.markquery(offset, queryCount,
                req.getTagStatus(),
                req.getShootTime(),
                req.getShootPlace(),
                req.getSensorType());
    }

    public QuerySummaryRsp count(VideoInformationQueryReq req){
        QuerySummaryRsp querySummaryRsp = new QuerySummaryRsp();
        long num = videoInformationDao.count(
                req.getVideoName(),
                req.getShootTimeBegin(),
                req.getShootTimeEnd(),
                req.getShootPlace(),
                req.getSensorType(),
                req.getTagStatus()
        );
        querySummaryRsp.setDataAmount(num);
        return querySummaryRsp;
    }

    public QuerySummaryRsp markcount(MarkInformationQueryReq req){
        QuerySummaryRsp querySummaryRsp = new QuerySummaryRsp();
        long num = videoInformationDao.markcount(
                req.getTagStatus(),
                req.getShootTime(),
                req.getShootPlace(),
                req.getSensorType()
        );
        querySummaryRsp.setDataAmount(num);
        return querySummaryRsp;
    }

    // 上传之前检测红外对应的可见光是否存在（如果是红外）
    // 设置上传要更新的 VideoInformation
    public VideoInformation setVideoInformation(VideoInformationCreateReq req){
        VideoInformation videoInformation = new VideoInformation();
        videoInformation.setVideoId(String.valueOf(cachedUidGenerator.getUID()));
        videoInformation.setShootTime(req.getShootTime());
        videoInformation.setShootPlace(req.getShootPlace());
        videoInformation.setSensorType(req.getSensorType());
        if(videoInformation.getSensorType().equals(SensorTypeEnum.INFRARED.getState())){
            VideoInformation visibleVideo = videoInformationDao.queryByVideoId(req.getTypeLinkId());
            if(visibleVideo == null){
                throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE,"要关联的可见光视频不存在");
            }
            videoInformation.setTypeLinkId(req.getTypeLinkId());
            visibleVideo.setTypeLinkId(videoInformation.getVideoId());
            visibleVideo.setUpdateTimestamp(DataUtils.getSysTimeByFormat("yyyyMMddHHmmss"));
            visibleVideo.setVersion(visibleVideo.getVersion() + 1);
            videoInformationDao.updateByPrimaryKeySelective(visibleVideo);
        }else {videoInformation.setTypeLinkId(" ");}

        videoInformation.setTagUserId("");
        videoInformation.setMotUserId("");
        videoInformation.setMotImgUrl("");
        videoInformation.setTagTime("");
        videoInformation.setTagStatus(TagStatusEnum.W.getState());
        videoInformation.setIsTagged(IsTaggedEnum.UNEXECUTED.getState());
        videoInformation.setIsMoted(IsMotedEnum.UNEXECUTED.getState());
        videoInformation.setOffsetX("");
        videoInformation.setOffsetY("");
        videoInformation.setSpecCode03("");
        videoInformation.setCreateTimestamp(DataUtils.getSysTimeByFormat());
        videoInformation.setUpdateTimestamp(DataUtils.getSysTimeByFormat());
        videoInformation.setDataStatus(DataStatusEnum.AVAILABLE.getState());
        videoInformation.setVersion(1);
        videoInformation.setRemarks("");
        videoInformation.setReserve("");
        return videoInformation;
    }

    // MultipartFile类型的拷贝
    public VideoInformation setMultipartFile(VideoInformation videoInformation, List<MultipartFile> files) throws IOException {
        for(MultipartFile file : files) {
//                    String fileUrl = minioUtils.putObject(minioBucketName, file, videoInformation.getVideoId() + "/" + file.getOriginalFilename(),
//                            file.getContentType());
            File dest = new File(String.valueOf(Paths.get(minioLocalUrl, minioBucketName, videoInformation.getVideoId(), file.getOriginalFilename())));
            if (!dest.exists()) {
                dest.mkdirs();
            }
            file.transferTo(dest);
            videoInformation.setVideoName(file.getOriginalFilename());
            videoInformation.setVideoFileUrl(String.valueOf(Paths.get(minioBucketName, videoInformation.getVideoId(), file.getOriginalFilename())));
        }
        return videoInformation;
    }

    // File类型直接拷贝
    public VideoInformation setFile(VideoInformation videoInformation, List<File> files) throws IOException {
        for (File file : files) {
            File destDir = new File(String.valueOf(Paths.get(minioLocalUrl, minioBucketName, videoInformation.getVideoId())));
            if (!(destDir.exists() && destDir.isDirectory())) {
                destDir.mkdirs();
            }
            File dest = new File(String.valueOf(Paths.get(minioLocalUrl, minioBucketName, videoInformation.getVideoId(), file.getName())));
            Files.copy(file.toPath(), dest.toPath());
            videoInformation.setVideoName(file.getName());
            videoInformation.setVideoFileUrl(String.valueOf(Paths.get(minioBucketName, videoInformation.getVideoId(), file.getName())));
        }
        return videoInformation;
    }

    // 上传后续操作
    public VideoInformation doCreate(VideoInformation videoInformation) throws InterruptedException, IOException {
        //若为高光谱,转换为伪彩色
        if(videoInformation.getSensorType().equals(SensorTypeEnum.HYPERSPECTRAL_IMAGE.getState())){
            String videoUrl = videoBucket + "/" + videoInformation.getVideoId();

            String command = "cd "
                    + executeUrl
                    + " && " + pythonName  +  " " + yoloUrl + "/his.py "
                    + minioLocalUrl + "/" + videoInformation.getVideoFileUrl().replace(".hdr","") + ".hdr" + " "
                    + minioLocalUrl + "/" + videoUrl + "/";

            log.info(command);

            videoInformation.setVideoFileUrl(videoUrl + "/1.jpg");

            //运行py文件
            Process p;
            p = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", command});
            String inStr = consumeInputStream(p.getInputStream());
            String errStr = consumeInputStream(p.getErrorStream());
            p.waitFor();

        }

        //非可见和红外在motimg也保存
        if(!videoInformation.getSensorType().equals(SensorTypeEnum.VISIBLE.getState())
                && !videoInformation.getSensorType().equals(SensorTypeEnum.INFRARED.getState())){
            String imgUrl = "/" + motimgBuket + "/" + videoInformation.getVideoId();
            String command = "cd "
                    + executeUrl
                    + " && " + pythonName + " " + yoloUrl + "/video2img.py "
                    + minioLocalUrl + "/" + videoInformation.getVideoFileUrl() + " "
                    + minioLocalUrl + imgUrl + '/';

            log.info(command);

            //运行py文件
            Process p;
            p = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", command});
            String inStr = consumeInputStream(p.getInputStream());
            String errStr = consumeInputStream(p.getErrorStream());
            p.waitFor();
        }

        //转为成图片帧所保存的的地址
        String imgUrl = "/" + imgBuket + "/" + videoInformation.getVideoId();
        videoInformation.setTagImgUrl(imgUrl);

        //调用保存图片帧数的算法
        String command = "cd "
                + executeUrl
                + " && " + pythonName + " " + yoloUrl + "/video2img.py "
                + minioLocalUrl + "/" + videoInformation.getVideoFileUrl() + " "
                + minioLocalUrl + imgUrl + '/';
        log.info(command);

        //运行py文件
        Process p = null;
        p = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", command});
        String inStr = consumeInputStream(p.getInputStream());
        String errStr = consumeInputStream(p.getErrorStream());
        p.waitFor();

        if(videoInformation.getSensorType().equals(SensorTypeEnum.HYPERSPECTRAL_IMAGE.getState())){
            File file1 = new File(minioLocalUrl + "/" + videoBucket + "/" + videoInformation.getVideoId() + '/');
            File file2 = new File (minioLocalUrl + imgUrl + '/');
            copyDir(file1, file2);
        }

        String encoding = "GBK";
        File sizeFile = new File(minioLocalUrl + imgUrl + '/' + "size.txt");
        if (sizeFile.isFile() && sizeFile.exists()) { //判断文件是否存在
            InputStreamReader read = new InputStreamReader(Files.newInputStream(sizeFile.toPath()), encoding);//考虑到编码格式
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                String[] message = lineTxt.split(" ");
                videoInformation.setHeight(message[1]);
                videoInformation.setWidth(message[2]);
                videoInformation.setLength(message[3]);
                System.out.println("======"+ Arrays.toString(message));
            }
            read.close();
        } else {
            System.out.println("找不到指定的文件");
        }
        return videoInformation;
    }


    //视频新增
    @Transactional
    public void create(List<MultipartFile> files, VideoInformationCreateReq req) {
        VideoInformation videoInformation = setVideoInformation(req);
        if (files != null) {
            try {
                videoInformation = setMultipartFile(videoInformation, files);
                videoInformation = doCreate(videoInformation);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        videoInformationDao.insert(videoInformation);
    }

    public void delete(VideoInformationDeleteReq req){
        VideoInformation videoInformation = videoInformationDao.queryByVideoId(req.getVideoId());
        videoInformation.setDataStatus(DataStatusEnum.DELETED.getState());
        videoInformation.setUpdateTimestamp(DataUtils.getSysTimeByFormat("yyyyMMddHHmmss"));
        videoInformation.setVersion(videoInformation.getVersion() + 1);

        // TODO 删除minio中的文件,联表删除
        videoInformationDao.updateByPrimaryKeySelective(videoInformation);
    }

//    获取图片标签
    public String getName(FrameInformation frameInformation){
        TargetTrackT targetTrackT = targetTrackDao.queryByVideoIdAndTrackId(frameInformation.getVideoId(),
                frameInformation.getTrackId());
        TargetTypeT targetTypeT = objectTypeDao.queryByTrackTypeIdAndTargetTypeId(targetTrackT.getTrackTypeId(),
                targetTrackT.getTargetTypeId());
        if (targetTypeT == null){
            return "undefine";
        } else {
            return targetTypeT.getObjectType() + "_" + targetTypeT.getObjectModel();
        }
    }

//    初始化文件夹
    public void dirInit(File dir){
        if (!dir.isDirectory()) {
            try {
                dir.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

//    清空文件夹
    public void dirDelete(File dir){
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    file.delete();
                } else if (file.isDirectory()) {
                    dirDelete(file);
                    file.delete();
                }
            }
        }
    }

//    生成图片xml
    public void getXmlFile(List<FrameInformation> frameInformations,
                           String xmlRootPath,
                           Integer frame) {
        try {
            File xmlFile = new File(Paths.get(xmlRootPath , frame + ".xml").toUri());
            // 读图
            Path imagePath = Paths.get(minioLocalUrl, motimgBuket, frameInformations.get(0).getVideoId(), String.valueOf(frame) + ".jpg");
            BufferedImage sourceImg = ImageIO.read(Files.newInputStream(imagePath));
            // 创建解析器工厂
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = factory.newDocumentBuilder();
            Document document = db.newDocument();
            document.setXmlStandalone(true);
            Element annotation = document.createElement("annotation");
//            filename
            Element filename = document.createElement("filename");
            filename.setTextContent(String.valueOf(frameInformations.get(0).getFrame()) + ".jpg");
//            size
            Element size = document.createElement("size");
//            width
            Element width = document.createElement("width");
            width.setTextContent(String.valueOf(sourceImg.getWidth()));
            size.appendChild(width);
//            height
            Element height = document.createElement("height");
            height.setTextContent(String.valueOf(sourceImg.getHeight()));
            size.appendChild(height);
//            depth
            Element depth = document.createElement("depth");
            depth.setTextContent(String.valueOf(sourceImg.getColorModel().getNumComponents()));
            size.appendChild(depth);
//            合并
            annotation.appendChild(filename);
            annotation.appendChild(size);
//            object
            for (FrameInformation frameInformation : frameInformations) {
                Element object = document.createElement("object");
                Element bndbox = document.createElement("bndbox");
//                xmin
                Element xmin = document.createElement("xmin");
                xmin.setTextContent(frameInformation.getLeftUpperCornerAbscissa());
                bndbox.appendChild(xmin);
//                ymin
                Element ymin = document.createElement("ymin");
                ymin.setTextContent(frameInformation.getLeftUpperCornerOrdinate());
                bndbox.appendChild(ymin);
//                xmax
                Element xmax = document.createElement("xmax");
                xmax.setTextContent(frameInformation.getRightLowerQuarterAbscissa());
                bndbox.appendChild(xmax);
//                ymax
                Element ymax = document.createElement("ymax");
                ymax.setTextContent(frameInformation.getRightLowerQuarterOrdinate());
                bndbox.appendChild(ymax);
//                name
                Element name = document.createElement("name");
                name.setTextContent(getName(frameInformation));
//                合并
                object.appendChild(bndbox);
                object.appendChild(name);
                annotation.appendChild(object);
            }
//            生成xml
            document.appendChild(annotation);
            TransformerFactory tff = TransformerFactory.newInstance();
            Transformer tf = tff.newTransformer();
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.transform(new DOMSource(document), new StreamResult(xmlFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    下载
    public String download(String VideoId) throws IOException{
//        获取视频信息
        VideoInformation videoInformation = videoInformationDao.queryByVideoId(VideoId);
        if (!videoInformation.getIsMoted().equals("1")){
            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE, VideoId + " 未标注");
        }
        if (!videoInformation.getIsTagged().equals("1")){
            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE, VideoId + " 未跟踪");
        }
//        获取该视频的最大帧数
        Integer frameMax = frameInformationDao.queryFrameMaxByVideoId(VideoId);
//        循环生成xml文件
        List<File> files = new ArrayList<>();
//        初始化文件夹
        File xmlRootDir = new File(Paths.get(minioLocalUrl, xml).toUri());
        dirInit(xmlRootDir);
        File xmlRootPath = new File(Paths.get(xmlRootDir.getPath(), VideoId).toUri());
        dirInit(xmlRootPath);
        dirDelete(xmlRootPath);

        for (int i = 1; i <= frameMax; i++) {
            List<FrameInformation> frameInformations = frameInformationDao.queryByFramesAndVideoId(VideoId, i);
            if (frameInformations.isEmpty()){
                throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE, VideoId + " frame " + i + " null");
            }
            getXmlFile(frameInformations, String.valueOf(xmlRootPath), i);
        }

        File[] fileXml = (new File(Paths.get(minioLocalUrl, xml, VideoId).toUri())).listFiles();
        File[] filePic = (new File(Paths.get(minioLocalUrl, motimgBuket, VideoId).toUri())).listFiles();
        if (fileXml == null || filePic == null){
            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE, VideoId + " fileXml or filePic " + " null");
        }
        File[] fileZip = Arrays.copyOf(fileXml, fileXml.length + filePic.length);
        System.arraycopy(filePic, 0, fileZip, fileXml.length, filePic.length);
        File zipFile = new File(Paths.get(minioLocalUrl, xml, VideoId, VideoId + ".zip").toUri());
        zipFiles(fileZip, zipFile);
//        return videoInformation.getVideoFileUrl();
        if (!zipFile.exists()){
            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE, VideoId + " zip not exit ");
        }
        return zipFile.getPath();
    }

    public static void zipFiles(File[] srcFiles, File zipFile) {
        // 判断压缩后的文件存在不，不存在则创建
        if (!zipFile.exists()) {
            try {
                zipFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 创建 FileOutputStream 对象
        FileOutputStream fileOutputStream = null;
        // 创建 ZipOutputStream
        ZipOutputStream zipOutputStream = null;
        // 创建 FileInputStream 对象
        FileInputStream fileInputStream = null;

        try {
            // 实例化 FileOutputStream 对象
            fileOutputStream = new FileOutputStream(zipFile);
            // 实例化 ZipOutputStream 对象
            zipOutputStream = new ZipOutputStream(fileOutputStream);
            // 创建 ZipEntry 对象
            ZipEntry zipEntry = null;
            // 遍历源文件数组
            for (int i = 0; i < srcFiles.length; i++) {
                // 将源文件数组中的当前文件读入 FileInputStream 流中
                fileInputStream = new FileInputStream(srcFiles[i]);
                // 实例化 ZipEntry 对象，源文件数组中的当前文件
                zipEntry = new ZipEntry(srcFiles[i].getName());
                zipOutputStream.putNextEntry(zipEntry);
                // 该变量记录每次真正读的字节个数
                int len;
                // 定义每次读取的字节数组
                byte[] buffer = new byte[1024];
                while ((len = fileInputStream.read(buffer)) > 0) {
                    zipOutputStream.write(buffer, 0, len);
                }
            }
            zipOutputStream.closeEntry();
            zipOutputStream.close();
            fileInputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public List<VideoInformationQueryShootPlaceRsp> queryShootPlace(){
        return videoInformationDao.queryShootPlace();
    }

    public VideoLengthRsp queryVideoLength(VideoLengthReq req){
        try{
            VideoLengthRsp rsp = videoInformationDao.queryVideoLength(req.getVideoId());
            if (rsp == null){
                throw new CMSException(404, "VideoId can not find");
            }
            return rsp;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    // 查询指定文件夹下的文件夹或者文件
    public List<GetDataListFromServerRsp> getDataListFromServer(GetDataListFromServerReq req){
        List<GetDataListFromServerRsp> getDataListFromServerRsps = new ArrayList<>();
        Map<String, List<String>> dataMap = readDirectoryList(req.getFilePathNow(), req.getType());
        List<String> dataNames = dataMap.get("dataNames");
        List<String> dataTypes = dataMap.get("dataTypes");
        assert dataNames.size() == dataTypes.size();
        for (int i = 0; i < dataTypes.size(); i++) {
            GetDataListFromServerRsp Rsp = new GetDataListFromServerRsp();
            // 根据系统的路径分隔符号对文件或者文件夹路径分割，只取文件或文件夹名字
            String dataListName = String.valueOf(dataNames.get(i)).split(File.separator)[String.valueOf(dataNames.get(i)).split(File.separator).length - 1];
            Rsp.setDataListName(dataListName);
            Rsp.setType(dataTypes.get(i));
            getDataListFromServerRsps.add(Rsp);
        }
        return getDataListFromServerRsps;
    }


    // 根据queryType查询指定文件夹（filePath为空代表yaml配置的文件夹，否则为后端配置文件夹下的filePath文件夹(","隔开)）
    // queryType代表查询的是文件夹还是文件或者所有
    // 用Map<String, List<String>>返回类型（文件夹或文件）以及路径
    public Map<String, List<String>> readDirectoryList(String filePath, String queryType){
        String filePathNow = getSplitFileName(filePath, serverFilePath, ",");
        Map<String, List<String>> map = new HashMap<>();
        List<String> dataNames = new ArrayList<>();
        List<String> dataTypes = new ArrayList<>();
        File file = new File(filePathNow);
        File[] tempList = file.listFiles();
        if (tempList != null) {
            for (File value : tempList) {
                switch (queryType) {
                    case "0":
                        if (value.isDirectory()) {
                            dataNames.add(String.valueOf(value));
                            dataTypes.add("0");
                        }
                        break;
                    case "1":
                        if (value.isFile()) {
                            dataNames.add(String.valueOf(value));
                            dataTypes.add("1");
                        }
                        break;
                    case "2":
                        if (value.isDirectory()) {
                            dataNames.add(String.valueOf(value));
                            dataTypes.add("0");
                        } else if (value.isFile()){
                            dataNames.add(String.valueOf(value));
                            dataTypes.add("1");
                        }
                        break;
                    default:
                        throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE, queryType + "查询模式错误");
                }
            }
            map.put("dataNames", dataNames);
            map.put("dataTypes", dataTypes);
            return map;
        }else {
            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE, filePathNow + "文件夹错误");
        }
    }

    // 从服务器内部上传
    public void createFromServer(VideoFromServerCreateReq req) throws Exception {
        String filePath = req.getFileName().replace(" ", "").replace("\n", "");
        String filePathNow = getSplitFileName(filePath, null, "/");
        if (filePathNow != null) {
            File file = new File(filePathNow);
            List<File> files = new ArrayList<>();
            files.add(file);
            if(req.getSensorType().equals(SensorTypeEnum.HYPERSPECTRAL_IMAGE.getState())){
                files.add(new File(req.getTypeLinkId()));
            }
            if (file.exists()) {
                VideoInformationCreateReq videoInformationCreateReq = new VideoInformationCreateReq();
                videoInformationCreateReq.setSensorType(req.getSensorType());
                videoInformationCreateReq.setShootPlace(req.getShootPlace());
                videoInformationCreateReq.setShootTime(req.getShootTime());
                if (! req.getTypeLinkId().equals("")){
                    if (! (req.getSensorType().equals(SensorTypeEnum.HYPERSPECTRAL_IMAGE.getState()) || req.getSensorType().equals(SensorTypeEnum.INFRARED.getState()))){
                        throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE, "TypeLinkId: " + req.getTypeLinkId() + " 不为空, 但却不是高光谱或者红外光");
                    };
                }else {
                    if (req.getSensorType().equals(SensorTypeEnum.HYPERSPECTRAL_IMAGE.getState()) || req.getSensorType().equals(SensorTypeEnum.INFRARED.getState())){
                        throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE, "TypeLinkId: " + req.getTypeLinkId() + " 为空, 但却是高光谱或者红外光");
                    };
                }
                videoInformationCreateReq.setTypeLinkId(req.getTypeLinkId());
                VideoInformation videoInformation = setVideoInformation(videoInformationCreateReq);
                try {
                    videoInformation = setFile(videoInformation, files);
                    videoInformation = doCreate(videoInformation);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                videoInformationDao.insert(videoInformation);
                System.out.println("From " + filePathNow + " upload");
            }else {
                throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE, filePathNow + "文件路径错误");
            }
        }else {
            throw new CMSException(Constants.BUSINESS_EXCEPTION_CODE, "文件路径错误");
        }

    }

    // 将按","分隔的路径拼接起来
    public String getSplitFileName(String filePath, String defaultPath, String separator){
        String filePathNow = defaultPath;
        if(! filePath.replace(" ", "").replace("\n", "").equals("")) {
            StringBuilder filePathSplit = new StringBuilder();
            for (String split : filePath.split(separator)){
                filePathSplit.append(File.separator).append(split.replace(" ", "").replace("\n", ""));
            }
            filePathNow = serverFilePath + filePathSplit;
        }
        return filePathNow;
    }

}
