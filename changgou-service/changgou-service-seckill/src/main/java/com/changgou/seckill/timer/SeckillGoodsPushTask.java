package com.changgou.seckill.timer;

import com.changgou.entity.DateUtil;
import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @Author pangzhao
 */
@Component
public class SeckillGoodsPushTask {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired(required = false)
    private SeckillGoodsMapper seckillGoodsMapper;

    @Scheduled(cron = "0/15 * * * * ?")
    public void pushGoodsToRedis() {
        // 获取对应时间段数据
        List<Date> dateMenus = DateUtil.getDateMenus();
        for (Date dateMenu : dateMenus) {
            // redis 的key构建
            String extName = DateUtil.data2str(dateMenu, DateUtil.PATTERN_YYYYMMDDHH);
            // 条件查询：审核通过，库存大于0，time > startTime and time < endTime
            Example example = new Example(SeckillGoods.class);
            Example.Criteria criteria = example.createCriteria();
            // 审核通过
            criteria.andEqualTo("status", "1");
            // 剩余库存数 > 0
            criteria.andGreaterThan("stockCount", 0);
            // 开始时间：大于该区间段
            criteria.andGreaterThanOrEqualTo("startTime", dateMenu);
            // 结束时间：小于该区间的2个小时后的
            criteria.andLessThanOrEqualTo("endTime", DateUtil.addDateHour(dateMenu, 2));
            // 排除已经添加到redis的数据
            Set ids = redisTemplate.boundHashOps("SeckillGoods_" + extName).keys();
            if (ids != null && ids.size() > 0) {
                // 继续拼接查询条件,排除已经存在的id
                criteria.andNotIn("id", ids);
            }
            // 根据条件查询数据库
            List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);
            System.out.println("查询的数据条数:" + seckillGoodsList.size());

            // 将数据写入缓存
            if (seckillGoodsList != null && seckillGoodsList.size() > 0) {
                for (SeckillGoods seckillGoods : seckillGoodsList) {
                    Long[] idArrays = pushId(seckillGoods.getStockCount(), seckillGoods.getId());
                    // 将秒杀的商品存到redis(展示商品)
                    redisTemplate.boundHashOps("SeckillGoods_" + extName).put(seckillGoods.getId().toString(), seckillGoods);
                    // 将该款商品的id存到redis队列中用于扣减库存
                    redisTemplate.boundListOps("SeckillGoodsCountList_" + seckillGoods.getId()).leftPushAll(idArrays);
                    // 将商品的库存存到redis
                    redisTemplate.boundHashOps("SeckillGoodsCount").put(seckillGoods.getId().toString(), seckillGoods.getStockCount());
                }
            }
        }

    }

    /**
     * 将当前商品的id存到数组中
     *
     * @param stockCount
     * @param id
     * @return
     */
    private Long[] pushId(Integer stockCount, Long id) {
        Long[] ids = new Long[stockCount];
        for (Integer i = 0; i < stockCount; i++) {
            ids[i] = id;
        }
        return ids;
    }
}
