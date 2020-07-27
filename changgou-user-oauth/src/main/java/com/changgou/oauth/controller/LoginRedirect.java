package com.changgou.oauth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author: Ye Jian Song
 * @Description:
 * @Date: Create in 20:48 2019/8/25
 */
@Controller
@RequestMapping("/oauth")
public class LoginRedirect {

    /**
     * 用户登录
     * @param ReturnUrl
     * @param model
     * @return
     */
    @GetMapping("/login")
    public String login(@RequestParam(value = "ReturnUrl",required = false) String ReturnUrl, Model model){
        model.addAttribute("ReturnUrl",ReturnUrl);
        return "login";
    }
}
