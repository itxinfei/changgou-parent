package com.changgou.order.consumer;

import com.alibaba.fastjson.JSON;
import com.changgou.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Author: Ye Jian Song
 * @Description: 监听订单的支付消息
 * @Date: Create in 17:16 2019/8/28
 */
@Component
@RabbitListener(queues = "${mq.pay.queue.order}")
public class OrderPayMessageListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void readMessage(String text){
        // 消息 支付消息
        Map<String, String> map = JSON.parseObject(text, Map.class);
        // 业务实现,更新订单状态
        // 通信状态
        String return_code = map.get("return_code");
        String orderId = map.get("transaction_id");
        if ("SUCCESS".equals(return_code)){
            // 业务状态(成功OR失败)
            String result_code = map.get("return_code");
            if ("SUCCESS".equals(result_code)){
                    if (orderId != null){
                        orderService.updateStatus(map.get("out_trade_no"),map.get("transaction_id"));
                    }
            }else {
                if (orderId != null){
                    orderService.deleteOrder(map.get("out_trade_no"));
                }
            }
        }
    }
}
