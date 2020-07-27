package com.changgou.content.feign;
import com.changgou.content.pojo.Content;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/****
 * @Author:shenkunlin
 * @Description:
 * @Date 2019/6/18 13:58
 *****/
@FeignClient(name="content")
@RequestMapping("/content")
public interface ContentFeign {

    /**
     * 通过分类id拿到 广告列表
     * @param categoryId
     * @return
     */
    @GetMapping("list/{categoryId}")
    Result<List<Content>> list(@PathVariable(value = "categoryId") long categoryId);

}