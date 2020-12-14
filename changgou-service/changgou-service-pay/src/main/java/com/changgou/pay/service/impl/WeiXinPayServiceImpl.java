package com.changgou.pay.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.HttpClient;
import com.changgou.pay.service.WeiXinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author pangzhao
 */
@Service
public class WeiXinPayServiceImpl implements WeiXinPayService {

    // 微信公众账号或开放平台APP的唯一标识
    @Value("${weixin.appid}")
    private String appid;
    // 商户号
    @Value("${weixin.partner}")
    private String partner;
    // 商户密钥
    @Value("${weixin.partnerkey}")
    private String partnerkey;
    // 回调地址
    @Value("${weixin.notifyurl}")
    private String notifyurl;


    /**
     * 关闭微信支付
     *
     * @param orderId
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, String> closePay(Long orderId) {
        try {
            // 统一下单的url
            String url = "https://api.mch.weixin.qq.com/pay/closeorder";
            // 封装支付接口调用需要的参数
            Map<String, String> data = new HashMap<>();
            // 微信支付分配的公众账号ID（企业号corpid即为此appId）
            data.put("appid", appid);
            // 微信支付分配的商户号
            data.put("mch_id", partner);
            // 随机字符串，长度要求在32位以内。推荐随机数生成算法
            data.put("nonce_str", WXPayUtil.generateNonceStr());
            data.put("out_trade_no", String.valueOf(orderId));
            String signedXml = WXPayUtil.generateSignedXml(data, partnerkey);
            // 发送请求
            HttpClient httpClient = new HttpClient(url);
            httpClient.setHttps(true);
            httpClient.setXmlParam(signedXml);
            httpClient.post();
            // 获得返回数据
            String strXML = httpClient.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(strXML);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 生成二维码所需要的数据，生成二维码
     *
     * @param params :需要传入商户订单号 需要传入支付金额 用户名
     * @return
     */
    @Override
//    public Map<String, String> createNative(String out_trade_no,String total_fee) {
    public Map<String, String> createNative(Map<String, String> params) {
        try {
            // 统一下单的url
            String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
            // 封装支付接口调用需要的参数
            Map<String, String> data = new HashMap<>();
            // 微信支付分配的公众账号ID（企业号corpid即为此appId）
            data.put("appid", appid);
            // 微信支付分配的商户号
            data.put("mch_id", partner);
            // 随机字符串，长度要求在32位以内。推荐随机数生成算法
            data.put("nonce_str", WXPayUtil.generateNonceStr());
            // 通过签名算法计算得出的签名值，详见签名生成算法
            // data.put("sign",);
            // 商品简单描述，该字段请按照规范传递，具体请见参数规定
            data.put("body", "畅购商城");
            // 商户订单号
            data.put("out_trade_no", params.get("out_trade_no"));
            // 支付金额
            data.put("total_fee", params.get("total_fee"));
            // 支持IPV4和IPV6两种格式的IP地址。用户的客户端IP
            data.put("spbill_create_ip", "127.0.0.1");
            // 异步接收微信支付结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数。
            data.put("notify_url", notifyurl);
            // (支付类型)JSAPI -JSAPI支付 NATIVE -Native支付 APP -APP支付 说明详见参数规定
            data.put("trade_type", "NATIVE");

            // 附加数据(请求头携带大小有限制)
            String username = params.get("username");
            String exchange = params.get("exchange");
            String routingKey = params.get("routingKey");
            Map<String, String> attach = new HashMap<>();
            attach.put("username", username);
            attach.put("exchange", exchange);
            attach.put("routingKey", routingKey);

            data.put("attach", JSON.toJSONString(attach));

            // 接口需要将参数装拆XML类型数据(通过商户秘钥解析)
            String signedXml = WXPayUtil.generateSignedXml(data, partnerkey);
            // 创建HttpClient进行调用(统一下单的url)
            HttpClient httpClient = new HttpClient(url);
            httpClient.setHttps(true);          // 设置是否发以Https发送请求,可选择发送http
            httpClient.setXmlParam(signedXml);  // 发送请求所需的XML数据
            httpClient.post();                  // post请求
            // 发松请求
            String strXML = httpClient.getContent();
            // 处理响应数据键xml数据转成map 生成二维码的数据格式为: key : value
            Map<String, String> map = WXPayUtil.xmlToMap(strXML);
            // 前端传入的订单号和金额
            map.put("out_trade_no", params.get("out_trade_no"));
            map.put("total_fee", params.get("total_fee"));
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询订单的状态
     *
     * @param out_trade_no
     * @return
     */
    @Override
    public Map<String, String> queryStatus(String out_trade_no) {
        try {
            // 指定接口地址,查询订单的api
            String url = "https://api.mch.weixin.qq.com/pay/orderquery";
            // 构建接口需要的map
            Map<String, String> data = new HashMap<>();
            data.put("appid", appid);
            data.put("mch_id", partner);
            data.put("out_trade_no", out_trade_no);
            data.put("nonce_str", WXPayUtil.generateNonceStr());
            // 将数据转成xml
            String signedXml = WXPayUtil.generateSignedXml(data, partnerkey);
            // 创建HttpClient调用
            HttpClient httpClient = new HttpClient(url);
            httpClient.setHttps(true);
            httpClient.setXmlParam(signedXml);
            httpClient.post();
            String strXML = httpClient.getContent();
            // 处理响应数据
            Map<String, String> map = WXPayUtil.xmlToMap(strXML);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
