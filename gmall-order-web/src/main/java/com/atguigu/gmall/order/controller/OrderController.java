package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.bean.*;
import com.atguigu.bean.enums.PaymentWay;
import com.atguigu.gmall.annotation.LoginRequire;
import com.atguigu.service.CartService;
import com.atguigu.service.OrderService;
import com.atguigu.service.SkuService;
import com.atguigu.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    UserService userService;

    @Reference
    CartService cartService;

    @Reference
    SkuService skuService;

    @Reference
    OrderService orderService;

    @LoginRequire(needSucess = true)
    @RequestMapping("submitOrder")
    public String submitOrder(HttpServletRequest request, String tradeCode, String addressId){

        //验证用户是否登录，获得用户id
        String userId = (String) request.getAttribute("userId");

        //tradecode用来判断是否重符提交订单页面
        boolean b = orderService.checkTradeCode(userId,tradeCode);

        if (b){
            //获得用户的收货信息
            UserAddress userAddress = userService.getAddressById(addressId);

            //获得购物车信息
            List<CartInfo> cartInfos = cartService.getCartInfosFromCacheByUserId(userId);

            //声明订单对象
            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setProcessStatus("订单提交");
            orderInfo.setOrderStatus("订单未支付");

            //当前日期加一天
            Calendar instance = Calendar.getInstance();
            instance.add(Calendar.DATE,1);
            orderInfo.setExpireTime(instance.getTime());

            //外部订单号
            SimpleDateFormat s = new SimpleDateFormat("yyyyMMddHHmmss");
            String format = s.format(new Date());
            String outTradeNo = "atguigugmall"+format+System.currentTimeMillis();
            orderInfo.setOutTradeNo(outTradeNo);
            //收件人联系电话
            orderInfo.setConsigneeTel(userAddress.getPhoneNum());
            orderInfo.setCreateTime(new Date());
            orderInfo.setDeliveryAddress(userAddress.getUserAddress());
            orderInfo.setOrderComment("硅谷快递，即时送达");
            orderInfo.setTotalAmount(getTotalPrice(cartInfos));
            orderInfo.setUserId(userId);
            orderInfo.setPaymentWay(PaymentWay.ONLINE);
            orderInfo.setConsignee(userAddress.getConsignee());


            /*封装订单详情信息*/
            List<OrderDetail> orderDetails = new ArrayList<>();
            List<String> cartIds = new ArrayList<>();  //用来删除购物车中的信息
            for (CartInfo cartInfo : cartInfos) {
                if (cartInfo.getIsChecked().equals("1")){
                    String cartInfoId = cartInfo.getId();
                    cartIds.add(cartInfoId);
                    OrderDetail orderDetail = new OrderDetail();

                    //将cartInfo 转变为 orderDetail
                    BeanUtils.copyProperties(cartInfo,orderDetail);

                    //验库存
                    orderDetail.setHasStock("1");

                    //验价格
                    SkuInfo skuInfo = skuService.getSkuById(cartInfo.getSkuId());
                    if (skuInfo.getPrice().compareTo(cartInfo.getSkuPrice()) == 0){
                        orderDetail.setOrderPrice(cartInfo.getCartPrice());
                    } else {
                        return "OrderErr";
                    }
                    orderDetails.add(orderDetail);
                }
            }
            orderInfo.setOrderDetailList(orderDetails);

            //保存订单
            orderService.saveOrder(orderInfo);

            //删除购物车数据
            cartService.deleteCart(StringUtils.join(cartIds,","),userId);


            //提交订单后重定向到支付系统
            return "redirect:http://payment.gmall.com:8087/index?outTradeNo="+outTradeNo+"&totalAmount="+getTotalPrice(cartInfos);
        } else {
            return "OrderErr";
        }
    }


    @LoginRequire(needSucess = true)
    @RequestMapping("toTrade")
    public String toTrade(HttpServletRequest request, ModelMap map){

        //验证用户是否登录
        String userId = (String) request.getAttribute("userId");

        //获取用户收货地址
        List<UserAddress> userAddresses = userService.getAddressListByUserId(userId);

        //获取用户订单详情
        List<OrderDetail> orderDetails = new ArrayList<>();

        List<CartInfo> cartInfos = cartService.getCartInfosFromCacheByUserId(userId);

        //将cartInfo 转换成 orderDetail，并加入到orderDetails中
        for (CartInfo cartInfo : cartInfos) {
            if (cartInfo.getIsChecked().equals("1")){
                OrderDetail orderDetail = new OrderDetail();
                BeanUtils.copyProperties(cartInfo,orderDetail);
                orderDetails.add(orderDetail);
            }
        }


        map.put("userAddressList",userAddresses);
        map.put("orderDetailList",orderDetails);
        map.put("totalAmount",getTotalPrice(cartInfos));

        //生成一个唯一的交易码，一个订单只能提交一次
        String tradeCode = orderService.genTradeCode(userId);
        map.put("tradeCode",tradeCode);

        return "trade";
    }


    /**
     * 计算购物车的总价格
     * @param cartInfos
     * @return
     */
    private BigDecimal getTotalPrice(List<CartInfo> cartInfos){

        BigDecimal totalPrice = new BigDecimal("0");

        for (CartInfo cartInfo : cartInfos) {
            String isChecked = cartInfo.getIsChecked();

            if (isChecked.equals("1")){
                totalPrice = totalPrice.add(cartInfo.getCartPrice());
            }
        }
        return totalPrice;
    }
}
