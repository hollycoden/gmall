package com.atguigu.gmall.manage.controller;

import com.atguigu.gmall.manage.util.FileUploadUtil;
import org.csource.common.MyException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class FileUploadController {
    @RequestMapping(value = "fileUpload",method = RequestMethod.POST)
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile file) throws IOException, MyException {
        // 调用fdfs的上传工具
        String imgUrl = FileUploadUtil.uploadImage(file);
        return imgUrl;
    }
}
