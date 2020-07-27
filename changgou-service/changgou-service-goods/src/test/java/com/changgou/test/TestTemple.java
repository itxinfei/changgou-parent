package com.changgou.test;

import com.changgou.GoodsApplication;
import com.changgou.goods.service.TemplateService;
import entity.IdWorker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Author: Ye Jian Song
 * @Description:
 * @Date: Create in 17:01 2019/8/13
 */
@SpringBootTest(classes = GoodsApplication.class)
@RunWith(SpringRunner.class)
public class TestTemple {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private IdWorker idWorker;
    @Test
    public void test01(){
//        Template byCategory = templateService.findByCategory(43);
//        System.out.println(byCategory.toString());
        for (int i = 1; i <= 10; i++) {
            System.out.println(idWorker.nextId());
        }
    }
}
