package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.bean.*;
import com.atguigu.service.CatalogService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller
public class AttrManageController {

    @Reference
    CatalogService catalogService;

    @RequestMapping("attrListPage")
    public String getAttrListPage(){
        return "attrListPage";
    }

    /**
     * 获得一级分类
     * @return
     */
    @RequestMapping("getCatalog1")
    @ResponseBody
    public List<BaseCatalog1> getCatalog1(){
        List<BaseCatalog1> catalog1List = catalogService.getCatalog1();
        return catalog1List;
    }

    /**
     * 获得二级分类
     * @param map
     * @return
     */
    @RequestMapping("getCatalog2")
    @ResponseBody
    public List<BaseCatalog2> getCatalog2(@RequestParam Map<String,String> map){
        String catalog1Id = map.get("catalog1Id") ;
        List<BaseCatalog2> catalog2List = catalogService.getCatalog2(catalog1Id);
        return catalog2List;
    }

    /**
     * 获得三级分类
     * @param map
     * @return
     */
    @RequestMapping("getCatalog3")
    @ResponseBody
    public List<BaseCatalog3> getCatalog3(@RequestParam Map<String,String> map){
        String catalog2Id = map.get("catalog2Id");
        List<BaseCatalog3> catalog3List = catalogService.getCatalog3(catalog2Id);
        return catalog3List;
    }

    /**
     * 获得属性列表
     * @param map
     * @return
     */
    @RequestMapping("getAttrList")
    @ResponseBody
    public List<BaseAttrInfo>  getAttrList(@RequestParam Map<String,String> map){
        String catalog3Id =   map.get("catalog3Id") ;
        List<BaseAttrInfo> attrList = catalogService.getAttrList(catalog3Id);
        return attrList;
    }

    /**
     * 返回属性的值
     * @return
     */
    @RequestMapping(value = "getAttrValueList",method = RequestMethod.POST)
    @ResponseBody
    public List<BaseAttrValue> getAttrValueList(@RequestParam Map<String,String> map){
        String attrId = map.get("attrId");
        BaseAttrInfo attrInfo = catalogService.getAttrInfo(attrId);
        return attrInfo.getAttrValueList();
    }


    /**
     * 属性的保存
     * @param baseAttrInfo
     * @return
     */
    @RequestMapping(value = "saveAttrInfo",method = RequestMethod.POST)
    @ResponseBody
    public String saveAttrInfo(BaseAttrInfo baseAttrInfo){
        catalogService.saveAttrInfo(baseAttrInfo);
        return "success";
    }

    @RequestMapping("attrInfoList")
    @ResponseBody
    public List<BaseAttrInfo> attrInfoList(HttpServletRequest request){
        String catalog3Id = request.getParameter("catalog3Id");
        List<BaseAttrInfo> attrList = catalogService.getAttrList(catalog3Id);
        return attrList;
    }
}
