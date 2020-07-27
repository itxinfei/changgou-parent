package com.changgou;

import com.changgou.search.pojo.SkuInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Author: Ye Jian Song
 * @Description:
 * @Date: Create in 9:52 2019/8/20
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class testCreatIndex {

    @Autowired(required = false)
    private ElasticsearchTemplate elasticsearchTemplate;

    @Test
    public void test() {
        elasticsearchTemplate.createIndex(SkuInfo.class);
        elasticsearchTemplate.putMapping(SkuInfo.class);


    }
}
