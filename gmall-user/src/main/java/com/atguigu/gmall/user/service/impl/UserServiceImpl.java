package com.atguigu.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.bean.UserAddress;
import com.atguigu.bean.UserInfo;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.util.RedisUtil;
import com.atguigu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserInfoMapper userInfoMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    UserAddressMapper userAddressMapper;


    @Override
    public List<UserInfo> getUserList() {
        List<UserInfo> userInfoList = userInfoMapper.selectAll();
        return userInfoList;
    }

    /**
     * 验证该用户是否存在
     * @param userInfo
     * @return
     */
    @Override
    public UserInfo login(UserInfo userInfo) {
        UserInfo user = userInfoMapper.selectOne(userInfo);

        if (null != user){

            //存储到redis中
            Jedis jedis = redisUtil.getJedis();
            jedis.setex("user:"+user.getId()+":info",1000*60*60*24, JSON.toJSONString(userInfo));
            jedis.close();
        }
        return user;
    }


    @Override
    public List<UserAddress> getAddressListByUserId(String userId) {

        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);

        List<UserAddress> select = userAddressMapper.select(userAddress);

        return select;
    }

    @Override
    public UserAddress getAddressById(String addressId) {
        UserAddress userAddress = userAddressMapper.selectByPrimaryKey(addressId);
        return userAddress;
    }

    @Override
    public UserInfo getUserById(String userId) {
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userId);
        return userInfo;
    }

}
