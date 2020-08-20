package com.changgou.listener;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
@RabbitListener(queues = "search_add_queue")
public class SpuAddListener {

    @Autowired
    private EsManagerService esManagerService;

    @RabbitHandler
    public void addDataToES(String spuId) {
        System.out.println("===接收到需要商品上架的spuId为======" + spuId);
        esManagerService.importDataToESBySpuId(spuId);
    }
}
