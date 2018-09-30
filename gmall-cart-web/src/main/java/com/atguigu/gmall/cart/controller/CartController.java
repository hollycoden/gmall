package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.bean.CartInfo;
import com.atguigu.bean.SkuInfo;
import com.atguigu.gmall.annotation.LoginRequire;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.service.CartService;
import com.atguigu.service.ManageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class CartController {

    @Reference
    ManageService manageService;

    @Reference
    CartService cartService;


    /**
     * 修改购物车的勾选状态，返回最新数据
     * @param cartInfo
     * @param request
     * @param response
     * @param map
     * @return
     */
    @LoginRequire(needSucess = false)
    @RequestMapping("checkCart")
    public String checkCart(CartInfo cartInfo ,HttpServletRequest request, HttpServletResponse response, ModelMap map){

        List<CartInfo> cartInfos = new ArrayList<>();
        String skuId = cartInfo.getSkuId();
        String userId = (String) request.getAttribute("userId");

        //修改购物车的勾选状态
        if (StringUtils.isNotBlank(userId)){

            //用户已登录，修改db
            cartInfo.setUserId(userId);
            cartService.updateCartByUserId(cartInfo);
            cartInfos = cartService.getCartInfosFromCacheByUserId(userId);

        } else {
            //修改cookie
            String listCartCookie = CookieUtil.getCookieValue(request, "listCartCookie", true);
            cartInfos = JSON.parseArray(listCartCookie, CartInfo.class);

            for (CartInfo info : cartInfos) {
                String skuId1 = info.getSkuId();

                if (skuId1.equals(skuId)){
                    info.setIsChecked(cartInfo.getIsChecked());
                }
            }
            //覆盖浏览器
            CookieUtil.setCookie(request,response,"listCartCookie",JSON.toJSONString(cartInfos),1000*60*60*24,true);
        }

        //返回购物车列表的最新数据
        map.put("cartList",cartInfos);
        BigDecimal totalPrice = getTotalPrice(cartInfos);
        map.put("totalPrice",totalPrice);
        return "cartListInner";
    }


    /**
     * 购物车列表,去购物车结算
     * @param request
     * @param map
     * @return
     */
   @LoginRequire(needSucess = false)
   @RequestMapping("cartList")
   public String cartList(HttpServletRequest request, ModelMap map){

       //声明一个处理后的购物车集合对象
       List<CartInfo> cartInfos = new ArrayList<>();

       String userId = (String) request.getAttribute("userId");

       //取出购物车集合
       if (StringUtils.isBlank(userId)){

           //用户尚未登录，从cookie中取值
           String cookieValue = CookieUtil.getCookieValue(request, "listCartCookie", true);

           if (StringUtils.isNotBlank(cookieValue)){

               //如果缓存不为空，加入到处理后的购物车集合对象
               cartInfos = JSON.parseArray(cookieValue, CartInfo.class);
           }

       }else {

           //如果用户已经登录，从redis中取值
           cartInfos = cartService.getCartInfosFromCacheByUserId(userId);
       }

       map.put("cartList",cartInfos);

       BigDecimal totalPrice = getTotalPrice(cartInfos);

       map.put("totalPrice",totalPrice);

       return "cartList";
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


    /**
     * 加入购物车
     * @param request
     * @param response
     * @param map
     * @return
     */
    @LoginRequire(needSucess = false)
    @RequestMapping("addToCart")
    public String addToCart(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String,String> map){

        //声明一个处理过后的购物车集合对象
        List<CartInfo> cartInfos = new ArrayList<>();

        //获取加入购物车的sku详情
        String skuId = map.get("skuId");
        Integer skuNum = Integer.parseInt(map.get("num"));

        SkuInfo skuInfo = manageService.getSkuInfo(skuId);

        //封装购物车对象
        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setCartPrice(skuInfo.getPrice().multiply(new BigDecimal(skuNum)));
        cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
        cartInfo.setIsChecked("1");
        cartInfo.setSkuPrice(skuInfo.getPrice());
        cartInfo.setSkuNum(skuNum);
        cartInfo.setSkuName(skuInfo.getSkuName());

        //判断用户是否登录
        String userId = (String) request.getAttribute("userId");

        if (StringUtils.isBlank(userId)){

            //用户尚未登录，cookie没有用户id
            cartInfo.setUserId("");

            //用户尚未登录,判断购物车cookie中是否有数据
            String cookieValue = CookieUtil.getCookieValue(request, "listCartCookie", true);

            if (StringUtils.isBlank(cookieValue)){

                //购物车中没有数据，直接添加
                cartInfos.add(cartInfo);
            } else {

                //购物车中有数据，判断是更新还是新增
                cartInfos = JSON.parseArray(cookieValue, CartInfo.class);
                boolean b = if_new_cart(cartInfos,cartInfo);

                if (b){
                    //新增
                    cartInfos.add(cartInfo);
                } else {
                    //遍历购物车中的数据，更新相同数据
                    for (CartInfo info : cartInfos) {
                        if (info.getSkuId().equals(cartInfo.getSkuId())){
                            info.setSkuNum(info.getSkuNum()+cartInfo.getSkuNum());
                            info.setCartPrice(info.getSkuPrice().multiply(new BigDecimal(info.getSkuNum())));
                        }
                    }
                }
            }
            //将购物车数据放入cookie
            CookieUtil.setCookie(request,response,"listCartCookie", JSON.toJSONString(cartInfos),
                    1000*60*60*24,true);
        } else {
            //用户已经登录
            cartInfo.setUserId(userId);

            //判断用户db是否存在该数据
            CartInfo cartInfoDb = cartService.ifCartExist(cartInfo);

            if (null != cartInfoDb){
                //存在，更新
                cartInfoDb.setSkuNum(cartInfo.getSkuNum()+cartInfoDb.getSkuNum());
                cartInfoDb.setCartPrice(cartInfoDb.getSkuPrice().multiply(new BigDecimal(cartInfoDb.getSkuNum())));
                cartService.updateCart(cartInfoDb);
            } else {
                //直接添加
                cartService.insertCart(cartInfo);
            }
            // 同步缓存
            cartService.flushCartCacheByUserId(userId);
        }

        return "redirect:/cartSuccess？skuId="+skuId+"&skuNum="+skuNum;
    }


    /**
     * 判断购物车数据是更新还是新增
     * @param listCartCookie
     * @param cartInfo
     * @return
     */
    private boolean if_new_cart(List<CartInfo> listCartCookie, CartInfo cartInfo){

        boolean b = true;

        //如果相同返回false
        for (CartInfo info : listCartCookie) {
            if (info.getSkuId().equals(cartInfo.getSkuId())){
                b = false;
            }
        }
        return b;
    }


    @LoginRequire(needSucess = false)
    @RequestMapping("cartSuccess")
    public String cartSuccess(ModelMap map, String skuId, String skuNum){
        //添加购物车成功后返回的重定向页面

        SkuInfo skuInfo = manageService.getSkuInfo(skuId);

        map.put("skuInfo",skuInfo);
        map.put("skuNum",skuNum);
        return "success";
    }
}
