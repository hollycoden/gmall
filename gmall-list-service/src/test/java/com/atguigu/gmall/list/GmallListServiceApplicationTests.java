package com.atguigu.gmall.list;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.bean.SkuInfo;
import com.atguigu.bean.SkuLsInfo;
import com.atguigu.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallListServiceApplicationTests {

    @Autowired
    JestClient jestClient;

    @Reference
    SkuService skuService;


    @Test
    public void contextLoads() throws IOException {

        List<SkuLsInfo> skuLsInfos = new ArrayList<>();
        //查询方法
//        Search search = new Search.Builder("{\n" +
//                "  \"query\": {},\n" +
//                "  \"from\": 0,\n" +
//                "  \"size\": 100\n" +
//                "}").addIndex("gmall0508")
//                .addType("SkuLsInfo")
//                .build();
        Search search = new Search.Builder(getMyDsl()).addIndex("gmall0508")
                .addType("SkuLsInfo").build();

        //封装结果集
        SearchResult execute = jestClient.execute(search);

        //获取结果集
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = execute.getHits(SkuLsInfo.class);

        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {

            //在hit中获取source，source是一个json数据
            SkuLsInfo source = hit.source;
            skuLsInfos.add(source);
        }
        System.out.println(skuLsInfos.size());
    }


    /**
     *  自动生成Dsl语句
     * @return
     */
    public String getMyDsl(){
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //bool查询
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        //过滤
        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", "61");
        boolQueryBuilder.filter(termQueryBuilder);

        //搜索
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", "小米");
        boolQueryBuilder.must(matchQueryBuilder);

        //将属性参数放入查询
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(100);

        return searchSourceBuilder.toString();
    }



    /**
     * 从数据库中查询数据插入到es中
     * @throws IOException
     */
    @Test
    public void addDate() throws IOException {

        //查询数据库中的sku信息
        List<SkuInfo> skuInfoList = skuService.getSkuByCatalog3Id(61);

        /*将skuInfo 转化成skuLsInfo*/
        List<SkuLsInfo> skuLsInfos = new ArrayList<>();

        for (SkuInfo skuInfo : skuInfoList) {
            SkuLsInfo skuLsInfo = new SkuLsInfo();

            //将source bean转换成 target bean，
            BeanUtils.copyProperties(skuInfo,skuLsInfo);

            skuLsInfos.add(skuLsInfo);
        }

        /*将SkuLsInfo插入es
        Index-新增  Search-查询*/
        for (SkuLsInfo skuLsInfo : skuLsInfos) {
            Index build = new Index.Builder(skuLsInfo)
                    .index("gmall0508").type("SkuLsInfo")
                    .id(skuLsInfo.getId()).build();
            jestClient.execute(build);
        }
        System.out.println(skuLsInfos.size());
    }
}