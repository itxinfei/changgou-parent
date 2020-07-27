package com.changgou.order.controller;

import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import entity.Result;
import entity.StatusCode;
import entity.TokenDecode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: Ye Jian Song
 * @Description:
 * @Date: Create in 22:10 2019/8/24
 */
@RestController
@RequestMapping("/cart")
@CrossOrigin
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private TokenDecode tokenDecode;

    /**
     * 删除购物车商品
     * @param skuId
     * @return
     */
    @DeleteMapping("/delete/{skuId}")
    public Result delete(@PathVariable(value = "skuId") Long skuId){
        String username = tokenDecode.getUserInfo().get("username");
        cartService.delete(skuId,username);
        return new Result(true,StatusCode.OK,"删除商品成功！！");
    }


    /**
     * 商品加入购物车
     * @param id
     * @param num
     * @return
     */
    @RequestMapping("/add")
    public Result add(Long id, Integer num){
        String username = tokenDecode.getUserInfo().get("username");
        cartService.add(id,num,username);
        return new Result(true, StatusCode.OK,"商品已加入购物车");
    }


    /**
     * 查询购物车数据
     * @return
     */
    @GetMapping("list")
     public Result<List<OrderItem>> list(){
        String username = tokenDecode.getUserInfo().get("username");
        List<OrderItem> list = cartService.list(username);
        return new Result<>(true,StatusCode.OK,"查询购物车列表数据成功",list);
    }


}
