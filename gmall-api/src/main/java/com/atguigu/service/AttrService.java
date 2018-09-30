package com.atguigu.service;

import com.atguigu.bean.BaseAttrInfo;

import java.util.List;

public interface AttrService {
    List<BaseAttrInfo> getAttrListByValueId(String join);
}
