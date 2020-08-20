package com.changgou.goods.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.dao.BrandMapper;
import com.changgou.goods.dao.CategoryMapper;
import com.changgou.goods.dao.SkuMapper;
import com.changgou.goods.dao.SpuMapper;
import com.changgou.goods.pojo.*;
import com.changgou.goods.service.SpuService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import entity.IdWorker;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;


/**
 * Spu业务层接口实现类
 */
@Service
public class SpuServiceImpl implements SpuService {

    @Autowired(required = false)
    private SpuMapper spuMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired(required = false)
    private CategoryMapper categoryMapper;

    @Autowired(required = false)
    private BrandMapper brandMapper;

    @Autowired(required = false)
    private SkuMapper skuMapper;

    @Autowired
    private RabbitMessagingTemplate rabbitTemplate;


    /**
     * 物理删除商品
     *
     * @param spuId
     */
    @Override
    public void Delete(Long spuId) {
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        //检查是否被逻辑删除  ,必须先逻辑删除后才能物理删除
        if ("1".equalsIgnoreCase(spu.getIsDelete())) {
            throw new RuntimeException("商品未逻辑删除，请先逻辑删除商品再进行删除操作");
        }
        spuMapper.deleteByPrimaryKey(spuId);
    }

    /**
     * 还原删除商品
     *
     * @param spuId
     */
    @Override
    public void restore(Long spuId) {
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        //先判断商品是否删除
        if (!"1".equalsIgnoreCase(spu.getIsDelete())) {
            throw new RuntimeException("商品未删除");
        }
        //还原删除商品
        spu.setIsDelete("0");
        //审核状态
        spu.setStatus("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 逻辑删除商品
     *
     * @param spuId
     */
    @Override
    public void logicDelete(Long spuId) {
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        //先判断商品是否下架
        if ("1".equalsIgnoreCase(spu.getIsMarketable())) {
            throw new RuntimeException("商品未下架，请先下架商品再进行删除操作");
        }
        spu.setIsDelete("1");
        spu.setStatus("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 批量下架
     *
     * @param ids
     * @param isMarketable
     */
    @Override
    public void isShows(Long[] ids, String isMarketable) {
        Spu spu = new Spu();
        //设置商品上下架
        spu.setIsMarketable(isMarketable);
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", Arrays.asList(ids));
        spuMapper.updateByExampleSelective(spu, example);
    }

    /**
     * 批量上架
     *
     * @param ids
     * @return 上架商品数量
     */
    @Override
    public int putMany(Long[] ids) {
        Spu spu = new Spu();
        //上架
        spu.setIsMarketable("1");
        //批量上架
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", Arrays.asList(ids));
        //下架
        criteria.andEqualTo("isMarketable", "0");
        //审核通过的
        criteria.andEqualTo("status", "1");
        //未删除的
        criteria.andEqualTo("isDelete", "0");
        return spuMapper.updateByExampleSelective(spu, example);
    }

    /**
     * 批量下架
     *
     * @param ids
     * @return 下架商品数量
     */
    @Override
    public int pullMany(Long[] ids) {
        Spu spu = new Spu();
        //下架
        spu.setIsMarketable("0");
        //批量下架
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", Arrays.asList(ids));
        //下架
        criteria.andEqualTo("isMarketable", "1");
        //审核通过的
        criteria.andEqualTo("status", "1");
        //未删除的
        criteria.andEqualTo("isDelete", "0");
        return spuMapper.updateByExampleSelective(spu, example);
    }

    /**
     * 商品上下架
     *
     * @param id
     * @param isMarketable
     */
    @Override
    public void isShow(Long id, String isMarketable) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        //如果商品未通过审核或已经删除
        if (!"1".equalsIgnoreCase(spu.getStatus()) || "1".equalsIgnoreCase(spu.getIsDelete())) {
            throw new RuntimeException("此商品不可上下架");
        }
        spu.setIsMarketable(isMarketable);
        spuMapper.updateByPrimaryKeySelective(spu);
    }


    /**
     * 上架商品
     *
     * @param spuId
     */
    @Override
    public void put(Long spuId) {
        /**
         * 1. 更改数据库中的上架状态
         */
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        String isDelete = "1";
        String isStatus = "0";
        if (isDelete.equalsIgnoreCase(spu.getIsDelete())) {
            throw new RuntimeException("商品已经删除,不可上架!!!!");
        }
        if (isStatus.equalsIgnoreCase(spu.getStatus())) {
            throw new RuntimeException("商品未通过审核，不可上架,请先审核商品!!!!");
        }
        spu.setIsMarketable("1");
        spuMapper.updateByPrimaryKeySelective(spu);

        /**
         * 2. 将数据发送到rabbitmq中
         */
        rabbitTemplate.convertAndSend("goods_up_exchange", "", spuId);
    }

    /**
     * 下架商品
     *
     * @param spuId
     */
    @Override
    public void pull(Long spuId) {
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        String isDelete = "1";
        if (isDelete.equalsIgnoreCase(spu.getIsDelete())) {
            throw new RuntimeException("此商品已经删除");
        }
        spu.setIsMarketable("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 商品审核
     *
     * @param spuId
     */
    @Override
    public void audit(Long spuId) {
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        String isDelete = "1";
        if (isDelete.equalsIgnoreCase(spu.getIsDelete())) {
            throw new RuntimeException("商品已经删除不能审核！！！");
        }
        //审核通过
        spu.setStatus("1");
        //上架
        spu.setIsMarketable("1");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 根据id查询返回Goods对象
     *
     * @param spuId
     * @return
     */
    @Override
    public Goods findGoodsById(Long spuId) {
        //查询的到spu
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        //查询sku对象返回
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skus = skuMapper.select(sku);
        //封装对象返回
        Goods goods = new Goods();
        goods.setSpu(spu);
        goods.setSkuList(skus);
        return goods;
    }

    /**
     * 保存商品
     *
     * @param goods
     */
    @Override
    public void save(Goods goods) {
        //先保存spu对象到spu表
        Spu spu = goods.getSpu();
        //是否上架
        spu.setIsMarketable("0");
        //是否删除
        spu.setIsDelete("0");
        //待审核
        spu.setIsEnableSpec("0");
        //spu主键id
        spu.setId(idWorker.nextId());
        spuMapper.insertSelective(spu);

        //添加sku
        List<Sku> skus = goods.getSkuList();
        if (skus != null && skus.size() > 0) {
            for (Sku sku : skus) {
                //sku主键id
                sku.setId(idWorker.nextId());
                //spu外键id
                sku.setSpuId(spu.getId());
                //商品规格
                String spec = sku.getSpec();
                //名称
                String name = spu.getName();
                //构建SKU名称，采用SPU+规格值组装（非空判断）
                if (StringUtils.isEmpty(spec)) {
                    sku.setSpec("{}");
                }
                //spec{"电视音响效果":"立体声","电视屏幕尺寸":"20英寸","尺码":"165"}
                Map<String, String> map = JSON.parseObject(spec, Map.class);
                if (map != null) {
                    Set<Map.Entry<String, String>> entrySet = map.entrySet();
                    for (Map.Entry<String, String> entry : entrySet) {
                        //遍历拼接sku的name
                        name += " " + entry.getValue();
                    }
                }
                sku.setName(name);
                sku.setCategoryId(spu.getCategory3Id());
                sku.setCreateTime(new Date());
                sku.setUpdateTime(new Date());
                //setCategoryName的值
                sku.setCategoryName(categoryMapper.selectByPrimaryKey(spu.getCategory3Id()).getName());
                //setBrandName的值
                sku.setBrandName(brandMapper.selectByPrimaryKey(spu.getBrandId()).getName());
                //商品状态
                sku.setStatus("1");
                //向sku表中添加数据
                skuMapper.insertSelective(sku);
            }
        }
    }


    /**
     * 保存修改
     *
     * @param goods
     */
    @Override
    public void Edit(Goods goods) {
        //先保存spu对象到spu表
        Spu spu = goods.getSpu();
        Long spuId = spu.getId();
        //判断是否存在此商品，不存在直接新增
        if (spuId == null) {
            //是否上架
            spu.setIsMarketable("0");
            //是否删除
            spu.setIsDelete("0");
            //待审核
            spu.setIsEnableSpec("0");
            //spu主键id
            spu.setId(idWorker.nextId());
            spuMapper.insertSelective(spu);
        } else {     //存在进行更新操作
            spu.setStatus("0");
            spuMapper.updateByPrimaryKeySelective(spu);
            //删除原来的sku
            Sku sku = new Sku();
            sku.setSpuId(spu.getId());
            skuMapper.delete(sku);
        }
        //添加sku
        List<Sku> skus = goods.getSkuList();
        if (skus != null && skus.size() > 0) {
            for (Sku sku : skus) {
                //sku主键id
                sku.setId(idWorker.nextId());
                //spu外键id
                sku.setSpuId(spu.getId());
                //商品规格
                String spec = sku.getSpec();
                //名称
                String name = spu.getName();
                //构建SKU名称，采用SPU+规格值组装（非空判断）
                if (StringUtils.isEmpty(spec)) {
                    sku.setSpec("{}");
                }
                //spec{"电视音响效果":"立体声","电视屏幕尺寸":"20英寸","尺码":"165"}
                Map<String, String> map = JSON.parseObject(spec, Map.class);
                if (map != null) {
                    Set<Map.Entry<String, String>> entrySet = map.entrySet();
                    for (Map.Entry<String, String> entry : entrySet) {
                        //遍历拼接sku的name
                        name += " " + entry.getValue();
                    }
                }
                sku.setName(name);
                sku.setCategoryId(spu.getCategory3Id());
                sku.setCreateTime(new Date());
                sku.setUpdateTime(new Date());
                //setCategoryName的值
                sku.setCategoryName(categoryMapper.selectByPrimaryKey(spu.getCategory3Id()).getName());
                //setBrandName的值
                sku.setBrandName(brandMapper.selectByPrimaryKey(spu.getBrandId()).getName());
                //商品状态
                sku.setStatus("1");
                //向sku表中添加数据
                skuMapper.insertSelective(sku);
            }
        }
    }


    /**
     * Spu条件+分页查询
     *
     * @param spu  查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<Spu> findPage(Spu spu, int page, int size) {
        //分页
        PageHelper.startPage(page, size);
        //搜索条件构建
        Example example = createExample(spu);
        //执行搜索
        return new PageInfo<Spu>(spuMapper.selectByExample(example));
    }

    /**
     * Spu分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<Spu> findPage(int page, int size) {
        //静态分页
        PageHelper.startPage(page, size);
        //分页查询
        return new PageInfo<Spu>(spuMapper.selectAll());
    }

    /**
     * Spu条件查询
     *
     * @param spu
     * @return
     */
    @Override
    public List<Spu> findList(Spu spu) {
        //构建查询条件
        Example example = createExample(spu);
        //根据构建的条件查询数据
        return spuMapper.selectByExample(example);
    }


    /**
     * Spu构建查询对象
     *
     * @param spu
     * @return
     */
    public Example createExample(Spu spu) {
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if (spu != null) {
            // 主键
            if (!StringUtils.isEmpty(spu.getId())) {
                criteria.andEqualTo("id", spu.getId());
            }
            // 货号
            if (!StringUtils.isEmpty(spu.getSn())) {
                criteria.andEqualTo("sn", spu.getSn());
            }
            // SPU名
            if (!StringUtils.isEmpty(spu.getName())) {
                criteria.andLike("name", "%" + spu.getName() + "%");
            }
            // 副标题
            if (!StringUtils.isEmpty(spu.getCaption())) {
                criteria.andEqualTo("caption", spu.getCaption());
            }
            // 品牌ID
            if (!StringUtils.isEmpty(spu.getBrandId())) {
                criteria.andEqualTo("brandId", spu.getBrandId());
            }
            // 一级分类
            if (!StringUtils.isEmpty(spu.getCategory1Id())) {
                criteria.andEqualTo("category1Id", spu.getCategory1Id());
            }
            // 二级分类
            if (!StringUtils.isEmpty(spu.getCategory2Id())) {
                criteria.andEqualTo("category2Id", spu.getCategory2Id());
            }
            // 三级分类
            if (!StringUtils.isEmpty(spu.getCategory3Id())) {
                criteria.andEqualTo("category3Id", spu.getCategory3Id());
            }
            // 模板ID
            if (!StringUtils.isEmpty(spu.getTemplateId())) {
                criteria.andEqualTo("templateId", spu.getTemplateId());
            }
            // 运费模板id
            if (!StringUtils.isEmpty(spu.getFreightId())) {
                criteria.andEqualTo("freightId", spu.getFreightId());
            }
            // 图片
            if (!StringUtils.isEmpty(spu.getImage())) {
                criteria.andEqualTo("image", spu.getImage());
            }
            // 图片列表
            if (!StringUtils.isEmpty(spu.getImages())) {
                criteria.andEqualTo("images", spu.getImages());
            }
            // 售后服务
            if (!StringUtils.isEmpty(spu.getSaleService())) {
                criteria.andEqualTo("saleService", spu.getSaleService());
            }
            // 介绍
            if (!StringUtils.isEmpty(spu.getIntroduction())) {
                criteria.andEqualTo("introduction", spu.getIntroduction());
            }
            // 规格列表
            if (!StringUtils.isEmpty(spu.getSpecItems())) {
                criteria.andEqualTo("specItems", spu.getSpecItems());
            }
            // 参数列表
            if (!StringUtils.isEmpty(spu.getParaItems())) {
                criteria.andEqualTo("paraItems", spu.getParaItems());
            }
            // 销量
            if (!StringUtils.isEmpty(spu.getSaleNum())) {
                criteria.andEqualTo("saleNum", spu.getSaleNum());
            }
            // 评论数
            if (!StringUtils.isEmpty(spu.getCommentNum())) {
                criteria.andEqualTo("commentNum", spu.getCommentNum());
            }
            // 是否上架,0已下架，1已上架
            if (!StringUtils.isEmpty(spu.getIsMarketable())) {
                criteria.andEqualTo("isMarketable", spu.getIsMarketable());
            }
            // 是否启用规格
            if (!StringUtils.isEmpty(spu.getIsEnableSpec())) {
                criteria.andEqualTo("isEnableSpec", spu.getIsEnableSpec());
            }
            // 是否删除,0:未删除，1：已删除
            if (!StringUtils.isEmpty(spu.getIsDelete())) {
                criteria.andEqualTo("isDelete", spu.getIsDelete());
            }
            // 审核状态，0：未审核，1：已审核，2：审核不通过
            if (!StringUtils.isEmpty(spu.getStatus())) {
                criteria.andEqualTo("status", spu.getStatus());
            }
        }
        return example;
    }

    /**
     * 删除
     *
     * @param id
     */
    @Override
    public void delete(Long id) {
        spuMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改Spu
     *
     * @param spu
     */
    @Override
    public void update(Spu spu) {
        spuMapper.updateByPrimaryKey(spu);
    }

    /**
     * 增加Spu
     *
     * @param spu
     */
    @Override
    public void add(Spu spu) {
        spuMapper.insert(spu);
    }

    /**
     * 根据ID查询Spu
     *
     * @param id
     * @return
     */
    @Override
    public Spu findById(Long id) {
        return spuMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询Spu全部数据
     *
     * @return
     */
    @Override
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }
}
