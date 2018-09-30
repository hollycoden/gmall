package com.atguigu.service;

import com.atguigu.bean.UserAddress;
import com.atguigu.bean.UserInfo;

import java.util.List;

public interface UserService {
    public List<UserInfo> getUserList();

    UserInfo login(UserInfo userInfo);

    List<UserAddress> getAddressListByUserId(String userId);

    UserAddress getAddressById(String addressId);

    UserInfo getUserById(String userId);
}
