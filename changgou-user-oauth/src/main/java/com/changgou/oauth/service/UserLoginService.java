package com.changgou.oauth.service;

import com.changgou.oauth.util.AuthToken;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * @Author: Ye Jian Song
 * @Description:
 * @Date: Create in 17:04 2019/8/23
 */
public interface UserLoginService {

    /**
     * 授权操作
     * @param username
     * @param password
     * @param clientId
     * @param clientSecret
     * @param grant_type
     */
    AuthToken login(String username, String password, String clientId, String clientSecret, String grant_type) throws UnsupportedEncodingException;
}
