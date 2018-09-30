package com.atguigu.service;

import com.atguigu.bean.OrderInfo;

public interface OrderService {
    void saveOrder(OrderInfo orderInfo);

    String genTradeCode(String userId);

    boolean checkTradeCode(String userId, String tradeCode);

    OrderInfo getOrderInfoByOutTradeNo(String outTradeNo);

    OrderInfo updateOrder(OrderInfo orderInfo);

    void sendOrderResultQueue(OrderInfo orderInfo);
}
