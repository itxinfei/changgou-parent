package com.changgou.oauth.controller;

import com.changgou.oauth.service.AuthService;
import com.changgou.oauth.service.UserLoginService;
import com.changgou.oauth.util.AuthToken;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * @Author: Ye Jian Song
 * @Description: 模拟用户登录并且授权
 * @Date: Create in 17:04 2019/8/23
 */
@RestController
@RequestMapping("/user")
public class UserLoginController {

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private AuthService authService;

    // 注入配置文件clientId
    @Value("${auth.clientId}")
    private String clientId;
    // 注入配置文件clientSecret
    @Value("${auth.clientSecret}")
    private String clientSecret;


    @RequestMapping("/login")
    public Result login(String username, String password, HttpServletResponse response) {
        try {
            // 定义授权方式(密码授权)
            //String grant_type = "password";
            AuthToken authToken = authService.login(username, password, clientId, clientSecret);
            // 将token存到cookie中
            Cookie cookie = new Cookie("Authorization",authToken.getAccessToken());
            // 跨域
            cookie.setDomain("localhost");
            cookie.setPath("/");
            response.addCookie(cookie);
            return new Result(true, StatusCode.OK,"登录成功",authToken);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,StatusCode.LOGINERROR,"登录失败,用户名或密码错误");
        }

        // 将token放到cookie中解析请求
//        String token = map.get("access_token").toString();
//        Cookie cookie = new Cookie("Authorization",token);
//        cookie.setDomain("localhost");
//        cookie.setPath("/");
//        response.addCookie(cookie);
    }





}
