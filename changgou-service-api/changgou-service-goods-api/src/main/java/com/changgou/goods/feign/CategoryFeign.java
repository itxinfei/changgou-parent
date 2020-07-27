package com.changgou.goods.feign;

import com.changgou.goods.pojo.Category;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author: Ye Jian Song
 * @Description:
 * @Date: Create in 20:58 2019/8/20
 */
@FeignClient(name = "goods")
@RequestMapping("/category")
public interface CategoryFeign {

    /**
     * 获取分类的商品信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
     Result<Category> findById(@PathVariable(name = "id") Integer id);
}
