package com.atguigu.service;

import com.atguigu.bean.PaymentInfo;

import java.util.Map;

public interface PaymentService {
    void savePayment(PaymentInfo paymentInfo);

    void updatePayment(PaymentInfo paymentInfo);

    void sendPaymentSuccessQueue(String out_trade_no, String alipayTradeNo);

    void sendDelayPaymentResult(String outTradeNo, int delaySec, int checkCount);

    Map<String, String> checkAlipayPayment(String outTradeNo);

    void updatePaymentSuccess(PaymentInfo paymentInfo);

    boolean checkStatus(String outTradeNo);
}
