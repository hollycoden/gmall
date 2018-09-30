package com.atguigu.gmall.interceptor;

import com.atguigu.gmall.annotation.LoginRequire;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.HttpClientUtil;
import com.atguigu.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);

        //不需要验证是否登录
        if (null == methodAnnotation){
            return true;
        }

        boolean b = methodAnnotation.needSucess();

        String token = "";
        String oldToken = CookieUtil.getCookieValue(request,"oldToken",true);
        String newToken = request.getParameter("token");

        //oldToken空，newToken空，从未登录

        //oldToken空，newToken不空，第一次登录
        if (StringUtils.isBlank(oldToken) && StringUtils.isNotBlank(newToken)){
            token = newToken;
        }

        //oldToken不空，newToken空，之前登录过
        if (StringUtils.isNotBlank(oldToken) && StringUtils.isBlank(newToken)){
            token = oldToken;
        }

        //oldToken不空，newToken不空，cookie中的token过期了
        if (StringUtils.isNotBlank(newToken) && StringUtils.isNotBlank(oldToken)){
            token = newToken;
        }

        //验证
        if (StringUtils.isNotBlank(token)){

            //进行验证
            String ip = getMyIpFromRequest(request);
            ip = getMyIpFromRequest(request);

            //远程调用认证中心，验证token，一个基于http的rest风格的webservice请求
            String url = "http://passport.gmall.com:8085/verify?token="+token+"&currentIp="+getMyIpFromRequest(request);
            String success = HttpClientUtil.doGet(url);

            if (success.equals("success")){

                //将token重新写入浏览器cookie,刷新用户的过期时间
                CookieUtil.setCookie(request,response,"oldToken",token,1000*60*60*24,true);

                //将用户信息放入请求中
                Map atguigugmall0508 = JwtUtil.decode("atguigugmall0508", token, ip);

                request.setAttribute("userId",atguigugmall0508.get("userId"));
                request.setAttribute("nickName",atguigugmall0508.get("nickName"));
                return true;
            }
        }


        if (b){
            //token为空并且需要登录
            response.sendRedirect("http://passport.gmall.com:8085/index?returnUrl=" + request.getRequestURL());
            return false;
        } else {
            //token为空并且不需要登录
            return true;
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