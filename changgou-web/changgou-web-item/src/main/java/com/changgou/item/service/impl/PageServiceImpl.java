package com.changgou.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.CategoryFeign;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Category;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.item.service.PageService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: Ye Jian Song
 * @Description:
 * @Date: Create in 21:14 2019/8/20
 */
@Service
public class PageServiceImpl implements PageService {
    @Autowired(required = false)
    private CategoryFeign categoryFeign;

    @Autowired(required = false)
    private SkuFeign skuFeign;

    @Autowired(required = false)
    private SpuFeign spuFeign;

    @Autowired(required = false)
    private TemplateEngine templateEngine;

    @Value("${pagepath}")
    private String pagePath;


    /**
     * 生成静态页的具体实现
     * @param spuId
     */
    @Override
    public void createHtml(Long spuId) {
        try {
            // 封装静态页面需要的数据
            Map<String,Object> dataModel = getDataModel(spuId);
            // 准备静态页需要的数据
            Context context = new Context();
            // 封装模板需要的数据
            context.setVariables(dataModel);
            // 指定生成静态页的位置(路径)
            File dir = new File(pagePath);
            if (!dir.exists()){
                dir.mkdirs();
            }
            // 生成静态页面的文件名(通过Id更新)
            File dest = new File(dir,spuId+".html");
            PrintWriter writer = new PrintWriter(dest,"UTF-8");
            // 生成静态页
            templateEngine.process("item",context,writer);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询静态页需要的数据
     * @param spuId
     * @return
     */
    private Map<String, Object> getDataModel(Long spuId) {
        Map<String,Object> dataModel  = new HashMap<>();
        // 商品信息
        Result<Spu> spuResult = spuFeign.findById(spuId);
        Spu spu = spuResult.getData();
        dataModel.put("spu",spu);
        // 库存信息
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        Result<List<Sku>> skuListResult = skuFeign.findList(sku);
        List<Sku> skuList = skuListResult.getData();
        dataModel.put("skuList",skuList);
        // 商品分类信息
        Category category1 = categoryFeign.findById(spu.getCategory1Id()).getData();
        Category category2 = categoryFeign.findById(spu.getCategory2Id()).getData();
        Category category3 = categoryFeign.findById(spu.getCategory3Id()).getData();
        dataModel.put("category1",category1);
        dataModel.put("category2",category2);
        dataModel.put("category3",category3);
        // 商品图片信息
        String image = spu.getImage();
        dataModel.put("image",image);
        String[] images = spu.getImages().split(",");
        dataModel.put("images",images);
        // 商品库存信息
        String specItems = spu.getSpecItems();
        Map<String,String> specificationList = JSON.parseObject(specItems, Map.class);
        dataModel.put("specificationList",specificationList);
        return dataModel;
    }
}
