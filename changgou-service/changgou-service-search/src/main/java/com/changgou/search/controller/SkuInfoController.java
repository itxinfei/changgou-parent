package com.changgou.search.controller;

import com.changgou.search.service.SkuInfoService;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @Author: Ye Jian Song
 * @Description:
 * @Date: Create in 20:19 2019/8/17
 */
@RestController
@CrossOrigin
@RequestMapping("/search")
public class SkuInfoController {

    @Autowired(required = false)
    private SkuInfoService skuInfoService;

    /**
     * 检索商品
     * @param searchMap
     * @return
     */
    @GetMapping
    public Map<String,Object> search(@RequestParam Map<String,String> searchMap){
        Map<String, Object> resultMap = skuInfoService.search(searchMap);
        return resultMap;
    }



    /**
     * 数据导入Es方法
     * @return
     */
    @GetMapping("/import")
    public Result importDateToEs(){
        skuInfoService.importDateToEs();
        return new Result(true, StatusCode.OK,"数据导入Es成功");
    }

}
