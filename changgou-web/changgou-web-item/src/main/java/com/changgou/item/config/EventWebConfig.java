package com.changgou.item.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author: Ye Jian Song
 * @Description:
 * @Date: Create in 18:41 2019/8/21
 */
@Configuration
@ControllerAdvice
public class EventWebConfig implements WebMvcConfigurer {

    /**
     * 对templates模板下的静态资源放行
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/items/**")
                .addResourceLocations("classpath:templates/items/");
    }
}
