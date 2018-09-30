package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.bean.SkuInfo;
import com.atguigu.bean.SkuSaleAttrValue;
import com.atguigu.bean.SpuSaleAttr;
import com.atguigu.service.ManageService;
import com.atguigu.service.SkuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {

    @Reference
    ManageService manageService;

    @Reference
    SkuService skuService;

    @RequestMapping("demo")
    public String demo(HttpServletRequest request){

        String strings = "hello thymeleaf";

        List<String> stringList = new ArrayList<>();
        stringList.add("one");
        stringList.add("two");
        stringList.add("three");

        request.setAttribute("hello",strings);
        request.setAttribute("stringList",stringList);
        return "demo";
    }


    /**
     * 商品详情页面
     * @param skuId
     * @param model
     * @return
     */
    @RequestMapping("{skuId}.html")
    public String getSkuInfo(@PathVariable("skuId") String skuId, Model model) {

        //当前sku
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        model.addAttribute("skuInfo",skuInfo);
        String spuId = skuInfo.getSpuId();

        //查询销售属性列表
        List<SpuSaleAttr> spuSaleAttrListCheckBySku = manageService.getSpuSaleAttrListCheckBySku(skuInfo.getId(), skuInfo.getSpuId());
        model.addAttribute("spuSaleAttrListCheckBySku",spuSaleAttrListCheckBySku);

        //查询sku的兄弟姐妹的has表HashMap
        Map<String,String> skuMap = new HashMap<String,String>();
        List<SkuInfo> skuInfos =  skuService.getSkuSaleAttrValueListBySpu(spuId);
        for (SkuInfo info : skuInfos) {
            String v = info.getId();
            String k = "";
            List<SkuSaleAttrValue> skuSaleAttrValueList = info.getSkuSaleAttrValueList();
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                String saleAttrValueId = skuSaleAttrValue.getSaleAttrValueId();
                k = k + "|" +  saleAttrValueId;
            }
            skuMap.put(k,v);
        }

        // 用json工具将hashmap转化成json字符串
        String skuMapJson = JSON.toJSONString(skuMap);
        model.addAttribute("skuMapJson",skuMapJson);
        System.out.println(skuMapJson);

        return "item";
    }
}