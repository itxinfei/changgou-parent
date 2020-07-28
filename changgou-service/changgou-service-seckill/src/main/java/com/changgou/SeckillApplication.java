package com.changgou;

import entity.FeignInterceptor;
import entity.IdWorker;
import entity.TokenDecode;
import feign.RequestInterceptor;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * 秒杀服务
 */
@SpringBootApplication
@EnableEurekaClient
@MapperScan(basePackages = {"com.changgou.seckill.dao"})
@EnableScheduling   // 开启定时任务
@EnableAsync        // 开启异步请求
@EnableFeignClients(basePackages = "com.changgou.pay.feign")
public class SeckillApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeckillApplication.class, args);
    }

    @Bean
    public IdWorker idWorker() {
        return new IdWorker(1, 1);
    }

    @Bean
    public TokenDecode tokenDecode() {
        return new TokenDecode();
    }

    @Autowired
    public Environment env;

    @Bean
    public RequestInterceptor feignInterceptor() {
        return new FeignInterceptor();
    }


    // -------------------------------------取消支付--------------------------------------
    // 队列
  /*  @Bean(name = "seckillordertimer")
    public Queue seckillOrderTimerQueue(){
        return new Queue("queue.seckillordertimer",true);
    }*/

    @Bean(name = "seckillOrderTimerQueue")
    public Queue seckillOrderTimerQueue() {
        return new Queue(env.getProperty("mq.pay.queue.seckillordertimer"), true);
    }

    /*// 创建延时队列
    @Bean(name = "seckillordertimerdelay")
    public Queue delaySeckillOrderTimerQueue(){
        return QueueBuilder.durable("queue.seckillordertimerdelay")
                .withArgument("x-dead-letter-exchange","exchange.seckillorder")
                .withArgument("x-dead-letter-routing-key","seckillorderkey").build();
    }*/

    // 创建延时队列
    @Bean(name = "delaySeckillOrderTimerQueue")
    public Queue delaySeckillOrderTimerQueue() {
        return QueueBuilder.durable(env.getProperty("mq.pay.queue.seckillordertimerdelay"))
                .withArgument("x-dead-letter-exchange", env.getProperty("mq.pay.exchange.order"))
                .withArgument("x-dead-letter-routing-key", env.getProperty("mq.pay.queue.seckillordertimer")).build();
    }


    // 创建交换机
    @Bean(name = "delayExchange")
    public Exchange delayExchange() {
        //return new DirectExchange("exchange.seckillorder",true, false);
        return new DirectExchange(env.getProperty("mq.pay.exchange.order"), true, false);
    }

    // 绑定交换机和队列
    @Bean(name = "basicBinding")
    public Binding basicBinding(Queue seckillOrderTimerQueue, Exchange delayExchange) {
        return BindingBuilder.bind(seckillOrderTimerQueue)
                .to(delayExchange)
                .with(env.getProperty("mq.pay.queue.seckillordertimer")).noargs();
    }


}
