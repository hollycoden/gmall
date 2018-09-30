package com.atguigu.gmall.manage.mapper;

import com.atguigu.bean.SkuInfo;
import com.atguigu.bean.SkuSaleAttrValue;
import com.atguigu.bean.SpuSaleAttr;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(@Param("spuId") Integer spuId, @Param("skuId") Integer skuId);
    List<SkuInfo> selectSkuSaleAttrValueListBySpu(Integer spuId);
}
