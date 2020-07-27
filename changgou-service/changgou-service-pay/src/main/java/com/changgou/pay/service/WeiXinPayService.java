package com.changgou.pay.service;

import java.util.Map;

/**
 * @Author: Ye Jian Song
 * @Description: 微信支付业务接口
 * @Date: Create in 17:27 2019/8/27
 */
public interface WeiXinPayService {

    /**
     * 关闭支付
     * @param orderId
     * @return
     * @throws Exception
     */
    Map<String,String> closePay(Long orderId) ;


    /**
     *  获得需要生成二微码码的参数,参数类型 key：value 所有用Map接收
     * @param out_trade_no :需要传入商户订单号
     * @param total_fee：需要传入支付金额
     * @return
     */
//    Map<String ,String> createNative(String out_trade_no,String total_fee);
    Map<String ,String> createNative(Map<String, String> params);

    /**
     * 查询订单的支付状态
     * @param out_trade_no
     * @return
     */
    Map<String ,String> queryStatus(String out_trade_no);

}
