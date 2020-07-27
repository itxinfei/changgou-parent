package com.changgou.token;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName CreateJwt66Test
 * @Description
 * @Author 传智播客
 * @Date 18:40 2019/8/21
 * @Version 2.1
 **/
public class CreateJwt66Test {

    /***
     * 创建令牌测试
     */
    @Test
    public void testCreateToken(){
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

        //定义Payload
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("id", "1");
        tokenMap.put("name", "changgou");
        tokenMap.put("roles", "ROLE_VIP,ROLE_USER");

        //生成Jwt令牌
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(tokenMap), new RsaSigner(rsaPrivate));

        //取出令牌
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
    }


    /***
     * 校验令牌
     */
    @Test
    public void testParseToken(){
        //令牌
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6WyJhcHAiXSwibmFtZSI6bnVsbCwiaWQiOm51bGwsImV4cCI6MTk5ODcyMTcwNywiYXV0aG9yaXRpZXMiOlsic2Vja2lsbF9saXN0IiwidXNlciIsImdvb2RzX2xpc3QiXSwianRpIjoiZDE4NjcwZDEtZmIyOC00ZWZhLWFkODAtYmU4YWRmOWJlOWMyIiwiY2xpZW50X2lkIjoiY2hhbmdnb3UiLCJ1c2VybmFtZSI6ImNoYW5nZ291In0.I91jFAQJiCRnQW-Qub3xc3l8zlLI1jp4u2JNh2n8MifXBAqBpuQj6KtAcmXBZei_TRpxiijpjHNg155eNC0kez1Yi8ijvIiB_K60twMmD89ABNHadp7AUxeF6O5o6snI_9CQLAKrgozFGXGgz-ReWtijcaDpjhe3ugEHFjAXCrPM2gqVy84PIgIJEllqIZrFQqrq9UgD7c2VwiJgjWcki2XVlbIoS-7WMr4JjIBM_-6UPiUoLfibSOCMmA9rkejPdOGBRSA_QBhIqgz4AjnHCg_y_JByTgCqMVShVlntDHU8R58mtwTqMvuue8IqQKRxf6PHavsJRIXmGyn5oERIKw";

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
