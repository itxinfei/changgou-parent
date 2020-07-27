package entity;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * @Author: Ye Jian Song
 * @Description: 将所有的请求头放到requestTemplate(用于其他服务的调用)
 * @Date: Create in 18:00 2019/8/25
 */
@Configuration
public class FeignInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        // 微服务之间调用，也需要将令牌放入请求头中
       ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null){
            HttpServletRequest request = attributes.getRequest();
            // 获得所有的请求信息
            Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames != null){
               while (headerNames.hasMoreElements()){
                   // 获得请求头的key
                   String name = headerNames.nextElement();
                   // 获得所有请求头的value
                   String value = request.getHeader(name);
                   // 将请求头的key和value放到请求中
                   requestTemplate.header(name,value);
               }
            }
        }
    }
}
