package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.bean.SkuInfo;
import com.atguigu.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class SkuController {

    @Reference
    ManageService manageService;

    @RequestMapping("skuInfoListBySpu")
    @ResponseBody
    public List<SkuInfo> getSkuInfoListBySpu(HttpServletRequest request){
        String spuId = request.getParameter("spuId");

        List<SkuInfo> skuInfoList = manageService.getSkuInfoListBySpu(spuId);
        return skuInfoList;
    }


    @RequestMapping(value = "saveSku",method = RequestMethod.POST)
    @ResponseBody
    public String saveSkuInfo(SkuInfo skuInfo){
        manageService.saveSkuInfo(skuInfo);
        return "success";
    }

}
