package com.changgou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @Author: Ye Jian Song
 * @Description:
 * @Date: Create in 20:51 2019/8/20
 */
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients(basePackages = {"com.changgou.goods.feign"})
public class WebItemApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebItemApplication.class,args);
    }
}
