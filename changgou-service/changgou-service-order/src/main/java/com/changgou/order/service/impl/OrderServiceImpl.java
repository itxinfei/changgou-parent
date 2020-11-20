package com.changgou.order.service.impl;

import com.changgou.entity.IdWorker;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.order.dao.OrderItemMapper;
import com.changgou.order.dao.OrderMapper;
import com.changgou.order.pojo.Order;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import com.changgou.order.service.OrderService;
import com.changgou.user.feign.UserFeign;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

/****
 * @Author:shenkunlin
 * @Description:Order业务层接口实现类
 * @Date 2019/6/14 0:16
 *****/
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired(required = false)
    private OrderMapper orderMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired(required = false)
    private OrderItemMapper orderItemMapper;

    @Autowired
    private CartService cartService;

    @Autowired(required = false)
    private RedisTemplate redisTemplate;


    @Autowired(required = false)
    private SkuFeign skuFeign;

    @Autowired(required = false)
    private UserFeign userFeign;


    /**
     * 支付失败更新订单
     * @param out_trade_no
     */
    @Override
    public void deleteOrder(String out_trade_no) {
        // 从redis中去取
        Order order = (Order) redisTemplate.boundHashOps("Order").get(out_trade_no);
        if (order == null){
        // 如果redis中没有就去msq去取
            order = orderMapper.selectByPrimaryKey(out_trade_no);
        }
        order.setOrderStatus("2");          // 订单状态失败
        order.setUpdateTime(new Date());    // 更新订单时间
        order.setPayTime(new Date());       // 支付失败时间
        // 更新支付失败订单状态
        orderMapper.updateByPrimaryKeySelective(order);
        // 支付失败删除订单数据
        redisTemplate.boundHashOps("Order").delete(out_trade_no);
    }

    /**
     * 交易成功更新订单
     * @param out_trade_no 订单号
     * @param transactionId 交易流水号
     */
    @Override
    public void updateStatus(String out_trade_no, String transactionId) {
        // 从redis中去取
        Order order = (Order) redisTemplate.boundHashOps("Order").get(out_trade_no);
        if (order == null){
            // 如果redis中没有就去msq去取
            order = orderMapper.selectByPrimaryKey(out_trade_no);
        }
        order.setOrderStatus("1");              // 订单状态
        order.setUpdateTime(new Date());        // 更新订单时间
        order.setPayTime(new Date());           // 支付完成时间
        order.setTransactionId(transactionId);  // 支付流水
        // 更新订单
        orderMapper.updateByPrimaryKeySelective(order);
        // 删除支付成功订单数据
        redisTemplate.boundHashOps("Order").delete(out_trade_no);
    }




    /**
     * 增加Order
     * @param
     */
    @Override
    public Order add(Order order){
        // 通过当前用户名查询到购物车数据
        List<OrderItem> itemList = cartService.list(order.getUsername());
        // 从购物车中取出订单表中要保存的数据
        Integer totalNum = 0;       // 商品数量
        Integer totalMoney = 0;     // 商品总金额
        Integer totalPayMoney = 0;       // 商品实付金额
        for (OrderItem orderItem : itemList) {
            totalNum += orderItem.getNum();
            totalMoney += orderItem.getMoney();
            totalPayMoney += orderItem.getPayMoney();
        }
//        order.setId("NO."+idWorker.nextId());
        order.setId(String.valueOf(idWorker.nextId()));
        order.setTotalNum(totalNum);
        order.setTotalMoney(totalMoney);
        order.setPayMoney(totalPayMoney);
        order.setPreMoney(totalMoney - totalPayMoney);
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());

        order.setBuyerRate("0");
        order.setSourceType("1");
        order.setOrderStatus("0");
        order.setPayStatus("0");
        order.setConsignStatus("0");
        // 保存订单数据
        orderMapper.insertSelective(order);

        // 从购物车中取出订单明细所需要的数据向订单明细表中添加数据
        for (OrderItem orderItem : itemList) {
        orderItem.setOrderId(order.getId());
//        orderItem.setId("NO."+idWorker.nextId());  支付接口中需要String类型的参数
        orderItem.setId(String.valueOf(idWorker.nextId()));
        orderItem.setIsReturn("0");
            // 保存订单明细数据
            orderItemMapper.insertSelective(orderItem);
        }
        // 下单成功，扣减库存
        skuFeign.decr(order.getUsername());
        // 添加积分
        userFeign.addPoints(10, order.getUsername());

        // 提交订单后，将数据存到redis中
        if ("1".equals(order.getPayType())){
            // 在线支付
            redisTemplate.boundHashOps("Order").put(order.getId(),order);
        }

        // 最后删除已添加订单购物车数据
        redisTemplate.delete("cart_"+order.getUsername());
        return order;
    }




    /**
     * Order条件+分页查询
     * @param order 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<Order> findPage(Order order, int page, int size){
        //分页
        PageHelper.startPage(page,size);
        //搜索条件构建
        Example example = createExample(order);
        //执行搜索
        return new PageInfo<Order>(orderMapper.selectByExample(example));
    }

    /**
     * Order分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<Order> findPage(int page, int size){
        //静态分页
        PageHelper.startPage(page,size);
        //分页查询
        return new PageInfo<Order>(orderMapper.selectAll());
    }

    /**
     * Order条件查询
     * @param order
     * @return
     */
    @Override
    public List<Order> findList(Order order){
        //构建查询条件
        Example example = createExample(order);
        //根据构建的条件查询数据
        return orderMapper.selectByExample(example);
    }


    /**
     * Order构建查询对象
     * @param order
     * @return
     */
    public Example createExample(Order order){
        Example example=new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        if(order!=null){
            // 订单id
            if(!StringUtils.isEmpty(order.getId())){
                    criteria.andEqualTo("id",order.getId());
            }
            // 数量合计
            if(!StringUtils.isEmpty(order.getTotalNum())){
                    criteria.andEqualTo("totalNum",order.getTotalNum());
            }
            // 金额合计
            if(!StringUtils.isEmpty(order.getTotalMoney())){
                    criteria.andEqualTo("totalMoney",order.getTotalMoney());
            }
            // 优惠金额
            if(!StringUtils.isEmpty(order.getPreMoney())){
                    criteria.andEqualTo("preMoney",order.getPreMoney());
            }
            // 邮费
            if(!StringUtils.isEmpty(order.getPostFee())){
                    criteria.andEqualTo("postFee",order.getPostFee());
            }
            // 实付金额
            if(!StringUtils.isEmpty(order.getPayMoney())){
                    criteria.andEqualTo("payMoney",order.getPayMoney());
            }
            // 支付类型，1、在线支付、0 货到付款
            if(!StringUtils.isEmpty(order.getPayType())){
                    criteria.andEqualTo("payType",order.getPayType());
            }
            // 订单创建时间
            if(!StringUtils.isEmpty(order.getCreateTime())){
                    criteria.andEqualTo("createTime",order.getCreateTime());
            }
            // 订单更新时间
            if(!StringUtils.isEmpty(order.getUpdateTime())){
                    criteria.andEqualTo("updateTime",order.getUpdateTime());
            }
            // 付款时间
            if(!StringUtils.isEmpty(order.getPayTime())){
                    criteria.andEqualTo("payTime",order.getPayTime());
            }
            // 发货时间
            if(!StringUtils.isEmpty(order.getConsignTime())){
                    criteria.andEqualTo("consignTime",order.getConsignTime());
            }
            // 交易完成时间
            if(!StringUtils.isEmpty(order.getEndTime())){
                    criteria.andEqualTo("endTime",order.getEndTime());
            }
            // 交易关闭时间
            if(!StringUtils.isEmpty(order.getCloseTime())){
                    criteria.andEqualTo("closeTime",order.getCloseTime());
            }
            // 物流名称
            if(!StringUtils.isEmpty(order.getShippingName())){
                    criteria.andEqualTo("shippingName",order.getShippingName());
            }
            // 物流单号
            if(!StringUtils.isEmpty(order.getShippingCode())){
                    criteria.andEqualTo("shippingCode",order.getShippingCode());
            }
            // 用户名称
            if(!StringUtils.isEmpty(order.getUsername())){
                    criteria.andLike("username","%"+order.getUsername()+"%");
            }
            // 买家留言
            if(!StringUtils.isEmpty(order.getBuyerMessage())){
                    criteria.andEqualTo("buyerMessage",order.getBuyerMessage());
            }
            // 是否评价
            if(!StringUtils.isEmpty(order.getBuyerRate())){
                    criteria.andEqualTo("buyerRate",order.getBuyerRate());
            }
            // 收货人
            if(!StringUtils.isEmpty(order.getReceiverContact())){
                    criteria.andEqualTo("receiverContact",order.getReceiverContact());
            }
            // 收货人手机
            if(!StringUtils.isEmpty(order.getReceiverMobile())){
                    criteria.andEqualTo("receiverMobile",order.getReceiverMobile());
            }
            // 收货人地址
            if(!StringUtils.isEmpty(order.getReceiverAddress())){
                    criteria.andEqualTo("receiverAddress",order.getReceiverAddress());
            }
            // 订单来源：1:web，2：app，3：微信公众号，4：微信小程序  5 H5手机页面
            if(!StringUtils.isEmpty(order.getSourceType())){
                    criteria.andEqualTo("sourceType",order.getSourceType());
            }
            // 交易流水号
            if(!StringUtils.isEmpty(order.getTransactionId())){
                    criteria.andEqualTo("transactionId",order.getTransactionId());
            }
            // 订单状态,0:未完成,1:已完成，2：已退货
            if(!StringUtils.isEmpty(order.getOrderStatus())){
                    criteria.andEqualTo("orderStatus",order.getOrderStatus());
            }
            // 支付状态,0:未支付，1：已支付，2：支付失败
            if(!StringUtils.isEmpty(order.getPayStatus())){
                    criteria.andEqualTo("payStatus",order.getPayStatus());
            }
            // 发货状态,0:未发货，1：已发货，2：已收货
            if(!StringUtils.isEmpty(order.getConsignStatus())){
                    criteria.andEqualTo("consignStatus",order.getConsignStatus());
            }
            // 是否删除
            if(!StringUtils.isEmpty(order.getIsDelete())){
                    criteria.andEqualTo("isDelete",order.getIsDelete());
            }
        }
        return example;
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(String id){
        orderMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改Order
     * @param order
     */
    @Override
    public void update(Order order){
        orderMapper.updateByPrimaryKey(order);
    }
    /**
     * 根据ID查询Order
     * @param id
     * @return
     */
    @Override
    public Order findById(String id){
        return  orderMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询Order全部数据
     * @return
     */
    @Override
    public List<Order> findAll() {
        return orderMapper.selectAll();
    }
}
