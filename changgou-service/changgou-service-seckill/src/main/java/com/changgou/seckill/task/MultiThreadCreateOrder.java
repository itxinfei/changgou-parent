package com.changgou.seckill.task;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.dao.SeckillOrderMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.pojo.SeckillStatus;
import entity.IdWorker;
import io.swagger.models.auth.In;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @Author: Ye Jian Song
 * @Description:
 * @Date: Create in 15:37 2019/8/29
 */
@Component
public class MultiThreadCreateOrder {


    @Autowired(required = false)
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired(required = false)
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired(required = false)
    private IdWorker idWorker;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Environment env;

    /**
     * 异步请求处理
     */
    @Async
    public void CreateOrder(){
        // 多线程下单
        try {
            // 取出redis中的排队信息
          SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundListOps("SeckillOrderQueue").rightPop();
          // 判断列队中是否还有商品(储存所有的id)
            Object goods = redisTemplate.boundListOps("SeckillGoodsCountList_" + seckillStatus.getGoodsId()).rightPop();
            if (goods == null){
          // 该商品已经售罄
                // 删除重复排队信息
                redisTemplate.boundHashOps("UserQueueCount").delete();
                // 删除订单的状态信息
                redisTemplate.boundHashOps("UserQueueStatus").delete(seckillStatus.getUsername());
                return;
            }
            // 下单
          if (seckillStatus != null){
                String time = seckillStatus.getTime();
                Long seckillId = seckillStatus.getGoodsId();
                String username = seckillStatus.getUsername();

                // 1、获取商品信息
                SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_" + time).get(seckillId.toString());
                // 2、判断商品是否还有库存
                if (seckillGoods == null || seckillGoods.getStockCount() < 0){
                    throw new RuntimeException("对不起，该商品已售罄");
                }
                // 3、抢购订单并且将订单存储到redis中
                SeckillOrder seckillOrder = new SeckillOrder();
                seckillOrder.setId(idWorker.nextId());      // 订单id
                seckillOrder.setSeckillId(seckillId);       // 商品id
                seckillOrder.setUserId(username);           // 用户
                seckillOrder.setCreateTime(new Date());     // 下单时间
                seckillOrder.setMoney(seckillGoods.getCostPrice()); // 支付金额
                seckillOrder.setStatus("0");                // 订单状态：未支付
                redisTemplate.boundHashOps("SeckillOrder").put(username, seckillOrder);
                System.out.println("需要下单的订单号："+seckillOrder.getId());
                // 4、扣减库存(扣减一个库存)
                //seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
                // 扣减库存操作redis中的库存信息(获得redis中的商品库存数据)
                Integer stockCount = (Integer) redisTemplate.boundHashOps("SeckillGoodsCount").get(seckillId.toString());
                stockCount--;
              redisTemplate.boundHashOps("SeckillGoodsCount").put(seckillId.toString(), stockCount);
                // 重新设置库存
                seckillGoods.setStockCount(stockCount.intValue());
                // 5、更新秒杀商品的数据到redis
                // 判断扣减后的库存量是否小于等于0
                if (stockCount <= 0){
                    // 删除商品
                    redisTemplate.boundHashOps("SeckillGoods_" + time).delete(seckillId.toString());
                    // 更新数据库中该商品（防止定时任务加数据加载到redis）
                    seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
                }else {
                    // 更新商品数据
                    redisTemplate.boundHashOps("SeckillGoods_" + time).put(seckillId.toString(), seckillGoods);
                }

                //下单成功更新SeckillStatus的状态信息
                seckillStatus.setStatus(2); // 待支付
                seckillStatus.setOrderId(seckillOrder.getId()); // 订单id
                seckillStatus.setMoney(Float.valueOf(seckillOrder.getMoney()));  // 订单支付金额
                // 将用户状态下单数据存到redis中(代支付)
                redisTemplate.boundHashOps("UserQueueStatus").put(username,seckillStatus);
                sendTimerMessage(seckillStatus);
          }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 项死性队列发送消息
     * @param seckillStatus
     */
    public void sendTimerMessage(SeckillStatus seckillStatus){
        rabbitTemplate.convertAndSend(env.getProperty("mq.pay.queue.seckillordertimerdelay"), (Object) JSON.toJSONString(seckillStatus), new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setExpiration("10000");
                return message;
            }
        });
    }


}
