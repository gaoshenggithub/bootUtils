package com.pay.ali;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.pay.model.AliConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RestController
@RequestMapping("/app")
public class AlipayController {
    @Autowired
    private AliConfig aliConfig;

    /*
     * 获取支付宝加签后台的订单信息字符串
     *统一下单
     * @param params
     * @return
     */
    @RequestMapping("/pay/getAliPayOrder.do")
    public JSONObject getAliPayOrderStr(JSONObject params) {
        //接收JSONObject参数
        String orderId = params.get("字段").toString();
        //初始化数据包对象
        Map<String, Object> resultData = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("orderId", orderId);
        //查找订单信息
        /**
         * 业务逻辑.查看是否有此订单的存在.假业务
         */

        //订单金额

        /**
         * 从数据库拿去金额,以防被人抓包修改金额,业务逻辑
         */
        //获取客户端的数据最后保存到支付记录表
        System.err.println("=========================================");
        //实例化客户端
        AlipayClient alipayClient = new DefaultAlipayClient(
                aliConfig.getURL(),
                aliConfig.getAPPID(),
                aliConfig.getRSA_PRIVATE_KEY(),
                aliConfig.getFORMAT(),
                aliConfig.getCHARSET(),
                aliConfig.ALIPAY_PUBLIC_KEY,
                aliConfig.getSIGNTYPE());

        //实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        //SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        model.setBody("");//此处设置为付款的名称
        model.setSubject("LongXin代理费用");
        /**
         * 我们设置一个唯一id给支付宝.等待回调的时候,支付宝会将信息回调给我们,
         * 我们根据这个字段去更新数据库的支付状态,也可以设置为其他参数
         * 比如auttch
         */
        model.setOutTradeNo(orderId);//流水号
        model.setTimeoutExpress("30m");//设置交易金额时间
        //测试阶段暂时写死一分
        model.setTotalAmount("0.01");
        //默认就行
        model.setProductCode("QUICK_MSECURITY_PAY");
        resultData.put("body", model.getBody());
        resultData.put("subJect", model.getSubject());
        resultData.put("outTradeNo", model.getOutTradeNo());
        resultData.put("timeOutExpress", model.getTimeoutExpress());
        resultData.put("totalAmount", model.getTotalAmount());
        resultData.put("productCode", model.getProductCode());
        request.setBizModel(model);
        request.setNotifyUrl(aliConfig.getNOTIFY_URL());
        //request.setReturnUrl(AlipayConfig.RETURN_URL);
        String orderInfo = "";
        //这里和普通的接口调用不同，使用的是sdkExecute
        /**
         * 将我们上面的信息填写返回给支付宝.支付宝给返回一个数据
         */
        try {
            AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);
            orderInfo = response.getBody();//最后返回给前端的数据..
            /**
             * 下面就是自己的业务逻辑.
             * 根据自己的情况而定..
             * 都是业务逻辑..修改订单的状态改为未支付
             * 还有一些表的日志插入记录.等待回调的时候.将未支付改为支付
             * 建议包上面的信息整理为一个数据包.放入数据库,方便以后支付支付出错
             * 好去查找原因,,最好是能获取对方IP
             */
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        JSONObject result = new JSONObject();
        result.put("code", 200);
        result.put("data", orderInfo);
        result.put("message", "SUCCESS");
        return result;
    }


