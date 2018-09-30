package com.atguigu.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.bean.OrderInfo;
import com.atguigu.bean.PaymentInfo;
import com.atguigu.gmall.annotation.LoginRequire;
import com.atguigu.gmall.payment.conf.AlipayConfig;
import com.atguigu.service.OrderService;
import com.atguigu.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class paymentController {

    @Autowired
    AlipayClient alipayClient;

    @Reference
    OrderService orderService;

    @Autowired
    PaymentService paymentService;

    @RequestMapping("alipay/callback/return")
    public String alipayReturn(HttpServletRequest request){

        //回调接口首先要验证阿里的签名
        try {
            boolean signVerified = AlipaySignature.rsaCheckV1(null, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type); //调用SDK验证签名

            if(signVerified){
                // TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
            }else{
                // TODO 验签失败则记录异常日志，并在response中返回failure.
            }
        } catch (Exception e) {
            System.out.println("验证阿里的签名");
        }

        //签名验证通过后，继续执行支付成功的业务
        String alipayTradeNo = (String)request.getParameter("trade_no");
        String callBack = request.getQueryString();
        String out_trade_no = (String)request.getParameter("out_trade_no");
        String sign = request.getParameter("sign");
        System.err.println("sign签名="+sign);

        //更新支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setAlipayTradeNo(alipayTradeNo);
        paymentInfo.setPaymentStatus("已支付");
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setCallbackContent(callBack);
        paymentInfo.setOutTradeNo(out_trade_no);

        // 更新支付信息
        paymentService.updatePaymentSuccess(paymentInfo);

        return "finish";
    }

    @LoginRequire(needSucess = true)
    @RequestMapping("index")
    public String index(HttpServletRequest request, String outTradeNo, String totalAmount,ModelMap map){

        String userId = (String) request.getAttribute("userId");

        map.put("outTradeNo",outTradeNo);
        map.put("totalAmount",totalAmount);

        return "index";
    }


    @RequestMapping("alipay/submit")
    @ResponseBody
    public String alipay(String outTradeNo){

        OrderInfo orderInfo = orderService.getOrderInfoByOutTradeNo(outTradeNo);

        //设置支付宝page.pay的参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url); //在公共参数中设置回跳和通知地址

        //设置即时到账系统对应的参数
        Map<String,Object> map = new HashMap<>();
        map.put("out_trade_no",outTradeNo);
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount",0.01);
        map.put("subject",orderInfo.getOrderDetailList().get(0).getSkuName());
        map.put("body","硅谷支付产品测试");

        String s = JSON.toJSONString(map);

        alipayRequest.setBizContent(s);//填充业务参数

        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        //<from action=">
        // 表单内容
        //</form>
        System.out.println(form);
        //保存交易信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(outTradeNo);
        paymentInfo.setPaymentStatus("未支付");
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setSubject(orderInfo.getOrderDetailList().get(0).getSkuName());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentService.savePayment(paymentInfo);

        //发送定时检查的延迟队列
        paymentService.sendDelayPaymentResult(paymentInfo.getOutTradeNo(),1000,5);

        return form;
    }
}
