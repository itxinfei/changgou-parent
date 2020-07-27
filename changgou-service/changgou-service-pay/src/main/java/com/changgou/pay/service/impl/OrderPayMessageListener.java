package com.changgou.pay.service.impl;


import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


/**
 * @Author: Ye Jian Song
 * @Description: 处理错误消息
 * @Date: Create in 17:16 2019/8/28
 */
//@Component
//@RabbitListener(queues = "${mq.pay.queue.order}")
//@RabbitListener(queues = "${mq.pay.queue.seckillorder}")
//@RabbitListener(queues = "${mq.pay.queue.seckillordertimer}")
public class OrderPayMessageListener {


    @RabbitHandler
    public void readMessage(String text){
        System.out.println(text);
    }
}
