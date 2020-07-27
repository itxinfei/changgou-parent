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
 * @Description:
 * @Date: Create in 19:44 2019/8/31
 */
@SpringBootTest(classes = WeiXinPayApplication.class)
@RunWith(SpringRunner.class)
public class TestSeckillSend {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testSeckillSend(){
        rabbitTemplate.convertAndSend("exchange.seckillorder","queue.seckillorder","rest");
    }


    @Test
    public void testConsumer() {
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
