package com.changgou.test.java;

import com.changgou.entity.IdWorker;
import io.jsonwebtoken.*;
import org.junit.Test;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Ye Jian Song
 * @Description:
 * @Date: Create in 21:40 2019/8/22
 */
public class JwtTest {


    /**
     * 创建令牌
     */
    @Test
    public void testCreateJwt(){
        JwtBuilder builder = Jwts.builder()
                .setId("888")                 // 设置唯一编号
                .setSubject("小白")           // 设置主题  可以是JSON数据
                .setIssuedAt(new Date())     // 设置签发日期
                .setExpiration(new Date())   // 设置令牌过期时间
                .signWith(SignatureAlgorithm.HS256, "changgou"); //设置签名 使用HS256算法，并设置SecretKey(字符串)

        //构建 并返回一个字符串
        System.out.println(builder.compact());
        //eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI4ODgiLCJzdWIiOiLlsI_nmb0iLCJpYXQiOjE1NjY0ODIwNTV9.B1Z7iovE7yE7Xje2h8GJDWc0-9RYCaeZ05h0vFEfcBk
        //eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI4ODgiLCJzdWIiOiLlsI_nmb0iLCJpYXQiOjE1NjY0ODIwNzJ9.eM4NGeL7e3bO4GsYJPinRW71HXzeVD18jpxLY1P3pu0
    }

    /**
     * 进行J问题令牌数据
     */
    @Test
    public void testParseJwt(){
        String compactJwt = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI4ODgiLCJzdWIiOiLlsI_nmb0iLCJpYXQiOjE1NjY0ODIwNTV9.B1Z7iovE7yE7Xje2h8GJDWc0-9RYCaeZ05h0vFEfcBk";
        Claims claims = Jwts.parser()
                .setSigningKey("changgou")   // 令牌签名
                .parseClaimsJws(compactJwt)  // 要解析的令牌数据
                .getBody();
        System.out.println(claims);
    }

    /**
     * 测试过期时间
     */
    @Test
    public void testParseJwtDate(){
        String compactJwt = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI4ODgiLCJzdWIiOiLlsI_nmb0iLCJpYXQiOjE1NjY0ODI0OTgsImV4cCI6MTU2NjQ4MjQ5OH0.el2O3fHBhYcZAaLZ2t_ptGDAu6fpj1MAOBWG5fDNy7U";
        Claims claims = Jwts.parser()
                .setSigningKey("changgou")   // 令牌签名
                .parseClaimsJws(compactJwt)  // 要解析的令牌数据
                .getBody();
        System.out.println(claims);
    }

    /**
     *  Base64编码解码
     * @throws Exception
     */
    @Test
    public void testEncoder() throws Exception{
        String msg = "www.changgou.com";
        // 编码
        byte[] encode = Base64.getEncoder().encode(msg.getBytes("UTF-8"));
        String encodeMsg = new String(encode, "UTF-8");
        System.out.println("编码后：" + encodeMsg);

        // 解码
        byte[] decode = Base64.getDecoder().decode(encode);
        String decodeMsg = new String(decode, "UTF-8");
        System.out.println("解码后：" + decodeMsg);
    }

    /**
     * 自定义创建令牌
     */
    @Test
    public void createToken(){
        // 创建JWT
        JwtBuilder builder = Jwts.builder();
        // 构建头信息
        Map<String, Object> map = new HashMap<>();
        map.put("alg", "HS256");
        map.put("keyId", "JWT");
        builder.setHeader(map);
        // 构建载荷信息
        builder.setId("001");
        builder.setIssuer("张三");
        builder.setIssuedAt(new Date());
        //builder.setExpiration(new Date(System.currentTimeMillis()+30000));

        // 自定义荷载信息
        Map<String,Object> claims = new HashMap<>();
        claims.put("school","东川路男子职业技术学院");
        claims.put("address","莲花南路剑川路");
        builder.setClaims(claims);
        builder.setExpiration(new Date(System.currentTimeMillis()+30000));
        // 添加签名
        builder.signWith(SignatureAlgorithm.HS256, "changgou");

        // 生成token
        String token = builder.compact();
        System.out.println("token:" + token);
    }

    @Test
    public void testParseToken(){
        // 需要解析的令牌
        String token = "eyJrZXlJZCI6IkpXVCIsImFsZyI6IkhTMjU2In0.eyJhZGRyZXNzIjoi6I6y6Iqx5Y2X6Lev5YmR5bed6LevIiwic2Nob29sIjoi5Lic5bed6Lev55S35a2Q6IGM5Lia5oqA5pyv5a2m6ZmiIiwiZXhwIjoxNTY2NTMzNzgxfQ.GYx4qAsq7IakXXeECK8x3POPzAjfkjEIM89QGhrvFc8";
        // 创建解析对象
        JwtParser parser = Jwts.parser();
        parser.setSigningKey("changgou");
        Claims claims = parser.parseClaimsJws(token).getBody();
        System.out.println(claims);
    }
    @Test
    public void createID(){
        IdWorker idWorker=new IdWorker(1,1);
        for(int i=0;i<10000;i++){
            long id = idWorker.nextId();
            System.out.println(id);
        }
    }
}
