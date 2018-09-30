package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.bean.BaseSaleAttr;
import com.atguigu.gmall.manage.mapper.BaseSaleMapper;
import com.atguigu.service.BaseSaleService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class BaseSaleServiceImpl implements BaseSaleService {

    @Autowired
    BaseSaleMapper baseSaleMapper;

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        List<BaseSaleAttr> baseSaleAttrList = baseSaleMapper.selectAll();
        return baseSaleAttrList;
    }
}
