package com.mq;

import com.changgou.WeiXinPayApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

/**
 * @Author: Ye Jian Song
 * @Description:  消费错误队列消息测试
 * @Date: Create in 19:15 2019/8/28
 */
@SpringBootTest(classes = WeiXinPayApplication.class)
@RunWith(SpringRunner.class)
public class TestSend {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testSend(){
        rabbitTemplate.convertAndSend("exchange.order","queue.order","rest");
    }

    @Test
    public void testConsumer(){
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
