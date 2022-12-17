package com.tagging.utils;

import org.apache.tomcat.util.http.fileupload.IOUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;

public class ZipUtils {
    public static void downloadZip(HttpServletResponse response, String path){
        File file = new File(path);
        try {
            ZipFile zipFile = new ZipFile(file);
            InputStream fis = Files.newInputStream(Path.of((path)));
            IOUtils.copy(fis, response.getOutputStream());
            fis.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        if (file.exists()) {
            file.delete();
        }
    }
}