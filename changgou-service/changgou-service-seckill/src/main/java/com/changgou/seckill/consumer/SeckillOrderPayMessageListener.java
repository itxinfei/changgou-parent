package com.changgou.seckill.consumer;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.service.SeckillOrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Map;

/**
 * @Author: Ye Jian Song
 * @Description:  监听mq队列的消息
 * @Date: Create in 19:55 2019/8/31
 */
@Component
@RabbitListener(queues = "${mq.pay.queue.seckillorder}")
public class SeckillOrderPayMessageListener {

    @Autowired
    private SeckillOrderService seckillOrderService;

    @Autowired
    private RedisTemplate redisTemplate;

    @RabbitHandler
    public void readseckillOrderMessage(String text) throws ParseException {
        Map<String, String> map = JSON.parseObject(text, Map.class);
        // 获取交易标识
        String return_code = map.get("return_code");
        if ("SUCCESS".equals(return_code)){
            // 获取用户名
            String attach = map.get("attach");
            Map<String, String> attachMap = JSON.parseObject(attach, Map.class);
            String username = attachMap.get("username");
            // 获取交易标识符
            String result_code = map.get("result_code");
            if ("SUCCESS".equals(result_code)){
            // 更新订单数据
                String out_trade_no = map.get("out_trade_no");
                String transaction_id = map.get("transaction_id");
                String time_end = map.get("time_end");
                seckillOrderService.updateStatus(out_trade_no,transaction_id,username,time_end);
            }else {
            // 删除订单
                seckillOrderService.deleteOrder(username);
            }
        }

    }
}
