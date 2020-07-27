package com.changgou.goods.dao;
import com.changgou.goods.pojo.Brand;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/****
 * @Author:传智播客
 * @Description:Brand的Dao
 * @Date 2019/6/14 0:12
 *****/
public interface BrandMapper extends Mapper<Brand> {

    /**
     * 根据id查询分类品牌数据
     * @param id
     * @return
     */
    @Select("select tb.id, tb.name from tb_brand tb, tb_category_brand cb\n" +
            "where tb.id = cb.brand_id and category_id = #{id}")
    List<Brand> findByGateGoryId(Integer id);
}
