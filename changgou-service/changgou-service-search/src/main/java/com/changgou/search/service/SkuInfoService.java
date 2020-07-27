package com.changgou.search.service;


import java.util.Map;

/**
 * @Author: Ye Jian Song
 * @Description:
 * @Date: Create in 20:05 2019/8/17
 */
public interface SkuInfoService {
    /**
     * 将数据库的数据导入到索引库
     */
    void importDateToEs();

    /**
     * 前台检索关键字检索
     * @param searchMap
     * @return
     */
    Map<String, Object> search(Map<String, String> searchMap);

}
