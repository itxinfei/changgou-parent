package com.changgou.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.changgou.canal.mq.queue.TopicQueue;
import com.changgou.canal.mq.send.TopicMessageSender;
import com.changgou.content.feign.ContentFeign;
import com.changgou.content.pojo.Content;
import com.xpand.starter.canal.annotation.*;
import entity.Message;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/**
 * @Author: Ye Jian Song
 * @Description: 监听对数据库的（CRUD）
 * @Date: Create in 11:54 2019/8/17
 */
@CanalEventListener
public class CanalDataEventListener {

    @Autowired(required = false)
    ContentFeign contentFeign;

    //RedisTemplate : 序列化
    //StringRedisTemplate ：不会序列化
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 向服务
     */
    @Autowired
    private TopicMessageSender topicMessageSender;

    /**
     * 需求：监听广告表数据是否发生改变
     * 自定义监听的库以及表
     *
     * @param entryType
     * @param rowData   destination : canal中conf中配置的名称，
     *                  schema ：监听的库
     *                  table ：监听的表
     *                  eventType ： 监听的操作类型(CRUD)
     */
    @ListenPoint(destination = "example", schema = ("changgou_content"), table = ("tb_content"),
            eventType = {CanalEntry.EventType.INSERT, CanalEntry.EventType.UPDATE, CanalEntry.EventType.DELETE})
    public void onEventContent(CanalEntry.EntryType entryType, CanalEntry.RowData rowData) {
        // 获取广告更新后的数据
        String category_id = getColumn(rowData, "category_id");
        // 通过id获得广告数据(调用changgou-service-content服务通过feign调用)
        Result<List<Content>> result = contentFeign.list(Long.parseLong(category_id));
        // 将数据写人redis中
        redisTemplate.boundValueOps("content" + category_id).set(JSON.toJSONString(result.getData()));
    }


    /***
     * 规格、分类数据修改监听
     * 同步数据到Redis
     * @param eventType
     * @param rowData
     */
    @ListenPoint(destination = "example", schema = "changgou_goods", table = {"tb_spu"}, eventType = {CanalEntry.EventType.UPDATE, CanalEntry.EventType.DELETE})
    public void onEventCustomSpu(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        //操作类型
        int number = eventType.getNumber();
        //操作的数据
        String id = getColumn(rowData, "id");
        System.out.println("监听到了数据库Spu表更新了数据。。。。");
        //封装Message
        Message message = new Message(number, id, TopicQueue.TOPIC_QUEUE_SPU, TopicQueue.TOPIC_EXCHANGE_SPU);
        //发送消息
        topicMessageSender.sendMessage(message);
    }


    /**
     * @param rowData    :监听到的数据
     * @param columnName ：列的名字(对应表中的category_id)
     * @return
     */
    private String getColumn(CanalEntry.RowData rowData, String columnName) {
        //获取更新后的数据
        List<CanalEntry.Column> afterList = rowData.getAfterColumnsList();
        for (CanalEntry.Column column : afterList) {
            if (columnName.equals(column.getName())) {
                return column.getValue();
            }
        }
        return null;
    }


    /**
     * 监听添加之后的数据
     *
     * @param entryType
     * @param rowData
     */
    @InsertListenPoint
    public void onEventInsert(CanalEntry.EntryType entryType, CanalEntry.RowData rowData) {
        List<CanalEntry.Column> list = rowData.getAfterColumnsList();
        for (CanalEntry.Column column : list) {
            System.out.println("列名：" + column.getName() + "<--->列值：" + column.getValue());
        }
    }


    /**
     * 监听更新前后的数据
     *
     * @param entryType
     * @param rowData
     */
    @UpdateListenPoint
    public void onEventUpdate(CanalEntry.EntryType entryType, CanalEntry.RowData rowData) {
        //更新之前的数据
        List<CanalEntry.Column> beforeList = rowData.getBeforeColumnsList();
        for (CanalEntry.Column column : beforeList) {
            System.out.println("列名：" + column.getName() + "<--->列值：" + column.getValue());
        }
        System.out.println("=====================================");
        //更新之后的数据
        List<CanalEntry.Column> afterList = rowData.getAfterColumnsList();
        for (CanalEntry.Column column : afterList) {
            System.out.println("列名：" + column.getName() + "<--->列值：" + column.getValue());
        }
    }

    /**
     * 监听删除之前的数据
     *
     * @param entryType
     * @param rowData
     */
    @DeleteListenPoint
    public void onEventDelete(CanalEntry.EntryType entryType, CanalEntry.RowData rowData) {
        List<CanalEntry.Column> beforeList = rowData.getBeforeColumnsList();
        for (CanalEntry.Column column : beforeList) {
            System.out.println("列名：" + column.getName() + "<--->列值：" + column.getValue());
        }
    }

}
