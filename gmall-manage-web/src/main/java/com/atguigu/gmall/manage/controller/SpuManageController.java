package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.bean.*;
import com.atguigu.service.BaseSaleService;
import com.atguigu.service.CatalogService;
import com.atguigu.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SpuManageController {

    @Reference
    CatalogService catalogService;

    @Reference
    BaseSaleService baseSaleService;

    @Reference
    ManageService manageService;

    @RequestMapping("spuListPage")
    public String getAttrListPage(){
        return "spuListPage";
    }

   /* @RequestMapping("spuSaleAttrPage")
    public String spuSaleAttrPage(){
        return "spuSaleAttrPage";
    }*/

    @RequestMapping("spuList")
    @ResponseBody
    //页面访问该方法时传递来一个catalog3Id=ctg3val 的键值对
    public List<SpuInfo> getSpuList(@RequestParam Map<String,String> map){
        String catalog3Id =   map.get("catalog3Id") ;
        List<SpuInfo> spuList = catalogService.getSpuList(catalog3Id);
        return spuList;
    }


    /**
     * 查询并返回基本信息表
     * @return
     */
    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<BaseSaleAttr> getBaseSaleAttrList(){
        List<BaseSaleAttr> baseSaleAttrList = baseSaleService.getBaseSaleAttrList();
        return baseSaleAttrList;
    }


    /**
     * 保存商品信息
     * @param spuInfo
     * @return
     */
    @RequestMapping(value = "saveSpuInfo",method = RequestMethod.POST)
    @ResponseBody
    public String saveSpuInfo(SpuInfo spuInfo){
        manageService.saveSpuInfo(spuInfo);
        return  "success";
    }


    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<SpuSaleAttr> getSpuSaleAttrList(HttpServletRequest httpServletRequest){
        String spuId = httpServletRequest.getParameter("spuId");
        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrList(spuId);

        for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            Map map=new HashMap();
            map.put("total",spuSaleAttrValueList.size());
            map.put("rows",spuSaleAttrValueList);
            // String spuSaleAttrValueJson = JSON.toJSONString(map);
            spuSaleAttr.setSpuSaleAttrValueJson(map);
        }
        return spuSaleAttrList;
    }


    @RequestMapping(value ="spuImageList" ,method = RequestMethod.GET)
    @ResponseBody
    public  List<SpuImage> getSpuImageList(@RequestParam Map<String,String> map){
        String spuId = map.get("spuId");
        List<SpuImage> spuImageList = manageService.getSpuImageList(spuId);
        return spuImageList;
    }


}
