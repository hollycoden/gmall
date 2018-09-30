package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.bean.OrderDetail;
import com.atguigu.bean.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.util.ActiveMQUtil;
import com.atguigu.gmall.util.RedisUtil;
import com.atguigu.service.OrderService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.List;
import java.util.UUID;

@Service
public class orderServiceImpl implements OrderService {

    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Override
    public void saveOrder(OrderInfo orderInfo) {

        //插入orderInfo到db数据库
        orderInfoMapper.insertSelective(orderInfo);

        String orderId = orderInfo.getId();

        //根据orderId插入订单详情表
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();

        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderId);
            orderDetailMapper.insertSelective(orderDetail);
        }
    }

    @Override
    public String genTradeCode(String userId) {

        String uuid = UUID.randomUUID().toString();
        Jedis jedis = redisUtil.getJedis();
        jedis.setex("user:"+userId+":tradeCode",1000*60*15,uuid);
        jedis.close();
        return uuid;
    }

    @Override
    public boolean checkTradeCode(String userId, String tradeCode) {

        boolean b = false;
        Jedis jedis = redisUtil.getJedis();
        String tradeCodeRedis = jedis.get("user:" + userId + ":tradeCode");
        if (null != tradeCodeRedis && tradeCode.equals(tradeCodeRedis)){
            b = true;
            jedis.del("user:"+userId+":tradeCode");
        }
        jedis.close();
        return b;
    }

    @Override
    public OrderInfo getOrderInfoByOutTradeNo(String outTradeNo) {

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOutTradeNo(outTradeNo);
        OrderInfo orderInfo1 = orderInfoMapper.selectOne(orderInfo);

        String orderID = orderInfo1.getId();

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderID);
        List<OrderDetail> orderDetails = orderDetailMapper.select(orderDetail);

        orderInfo1.setOrderDetailList(orderDetails);

        return orderInfo1;
    }

    @Override
    public OrderInfo updateOrder(OrderInfo orderInfo) {
        Example example = new Example(OrderInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",orderInfo.getOutTradeNo());
        orderInfoMapper.updateByExampleSelective(orderInfo,example);

        return getOrderInfoByOutTradeNo(orderInfo.getOutTradeNo());

    }

    /**
     * 订单结果消息队列，库存系统来消费
     * @param orderInfo
     */
    @Override
    public void sendOrderResultQueue(OrderInfo orderInfo) {
        Connection connection = activeMQUtil.getConnection();
        try {

            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);

            // 队列消息对象
            Queue testqueue = session.createQueue("ORDER_RESULT_QUEUE");
            MessageProducer producer = session.createProducer(testqueue);

            // 消息内容
            TextMessage textMessage = new ActiveMQTextMessage();
            textMessage.setText(JSON.toJSONString(orderInfo));

            producer.setDeliveryMode(DeliveryMode.PERSISTENT);

            // 发出消息
            producer.send(textMessage);
            session.commit();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
