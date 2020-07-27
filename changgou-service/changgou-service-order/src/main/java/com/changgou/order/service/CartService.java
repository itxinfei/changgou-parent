package com.changgou.order.service;


import com.changgou.order.pojo.OrderItem;

import java.util.List;

/**
 * @Author: Ye Jian Song
 * @Description: 购物车服务接口
 * @Date: Create in 21:49 2019/8/24
 */
public interface CartService {

    /**
     * 添加购物车
     * @param skuId
     * @param num
     */
    void add(Long skuId, Integer num,String username);


    /**
     * 获得购物车的数据
     * @param username
     * @return
     */
    List<OrderItem> list(String username);


    /**
     * 删除购物车商品
     * @param skuId
     */
    void delete(Long skuId, String username);
}
