package com.atguigu.gmall.manage.util;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class FileUploadUtil {
    public static String uploadImage(MultipartFile file) {

        // 加载fastdfs的配置文件
        String path = FileUploadUtil.class.getClassLoader().getResource("tracker.conf").getPath();
        try {
            ClientGlobal.init(path);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        // 创建一个tracker的连接
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer connection = null;
        try {
            connection = trackerClient.getConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 从tracker中返回一个可用的storage
        StorageClient storageClient = new StorageClient(connection, null);

        // 上传文件
        String[] gifs = new String[0];//上传的数据的byte数组，需要上传的数据的路径
        try {
            // 获得文件的扩展名
            String originalFilename = file.getOriginalFilename();
            String[] split = originalFilename.split("\\.");
            String extName = split[(split.length - 1)];
            gifs = storageClient.upload_file(file.getBytes(), extName, null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        // 返回上传结果
        String imgUrl = "http://192.168.217.130";
        for (String gif : gifs) {
            imgUrl = imgUrl +"/"+gif;
        }
        return imgUrl;
    }
}
