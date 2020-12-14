package com.changgou.pay.controller;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.pay.service.WeiXinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
* @Author pangzhao
*/
@RestController
@RequestMapping("/weixin/pay")
@CrossOrigin
public class WeiXinPayController {

    @Autowired(required = false)
    private WeiXinPayService weiXinPayService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Environment env;


    //@RequestMapping("/closePay")
    @GetMapping("/closePay/{orderId}")
    public Result closePay(@PathVariable("orderId") Long orderId){
        Map<String, String> map = weiXinPayService.closePay(orderId);
        return new Result(true, StatusCode.OK,"关闭微信支付成功",map);
    }



    /**
     * 获取微信的回调信息(回调,获取支付信息)
     * @return
     */
    @RequestMapping("/notify/url")
    public Map<String, String> notifyUrl(HttpServletRequest request) throws Exception{
        // 获取微信回调信息
        ServletInputStream inputStream = request.getInputStream();
        // 网络传输的字节流操作（内存操作）
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // 定义缓冲去
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len=inputStream.read(buffer))!=-1){
            byteArrayOutputStream.write(buffer, 0, len);
        }
        byteArrayOutputStream.flush();
        byteArrayOutputStream.close();
        inputStream.close();
        // 获取数据
        String strXML = new String(byteArrayOutputStream.toByteArray(), "UTF-8");
        System.out.println(strXML);
        Map<String, String> map = WXPayUtil.xmlToMap(strXML);
        System.out.println("回调数据:" + map);
        // 将map转JSON
        String attach = map.get("attach");
        Map<String, String> attachMap = JSON.parseObject(attach, Map.class);
        String exchange = env.getProperty(attachMap.get("exchange"));
        String routingKey = env.getProperty(attachMap.get("routingKey"));
        System.out.println("交换机:"+exchange+",路由器："+routingKey+",用户名:"+attachMap.get("username"));
        String jsonString = JSON.toJSONString(map);
        // 将参数发送到mq
        rabbitTemplate.convertAndSend(exchange,routingKey,jsonString);
        System.out.println("------------------------------------");
        return map;
    }

    /**
     * 生成支付的二维码
     * @param parames : 封装数据（订单，金额，用户名，交换机，路由）
     * @return
     */
    @RequestMapping("/create/native")
    //public Result createNavite(String outtradeno, String money){
    public Result createNavite(@RequestParam Map<String ,String> parames){
        Map<String, String> map = weiXinPayService.createNative(parames);
        return new Result(true, StatusCode.OK,"创建支付二维码成功",map);
    }


    /**
     * 查询支付状态
     * @param outtradeno
     * @return
     */
    @RequestMapping("/status/query")
    public Result queryStatus(String outtradeno){
        Map<String, String> map = weiXinPayService.queryStatus(outtradeno);
        return new Result(true,StatusCode.OK,"查询支付状态成功",map);
    }
}
