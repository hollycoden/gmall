package com.atguigu.service;

import com.atguigu.bean.*;

import java.util.List;

public interface CatalogService {

    List<BaseCatalog1> getCatalog1();

    List<BaseCatalog2> getCatalog2(String catalog1Id);

    List<BaseCatalog3> getCatalog3(String catalog2Id);

    List<BaseAttrInfo> getAttrList(String catalog3Id);

    BaseAttrInfo getAttrInfo(String attrId);

    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    List<SpuInfo> getSpuList(String catalog3Id);
}
