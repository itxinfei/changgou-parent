package com.changgou.search.dao;

import com.changgou.search.pojo.SkuInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @Author: Ye Jian Song
 * @Description:
 * @Date: Create in 20:02 2019/8/17
 */
public interface SkuInfoMapper extends ElasticsearchRepository<SkuInfo,Integer> {


}
