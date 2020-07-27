package com.changgou.canal.mq.send;

import com.alibaba.fastjson.JSON;
import entity.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: Ye Jian Song
 * @Description:
 * @Date: Create in 22:03 2019/8/21
 */
@Component
public class TopicMessageSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(Message message){
        rabbitTemplate.convertAndSend(message.getExechange(),
                message.getRoutekey(), JSON.toJSONString(message));
    }

}
