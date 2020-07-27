package com.changgou.seckill.feign;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/****
 * @Author:shenkunlin
 * @Description:
 * @Date 2019/6/18 13:58
 *****/
@FeignClient(name="seckill")
@RequestMapping("/seckillOrder")
public interface SeckillOrderFeign {


}