package com.changgou.pay.feign;

import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author: Ye Jian Song
 * @Description:
 * @Date: Create in 20:59 2019/9/1
 */
@FeignClient(name = "pay")
@RequestMapping("/weixin/pay")
public interface WeixinPayFeign {

   /* @RequestMapping("/closePay")
    Result closePay(@RequestParam Long orderId);

    @RequestMapping("/status/query")
    Result queryStatus(String outtradeno);*/

    @GetMapping("/closePay/{orderId}")
    Result closePay(@PathVariable("orderId") Long orderId);



}
