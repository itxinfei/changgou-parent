package com.changgou.goods.feign;

import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 *
 */
@FeignClient(name = "goods")
@RequestMapping("/spu")
public interface SpuFeign {

    /**
     * 根据从库存商品对象中获得的spuId查询商品信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    Result<Spu> findById(@PathVariable(name = "id") Long id);

    /**
     * 根据条件搜索
     *
     * @param spuId
     * @return
     */
    @GetMapping("/findSkuListBySpuId/{spuId}")
    public List<Sku> findSkuListBySpuId(@PathVariable String spuId);

    Result findBySpuId(String spuId);
}
