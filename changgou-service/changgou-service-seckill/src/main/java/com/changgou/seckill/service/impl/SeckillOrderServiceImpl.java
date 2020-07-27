package com.changgou.seckill.service.impl;

import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.dao.SeckillOrderMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.pojo.SeckillStatus;
import com.changgou.seckill.service.SeckillOrderService;
import com.changgou.seckill.task.MultiThreadCreateOrder;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import entity.IdWorker;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/****
 * @Author:shenkunlin
 * @Description:SeckillOrder业务层接口实现类
 * @Date 2019/6/14 0:16
 *****/
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired(required = false)
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired(required = false)
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired(required = false)
    private IdWorker idWorker;

    @Autowired
    private MultiThreadCreateOrder multiThreadCreateOrder;


    /**
     *
     * 关闭订单
     * @param username
     */
    @Override
    public void closeOrder(String username) {
    deleteOrder(username);

    }

    /**
     * 支付失败删除订单
     * @param username
     */
    @Override
    public void deleteOrder(String username) {
    // 获取抢单状态以及订单信息
        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundHashOps("UserQueueStatus").get(username);
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("SeckillOrder").get(username);
        if (seckillOrder != null && seckillStatus != null){
            // 已经下单未支付,删除订单
            redisTemplate.boundHashOps("SeckillOrder").delete(username);
            // 回滚商品库存信息
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_"+seckillStatus.getTime()).get(seckillStatus.getGoodsId().toString());
        if (seckillGoods != null){
             seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillStatus.getGoodsId());
            // 将id添加大扣减库存的id队列中去
            Integer stockCont = (Integer) redisTemplate.boundHashOps("SeckillGoodsCount").get(seckillStatus.getGoodsId().toString());
            stockCont++;
            seckillGoods.setStockCount(stockCont.intValue());
            // 将回滚后的商品重新添加到redis
            redisTemplate.boundHashOps("SeckillGoods_"+seckillStatus.getTime()).put(seckillStatus.getGoodsId().toString(),seckillGoods);
            // 2-3、将商品id重新放入到队列中
            redisTemplate.boundListOps("SeckillGoodsCountList_" + seckillStatus.getGoodsId()).leftPush(seckillStatus.getGoodsId());
        }
            // 删除排队信息和抢购信息
            redisTemplate.boundHashOps("UserQueueCount").delete(username);
            redisTemplate.boundHashOps("UserQueueStatus").delete(username);
        }

    }

    /**
     * 支付成功修改订单
     * @param out_trade_no 商户订单号
     * @param transaction_id  微信支付订单号
     * @param username  用户
     * @param time_end  支付时间
     */
    @Override
    public void updateStatus(String out_trade_no, String transaction_id, String username, String time_end) throws ParseException {
        // 获得redis中的详单数据
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("SeckillOrder").get(username);
        if (seckillOrder != null){
            // 删除redis中的点单信息
            redisTemplate.boundHashOps("SeckillOrder").delete(username);
            // 更新订单状态
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            seckillOrder.setPayTime(dateFormat.parse(time_end));
            seckillOrder.setTransactionId(transaction_id);
            seckillOrder.setStatus("1");
            // 同步到msq中
            seckillOrderMapper.insertSelective(seckillOrder);
            // 删除redis中的排队信息，抢购状态
            redisTemplate.boundHashOps("UserQueueCount").delete(username);
            redisTemplate.boundHashOps("UserQueueStatus").delete(username);
        }
    }

    /**
     * 查询用户的下单状态信息
     * @param usernmae
     * @return
     */
    @Override
    public SeckillStatus queryStatus(String usernmae) {
       SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundHashOps("UserOrderQueue").get(usernmae);
        return seckillStatus;
    }

    /**
     * 秒杀商品下单操作
     * @param time
     * @param seckillId
     * @param username
     * @return
     */
    @Override
    public Boolean add(String time, Long seckillId, String username) {
        // 防止秒杀重复排队,登记用户，用户进来一次加一次，判断大于1时停止
       Long UserQueueCount = redisTemplate.boundHashOps("UserQueueCount").increment(username,1);
        if (UserQueueCount > 1){
            throw new RuntimeException("抱歉，不可以重复下单,请先去支付！！！！");
        }

        // 让用户排队下单.创建一个pojo，存放用户信息
        SeckillStatus seckillStatus = new SeckillStatus(username,new Date(),1,seckillId,time);
        redisTemplate.boundListOps("SeckillOrderQueue").leftPush(seckillStatus);

        //下单成功更新用户支付状态(排队中)
        redisTemplate.boundHashOps("UserQueueStatus").put(username,seckillStatus);

        // 多线程异步下单
        multiThreadCreateOrder.CreateOrder();

        // TODO 处理其他业务
        // 1、获取商品信息
//        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_" + time).get(seckillId.toString());
//        // 2、判断商品是否还有库存
//        if (seckillGoods == null || seckillGoods.getStockCount() < 0){
//            throw new RuntimeException("对不起，该商品已售罄");
//        }
//        // 3、抢购订单并且将订单存储到redis中
//        SeckillOrder seckillOrder = new SeckillOrder();
//        seckillOrder.setId(idWorker.nextId());      // 订单id
//        seckillOrder.setSeckillId(seckillId);       // 商品id
//        seckillOrder.setUserId(username);           // 用户
//        seckillOrder.setCreateTime(new Date());     // 下单时间
//        seckillOrder.setMoney(seckillGoods.getCostPrice()); // 支付金额
//        seckillOrder.setStatus("0");                // 订单状态：未支付
//        redisTemplate.boundHashOps("SeckillOrder").put(username, seckillOrder);
//        // 4、扣减库存(扣减一个库存)
//        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
//        // 更新msq中的库存
//        seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
//        // 添加订单表中的数据
//        seckillOrderMapper.insertSelective(seckillOrder);
//        // 5、更新秒杀商品的数据到redis
//        // 判断扣减后的库存量是否小于等于0
//        if (seckillGoods.getStockCount() <= 0){
//            // 删除商品
//            redisTemplate.boundHashOps("SeckillGoods_" + time).delete(seckillId.toString());
//            // 更新数据库中该商品（防止定时任务加数据加载到redis）
//            seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
//        }else {
//            // 更新商品数据
//            redisTemplate.boundHashOps("SeckillGoods_" + time).put(seckillId.toString(), seckillGoods);
//        }

        return true;
    }

    /**
     * SeckillOrder条件+分页查询
     * @param seckillOrder 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<SeckillOrder> findPage(SeckillOrder seckillOrder, int page, int size){
        //分页
        PageHelper.startPage(page,size);
        //搜索条件构建
        Example example = createExample(seckillOrder);
        //执行搜索
        return new PageInfo<SeckillOrder>(seckillOrderMapper.selectByExample(example));
    }

    /**
     * SeckillOrder分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<SeckillOrder> findPage(int page, int size){
        //静态分页
        PageHelper.startPage(page,size);
        //分页查询
        return new PageInfo<SeckillOrder>(seckillOrderMapper.selectAll());
    }

    /**
     * SeckillOrder条件查询
     * @param seckillOrder
     * @return
     */
    @Override
    public List<SeckillOrder> findList(SeckillOrder seckillOrder){
        //构建查询条件
        Example example = createExample(seckillOrder);
        //根据构建的条件查询数据
        return seckillOrderMapper.selectByExample(example);
    }


    /**
     * SeckillOrder构建查询对象
     * @param seckillOrder
     * @return
     */
    public Example createExample(SeckillOrder seckillOrder){
        Example example=new Example(SeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();
        if(seckillOrder!=null){
            // 主键
            if(!StringUtils.isEmpty(seckillOrder.getId())){
                    criteria.andEqualTo("id",seckillOrder.getId());
            }
            // 秒杀商品ID
            if(!StringUtils.isEmpty(seckillOrder.getSeckillId())){
                    criteria.andEqualTo("seckillId",seckillOrder.getSeckillId());
            }
            // 支付金额
            if(!StringUtils.isEmpty(seckillOrder.getMoney())){
                    criteria.andEqualTo("money",seckillOrder.getMoney());
            }
            // 用户
            if(!StringUtils.isEmpty(seckillOrder.getUserId())){
                    criteria.andEqualTo("userId",seckillOrder.getUserId());
            }
            // 创建时间
            if(!StringUtils.isEmpty(seckillOrder.getCreateTime())){
                    criteria.andEqualTo("createTime",seckillOrder.getCreateTime());
            }
            // 支付时间
            if(!StringUtils.isEmpty(seckillOrder.getPayTime())){
                    criteria.andEqualTo("payTime",seckillOrder.getPayTime());
            }
            // 状态，0未支付，1已支付
            if(!StringUtils.isEmpty(seckillOrder.getStatus())){
                    criteria.andEqualTo("status",seckillOrder.getStatus());
            }
            // 收货人地址
            if(!StringUtils.isEmpty(seckillOrder.getReceiverAddress())){
                    criteria.andEqualTo("receiverAddress",seckillOrder.getReceiverAddress());
            }
            // 收货人电话
            if(!StringUtils.isEmpty(seckillOrder.getReceiverMobile())){
                    criteria.andEqualTo("receiverMobile",seckillOrder.getReceiverMobile());
            }
            // 收货人
            if(!StringUtils.isEmpty(seckillOrder.getReceiver())){
                    criteria.andEqualTo("receiver",seckillOrder.getReceiver());
            }
            // 交易流水
            if(!StringUtils.isEmpty(seckillOrder.getTransactionId())){
                    criteria.andEqualTo("transactionId",seckillOrder.getTransactionId());
            }
        }
        return example;
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(Long id){
        seckillOrderMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改SeckillOrder
     * @param seckillOrder
     */
    @Override
    public void update(SeckillOrder seckillOrder){
        seckillOrderMapper.updateByPrimaryKey(seckillOrder);
    }

    /**
     * 增加SeckillOrder
     * @param seckillOrder
     */
    @Override
    public void add(SeckillOrder seckillOrder){
        seckillOrderMapper.insert(seckillOrder);
    }

    /**
     * 根据ID查询SeckillOrder
     * @param id
     * @return
     */
    @Override
    public SeckillOrder findById(Long id){
        return  seckillOrderMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询SeckillOrder全部数据
     * @return
     */
    @Override
    public List<SeckillOrder> findAll() {
        return seckillOrderMapper.selectAll();
    }
}
