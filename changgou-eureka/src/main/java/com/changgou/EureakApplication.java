package com.changgou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @Author: Ye Jian Song
 * @Description:
 * @Date: Create in 20:15 2019/8/10
 */
@SpringBootApplication
@EnableEurekaServer
public class EureakApplication {
    public static void main(String[] args) {
        SpringApplication.run(EureakApplication.class,args);
    }
}
