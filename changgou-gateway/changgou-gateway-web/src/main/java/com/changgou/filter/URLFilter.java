package com.changgou.filter;

/**
 * @Author: Ye Jian Song
 * @Description: 此配置用于后期维护需要放行的URL
 * @Date: Create in 19:00 2019/8/25
 */
public class URLFilter {
    // 后期需要方向的请求url
    private static String uri = "/api/user/login,/api/user/add,/api/search,/api/weixin/pay/notify/url";

    // 判断用户的请求是否包含上面允许放行的uri
    public static boolean hasAuthorization(String url){
        String[] uris = uri.split(",");
        for (String uri : uris) {
            if (url.startsWith(uri)){
                return true;            // 放行
            }
        }
        return false;                   // 拦截
    }
}
