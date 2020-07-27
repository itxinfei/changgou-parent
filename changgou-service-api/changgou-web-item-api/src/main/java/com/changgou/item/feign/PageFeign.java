package com.changgou.item.feign;

import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author: Ye Jian Song
 * @Description:
 * @Date: Create in 21:52 2019/8/21
 */
@FeignClient(name = "item")
@RequestMapping("/page")
public interface PageFeign {

    @GetMapping("/createHtml/{id}")
    Result createHtml(@PathVariable(name = "id") Long id);
}
