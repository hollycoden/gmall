package com.atguigu.gmall.payment.payMq;

import com.atguigu.bean.PaymentInfo;
import com.atguigu.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;
import java.util.Map;

@Component
public class PaymentCheckQueueListener {

    @Autowired
    PaymentService paymentService;

    /**
     *
     * 延迟队列消息的消费端
     * @param mapMessage
     * @throws JMSException
     */
    @JmsListener(destination = "PAYMENT_CHECK_QUEUE", containerFactory = "jmsQueueListener")
    public void consumeCheckResult(MapMessage mapMessage) throws JMSException {

        //检查次数
        int count = mapMessage.getInt("checkCount");
        int delaySec = mapMessage.getInt("delaySec");
        String outTradeNo = mapMessage.getString("outTradeNo");

        //调用支付宝检查接口，得到支付状态
        Map<String, String> statusMap = paymentService.checkAlipayPayment(outTradeNo);
        String status = statusMap.get("status");

        //根据支付情况决定是否调用支付成功队列，还是继续延迟检查
        if (status.equals("TRADE_SUCCESS")){

            //支付状态的幂等性
            boolean b = paymentService.checkStatus(outTradeNo);

            if (!b){
                //交易成功
                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setAlipayTradeNo(statusMap.get("alipayTradeNo"));
                paymentInfo.setPaymentStatus("已支付");
                paymentInfo.setCallbackTime(new Date());
                paymentInfo.setCallbackContent(statusMap.get("callback"));
                paymentInfo.setOutTradeNo(outTradeNo);

                //更新支付信息
                paymentService.updatePaymentSuccess(paymentInfo);
            } else {
                System.err.println("检查到该笔交易已经支付完毕，直接返回结果，消息队列任务结束");
            }



        } else {
            if (count>0){
                System.err.println("进行第"+(6-count)+"次检查订单"+outTradeNo+"的支付状态，");

                //继续发送延迟队列
                paymentService.sendDelayPaymentResult(outTradeNo,delaySec,count-1);
            } else {
                System.err.println("检查次数上限，用户在规定时间内没有支付");
            }
        }
    }
}
