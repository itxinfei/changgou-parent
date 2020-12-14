package com.changgou.search.controller;

import com.changgou.entity.Page;
import com.changgou.search.feign.SkuInfoFeign;
import com.changgou.search.pojo.SkuInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/search")
public class SkuController {

    //注入skuInfoFeign调用SkuInfoController中的search
    @Autowired(required = false)
    private SkuInfoFeign skuInfoFeign;

    /**
     * 搜索页面回显数据
     * @param searchMap
     * @param model
     * @return
     */
    @GetMapping("/list")
    public String list(@RequestParam(required = false) Map<String,String> searchMap, Model model){
        // 处理特殊字符
        handlerSearchMap(searchMap);
        // 回显搜索服务数据
        Map<String, Object> result = skuInfoFeign.search(searchMap);
        model.addAttribute("result",result);
        // 回显检索条件
        model.addAttribute("searchMap",searchMap);
        // 拼接搜索url回显搜索条件
        String[] urls = getUrl(searchMap);
        // 搜索框显示搜索条件
        model.addAttribute("url",urls[0]);
        model.addAttribute("sorturl",urls[1]);
        //分页计算
        Page<SkuInfo> page = new Page<>(
                Long.parseLong(result.get("TotalElements").toString()),
                Integer.parseInt(result.get("pageNum").toString()),
                Integer.parseInt(result.get("pageSize").toString())
        );
        model.addAttribute("page", page);
        // 视图地址
        return "search";
    }

    /**
     * 处理关键字符
     * @param searchMap
     */
    private void handlerSearchMap(Map<String, String> searchMap) {
        if (searchMap != null){
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                if (entry.getKey().startsWith("spec_")){
                    entry.setValue(entry.getValue().replace("+","%2B"));
                }
            }
        }
    }

    /**
     * 拼接请求的url地址
     * @param searchMap
     * @return
     */
    private String[] getUrl(Map<String, String> searchMap) {
        // http://ip:port/search/list
        // http://ip:port/search/list?keywords=xxx
        // http://ip:port/search/list?keywords=xxx&catetory=xxx、
        String url= "/search/list";
        String sorturl= "/search/list";
        if (searchMap != null && searchMap.size() > 0){
            url += "?";
            sorturl += "?";
            // 获得map的所有Key然后拼接通过key获得的value
            Set<Map.Entry<String, String>> entrySet = searchMap.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (key.equals("pageNum")){
                    continue;
                }
                if (key.equals("sortRule") || key.equals("sortField")){
                    continue;
                }
                // 拼接
                url += key + "=" + value + "&";
                // 排序的url
                sorturl += key + "=" + value + "&";
            }
            // 去掉最后一个&
            url = url.substring(0, url.length() - 1);
            sorturl = sorturl.substring(0,url.length() - 1);
        }
        return new String[]{url,sorturl};
    }

}

