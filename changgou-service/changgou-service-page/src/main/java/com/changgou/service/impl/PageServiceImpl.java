package com.changgou.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.CategoryFeign;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.service.PageService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 静态页生成代码
 */
@Service
public class PageServiceImpl implements PageService {

    //静态化模板引擎
    @Autowired
    private TemplateEngine templateEngine;

    //静态化页面保存路径
    @Value("${pagepath}")
    private String pagepath;

    @Autowired
    private SpuFeign spuFeign;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private CategoryFeign categoryFeign;

    //生成静态化页面
    @Override
    public void createHtml(String spuId) {

        //获取静态页需要的数据
        Map<String, Object> dataMap = buildDataModel(spuId);

        Context context = new Context();
        context.setVariables(dataMap);

        File file = new File(pagepath);
        if(!file.exists()){
            file.mkdirs();
        }

        Writer writer = null;
        try {
            writer = new PrintWriter(file + "/" + spuId + ".html");

            //处理 开始生成静态化页面
            templateEngine.process("item",context,writer);

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(null != writer){
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    /**
     * 获取静态页需要的数据
     * @param spuId
     * @return
     */
    public Map<String,Object> buildDataModel(String spuId){
        Map<String,Object> resultMap = new HashMap<>();
        //查询如下数据

        //2：商品对象
        Result result = spuFeign.findBySpuId(spuId);
        Spu spu = JSON.parseObject(JSON.toJSONString(result.getData()), Spu.class);
        resultMap.put("spu",spu);
        if(!StringUtils.isEmpty(spu.getImages())){
            resultMap.put("imageList",spu.getImages().split(","));
        }

        //1:商品分类 一级 二级 三级  名称
        resultMap.put("category1",categoryFeign.findCategoryById(spu.getCategory1Id()));
        resultMap.put("category2",categoryFeign.findCategoryById(spu.getCategory2Id()));
        resultMap.put("category3",categoryFeign.findCategoryById(spu.getCategory3Id()));

        //3: 库存对象集合
        List<Sku> skuList = skuFeign.findSkuListBySpuId(spuId);
        resultMap.put("skuList",skuList);

        //{"颜色":["深色","蓝色"],"尺码":["27","28","29"]}
        //Map<String,Set<String>> specificationList
        resultMap.put("specificationList",JSON.parseObject(spu.getSpecItems(), Map.class));
        return resultMap;
    }
}