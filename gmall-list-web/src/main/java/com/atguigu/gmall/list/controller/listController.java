package com.atguigu.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.bean.BaseAttrInfo;
import com.atguigu.bean.SkuLsAttrValue;
import com.atguigu.bean.SkuLsInfo;
import com.atguigu.bean.SkuLsParam;
import com.atguigu.service.AttrService;
import com.atguigu.service.ListService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashSet;
import java.util.List;

@Controller
public class listController {

    @Reference
    ListService listService;

    @Reference
    AttrService attrService;

    /**
     * 检索并返回商品分类信息
     * @param skuLsParam
     * @param map
     * @return
     */
    @RequestMapping("list.html")
    public String list(SkuLsParam skuLsParam, ModelMap map){

        List<SkuLsInfo> skuLsInfoList = listService.search(skuLsParam);

        //取出所有平台属性值 id ，并去重复
        HashSet<String> strings = new HashSet<>();

        for (SkuLsInfo skuLsInfo : skuLsInfoList) {

            List<SkuLsAttrValue> skuAttrValueList = skuLsInfo.getSkuAttrValueList();
            //拿到每一个sku的 平台属性值 id
            for (SkuLsAttrValue skuLsAttrValue : skuAttrValueList) {
                String valueId = skuLsAttrValue.getValueId();
                strings.add(valueId);
            }
        }

        String join = StringUtils.join(strings, ",");
        List<BaseAttrInfo> baseAttrInfos = attrService.getAttrListByValueId(join);

        map.put("skuLsInfoList",skuLsInfoList);
        map.put("attrList",baseAttrInfos);
        return "list";
    }
}