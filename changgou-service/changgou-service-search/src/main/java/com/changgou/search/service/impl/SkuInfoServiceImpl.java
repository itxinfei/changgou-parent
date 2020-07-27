package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.SkuInfoMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuInfoService;
import entity.Result;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @Author: Ye Jian Song
 * @Description:
 * @Date: Create in 20:05 2019/8/17
 */
@Service
public class SkuInfoServiceImpl implements SkuInfoService {

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired(required = false)  //调用goods中的findSkusByStatus方法获得List<Sku>
    private SkuFeign skuFegin;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;


    /**
     * 将数据库的数据导入到索引库
     */
    @Override
    public void importDateToEs() {
        // 查询数据库的List<Sku>
        Result<List<Sku>> result = skuFegin.findSkusByStatus("1");
        List<Sku> skuList = result.getData();
        String text = JSON.toJSONString(skuList);
        // 处理结果集List<SkuInfo>
        List<SkuInfo> skuInfoList = JSON.parseArray(text, SkuInfo.class);
        // 处理spec属性
        if (skuInfoList != null && skuInfoList.size() > 0) {
            for (SkuInfo skuInfo : skuInfoList) {
                // {"电视音响效果":"小影院","电视屏幕尺寸":"20英寸","尺码":"165"}
                String spec = skuInfo.getSpec();
                Map<String, Object> specMap = JSON.parseObject(spec, Map.class);
                skuInfo.setSpecMap(specMap);
            }
        }
        // 保存数据
        skuInfoMapper.saveAll(skuInfoList);
    }


    /**
     * 关键字检索
     * @param searchMap
     * @return
     */
    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {
        // 1.封装检索条件(后期有多个检索条件，专门封装一个方法)
        NativeSearchQueryBuilder builder = builderBasicQuery(searchMap);
        // 2.根据关键字检索,获取改关键字下的商品信息
        Map<String, Object> resultMap = searchForPage(builder);
        // 3.商品分类列表
//        List<String> categoryList = searchCategoryList(builder);
//        resultMap.put("categoryList", categoryList);
        // 4.品牌分类列表查询
//        List<String> brandList = searchBrandList(builder);
//        resultMap.put("brandList", brandList);
        // 获取数据的总条数
        String totalElements = resultMap.get("TotalElements").toString();
        int totalSize = Integer.parseInt(totalElements);
        if (totalSize <= 0){
            //判断totalSize是否小于等于0，如果小于等于0会报角标越界异常，需要给totalSize设置默认值防止报错
            totalSize = 10000;
        }
        // 5.统计规格分类列表
//        Map<String, Set<String>> specList = searchSpecList(builder,totalSize);
//        resultMap.put("specList", specList);
        // 6.将检索的结果封装到map中
        Map<String, Object> map = searchGroupList(builder,totalSize);
        resultMap.putAll(map);
        return resultMap;
    }


