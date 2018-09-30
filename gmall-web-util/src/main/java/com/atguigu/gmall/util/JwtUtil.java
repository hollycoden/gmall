package com.atguigu.gmall.util;

import io.jsonwebtoken.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @param
 * @return
 */
public class JwtUtil {

    public static void main(String[] args){
        Map<String,String> map = new HashMap<>();
        map.put("userId","2");
        map.put("nickName","boge");

        //生成token
        String salt = "192.168.2.1";
        String atguigugmall0508 = encode("atguigugmall0508", map, salt);
        System.out.println(atguigugmall0508);

        //验证token
        Map atguigugmall05081 = decode("atguigugmall0508", atguigugmall0508, salt);
        System.out.println(atguigugmall05081);

    }


    /***
     * jwt加密
     * @param key
     * @param map
     * @param salt
     * @return
     */
    public static String encode(String key,Map map,String salt){

        if(salt!=null){
            key+=salt;
        }
        JwtBuilder jwtBuilder = Jwts.builder().signWith(SignatureAlgorithm.HS256, key);
        jwtBuilder.addClaims(map);

        String token = jwtBuilder.compact();
        return token;
    }

    /***
     * jwt解密
     * @param key
     * @param token
     * @param salt
     * @return
     * @throws SignatureException
     */
    public static  Map decode(String key,String token,String salt)throws SignatureException{
        if(salt!=null){
            key+=salt;
        }
        Claims map = null;

        map = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();

        System.out.println("map.toString() = " + map.toString());

        return map;

    }

}
