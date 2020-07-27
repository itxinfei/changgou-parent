package com.changgou.oauth.config;

import com.changgou.oauth.util.JwtToken;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * @Author: Ye Jian Song
 * @Description:
 * @Date: Create in 17:15 2019/8/25
 */
@Configuration
public class FeignOauth2RequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        // 获取的全部请求信息
       ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null){
            HttpServletRequest request = attributes.getRequest();
            // 获取所有的请求头信息
            Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames != null){
                while (headerNames.hasMoreElements()){
                    // 获取请求头的key
                    String element = headerNames.nextElement();
                    // 获取请求头的value
                    String value = request.getHeader(element);
                    // 将请求头信息放入到请求头
                    requestTemplate.header(element,value);
                }
            }
        }
        // 获得带有权限的令牌(将令牌放入请求头中,用于登录到的时候携带权限信息)
        String token = JwtToken.adminJwt();
        // 如果微服务之间相互调用，也需要将令牌放入请求头中，
        requestTemplate.header("Authorization","bearer " + token);
    }
}
