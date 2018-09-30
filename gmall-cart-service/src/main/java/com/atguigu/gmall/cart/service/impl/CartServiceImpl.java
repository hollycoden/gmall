package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.bean.CartInfo;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.util.RedisUtil;
import com.atguigu.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    CartInfoMapper cartInfoMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public CartInfo ifCartExist(CartInfo cartInfo) {
        Example example = new Example(CartInfo.class);

        example.createCriteria().andEqualTo("userId",cartInfo.getUserId()).
                andEqualTo("skuId",cartInfo.getSkuId());

        CartInfo cartInfoReturn = cartInfoMapper.selectOneByExample(example);

        return cartInfoReturn;
    }

    @Override
    public void updateCart(CartInfo cartInfoDb) {
        cartInfoMapper.updateByPrimaryKeySelective(cartInfoDb);

        //同步缓存
        flushCartCacheByUserId(cartInfoDb.getUserId());
    }

    @Override
    public void insertCart(CartInfo cartInfo) {
        cartInfoMapper.insertSelective(cartInfo);

        //同步缓存
        flushCartCacheByUserId(cartInfo.getUserId());
    }


    /**
     * 刷新redis缓存
     * @param userId
     */
    @Override
    public void flushCartCacheByUserId(String userId) {

        //查询userId对应的购物车结合
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        List<CartInfo> cartInfos = cartInfoMapper.select(cartInfo);

        //判断购物车集合是否为空
        if (null != cartInfos && cartInfos.size() > 0){
            //不为空，将数据更新到redis中

            //将购物车集合转化为map
            Map<String,String> map = new HashMap<String,String>();
            for (CartInfo info : cartInfos) {
                map.put(info.getId(), JSON.toJSONString(info));
            }

            Jedis jedis = redisUtil.getJedis();

            //删除redis中的数据，否则的话刷新缓存将变成增量添加
            jedis.del("cart:"+userId+":list");

            //将购物车的HashMap放入redis
            jedis.hmset("cart:"+userId+":list",map);
            jedis.close();
        } else {

            //清理redis
            Jedis jedis = redisUtil.getJedis();

            //删除该值
            jedis.del("cart:"+userId+":list");
            jedis.close();
        }
    }


    @Override
    public List<CartInfo> getCartInfosFromCacheByUserId(String userId) {
        //声明一个处理后的购物车集合对象
        List<CartInfo> cartInfos = new ArrayList<>();

        Jedis jedis = redisUtil.getJedis();

        //建购物车的HashMap放入redis
        List<String> hvals = jedis.hvals("cart:"+userId+":list");

        if (null != hvals && hvals.size()>0){
            for (String hval : hvals) {
                CartInfo cartInfo = JSON.parseObject(hval, CartInfo.class);
                cartInfos.add(cartInfo);
            }
        }
        jedis.close();

        return cartInfos;
    }

    @Override
    public void updateCartByUserId(CartInfo cartInfo) {

        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("skuId",cartInfo.getSkuId()).andEqualTo("userId",cartInfo.getUserId());
        cartInfoMapper.updateByExampleSelective(cartInfo,example);

        //同步缓存
        flushCartCacheByUserId(cartInfo.getUserId());

    }


    /**
     * 合并购物车
     * @param userId
     * @param listCartCookie
     */
    @Override
    public void combine(String userId, List<CartInfo> listCartCookie) {

        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        List<CartInfo> listCartDb = cartInfoMapper.select(cartInfo);

        if(null!=listCartCookie&&listCartCookie.size()>0){
            for (CartInfo cartCookie : listCartCookie) {
                String skuIdCookie = cartCookie.getSkuId();
                boolean b = true;
                if(null!=listCartDb&&listCartDb.size()>0){
                    b = if_new_cart(listCartDb, cartCookie);
                }

                if (!b) {
                    CartInfo cartDb = new CartInfo();
                    // 更新
                    for (CartInfo info : listCartDb) {
                        if (info.getSkuId().equals(cartCookie.getSkuId())) {
                            cartDb = info;
                        }
                    }
                    cartDb.setSkuNum(cartCookie.getSkuNum());
                    cartDb.setIsChecked(cartCookie.getIsChecked());
                    cartDb.setCartPrice(cartDb.getSkuPrice().multiply(new BigDecimal(cartDb.getSkuNum())));
                    cartInfoMapper.updateByPrimaryKeySelective(cartDb);
                } else {
                    // 添加
                    cartCookie.setUserId(userId);
                    cartInfoMapper.insertSelective(cartCookie);
                }

            }
        }

        // 同步刷新缓存
        flushCartCacheByUserId(userId);

    }

    @Override
    public void deleteCart(String join, String userId) {

        //删除购物车已经下单数据
        cartInfoMapper.deleteCartsById(join);

        //同步购物车缓存
        flushCartCacheByUserId(userId);

    }


    /**
     * 判断购物车数据是更新还是新增
     * @param listCartDb
     * @param cartInfo
     * @return
     */
    private boolean if_new_cart(List<CartInfo> listCartDb, CartInfo cartInfo){

        boolean b = true;

        //如果相同返回false
        for (CartInfo info : listCartDb) {
            if (info.getSkuId().equals(cartInfo.getSkuId())){
                b = false;
            }
        }
        return b;
    }
}
