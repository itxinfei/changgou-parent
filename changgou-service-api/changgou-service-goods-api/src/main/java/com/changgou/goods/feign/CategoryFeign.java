package com.changgou.goods.feign;

import com.changgou.goods.pojo.Category;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
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

    /***
     * 根据ID查询数据
     * @param id
     * @return
     */
    @GetMapping("/findCategoryById/{id}")
    public Category findCategoryById(@PathVariable(name = "id") Integer id);
}
