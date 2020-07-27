package com.changgou.rabbitmq.listener.item;

import com.alibaba.fastjson.JSON;
import com.changgou.item.feign.PageFeign;
import entity.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author: Ye Jian Song
 * @Description:
 * @Date: Create in 22:07 2019/8/21
 */
@Component
@RabbitListener(queues = "topic.queue.spu")
@RequestMapping("/page")
public class HtmlGeneratListener {

    @Autowired(required = false)
    private PageFeign pageFeign;


    /**
     * 更新数据生成静态页面
     * @param msg
     */
    @RabbitHandler
    public void getInfo(String msg){
        //将数据转成JSON
        Message message = JSON.parseObject(msg,Message.class);
        if (message.getCode() == 2){
            pageFeign.createHtml(Long.parseLong(message.getContent().toString()));
        }
    }
}
