package com.changgou.seckill.consumer;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.Result;
import com.changgou.pay.feign.WeixinPayFeign;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.pojo.SeckillStatus;
import com.changgou.seckill.service.SeckillOrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Author pangzhao
 */
@Component
@RabbitListener(queues = "${mq.pay.queue.seckillordertimer}")
public class SeckillOrderDelayMessageListener {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillOrderService seckillOrderService;

    @Autowired(required = false)
    private WeixinPayFeign weixinPayFeign;


    @RabbitHandler
    public void consumeMessage(String message) {
        // 读取消息
        SeckillStatus seckillStatus = JSON.parseObject(message, SeckillStatus.class);
        // redis 中的订单消息
        String username = seckillStatus.getUsername();
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("SeckillOrder").get(username);
        // 如果redis 中有订单消息,说明用户未支付
        if (seckillOrder != null) {
            System.out.println("用户未支付,准备回滚！！！");
            // 关闭支付
            Result result = weixinPayFeign.closePay(seckillStatus.getOrderId());
            Map<String, String> closeMap = (Map<String, String>) result.getData();
            if (closeMap != null
                    && "SUCCESS".equalsIgnoreCase(closeMap.get("return_code"))
                    && "SUCCESS".equalsIgnoreCase(closeMap.get("result_code"))) {
                // 关闭订单
                seckillOrderService.deleteOrder(username);
            }

        }

    }


}
