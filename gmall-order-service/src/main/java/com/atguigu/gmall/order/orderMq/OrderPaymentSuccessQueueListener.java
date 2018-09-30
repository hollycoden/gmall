package com.atguigu.gmall.order.orderMq;

import com.atguigu.bean.OrderInfo;
import com.atguigu.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@Component
public class OrderPaymentSuccessQueueListener {

    @Autowired
    OrderService orderService;

    /**
     * PAYMENT_SUCCESS_QUEUE 的消费端
     * @param mapMessage
     * @throws JMSException
     */
    @JmsListener(destination = "PAYMENT_SUCCESS_QUEUE",containerFactory = "jmsQueueListener")
    public void consumePaymentResult(MapMessage mapMessage) throws JMSException {
        String outTradeNo = mapMessage.getString("outTradeNo");
        String trackingNo = mapMessage.getString("trackingNo");
        System.err.println(outTradeNo+"该订单已经支付成功，根据这个消息，进行订单的后续业务");

        // 订单消费支付消息的业务
        // 订单状态、支付方式、预计送达时间、支付宝交易号、整体状态
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setTrackingNo(trackingNo);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE,3);
        orderInfo.setExpectDeliveryTime(c.getTime());
        orderInfo.setOrderStatus("订单已支付");
        orderInfo.setProcessStatus("订单已支付");
        OrderInfo orderInfo1 = orderService.updateOrder(orderInfo);

        //发送订单状态通知,由库存系统来消费
        orderService.sendOrderResultQueue(orderInfo1);

    }
}