package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.bean.BaseAttrInfo;
import com.atguigu.gmall.manage.mapper.BaseAttrInfoMapper;
import com.atguigu.service.AttrService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class AttrServiceImpl implements AttrService {

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

    @Override
    public List<BaseAttrInfo> getAttrListByValueId(String join) {
        List<BaseAttrInfo> baseAttrInfos = baseAttrInfoMapper.selectAttrListByValueId(join);
        return baseAttrInfos;
    }


}
