package com.atguigu.service;

import com.atguigu.bean.*;

import java.util.List;

public interface ManageService {
    void saveSpuInfo(SpuInfo spuInfo);

    SkuInfo getSkuInfo(String skuId);

    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(String id, String spuId);

    List<SkuInfo> getSkuInfoListBySpu(String spuId);

    List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    List<SpuSaleAttrValue> getSpuSaleAttrValueList(SpuSaleAttrValue spuSaleAttrValue);

    void saveSkuInfo(SkuInfo skuInfo);

    List<SpuImage> getSpuImageList(String spuId);
}
