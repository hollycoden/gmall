package com.atguigu.gmall.payment.testMq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

public class BossTopicProducer {
    public static void main(String[] args) {
        //消息模式的消息生产者

        ConnectionFactory connect = new ActiveMQConnectionFactory("tcp://192.168.217.130:61616");
        try {
            Connection connection = connect.createConnection();
            connection.start();

            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);

            //生成一个消息queue对象，并命名
            Topic topic = session.createTopic("TEST2");
            MessageProducer producer = session.createProducer(topic);

            //消息内容
            TextMessage textMessage=new ActiveMQTextMessage();
            textMessage.setText("今晚月色不错！");
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);  //投递模式，默认是持久化的

            //发出消息
            producer.send(textMessage);
            session.commit();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
