package com.atguigu.gmall.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.bean.UserInfo;
import com.atguigu.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HelloController {

    @Reference
    UserService userService;

    @RequestMapping("list")
    public List<UserInfo> hello(){
        List<UserInfo> userList = userService.getUserList();
        return userList;
    }
}
