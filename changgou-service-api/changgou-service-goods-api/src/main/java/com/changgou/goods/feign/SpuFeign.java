package com.changgou.goods.feign;

import com.changgou.goods.pojo.Spu;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author: Ye Jian Song
 * @Description:
 * @Date: Create in 21:08 2019/8/20
 */
@FeignClient(name = "goods")
@RequestMapping("/spu")
public interface SpuFeign {

    /**
     * 根据从库存商品对象中获得的spuId查询商品信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    Result<Spu> findById(@PathVariable(name = "id") Long id);


}
