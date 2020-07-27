package com.changgou.seckillOrder;

import com.changgou.SeckillApplication;
import com.changgou.seckill.pojo.SeckillOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Author: Ye Jian Song
 * @Description:
 * @Date: Create in 15:29 2019/8/29
 */
@SpringBootTest(classes = SeckillApplication.class)
@RunWith(SpringRunner.class)
public class seckillOrder {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Environment env;

    @Test
    public void fun01(){
        //SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("SeckillOrder").get("zhangsan");
       // System.out.println(seckillOrder);

        //Long seckillGoodsCount = redisTemplate.boundHashOps("SeckillGoodsCount").increment("1131815363101724672", -1);
        Integer seckillGoodsCount1 = (Integer) redisTemplate.boundHashOps("SeckillGoodsCount").get("1131815363101724672");
        seckillGoodsCount1--;
        redisTemplate.boundHashOps("SeckillGoodsCount").put("1131815363101724672", seckillGoodsCount1);
        Integer seckillGoodsCount2 = (Integer) redisTemplate.boundHashOps("SeckillGoodsCount").get("1131815363101724672");
        System.out.println(seckillGoodsCount1);
        System.out.println(seckillGoodsCount2);
    }

    @Test
    public void fun02(){
        //SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("SeckillOrder").get("zhangsan");
        // System.out.println(seckillOrder);

        //Long seckillGoodsCount = redisTemplate.boundHashOps("SeckillGoodsCount").increment("1131815363101724672", -1);

        redisTemplate.boundHashOps("testIncre").put("test01", 300);
        redisTemplate.boundHashOps("testIncre").increment("test01",-1);
        System.out.println(redisTemplate.boundHashOps("testIncre").get("test01"));
    }


    @Test
    public void testSendMq(){
        rabbitTemplate.convertAndSend(env.getProperty("mq.pay.queue.seckillordertimerdelay"), (Object) "欢迎来到黑马程序员", new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setExpiration("10000");
                return message;
            }
        });
    }
}
