package com.changgou.oauth.util;


import com.alibaba.fastjson.JSON;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;


/**
 * @Author: Ye Jian Song
 * @Description:
 * @Date: Create in 16:56 2019/8/25
 */
public class JwtToken {
    public static String adminJwt(){
        //证书文件路径
        String key_location="changgou66.jks";
        //秘钥库密码
        String key_password="changgou66";
        //秘钥密码
        String keypwd = "changgou66";
        //秘钥别名
        String alias = "changgou66";

        //访问证书路径
        ClassPathResource resource = new ClassPathResource(key_location);

        //创建秘钥工厂
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource,key_password.toCharArray());

        //读取秘钥对(公钥、私钥)
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(alias,keypwd.toCharArray());

        //获取私钥
        RSAPrivateKey rsaPrivate = (RSAPrivateKey) keyPair.getPrivate();

        //定义Payload9(载荷,给生成的令牌加权限)
        Map<String, Object> tokenMap = new HashMap<>();
        // key 就是解析后的authorities, value是权限 信息可以有多个
        tokenMap.put("authorities", new String[]{"admin"});

        //生成Jwt令牌
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(tokenMap), new RsaSigner(rsaPrivate));

        //取出令牌
        String encoded = jwt.getEncoded();
        return encoded;


    }
}
