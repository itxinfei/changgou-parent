package com.changgou.test;

import com.changgou.GoodsApplication;
import com.changgou.entity.IdWorker;
import com.changgou.goods.service.TemplateService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = GoodsApplication.class)
@RunWith(SpringRunner.class)
public class TestTemple {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private IdWorker idWorker;

    @Test
    public void test01() {
//        Template byCategory = templateService.findByCategory(43);
//        System.out.println(byCategory.toString());
        for (int i = 1; i <= 10; i++) {
            System.out.println(idWorker.nextId());
        }
    }
}
