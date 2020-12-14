package com.changgou.pay.feign;

import com.changgou.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

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
