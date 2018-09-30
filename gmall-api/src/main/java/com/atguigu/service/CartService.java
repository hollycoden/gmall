package com.atguigu.service;

import com.atguigu.bean.CartInfo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CartService {
    CartInfo ifCartExist(CartInfo cartInfo);

    void updateCart(CartInfo cartInfoDb);

    void insertCart(CartInfo cartInfo);

    void flushCartCacheByUserId(String userId);

    List<CartInfo> getCartInfosFromCacheByUserId(String userId);

    void updateCartByUserId(CartInfo cartInfo);

    void combine(String userId, List<CartInfo> parseArray);

    void deleteCart(String join, String userId);
}
