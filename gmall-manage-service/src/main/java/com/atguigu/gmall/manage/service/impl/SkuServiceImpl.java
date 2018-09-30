package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.bean.SkuAttrValue;
import com.atguigu.bean.SkuImage;
import com.atguigu.bean.SkuInfo;
import com.atguigu.bean.SkuSaleAttrValue;
import com.atguigu.gmall.manage.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.manage.mapper.SkuImageMapper;
import com.atguigu.gmall.manage.mapper.SkuInfoMapper;
import com.atguigu.gmall.manage.mapper.SkuSaleAttrValueMapper;
import com.atguigu.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    SkuInfoMapper skuInfoMapper;

    @Autowired
    SkuImageMapper skuImageMapper;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;


    /**
     * 根据spuId查询sku销售属性值
     * @param spuId
     * @return
     */
    @Override
    public List<SkuInfo> getSkuSaleAttrValueListBySpu(String spuId) {
        List<SkuInfo> skuInfos = skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(Integer.parseInt(spuId));

        return skuInfos;
    }


    /**
     * 根据三级分类id查询SkuInfo
     * @param catalog3Id
     * @return
     */
    @Override
    public List<SkuInfo> getSkuByCatalog3Id(int catalog3Id) {

        /*查询skuInfo*/
        SkuInfo skuInfoParam = new SkuInfo();
        skuInfoParam.setCatalog3Id(catalog3Id+"");
        List<SkuInfo> skuInfos = skuInfoMapper.select(skuInfoParam);


        for (SkuInfo skuInfo : skuInfos) {
            String skuId = skuInfo.getId();

            //通过skuId查询skuImage集合，放入到每一个skuInfo对象中
            SkuImage skuImageParam = new SkuImage();
            skuImageParam.setSkuId(skuId);
            List<SkuImage> skuImages = skuImageMapper.select(skuImageParam);
            skuInfo.setSkuImageList(skuImages);

            //平台属性集合
            SkuAttrValue skuAttrValue = new SkuAttrValue();
            skuAttrValue.setSkuId(skuId);
            List<SkuAttrValue> skuAttrValues = skuAttrValueMapper.select(skuAttrValue);
            skuInfo.setSkuAttrValueList(skuAttrValues);

            //销售属性信息
            SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
            skuSaleAttrValue.setSkuId(skuId);
            List<SkuSaleAttrValue> skuSaleAttrValues = skuSaleAttrValueMapper.select(skuSaleAttrValue);
            skuInfo.setSkuSaleAttrValueList(skuSaleAttrValues);
        }
        return skuInfos;
    }


    @Override
    public SkuInfo getSkuById(String skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        return skuInfo;
    }
}
