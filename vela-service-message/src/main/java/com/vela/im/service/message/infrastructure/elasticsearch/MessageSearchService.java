package com.vela.im.service.message.infrastructure.elasticsearch;

import com.vela.im.shared.base.Result;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MessageSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    public MessageSearchService(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public Result<Map<String, Object>> search(String keyword, String fromId, int page, int size) {
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                .withPageable(PageRequest.of(page, size));

        boolean hasFilter = false;

        if (keyword != null && !keyword.isEmpty()) {
            queryBuilder.withQuery(QueryBuilders.matchQuery("messageBody", keyword));
            hasFilter = true;
        }

        if (fromId != null && !fromId.isEmpty()) {
            queryBuilder.withFilter(QueryBuilders.termQuery("fromId", fromId));
        }

        if (!hasFilter && fromId == null) {
            queryBuilder.withQuery(QueryBuilders.matchAllQuery());
        }

        org.springframework.data.elasticsearch.core.SearchHits<MessageDocument> searchHits =
                elasticsearchOperations.search(queryBuilder.build(), MessageDocument.class);

        List<MessageDocument> list = searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        long total = searchHits.getTotalHits();

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("pages", (int) Math.ceil((double) total / size));
        return Result.ok(result);
    }
}
