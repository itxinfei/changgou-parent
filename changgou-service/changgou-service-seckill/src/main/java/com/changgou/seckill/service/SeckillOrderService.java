package com.changgou.seckill.service;

import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.pojo.SeckillStatus;
import com.github.pagehelper.PageInfo;

import java.text.ParseException;
import java.util.List;

/****
 * @Author:shenkunlin
 * @Description:SeckillOrder业务层接口
 * @Date 2019/6/14 0:16
 *****/
public interface SeckillOrderService {


    /**
     * 关闭订单
     * @param username
     */
    void closeOrder(String username);

    /**
     * 支付失败删除订单
     * @param username
     */
    void deleteOrder(String username);


    /**
     * 支付成功修改订单
     * @param out_trade_no
     * @param transaction_id
     * @param username
     * @param time_end
     */
    void updateStatus(String out_trade_no, String transaction_id, String username, String time_end) throws ParseException;


    /**
     * 查询用户的下单状态信息
     * @param usernmae
     * @return
     */
    SeckillStatus queryStatus(String usernmae);


    /**
     * 用户对秒杀商品下单操作
     * @param time
     * @param seckillId
     * @param username
     * @return
     */
    Boolean add(String time ,Long seckillId,String username);



    /***
     * SeckillOrder多条件分页查询
     * @param seckillOrder
     * @param page
     * @param size
     * @return
     */
    PageInfo<SeckillOrder> findPage(SeckillOrder seckillOrder, int page, int size);

    /***
     * SeckillOrder分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<SeckillOrder> findPage(int page, int size);

    /***
     * SeckillOrder多条件搜索方法
     * @param seckillOrder
     * @return
     */
    List<SeckillOrder> findList(SeckillOrder seckillOrder);

    /***
     * 删除SeckillOrder
     * @param id
     */
    void delete(Long id);

    /***
     * 修改SeckillOrder数据
     * @param seckillOrder
     */
    void update(SeckillOrder seckillOrder);

    /***
     * 新增SeckillOrder
     * @param seckillOrder
     */
    void add(SeckillOrder seckillOrder);

    /**
     * 根据ID查询SeckillOrder
     * @param id
     * @return
     */
     SeckillOrder findById(Long id);

    /***
     * 查询所有SeckillOrder
     * @return
     */
    List<SeckillOrder> findAll();



}
