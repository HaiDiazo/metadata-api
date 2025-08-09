package com.portal.data.api.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.portal.data.api.utils.DataUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class MetadataService {

    private final ElasticsearchClient elasticsearchClient;

    public MetadataService(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    public record SearchResult(Long totalHits, List<Map> hits) {}

    public SearchResult getIndexElasticsearch(
        String region,
        String pathData,
        String medallion,
        String source
    ) {
        try {
            List<Query> mustClauses = new ArrayList<>();

            if (region != null) {
                mustClauses.add(new Query.Builder()
                        .term(term -> term
                        .field("region")
                        .value(region)
                ).build());
            }

            if (pathData != null) {
                mustClauses.add(new Query.Builder()
                        .term(term -> term
                        .field("path_data.keyword")
                        .value(pathData)
                ).build());
            }

            if (medallion != null) {
                mustClauses.add(new Query.Builder()
                        .term(term -> term
                        .field("medallion")
                        .value(medallion)
                ).build());
            }

            if (source != null) {
                mustClauses.add(new Query.Builder()
                        .term(term -> term
                        .field("source")
                        .value(source)
                ).build());
            }

            Query finalQuery;
            if (!mustClauses.isEmpty()) {
                finalQuery = new Query.Builder()
                        .bool(bool -> bool.must(mustClauses))
                        .build();
            } else {
                finalQuery = new Query.Builder().matchAll(match -> match).build();
            }


            SearchResponse<Map> response = elasticsearchClient.search(
        s -> s.index("portal-metadata-dataset")
                .trackTotalHits(track -> track.enabled(true))
                .query(finalQuery)
                .sort(sort -> sort
                .field(field -> field.field("crawling_time")
                .order(SortOrder.Desc)
            )), Map.class);

            Long totalHits = response.hits().total().value();
            List<Map> hits = response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .peek(data -> {
                        Object crawlingTime = data.get("crawling_time");
                        if (crawlingTime != null) {
                            String datetime = DataUtils.convertEpochToDatetime(crawlingTime);
                            data.put("crawling_time", datetime);
                        }

                        Object updateDate = data.get("update_date");
                        if (updateDate != null) {
                            String datetimeUpdatedate = DataUtils.convertEpochToDatetime(updateDate);
                            data.put("update_date", datetimeUpdatedate);
                        }

                        Object createDate = data.get("create_date");
                        if (createDate != null) {
                            String datetimeCreateDate = DataUtils.convertEpochToDatetime(createDate);
                            data.put("create_date", datetimeCreateDate);
                        }
                    }).toList();
            return new SearchResult(totalHits, hits);

        } catch (IOException e) {
            throw new RuntimeException("Failed to query Elasticsearch", e);
        }
    }
}
