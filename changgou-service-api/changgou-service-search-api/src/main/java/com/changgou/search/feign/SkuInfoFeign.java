package com.changgou.search.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @Author: Ye Jian Song
 * @Description:
 * @Date: Create in 20:01 2019/8/19
 */
@FeignClient(name = "search")
@RequestMapping("/search")
public interface SkuInfoFeign {

    /**
     * 调用search中的controller中的条件检索所有数据的方法获得检索后的所有数据
     * @param searchMap
     * @return
     */
    @GetMapping
    Map<String, Object> search(@RequestParam(required = false) Map<String, String> searchMap);
}
