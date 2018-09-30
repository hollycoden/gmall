package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.bean.CartInfo;
import com.atguigu.bean.UserInfo;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.JwtUtil;
import com.atguigu.service.CartService;
import com.atguigu.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Reference
    UserService userService;

    @Reference
    CartService cartService;

    /**
     * 跳入登录页面
     * @return
     */
    @RequestMapping("index")
    public String index(String returnUrl, ModelMap map){
        map.put("originUrl",returnUrl);
        return "index";
    }


    /***
     * 用户登录,生成token
     * @param request
     * @param map
     * @return
     */
    @RequestMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo, HttpServletRequest request, HttpServletResponse response, ModelMap map){

        //验证用户名和密码
        UserInfo user = userService.login(userInfo);

        if (null == user){
            //提示用户名或者密码错误
            return "err";

        } else {
            //如果验证成功，根据用户名和密码生成token，然后将该用户的信息从db中提取到redis，设置用户的过期时间
            Map<String,String> userMap = new HashMap<>();
            userMap.put("userId",user.getId());
            userMap.put("nickName",user.getNickName());
            String ip = getMyIpFromRequest(request);
            String token = JwtUtil.encode("atguigugmall0508", userMap, ip);

            //合并购物车
            String listCartCookie = CookieUtil.getCookieValue(request, "listCartCookie", true);
            cartService.combine(user.getId(), JSON.parseArray(listCartCookie, CartInfo.class));

            //删除购物车中的cookie
            CookieUtil.deleteCookie(request,response,"listCartCookie");

            return token;
        }
    }


    /**
     * 验证用户token
     * @param request
     * @param token
     * @param map
     * @return
     */
    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request,String token, String currentIp, ModelMap map){

        //验证token的真伪
        Map atguigugmall0508 = JwtUtil.decode("atguigugmall0508", token, currentIp);

        try {
            if (null != atguigugmall0508){
                //验证token对应的用户的过期时间
                return "success";
            } else {
                return  "fail";
            }
        }catch (Exception e){
            return "fail";
        }
    }

    /**
     * 获得客户端IP
     * @param request
     * @return
     */
    private String getMyIpFromRequest(HttpServletRequest request) {

        String ip = "";
        ip = request.getRemoteAddr();
        if (StringUtils.isBlank(ip)){
            ip = request.getHeader("x-forwarded-for");
            if (StringUtils.isBlank(ip)){
                ip = "127.0.0.1";
            }
        }
        return ip;
    }
}
