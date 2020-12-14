package com.changgou.service;

/**
 *
 */
public interface PageService {
    /**
     * 根据商品的ID 生成静态页
     *
     * @param spuId
     */
    void createHtml(String spuId);

    void createPageHtml(Long id);
}
