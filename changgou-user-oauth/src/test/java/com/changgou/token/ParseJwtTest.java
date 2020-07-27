package com.changgou.token;

import org.junit.Test;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;

/*****
 * @Author: shenkunlin
 * @Date: 2019/7/7 13:48
 * @Description: com.changgou.token
 *  使用公钥解密令牌数据
 ****/
public class ParseJwtTest {

    /***
     * 校验令牌
     */
    @Test
    public void testParseToken(){
        //令牌
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlcyI6IlJPTEVfVklQLFJPTEVfVVNFUiIsIm5hbWUiOiJpdGhlaW1hIiwiaWQiOiIxIn0.Wovb4zPf3p5_QvEPabz3c1YRFO-KrH20edA3recT5eIQi3UE69Ls5tOH7dLWkOXkD7Yxdgop2Zwkdp5AJdIEY05TBKR4TLH1tKtrimrmbkeZl6X6nkykkSAmN8SNi7hS0TzCMVgmqWtrqpFPAq9OymeGVs944CJfBpZYg5nVRHvFyYH57vB_diKE7XeOLeVc45141bBxOArJURpkHI3ELFkBjy2hFY-Ip5rVsfK-43gBOEgx6QqOW78_jtR-d81Wo5pGS_YMtEpE5TYPw_dBB_HKBi_1ATKookz-HaXEMCNjoXj-URV2Wx5Ii7fID14JLZyzcOf6nBeS5vQpaDNuXg";

        //公钥
        String publickey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjsMeQjIxjWQQ/bpvet+T4AEXx78zWzoNs83GpKvoczhAUZeajhAH01e25gZlukqL/zHok0PKMVKuR1p1Ro3AQE2qhkpIzjbOxzz7ez2qUo07uVNiFv4oOXvD5yq1p7l9uLFI4eFokYZhM1epTT5WjGJXxjQbCMeZleDLafT2yqKGaSMcOwQdtfJsdsWSdhwsdMuxkJ9Wyq9o3YOF/oCuqgJ4Ak0QnEB7VeWTe+a77YEbiRjdut4C3FTdhNobdI0EsiWps29MQDHZcZHclhO4LxhQ+Ett/RYXDkrKToJc3EVZ2J36++eSE8FdCy1c/qwyLb2Y/BHOPBjf1rpiEx8CYQIDAQAB-----END PUBLIC KEY-----";

        //校验Jwt
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publickey));

        System.out.println(jwt);

        //获取Jwt原始内容
        String claims = jwt.getClaims();
        System.out.println(claims);
        //jwt令牌
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
    }
}
