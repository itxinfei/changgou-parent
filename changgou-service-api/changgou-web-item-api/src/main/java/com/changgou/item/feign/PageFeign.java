package com.changgou.item.feign;

import com.changgou.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "item")
@RequestMapping("/page")
public interface PageFeign {

    @GetMapping("/createHtml/{id}")
    Result createHtml(@PathVariable(name = "id") Long id);
}
