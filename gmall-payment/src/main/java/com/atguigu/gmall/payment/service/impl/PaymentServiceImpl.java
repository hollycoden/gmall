package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.bean.PaymentInfo;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.util.ActiveMQUtil;
import com.atguigu.service.PaymentService;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Autowired
    AlipayClient alipayClient;

    @Override
    public void savePayment(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public void updatePayment(PaymentInfo paymentInfo) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",paymentInfo.getOutTradeNo());
        paymentInfoMapper.updateByExampleSelective(paymentInfo,example);
    }


    /**
     * 发送支付成功消息队列，订单系统消费
     * @param out_trade_no
     * @param trackingNo
     */
    @Override
    public void sendPaymentSuccessQueue(String out_trade_no, String trackingNo) {
        //发送支付成功消息队列
        //队列模式的消息生产者
        Connection connection = activeMQUtil.getConnection();
        try {

            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);

            // 队列消息对象
            Queue testqueue = session.createQueue("PAYMENT_SUCCESS_QUEUE");
            MessageProducer producer = session.createProducer(testqueue);

            // 消息内容
            MapMessage mapMessage=new ActiveMQMapMessage();
            mapMessage.setString("outTradeNo",out_trade_no);
            mapMessage.setString("trackingNo",trackingNo);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);

            // 发出消息
            producer.send(mapMessage);
            session.commit();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }


    /**
     *
     * 延迟队列，定期检查支付结果，支付系统自己消费
     * @param outTradeNo 外部订单号
     * @param delaySec 延迟时间
     * @param checkCount 检查次数
     */
    @Override
    public void sendDelayPaymentResult(String outTradeNo,int delaySec,int checkCount) {

        //队列模式的消息生产者
        Connection connection = activeMQUtil.getConnection();
        try {
            connection.start();

            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);

            // 消息对象
            Queue paymentResultQueue = session.createQueue("PAYMENT_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(paymentResultQueue);

            // 消息内容
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            MapMessage mapMessage= new ActiveMQMapMessage();
            mapMessage.setString("outTradeNo",outTradeNo);
            mapMessage.setInt("delaySec",delaySec);
            mapMessage.setInt("checkCount",checkCount);

            //设置消息队列的执行计划方式—延迟队列
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,delaySec*10);

            // 发出消息
            producer.send(mapMessage);

            session.commit();
            producer.close();
            session.close();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    /**
     * 支付宝的支付状态查询
     * @param outTradeNo
     * @return
     */
    @Override
    public Map<String, String> checkAlipayPayment(String outTradeNo) {
        Map<String,String> returnMap = new HashMap<String,String>();

        // 支付宝的支付状态查询
        System.err.println("开始检查支付宝的支付状态。。第n次，返回支付结果");

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();

        Map<String,String> map = new HashMap<String,String>();
        map.put("out_trade_no",outTradeNo);
        request.setBizContent(JSON.toJSONString(map));

        //
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        if(response.isSuccess()){
            System.out.println("调用成功");
            String tradeNo = response.getTradeNo();// 支付宝的交易号
            String tradeStatus = response.getTradeStatus();//交易状态
            if(StringUtils.isNotBlank(tradeStatus)){
                returnMap.put("status",tradeStatus);
                returnMap.put("alipayTradeNo",response.getTradeNo());
                returnMap.put("callback",response.getMsg());
                return returnMap;
            }else{
                returnMap.put("status","fail");
                return returnMap;
            }
        } else {
            System.err.println("用户未创建交易");
            returnMap.put("status","fail");
            return returnMap;
        }
    }

    /**
     * 更新支付信息，并发送订单成功消息队列，由订单系统消费
     * @param paymentInfo
     */
    @Override
    public void updatePaymentSuccess(PaymentInfo paymentInfo) {
        // 更新支付信息
        updatePayment(paymentInfo);

        // 支付成功队列，通知订单系统
        sendPaymentSuccessQueue(paymentInfo.getOutTradeNo(),paymentInfo.getAlipayTradeNo());
    }


    /**
     * 支付状态的幂等性判断
     * @param outTradeNo
     * @return
     */
    @Override
    public boolean checkStatus(String outTradeNo) {
        boolean b = false;
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(outTradeNo);
        PaymentInfo paymentInfo1 = paymentInfoMapper.selectOne(paymentInfo);

        if (paymentInfo1.getPaymentStatus().equals("已支付")){
            b = true;
        }
        return b;
    }
}