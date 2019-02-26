package com.pay.wx;


import com.alibaba.fastjson.JSONObject;
import com.pay.wxutils.WXPayConstants;
import com.pay.wxutils.WXPayUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.io.*;
import java.security.KeyStore;

import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import sun.net.www.protocol.https.DefaultHostnameVerifier;

@RestController
@RequestMapping(value = "/app")
public class WxpayController {
    private static final String APPID = "";
    private static final String MCH_ID = "";
    private static final String BODY = "";
    private static final String API_PRIVATE_KEY = "";
    public static final Logger logger = LoggerFactory.getLogger(WxpayController.class);

    /**
     * APP微信支付
     */
    @RequestMapping("/wxController/pay.do")
    @ResponseBody
    public JSONObject micropay(JSONObject params) {

        Map<String, String> modelMap = new HashMap<String, String>();
        Map<String, String> reqData = new HashMap<String, String>();
        //保存到数据库
        Map<String, Object> saveMap = new HashMap<>();
        //封装给前端得map
        Map<String, String> newMap = new HashMap<String, String>();
        Map<String, Object> map = new HashMap<>();
        //查找订单信息
        /**
         * 查找是否存在该订单的逻辑
         */
        //获取该订单的金额
        /**
         * 金额从数据获取是最好的,避免别人抓包修改金额
         */
        reqData.put("appid", APPID);
        reqData.put("mch_id", MCH_ID);
        reqData.put("nonce_str", WXPayUtil.generateNonceStr());
        reqData.put("sign_type", WXPayConstants.HMACSHA256);
        reqData.put("body", BODY);
        //用户ID
        reqData.put("out_trade_no", "扩展字段,");
        //(微信支付的扩展字段,可以传入订单号或者是其他信息,
        // 微信异步回调接口会将信息返回,)
        reqData.put("attach", "");
        //金额测试阶段写死1分
        reqData.put("total_fee", "1");
        //reqData.put("total_fee", new BigDecimal(totalAmount).multiply(new BigDecimal(100)));
        reqData.put("spbill_create_ip", "获取当前支付的IP");
        //通知地址
        reqData.put("notify_url", "http://192.168.31.248:9528/app/wxback/payBack.do");
        //交易类型
        reqData.put("trade_type", "APP");
        Object o = null;
        //reqData.put("auth_code", "");
        try {
            String sign = WXPayUtil.generateSignature(reqData, API_PRIVATE_KEY, WXPayConstants.SignType.HMACSHA256);
            reqData.put("sign", sign);
            o = reqData;
            String s = com.github.wxpay.sdk.WXPayUtil.mapToXml(reqData);
            logger.info("参数返回");
            logger.info(s);
            String resultString =
                    requestOnce(WXPayConstants.DOMAIN_API,
                            WXPayConstants.UNIFIEDORDER_URL_SUFFIX, s, 6000, 10000, false);
            logger.info("结果返回");
            logger.info(resultString);
            //modelMap = WXPayUtil.xmlToMap(resultString);
            modelMap = com.github.wxpay.sdk.WXPayUtil.xmlToMap(resultString);
            newMap.put("appid", modelMap.get("appid"));
            newMap.put("partnerid", modelMap.get("mch_id"));
            newMap.put("prepayid", modelMap.get("prepay_id"));
            newMap.put("package", "Sign=WXPay");
            newMap.put("noncestr", modelMap.get("nonce_str"));
            newMap.put("timestamp", String.valueOf(WXPayUtil.getCurrentTimestamp()));
            String sign2 = WXPayUtil.generateSignature(newMap, API_PRIVATE_KEY, WXPayConstants.SignType.HMACSHA256);
            newMap.put("sign", sign2);
            saveMap.put("newMap", newMap.toString());
            HashMap<String, Object> dataMap = new HashMap<>();
            //数据包====>保存的数据包
            dataMap.put("newMap", newMap.toString());
            dataMap.put("reqData", o);
            /**
             * 业务逻辑部分....
             */
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data",newMap);
        return jsonObject;
    }

    /**
     * 支付成功回调
     *
     * @param
     * @throws IOException
     * @date 2018-11-19 16:37:36
     */
    @PostMapping("/wxback/payBack.do")
    @ResponseBody
    @Transactional
    public JSONObject payBack(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();
        response.reset();
        InputStream inStream = request.getInputStream();
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outSteam.write(buffer, 0, len);
        }
        outSteam.close();
        inStream.close();
        String result = new String(outSteam.toByteArray(), "utf-8");
        if (StringUtils.isEmpty(result)) {
            writer.write("request parameter is empty!");
            return null;
        }
        Map<String, String> map = null;
        try {
            map = WXPayUtil.xmlToMap(result);
            if (map.get("return_code").equals("SUCCESS")) {
                if (map.get("result_code").equals("SUCCESS")) {
                    // 这里是支付成功  的逻辑
                    for (int i = 0; i < 20; i++) {
                        logger.info("================>" + map.get("out_trade_no").toString());
                        logger.info("" + map.get("attach").toString());
                    }
                    /**
                     * 支付成功就改变订单状态
                     */
                    //用户Id
                    String userId = map.get("out_trade_no").toString();
                    //订单Id
                    String orderId = map.get("attach").toString();
                    //根据返回的信息查出微信更新订单的状态
                } else {
                    //这里是支付失败的逻辑
                }
            }
        } catch (Exception e) {
            logger.error("wxpay order notify error ,the response is {}", result, e);
        }
        try {
            //通知微信.异步确认成功.必写.不然会一直通知后台.八次之后就认为交易失败了.
            String backToWeixinXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
                    + "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
            logger.info("weixin payback notify xml is {} ,return to weixin xml is {}", result,
                    backToWeixinXml);
            writer.write(backToWeixinXml);
            writer.flush();
        } catch (Exception e) {
            logger.error("wxpay order notify to xml string error ,the response is {}", result, e);
        }
        return new JSONObject();
    }


