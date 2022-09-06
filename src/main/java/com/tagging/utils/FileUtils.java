package com.tagging.utils;

import com.tagging.entity.LineReplaceEntity;

import java.io.*;
import java.util.List;

public class FileUtils {
    public static String consumeInputStream(InputStream is){
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String s = null;
        StringBuilder sb = new StringBuilder();
        while(true){
            try {
                if (!((s=br.readLine())!=null)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(s);
            sb.append(s);
        }
        return sb.toString();
    }

    public static Integer numberOfFiles(String url){
        File folder = new File(url);
        File []list = folder.listFiles();
        int fileCount = 0;
        for (File file : list){
            fileCount += 1;
        }
        return fileCount;
    }

    public static void rpFileContentByLineNo(String path, List<LineReplaceEntity> replaceList) {
        File file = new File(path);
        BufferedReader br = null;
        FileReader in = null;
        // 内存流, 作为临时流
        CharArrayWriter tempStream = null;
        FileWriter out = null;
        try {
            in = new FileReader(file);
            br = new BufferedReader(in);
            // 内存流, 作为临时流
            tempStream = new CharArrayWriter();
            // 替换
            String line = null;
            while ((line = br.readLine()) != null) {
                for (LineReplaceEntity rpEntity : replaceList) {
                    if (line.equals(rpEntity.getLineStr())) {
                        line = rpEntity.getReplaceStr();
                        break;
                    }
                }
                if (line == "")
                    continue;
                // 将该行写入内存
                tempStream.write(line);
                // 添加换行符
                tempStream.append(System.getProperty("line.separator"));
            }
            // 将内存中的流 写入 文件
            out = new FileWriter(path);
            tempStream.writeTo(out);
            tempStream.close();
            out.close();
            br.close();
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //复制文件夹下所有文件
    public static void copyDir(File srcDir,File targetDir){
        if(!targetDir.exists()){
            targetDir.mkdir();//如果目的地的目录不存在，则需要使用File类的方法进行创建目录
        }
        File []files=srcDir.listFiles(); //获取指定目录下的所有File对象
        for (File file : files) {
            if (file.isFile()) {
                //如果file是文件则复制  srcDir -->D:\\PanDownload  拼接  D:\\PanDownload\\XXXX文件
                //    targetDir-->E:\\PanDownload 拼接 E:\\PanDownload\\XXXX文件
                copyFile(new File(srcDir+"/"+file.getName()), new File(targetDir+"/"+file.getName()));
            }else{
                //不是继续调用该方法判断，使用递归实现
                copyDir(new File(srcDir+"/"+file.getName()), new File(targetDir+"/"+file.getName()));
            }
        }
    }

    //复制文件
    public static void copyFile(File srcFile,File targetFile){
        //(1)提高读取效率，从数据源
        BufferedInputStream bis=null;
        //(2)提高写入效率，写到目的地
        BufferedOutputStream bos=null;
        try {
            bis = new BufferedInputStream(new FileInputStream(srcFile));

            bos = new BufferedOutputStream(new FileOutputStream(targetFile));
            //(3)边读边写
            byte [] buf=new byte[1024];//中转站
            int len=0;//用于存储读到的字节的个数
            while((len=bis.read(buf))!=-1){
                bos.write(buf,0,len);
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            //(4)关闭
            if(bos!=null){
                try {
                    bos.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if(bis!=null){
                try {
                    bis.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }
    }
}
