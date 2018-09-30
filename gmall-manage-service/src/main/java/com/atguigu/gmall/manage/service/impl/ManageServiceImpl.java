package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.bean.*;
import com.atguigu.gmall.manage.mapper.*;
import com.atguigu.gmall.util.RedisUtil;
import com.atguigu.service.ManageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    SpuInfoMapper spuInfoMapper;

    @Autowired
    SpuImageMapper spuImageMapper;

    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    SkuInfoMapper skuInfoMapper;

    @Autowired
    SkuImageMapper skuImageMapper;

    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;


    @Override
    public void saveSpuInfo(SpuInfo spuInfo){
        //保存主表 通过主键存在判断是修改 还是新增
        if(spuInfo.getId()==null||spuInfo.getId().length()==0){
            spuInfo.setId(null);
            spuInfoMapper.insertSelective(spuInfo);
        }else{
            spuInfoMapper.updateByPrimaryKey(spuInfo);
        }

        //保存图片信息 先删除 再插入
        Example spuImageExample=new Example(SpuImage.class);
        spuImageExample.createCriteria().andEqualTo("spuId",spuInfo.getId());
        spuImageMapper.deleteByExample(spuImageExample);

        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if(spuImageList!=null) {
            for (SpuImage spuImage : spuImageList) {
                if(spuImage.getId()!=null&&spuImage.getId().length()==0){
                    spuImage.setId(null);
                }
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insertSelective(spuImage);
            }
        }

        //保存销售属性信息 先删除
        Example spuSaleAttrExample=new Example(SpuSaleAttr.class);
        spuSaleAttrExample.createCriteria().andEqualTo("spuId",spuInfo.getId());
        spuSaleAttrMapper.deleteByExample(spuSaleAttrExample);

        //保存销售属性值信息 先删除
        Example spuSaleAttrValueExample=new Example(SpuSaleAttrValue.class);
        spuSaleAttrValueExample.createCriteria().andEqualTo("spuId",spuInfo.getId());
        spuSaleAttrValueMapper.deleteByExample(spuSaleAttrValueExample);

        //保存销售属性信息 再插入
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if(spuSaleAttrList!=null) {
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                if(spuSaleAttr.getId()!=null&&spuSaleAttr.getId().length()==0){
                    spuSaleAttr.setId(null);
                }
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insertSelective(spuSaleAttr);
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                    if(spuSaleAttrValue.getId()!=null&&spuSaleAttrValue.getId().length()==0){
                        spuSaleAttrValue.setId(null);
                    }
                    spuSaleAttrValue.setSpuId(spuInfo.getId());
                    spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
                }
            }
        }
    }


    /**
     * 从db中查看sku详情
     * @param skuId
     * @return
     */
    public SkuInfo getSkuInfoFromDB(String skuId){


        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        if(skuInfo==null){
            return null;
        }
        //查询图片集合
        SkuImage skuImage=new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);
        skuInfo.setSkuImageList(skuImageList);

        //查询sku信息
        SkuSaleAttrValue skuSaleAttrValue=new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuId);
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuSaleAttrValueMapper.select(skuSaleAttrValue);
        skuInfo.setSkuSaleAttrValueList(skuSaleAttrValueList);
        return  skuInfo;

    }


    /**
     * 从redis中查询sku和图片集合
     * @param skuId
     * @return
     */
    @Override
    public SkuInfo getSkuInfo(String skuId) {

        SkuInfo skuInfo = null;
        String skuKey = "sku:"+skuId+":info";

        //缓存Redis查询
        Jedis jedis = redisUtil.getJedis();
        String s = jedis.get(skuKey);

        if (StringUtils.isNotBlank(s)&&!"empty".equals(s)){
            skuInfo = JSON.parseObject(s,SkuInfo.class);
        } else {

            //db查询
            skuInfo = getSkuInfoFromDB(skuId);

            //同步redis
            jedis.set(skuKey,JSON.toJSONString(skuInfo));
        }
        return  skuInfo;
    }


    /**
     * 查询销售属性值信息列表，返回在页面上
     * @param spuId
     * @param skuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(String spuId, String skuId) {
        List<SpuSaleAttr> spuSaleAttrs = skuSaleAttrValueMapper.selectSpuSaleAttrListCheckBySku(Integer.parseInt(spuId),Integer.parseInt(skuId));
        return  spuSaleAttrs;
    }

    @Override
    public List<SkuInfo> getSkuInfoListBySpu(String spuId) {
        Example example = new Example(SkuInfo.class);
        example.createCriteria().andEqualTo("spuId",spuId);
        List<SkuInfo> skuInfos = skuInfoMapper.selectByExample(example);
        return skuInfos;
    }


    @Override
    public  List<SpuSaleAttr> getSpuSaleAttrList(String spuId){
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectSpuSaleAttrList(Long.parseLong(spuId));
        return spuSaleAttrList;
    }



    @Override
    public List<SpuSaleAttrValue> getSpuSaleAttrValueList(SpuSaleAttrValue spuSaleAttrValue) {
        List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttrValueMapper.select(spuSaleAttrValue);
        return spuSaleAttrValueList;
    }


    /**
     * 保存sku
     * @param skuInfo
     */
    @Override
    public void saveSkuInfo(SkuInfo skuInfo){

        //插入或更新
        if(skuInfo.getId()==null||skuInfo.getId().length()==0){

            //该数据不存在，直接插入
            skuInfo.setId(null);
            skuInfoMapper.insertSelective(skuInfo);
        }else {
            //该数据已存在，更新
            skuInfoMapper.updateByPrimaryKeySelective(skuInfo);
        }

        //更新、插入skuImage
        Example example=new Example(SkuImage.class);
        example.createCriteria().andEqualTo("skuId",skuInfo.getId());
        skuImageMapper.deleteByExample(example);

        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage skuImage : skuImageList) {
            skuImage.setSkuId(skuInfo.getId());
            if(skuImage.getId()!=null&&skuImage.getId().length()==0) {
                skuImage.setId(null);
            }
            skuImageMapper.insertSelective(skuImage);
        }


        //更新、插入SkuAttrValue
        Example skuAttrValueExample=new Example(SkuAttrValue.class);
        skuAttrValueExample.createCriteria().andEqualTo("skuId",skuInfo.getId());
        skuAttrValueMapper.deleteByExample(skuAttrValueExample);

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            skuAttrValue.setSkuId(skuInfo.getId());
            if(skuAttrValue.getId()!=null&&skuAttrValue.getId().length()==0) {
                skuAttrValue.setId(null);
            }
            skuAttrValueMapper.insertSelective(skuAttrValue);
        }


        //更新、插入SkuSaleAttrValue
        Example skuSaleAttrValueExample=new Example(SkuSaleAttrValue.class);
        skuSaleAttrValueExample.createCriteria().andEqualTo("skuId",skuInfo.getId());
        skuSaleAttrValueMapper.deleteByExample(skuSaleAttrValueExample);

        //插入skuSaleAttrValueList
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
            skuSaleAttrValue.setSkuId(skuInfo.getId());
            skuSaleAttrValue.setId(null);
            skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
        }
    }

    @Override
    public List<SpuImage> getSpuImageList(String spuId) {

        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        List<SpuImage> spuImageList = spuImageMapper.select(spuImage);

        return spuImageList;
    }
}