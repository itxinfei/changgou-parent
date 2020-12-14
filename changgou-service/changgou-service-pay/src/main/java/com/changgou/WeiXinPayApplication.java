package com.changgou;

import com.changgou.entity.FeignInterceptor;
import com.changgou.entity.IdWorker;
import com.changgou.entity.TokenDecode;
import feign.RequestInterceptor;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 *
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableEurekaClient
public class WeiXinPayApplication {
    public static void main(String[] args) {
        SpringApplication.run(WeiXinPayApplication.class, args);
    }

    @Autowired
    private Environment env;

    // 创建队列
    @Bean
    public Queue orderQueue() {
        return new Queue(env.getProperty("mq.pay.queue.order"), true);
    }

    // 创建交换机
    @Bean
    public Exchange orderExchange() {
        return new DirectExchange(env.getProperty("mq.pay.exchange.order"), true, false);
    }

    // 队列交换机绑定
    @Bean
    public Binding bindQueueToExchange() {
        return BindingBuilder.bind(orderQueue()).to(orderExchange()).with(env.getProperty("mq.pay.routing.key")).noargs();
    }

    // ------------------------------------- 秒杀对列---------------------

    @Bean
    public Queue seckillQueue() {
        return new Queue(env.getProperty("mq.pay.queue.seckillorder"), true);
    }

    @Bean
    public Exchange seckillExchange() {
        return new DirectExchange(env.getProperty("mq.pay.exchange.seckillorder"), true, false);
    }

    @Bean
    public Binding bindingQueueToExchangeForSeckill() {
        return BindingBuilder.bind(seckillQueue()).to(seckillExchange()).with(env.getProperty("mq.pay.routing.seckillkey")).noargs();

    }

    @Bean
    public TokenDecode tokenDecode() {
        return new TokenDecode();
    }

    @Bean
    public IdWorker idWorker() {
        return new IdWorker(1, 1);
    }

    @Bean
    public RequestInterceptor feignInterceptor() {
        return new FeignInterceptor();
    }
}
