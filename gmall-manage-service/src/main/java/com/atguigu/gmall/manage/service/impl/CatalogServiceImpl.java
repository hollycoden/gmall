package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.bean.*;
import com.atguigu.gmall.manage.mapper.*;
import com.atguigu.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class CatalogServiceImpl implements CatalogService {

    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    SpuInfoMapper spuInfoMapper;

    @Override
    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        List<BaseCatalog2> select = baseCatalog2Mapper.select(baseCatalog2);
        return select;
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        List<BaseCatalog3> select = baseCatalog3Mapper.select(baseCatalog3);
        return select;
    }

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);
        List<BaseAttrInfo> attrInfoList = baseAttrInfoMapper.select(baseAttrInfo);

        for (BaseAttrInfo attrInfo : attrInfoList) {
            String id = attrInfo.getId();

            BaseAttrValue baseAttrValue = new BaseAttrValue();
            baseAttrValue.setAttrId(id);
            List<BaseAttrValue> attrValueList = baseAttrValueMapper.select(baseAttrValue);
            attrInfo.setAttrValueList(attrValueList);
        }

        return attrInfoList;
    }

    @Override
    public BaseAttrInfo getAttrInfo(String id) {
        //查询属性基本信息
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(id);

        //查询属性对应的属性值
        BaseAttrValue  baseAttrValue4Query =new BaseAttrValue();
        baseAttrValue4Query.setAttrId(baseAttrInfo.getId());
        List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.select(baseAttrValue4Query);

        baseAttrInfo.setAttrValueList(baseAttrValueList);
        return baseAttrInfo;

    }

    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //如果有主键就进行更新，如果没有就插入
        if (baseAttrInfo.getId()!= null && baseAttrInfo.getId().length()>0){
            baseAttrInfoMapper.updateByPrimaryKey(baseAttrInfo);
        } else {
            //防止主键被赋值一个空字符串
            if (baseAttrInfo.getId().length() == 0){
                baseAttrInfo.setId(null);
            }
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }
        //把原属性值全部清空
        BaseAttrValue baseAttrValue4Del  = new BaseAttrValue();
        baseAttrValue4Del.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValue4Del);

        //重新插入属性
        if (baseAttrInfo.getAttrValueList()!= null&&baseAttrInfo.getAttrValueList().size()>0){
            for (BaseAttrValue attrValue : baseAttrInfo.getAttrValueList()) {
                //防止主键被赋上一个空字符串
                if(attrValue.getId()!=null&&attrValue.getId().length()==0){
                    attrValue.setId(null);
                }
                attrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(attrValue);
            }
        }
    }

    @Override
    public List<SpuInfo> getSpuList(String catalog3Id) {

        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);
        List<SpuInfo> select = spuInfoMapper.select(spuInfo);
        return select;
    }
}
