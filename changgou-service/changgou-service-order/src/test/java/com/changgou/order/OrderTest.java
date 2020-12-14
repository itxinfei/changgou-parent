package com.changgou.order;
import com.changgou.entity.TokenDecode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class OrderTest {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 将redis序列化
     * @return
     */
//    @Bean
//    public RedisTemplate redisTemplateInit(){
//        redisTemplate.setKeySerializer(new StringRedisSerializer());
//        return redisTemplate;
//    }


    @Test
    public void test01(){
        //redisTemplate.delete("cart_laowang");
        //redisTemplate.opsForHash().get("cart_changgou", 1148478064120832000L);
        //Object key = redisTemplate.boundHashOps("cart_laowang").get("1148478064120832000");
       redisTemplate.delete("cart_changgou");
    }



    @Test
    public void test02(){
        TokenDecode tokenDecode = new TokenDecode();
        String username = tokenDecode.getUserInfo().get("username");
        System.out.println(username);
    }
}