    /**
     * 支付宝回调接口
     * <p>
     * 支付宝支付成功后.异步请求该接口
     * 异步回调需要部署到服务器上面才能测试
     *
     * @return
     */
    @RequestMapping("/alipay/notify_url.do")//===>和我们自定定义回调路径一样
    @ResponseBody
    public JSONObject noitfy(HttpServletRequest request, HttpServletResponse response) {
        JSONObject result = new JSONObject();
        //获取支付宝POST过来反馈信息
        Map<String, String> params = new HashMap<String, String>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }
        //移到下面做处理
        String notify = notify(requestParams);
        if (notify.equals("fail")) {
            result.put("code", 200);
            result.put("data", "");
            result.put("message", "FAIL");
            return result;
        }
        result.put("code", 200);
        result.put("data", "");
        result.put("message", "FAIL");
        return result;
    }

    /**
     * 支付宝异步请求逻辑处理
     *
     * @param params
     * @return
     */
    public String notify(Map<String, String> params) {
        //签名验证(对支付宝返回的数据验证，确定是支付宝返回的)
        boolean signVerified = false;
        try {
            //调用SDK验证签名
            signVerified = AlipaySignature.rsaCheckV1(params,
                    aliConfig.getALIPAY_PUBLIC_KEY(),
                    aliConfig.getCHARSET(), aliConfig.getSIGNTYPE());
        } catch (AlipayApiException e) {
            return "FAIL";
        }
        if (signVerified) {
            //验签通过
            //获取需要保存的数据
            //支付宝分配给开发者的应用Id
            String appId = params.get("app_id");
            //通知时间:yyyy-MM-dd HH:mm:ss
            String notifyTime = params.get("notify_time");
            //交易创建时间:yyyy-MM-dd HH:mm:ss
            String gmtCreate = params.get("gmt_create");
            //交易付款时间
            String gmtPayment = params.get("gmt_payment");
            //交易退款时间
            String gmtRefund = params.get("gmt_refund");
            //交易结束时间
            String gmtClose = params.get("gmt_close");
            //支付宝的交易号
            String tradeNo = params.get("trade_no");
            //获取商户之前传给支付宝的订单号（商户系统的唯一订单号）
            String outTradeNo = params.get("out_trade_no");
            //商户业务号(商户业务ID，主要是退款通知中返回退款申请的流水号)
            String outBizNo = params.get("out_biz_no");
            //买家支付宝账号
            String buyerLogonId = params.get("buyer_logon_id");
            //卖家支付宝用户号
            String sellerId = params.get("seller_id");
            //卖家支付宝账号
            String sellerEmail = params.get("seller_email");
            //订单金额:本次交易支付的订单金额，单位为人民币（元）
            String totalAmount = params.get("total_amount");
            //实收金额:商家在交易中实际收到的款项，单位为元
            String receiptAmount = params.get("receipt_amount");
            //开票金额:用户在交易中支付的可开发票的金额
            String invoiceAmount = params.get("invoice_amount");
            //付款金额:用户在交易中支付的金额
            String buyerPayAmount = params.get("buyer_pay_amount");
            // 获取交易状态！
            String tradeStatus = params.get("trade_status");
            //业务逻辑
            //1.判断数据库是否存在该订单

            //获取金额
            //2.对比数据库的订单金额是否和支付宝返回的订单金额相同

            //3.对比支付宝的appId和自己的本地的appId是否相同

            // 判断交易结果
            //只处理支付成功的订单: 修改交易表状态,支付成功
            if (tradeStatus.equals("TRADE_SUCCESS")) {
                //支付成功,开始修改状态
                return "SUCCESS";
            } else {//更新数据失败
                return "FAIL";
            }
        } else {  //验签不通过
            return "FAIL";
        }
    }

    /**
     * 向支付宝发起订单查询请求
     *
     * @param
     * @return
     * @throws
     */

   /* @PostMapping("/alipay/check.do")
    @ResponseBody
    @Transactional
    public ApiResponse checkAlipay(ApiRequest apiRequest) {
        Map reqMap = getReqMap(apiRequest);
        String serielNo = getMapString(reqMap, "serielNo");
        log.info("==================向支付宝发起查询，查询商户订单号为：" + serielNo);

        try {
            //实例化客户端（参数：网关地址、商户appid、商户私钥、格式、编码、支付宝公钥、加密类型）
            AlipayClient alipayClient = new DefaultAlipayClient(
                    gDqunyuConfigZFB.getUrl(),
                    gDqunyuConfigZFB.getAppid(),
                    gDqunyuConfigZFB.getRsa_private_key(),
                    gDqunyuConfigZFB.getFormat(),
                    gDqunyuConfigZFB.getCharset(),
                    gDqunyuConfigZFB.getRsa_private_key(),
                    gDqunyuConfigZFB.getSigntype());
           *//*  AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, AlipayConfig.APPID,
                    AlipayConfig.RSA_PRIVATE_KEY, AlipayConfig.FORMAT, AlipayConfig.CHARSET,
                    AlipayConfig.ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE);*//*
            AlipayTradeQueryRequest alipayTradeQueryRequest = new AlipayTradeQueryRequest();
            alipayTradeQueryRequest.setBizContent("{" +
                    "\"out_trade_no\":\"" + serielNo + "\"" +
                    "}");
            AlipayTradeQueryResponse alipayTradeQueryResponse = alipayClient.execute(alipayTradeQueryRequest);
            if (alipayTradeQueryResponse.isSuccess()) {

                // AlipaymentOrder alipaymentOrder=this.selectByOutTradeNo(outTradeNo);
                //  //修改数据库支付宝订单表
//                alipaymentOrder.setBuyerLogonId(alipayTradeQueryResponse.getBuyerLogonId());
                //      alipaymentOrder.setTotalAmount(Double.parseDouble(alipayTradeQueryResponse.getTotalAmount()));
                //       alipaymentOrder.setReceiptAmount(Double.parseDouble(alipayTradeQueryResponse.getReceiptAmount()));
                //       alipaymentOrder.setInvoiceAmount(Double.parseDouble(alipayTradeQueryResponse.getInvoiceAmount()));
                //       alipaymentOrder.setBuyerPayAmount(Double.parseDouble(alipayTradeQueryResponse.getBuyerPayAmount()));
                switch (alipayTradeQueryResponse.getTradeStatus()) // 判断交易结果
                {
                    case "TRADE_FINISHED": // 交易结束并不可退款
                        //   alipaymentOrder.setTradeStatus((byte) 3);
                        break;
                    case "TRADE_SUCCESS": // 交易支付成功
                        //    alipaymentOrder.setTradeStatus((byte) 2);
                        break;
                    case "TRADE_CLOSED": // 未付款交易超时关闭或支付完成后全额退款
                        //     alipaymentOrder.setTradeStatus((byte) 1);
                        break;
                    case "WAIT_BUYER_PAY": // 交易创建并等待买家付款
                        //      alipaymentOrder.setTradeStatus((byte) 0);
                        break;
                    default:
                        break;
                }
                //this.updateByPrimaryKey(alipaymentOrder); //更新表记录
                // return alipaymentOrder.getTradeStatus();
            } else {
                log.info("==================调用支付宝查询接口失败！");
            }
        } catch (AlipayApiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ApiResponse.getDefaultResponse();
    }*/
}
