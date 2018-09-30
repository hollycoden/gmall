package com.atguigu.service;

import com.atguigu.bean.SkuInfo;

import java.util.List;

public interface SkuService {
    List<SkuInfo> getSkuSaleAttrValueListBySpu(String spuId);

    List<SkuInfo> getSkuByCatalog3Id(int catalog3Id);

    SkuInfo getSkuById(String skuId);
}