    private static String requestOnce(String domain, String urlSuffix, String data,
                                      int connectTimeoutMs, int readTimeoutMs, boolean useCert) throws Exception {
        BasicHttpClientConnectionManager connManager;
        if (useCert) {
            // 证书
            char[] password = WXPayConstants.MCH_ID.toCharArray();
            // InputStream certStream = new FileInputStream(new File(""));
            KeyStore ks = KeyStore.getInstance("PKCS12");
            // ks.load(certStream, password);

            // 实例化密钥库 & 初始化密钥工厂
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, password);

            // 创建 SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());

            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
                    sslContext,
                    new String[]{"TLSv1"},
                    null,
                    new DefaultHostnameVerifier());

            connManager = new BasicHttpClientConnectionManager(
                    RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("http", PlainConnectionSocketFactory.getSocketFactory())
                            .register("https", sslConnectionSocketFactory)
                            .build(),
                    null,
                    null,
                    null
            );
        } else {
            connManager = new BasicHttpClientConnectionManager(
                    RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("http", PlainConnectionSocketFactory.getSocketFactory())
                            .register("https", SSLConnectionSocketFactory.getSocketFactory())
                            .build(),
                    null,
                    null,
                    null
            );
        }

        HttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(connManager)
                .build();

        String url = "https://" + domain + urlSuffix;
        HttpPost httpPost = new HttpPost(url);

        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(readTimeoutMs).setConnectTimeout(connectTimeoutMs).build();
        httpPost.setConfig(requestConfig);

        StringEntity postEntity = new StringEntity(data, "UTF-8");
        httpPost.addHeader("Content-Type", "text/xml");
        httpPost.addHeader("User-Agent", WXPayConstants.USER_AGENT + " " + WXPayConstants.MCH_ID);
        httpPost.setEntity(postEntity);

        HttpResponse httpResponse = httpClient.execute(httpPost);
        HttpEntity httpEntity = httpResponse.getEntity();
        return EntityUtils.toString(httpEntity, "UTF-8");

    }


    public Map<String, Object> queryOrder() {
        Map<String, String> reqData = new HashMap<String, String>();
        reqData.put("appid", "");
        reqData.put("mch_id", "");
        reqData.put("out_trade_no", "");
        reqData.put("nonce_str", WXPayUtil.generateNonceStr());
        reqData.put("sign_type", "HMAC-SHA256");
        try {
            String sign = WXPayUtil.generateSignature(reqData, "", WXPayConstants.SignType.HMACSHA256);
            reqData.put("sign", sign);

            String data = com.github.wxpay.sdk.WXPayUtil.mapToXml(reqData);
            logger.info("参数返回");
            logger.info(data);

            String resultString = requestOnce(WXPayConstants.DOMAIN_API,
                    WXPayConstants.ORDERQUERY_URL_SUFFIX, data, 6000, 10000, false);
            logger.info("结果返回");
            logger.info(resultString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public Map<String, Object> revocationOrder() {
        Map<String, String> reqData = new HashMap<String, String>();
        reqData.put("appid", "");
        reqData.put("mch_id", "");
        reqData.put("out_trade_no", "");
        reqData.put("nonce_str", WXPayUtil.generateNonceStr());
        reqData.put("sign_type", "HMAC-SHA256");
        try {
            String sign = WXPayUtil.generateSignature(reqData, "", WXPayConstants.SignType.HMACSHA256);
            reqData.put("sign", sign);

            String data = com.github.wxpay.sdk.WXPayUtil.mapToXml(reqData);
            logger.info("参数返回");
            logger.info(data);

            String resultString = requestOnce(WXPayConstants.DOMAIN_API,
                    WXPayConstants.REVERSE_URL_SUFFIX, data, 6000, 10000, false);
            logger.info("结果返回");
            logger.info(resultString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public Map<String, Object> requestRefund() {
        Map<String, String> reqData = new HashMap<String, String>();
        reqData.put("appid", "");
        reqData.put("mch_id", "");
        reqData.put("nonce_str", WXPayUtil.generateNonceStr());
        reqData.put("out_trade_no", "");
        reqData.put("out_refund_no", WXPayUtil.getRandomRefundCode());
        reqData.put("total_fee", "1");
        reqData.put("refund_fee", "1");
        reqData.put("sign_type", "HMAC-SHA256");

        try {
            String sign = WXPayUtil.generateSignature(reqData, "", WXPayConstants.SignType.HMACSHA256);
            reqData.put("sign", sign);

            String data = com.github.wxpay.sdk.WXPayUtil.mapToXml(reqData);
            logger.info("参数返回");
            logger.info(data);

            String resultString = requestOnce(WXPayConstants.DOMAIN_API,
                    WXPayConstants.REFUND_URL_SUFFIX, data, 6000, 10000, true);
            logger.info("结果返回");
            logger.info(resultString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public Map<String, Object> queryRefund() {
        Map<String, String> reqData = new HashMap<String, String>();
        reqData.put("appid", "");
        reqData.put("mch_id", "");
        reqData.put("nonce_str", WXPayUtil.generateNonceStr());
        reqData.put("out_trade_no", "");
        reqData.put("sign_type", "HMAC-SHA256");
        try {
            String sign = WXPayUtil.generateSignature(reqData, "", WXPayConstants.SignType.HMACSHA256);
            reqData.put("sign", sign);

            String data = com.github.wxpay.sdk.WXPayUtil.mapToXml(reqData);
            logger.info("参数返回");
            logger.info(data);

            String resultString = requestOnce(WXPayConstants.DOMAIN_API,
                    WXPayConstants.REFUNDQUERY_URL_SUFFIX, data, 6000, 10000, false);
            logger.info("结果返回");
            logger.info(resultString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 测试支付的main方法
     *
     * @param args
     */


    public static void main(String[] args) {
        Map<String, String> reqData = new HashMap<String, String>();
        reqData.put("appid", "");
        reqData.put("mch_id", "");
        reqData.put("out_trade_no", "");
        reqData.put("nonce_str", WXPayUtil.generateNonceStr());
        reqData.put("sign_type", "HMAC-SHA256");
        reqData.put("body", "111");
        //商户订单号
        reqData.put("out_trade_no", WXPayUtil.generateNonceStr());
        //总金额  分为单位
        reqData.put("total_fee", "1");
        //终端IP
        reqData.put("spbill_create_ip", "127.0.0.1");
        //通知地址


        reqData.put("notify_url", "");
        //
        reqData.put("trade_type", "NATIVE");
        try {
            String sign = WXPayUtil.generateSignature(reqData, "", WXPayConstants.SignType.HMACSHA256);
            reqData.put("sign", sign);

            String data = com.github.wxpay.sdk.WXPayUtil.mapToXml(reqData);
            logger.info("参数返回");
            logger.info(data);

            String resultString = requestOnce(WXPayConstants.DOMAIN_API,
                    WXPayConstants.ORDERQUERY_URL_SUFFIX, data, 6000, 10000, false);
            logger.info("结果返回");
            logger.info(resultString);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}