    /**
     * 统计规格分类列表查询, 品牌分类列表查询 ,商品分类分组统计实现(封装一个方法返回所有检索条件结果返回)
     * @param builder
     * @return
     */
    private  Map<String, Object> searchGroupList(NativeSearchQueryBuilder builder, int totalSize) {
        // 聚合查询   (分类)                                       别名                对应kibana中的字段
        builder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName").size(totalSize));
        // 聚合查询   (品牌)                                       别名                对应kibana中的字段
        builder.addAggregation(AggregationBuilders.terms("skuBrand").field("brandName").size(totalSize));
        // 聚合查询   (品牌)                                       别名                对应kibana中的字段
        builder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword").size(totalSize));
        // 分组结果集
        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);
        //处理结果集
        Aggregations aggregations = page.getAggregations();
        // 统计分类
        List<String> categoryList = getGroupList(aggregations,"skuCategory");
        // 统计品牌
        List<String> brandList = getGroupList(aggregations,"skuBrand");
        // 统计规格
        List<String> spceList = getGroupList(aggregations,"skuSpec");
        // 将统计规格的List结果集转成Map返回
        Map<String, Set<String>> specmap = pullMap(spceList);
        // 将所有的数据封装Map
        Map<String, Object> map = new HashMap<>();
        map.put("categoryList",categoryList);
        map.put("brandList",brandList);
        map.put("spceList",specmap);
        // 返回最终结果集
        return map;
    }

    /**
     * 处理聚合查询(分类，品牌，品牌)结果集
     * @param skuCategory
     * @return
     */
    private List<String> getGroupList(Aggregations aggregations,String groupName) {
        //获得词条数据
        StringTerms stringTerms = aggregations.get(groupName);
        List<StringTerms.Bucket> buckets = stringTerms.getBuckets();
        List<String> list = new ArrayList<>();
        for (StringTerms.Bucket bucket : buckets) {
            list.add(bucket.getKeyAsString());
        }
        return list;
    }



    /**
     * 统计规格分类列表查询
     * @param builder
     * @return
     */
    private Map<String, Set<String>> searchSpecList(NativeSearchQueryBuilder builder,Integer totalSize) {
        // 分组查询获得List                                   别名             对应kibana中的字段     每页加载的数据条数，写索引库的数据总数
        builder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword").size(totalSize));
        // 分组结果集
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);
        // 处理结果集
        Aggregations aggregations = aggregatedPage.getAggregations();
        // 获得词条数据
        StringTerms stringTerms = aggregations.get("skuSpec");
        List<StringTerms.Bucket> buckets = stringTerms.getBuckets();
        // 处理结果集封装list
        List<String> list = new ArrayList<>();
        for (StringTerms.Bucket bucket : buckets) {
            list.add(bucket.getKeyAsString());
        }
        // 获取的数据，列如数据内容:
        // {"电视音响效果":"小影院","电视屏幕尺寸":"20英寸","尺码":"170"}
        // 处理数据
       Map<String,Set<String>> map = pullMap(list);
        return map;
    }



    /**
     * 处理规格数据封装Map
     * @param list
     * @return
     */
    private Map<String, Set<String>> pullMap(List<String> list) {
        Map<String, Set<String>> map = new HashMap<>();
        for (String spec : list) {
            // 将字符JSON数据转Map
            Map<String,String> specMap = JSON.parseObject(spec, Map.class);
            // 遍历map
            Set<Map.Entry<String, String>> entrySet = specMap.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                // 电视音响效果":
                String key = entry.getKey();
                // 小影院 。。。。
                String value = entry.getValue();
                // value是多个且不能重复使用Set集合存储
                // 首先判断map中是否有set
                Set<String> set = map.get(key);
                if (set == null){
                // 判断set是否为空，如果是空就new HashSet
                    set = new HashSet<>();
                }
                // set不为空就直接往里面添加数据
                set.add(value);
                map.put(key,set);
            }
        }
        return map;
    }


    /**
     * 品牌分类列表查询
     *
     * @param builder
     * @return
     */
    private List<String> searchBrandList(NativeSearchQueryBuilder builder) {
        // 聚合查询                                         别名                对应kibana中的字段
        builder.addAggregation(AggregationBuilders.terms("skuBrand").field("brandName"));
        // 分组结果集
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);
        //处理结果集
        Aggregations aggregations = aggregatedPage.getAggregations();
        //获得词条数据
        StringTerms stringTerms = aggregations.get("skuBrand");
        List<StringTerms.Bucket> buckets = stringTerms.getBuckets();
        //处理结果集封装list
        List<String> list = new ArrayList<>();
        for (StringTerms.Bucket bucket : buckets) {
            list.add(bucket.getKeyAsString());
        }
        return list;
    }


    /**
     * 商品分类分组统计实现
     * @param builder
     * @return
     */
    private List<String> searchCategoryList(NativeSearchQueryBuilder builder) {
        // 聚合查询                                         别名                       对应kibana中的字段
        builder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName"));
        // 分组结果集
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);
        //处理结果集
        Aggregations aggregations = aggregatedPage.getAggregations();
        //获得词条数据
        StringTerms stringTerms = aggregations.get("skuCategory");
        List<StringTerms.Bucket> buckets = stringTerms.getBuckets();
        List<String> list = new ArrayList<>();
        for (StringTerms.Bucket bucket : buckets) {
            list.add(bucket.getKeyAsString());
        }
        return list;
    }


    /**
     * 根据关键字进行检索
     * @param builder
     * @return
     */
    private Map<String, Object> searchForPage(NativeSearchQueryBuilder builder) {
        // 关键字的高亮显示
        // 继续封装检索条件
        HighlightBuilder.Field field = new HighlightBuilder.Field("name");  //sku的name如果有关键字就进行高亮
        field.preTags("<font color='red'>");    // 开始标签
        field.postTags("</font>");              // 结束标签
        field.fragmentSize(100);              // 显示的字符个数
        builder.withHighlightFields(field);
        //取出高亮的结果数据，在该对象中
        SearchResultMapper searchResultMapper = new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                SearchHits hits = response.getHits();
               List<T> list = new ArrayList<>();
                // 处理结果集
                for (SearchHit hit : hits) {
                    // Json数据，普通数据 ，要转换pojo
                    String result = hit.getSourceAsString();
                    SkuInfo skuInfo = JSON.parseObject(result, SkuInfo.class);
                    // 获取高亮的数据
                    HighlightField highlightField = hit.getHighlightFields().get("name");
                    if (highlightField != null){
                        //有高亮数据
                        Text[] texts = highlightField.getFragments();
                        // 将普通的Sku名称(name)替换成高亮的名称(name)
                        skuInfo.setName(texts[0].toString());
                    }
                    // 每遍历一将sku高亮对象放入List中
                    list.add((T) skuInfo);
                }
                // 高亮结果集，将高亮数据替换普通的结果集arg01:高亮对象   arg02:参数传过来的pageable     arg03:总条数
                return new AggregatedPageImpl<>(list,pageable,hits.getTotalHits());
            }
        };
        NativeSearchQuery build = builder.build();
        //AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(build, SkuInfo.class);
        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(build,SkuInfo.class,searchResultMapper);
        Map<String,Object> map = new HashMap<>();
        //商品结果集
        map.put("rows",page.getContent());
        //总条数
        map.put("TotalElements",page.getTotalElements());
        //总页数
        map.put("TotalPages",page.getTotalPages());
        // 分页当前页码
        map.put("pageNum",build.getPageable().getPageNumber() + 1);
        // 每页显示条数
        map.put("pageSize",build.getPageable().getPageSize());
        return map;
    }


    /**此方法用于封装检索条件
     * @param searchMap
     * @return
     */
    private NativeSearchQueryBuilder builderBasicQuery(Map<String, String> searchMap) {
        // 封装检索条件
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        // 添加过滤条件
        BoolQueryBuilder boolBuilder = new BoolQueryBuilder();
        if (searchMap != null){

            // 1.根据关键字检索
            String keywords = searchMap.get("keywords");
            if (!StringUtils.isEmpty(keywords)){
               builder.withQuery(QueryBuilders.matchQuery("name", keywords));
            }

            // 继续拼接条件
            // 2.根据商品分类过滤
            String category = searchMap.get("category");
            if (!StringUtils.isEmpty(category)){
                boolBuilder.must(QueryBuilders.matchQuery("categoryName",category));
            }

            // 3.根据商品品牌过滤
            String brand = searchMap.get("brand");
            if (!StringUtils.isEmpty(brand)){
                boolBuilder.must(QueryBuilders.matchQuery("brandName",brand));
            }

            // 4.根据商品规格过滤(选择的规格有多个)
            // ::spec_屏幕尺寸 ：5.7, spec_内存 ：40G
            Set<String> keys = searchMap.keySet();
            for (String key : keys) {
                // 判断规格条件是否是spec_开头的
                if (key.startsWith("spec_")){
                    String value = searchMap.get(key).replace("\\","");
                    boolBuilder.must(QueryBuilders.matchQuery("specMap." + key.substring(5) +".keyword", value));
                }
            }

            // 5.根据商品价格过滤(区间段)
            String price = searchMap.get("price");
            if (!StringUtils.isEmpty(price)){
              // 页面传的价格式(min ~ max / >price /  <price)
                String[] priceArray = price.split("-");
                // 如果传的价格参数是一个就大于(>=)查询
                boolBuilder.must(QueryBuilders.rangeQuery("price").gte(priceArray[0]));
                if (priceArray.length > 1){
                // 如果传的价格参数是两个就小于(<=)查询
                boolBuilder.must(QueryBuilders.rangeQuery("price").lte(priceArray[1]));
                }
            }

            // 6.进行排序查询(排序字段,ASC DESC)
            // 排序的字段
            String sortField = searchMap.get("sortField");
            // 排序的规则(ASC DESC)
            String sortRule = searchMap.get("sortRule");
            if (!StringUtils.isEmpty(sortField)){
                builder.withSort(SortBuilders.fieldSort(sortField).order(SortOrder.valueOf(sortRule)));
            }
        }
            // 7. 将过滤的条件都加到builder中NativeSearchQueryBuilder
            builder.withFilter(boolBuilder);

        // 8. 添加分页条件 age1:当前页（page）   age2：每页显示数据（size）
        String page = searchMap.get("pageNum");
        if (StringUtils.isEmpty(page)){
            // 默认起始页为第一页
            page = "1";
        }
        int pageNum = Integer.parseInt(page);
        // 动态获得前端传
        String size = searchMap.get("size");
        // 默认一页显示10条数据
        if (StringUtils.isEmpty(size)){
            size = "10";
        }
        int pageSize = Integer.parseInt(size);
        Pageable pageable = PageRequest.of(pageNum - 1,pageSize);
        builder.withPageable(pageable);
        return builder;
    }
}

