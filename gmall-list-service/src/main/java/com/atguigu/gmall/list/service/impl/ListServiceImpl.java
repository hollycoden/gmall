package com.atguigu.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.bean.BaseAttrInfo;
import com.atguigu.bean.SkuLsInfo;
import com.atguigu.bean.SkuLsParam;
import com.atguigu.service.AttrService;
import com.atguigu.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
public class ListServiceImpl implements ListService {

    @Autowired
    JestClient jestClient;

    @Reference
    AttrService attrService;

    @Override
    public List<SkuLsInfo> search(SkuLsParam skuLsParam) {

        //list页面所需要展现的数据的集合
        List<SkuLsInfo> skuLsInfos = new ArrayList<>();

        //封装搜索的数据
        Search search = new Search.Builder(getMyDsl(skuLsParam))
                .addIndex("gmall0508")
                .addType("SkuLsInfo").build();

        SearchResult execute = null;
        try {
            execute = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<SearchResult.Hit<SkuLsInfo, Void>> hits = execute.getHits(SkuLsInfo.class);
        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
            SkuLsInfo source = hit.source;

            /*关键字高亮*/
            Map<String, List<String>> highlight = hit.highlight;

            if (null != highlight){
                List<String> skuName = highlight.get("skuName");
                if (StringUtils.isNotBlank(skuName.get(0))){
                    //不为空说明存在高亮关键字
                    source.setSkuName(skuName.get(0));
                }
            }
            skuLsInfos.add(source);
        }

        return skuLsInfos;
    }




    /**
     * 封装Dsl 语句
     * @param skuLsParam
     * @return
     */
    public String getMyDsl(SkuLsParam skuLsParam){

        /*可能会携带的参数*/
        String keyword = skuLsParam.getKeyword();
        String catalog3Id = skuLsParam.getCatalog3Id();
        String[] valueId = skuLsParam.getValueId();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // bool查询
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        // 过滤
        if(StringUtils.isNotBlank(catalog3Id)){
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id",catalog3Id);
            boolQueryBuilder.filter(termQueryBuilder);
        }

        if(null!=valueId&&valueId.length>0){
            // 加载分类属性的条件
        }

        // 搜索
        if(StringUtils.isNotBlank(keyword)){
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName",keyword);
            boolQueryBuilder.must(matchQueryBuilder);
        }

        // 将属性参数放入查询
        searchSourceBuilder.query(boolQueryBuilder);

        //设置分页
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(100);

        //设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<span style='color:red;font-weight:bolder'>");
        highlightBuilder.field("skuName");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlight(highlightBuilder);

        return searchSourceBuilder.toString();
    }
}