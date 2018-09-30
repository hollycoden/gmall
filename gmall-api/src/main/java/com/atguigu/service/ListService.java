package com.atguigu.service;

import com.atguigu.bean.SkuLsInfo;
import com.atguigu.bean.SkuLsParam;

import java.util.List;

public interface ListService {
    List<SkuLsInfo> search(SkuLsParam skuLsParam);


}
