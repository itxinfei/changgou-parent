package com.changgou.order.service.impl;

import com.changgou.entity.Result;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: Ye Jian Song
 * @Description:
 * @Date: Create in 21:49 2019/8/24
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired(required = false)
    private SkuFeign skuFeign;

    @Autowired(required = false)
    private SpuFeign spuFeign;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 添加购物车 商品加入购物车
     *
     * @param skuId
     * @param num
     */
    @Override
    public void add(Long skuId, Integer num, String username) {
        if (num <= 0) {
            redisTemplate.boundHashOps("cart_" + username).delete(skuId);
            return;
        }

        // 获得商品库存信息
        //Result<Sku> skuResult = skuFeign.findById(skuId.toString());
        /*if (skuResult != null && skuResult.getData() != null) {
            //获取sku
            Sku sku = skuResult.getData();
            // 获得spu数据
            Spu spu = spuFeign.findById(sku.getSpuId()).getData();
            // 将商品存放到购物车
            OrderItem orderItem = goodsToOrderItem(sku, spu, num);
            // 将商品存到redis中
            redisTemplate.boundHashOps("cart_" + username).put(skuId, orderItem);
            //redisTemplate.opsForHash().get("cart_ "+ username,skuId);
        }*/
    }


    /**
     * 获得购物车的数据回显
     *
     * @param
     * @return
     */
    @Override
    public List<OrderItem> list(String username) {
        List<OrderItem> list = redisTemplate.boundHashOps("cart_" + username).values();
        return list;
    }

    /**
     * 删除购物车商品
     *
     * @param skuId
     */
    @Override
    public void delete(Long skuId, String username) {
        redisTemplate.boundHashOps("cart_" + username).delete(skuId);
    }


    /**
     * 将商品信息封装OrderItem对象
     *
     * @param sku
     * @param spu
     * @param num
     * @return
     */
    private OrderItem goodsToOrderItem(Sku sku, Spu spu, Integer num) {
        OrderItem orderItem = new OrderItem();
        orderItem.setCategoryId1(spu.getCategory1Id());
        orderItem.setCategoryId2(spu.getCategory2Id());
        orderItem.setCategoryId3(spu.getCategory3Id());
        orderItem.setSkuId(sku.getId());
        orderItem.setPayMoney(num * sku.getPrice());
        orderItem.setSpuId(spu.getId());
        orderItem.setName(sku.getName());
        orderItem.setPrice(sku.getPrice());
        orderItem.setNum(num);
        orderItem.setMoney(num * sku.getPrice());
        orderItem.setImage(sku.getImage());
        orderItem.setWeight(sku.getWeight());
        orderItem.setIsReturn("0");
        return orderItem;
    }
}
