package com.changgou.goods.service;

import com.changgou.goods.pojo.Goods;
import com.changgou.goods.pojo.Spu;
import com.github.pagehelper.PageInfo;

import java.util.List;


public interface SpuService {

    /**
     * 物理删除商品
     * @param spuId
     */
     void Delete(Long spuId);

    /**
     * 还原删除商品
     * @param spuId
     */
    void restore(Long spuId);


    /**
     * 逻辑删除商品
     * @param spuId
     */
    void logicDelete(Long spuId);


    /**
     * 批量上下架
     * @param ids
     * @param isMarketable
     */
    void isShows(Long[] ids,String isMarketable);

    /**
     * 批量下架
     * @param ids
     * @return
     */
    int putMany(Long[] ids);

    /**
     * 批量下架
     * @param ids
     * @return
     */
    int pullMany(Long[] ids);

    /**
     * 商品上下架
     * @param id
     * @param isMarketable
     */
    void isShow(Long id,String isMarketable);

    /**
     * 上架商品
     * @param spuId
     */
    void put(Long spuId);

    /**
     * 下架商品
     * @param spuId
     */
    void pull(Long spuId);

    /**
     * 商品审核
     * @param spuId
     */
   void audit(Long spuId);

    /**
     * 根据id查询Goods返回
     * @param spuId
     * @return
     */
    Goods findGoodsById(Long spuId);

    /**
     * 更新保存商品
     * @param goods
     */
    void Edit(Goods goods);

    /**
     * 保存商品
     * @param goods
     */
    void save(Goods goods);

    /***
     * Spu多条件分页查询
     * @param spu
     * @param page
     * @param size
     * @return
     */
    PageInfo<Spu> findPage(Spu spu, int page, int size);

    /***
     * Spu分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<Spu> findPage(int page, int size);

    /***
     * Spu多条件搜索方法
     * @param spu
     * @return
     */
    List<Spu> findList(Spu spu);

    /***
     * 删除Spu
     * @param id
     */
    void delete(Long id);

    /***
     * 修改Spu数据
     * @param spu
     */
    void update(Spu spu);

    /***
     * 新增Spu
     * @param spu
     */
    void add(Spu spu);

    /**
     * 根据ID查询Spu
     * @param id
     * @return
     */
     Spu findById(Long id);

    /***
     * 查询所有Spu
     * @return
     */
    List<Spu> findAll();
}
