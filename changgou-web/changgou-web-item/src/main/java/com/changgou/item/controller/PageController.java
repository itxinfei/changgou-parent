package com.changgou.item.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.item.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 */
@RestController
@RequestMapping("/page")
public class PageController {

    @Autowired(required = false)
    private PageService pageService;

    /**
     * 生成静态页
     *
     * @param id
     * @return
     */
    @GetMapping("/createHtml/{id}")
    public Result createHtml(@PathVariable(name = "id") Long id) {
        pageService.createHtml(id);
        return new Result(true, StatusCode.OK, "生成静态详情页面成功");
    }


}
