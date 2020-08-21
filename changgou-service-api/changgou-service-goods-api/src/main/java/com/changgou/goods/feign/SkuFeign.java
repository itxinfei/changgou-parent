package com.changgou.goods.feign;

import com.changgou.goods.pojo.Sku;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 */
@FeignClient(name = "goods")
@RequestMapping("/sku")
public interface SkuFeign {


    /**
     * 更新库存
     *
     * @param username
     * @return
     */
    @GetMapping("/decr/{username}")
    Result decr(@PathVariable(value = "username") String username);

    /**
     * 根据spuId查询商品库存信息生成购物车商品信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    Result<Sku> findById(@PathVariable(value = "id") String id);


    /**
     * 调用商品微服务中的库存数据的接口查询出库存的数据
     *
     * @param status
     * @return
     */
    @GetMapping("/findSkusByStatus/{status}")
    Result<List<Sku>> findSkusByStatus(@PathVariable(name = "status") String status);

    /**
     * 条件查询Sku返回数据生成详情页面
     *
     * @param sku
     * @return
     */
    @PostMapping(value = "/search")
    Result<List<Sku>> findList(@RequestBody(required = false) Sku sku);

    /***
     * 根据SpuID查询Spu信息
     * @param id
     * @return
     */
    @GetMapping("/findBySpuId/{id}")
    public Result findBySpuId(@PathVariable String id);

    List<Sku> findSkuListBySpuId(String spuId);
}
