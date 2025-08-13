package com.portal.data.api.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.PercolateQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.CountRequest;
import co.elastic.clients.elasticsearch.core.CountResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.portal.data.api.dto.requests.MetadataRequest;
import com.portal.data.api.dto.requests.PercolateRequest;
import com.portal.data.api.utils.DataUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class MetadataService {

    private final ElasticsearchClient elasticsearchClient;
    private final ElasticsearchClient elasticsearchClientPercolate;

    public MetadataService(
            @Qualifier("esClientMetadata") ElasticsearchClient elasticsearchClient,
            @Qualifier("esClientPercolate") ElasticsearchClient elasticsearchClientPercolate) {
        this.elasticsearchClient = elasticsearchClient;
        this.elasticsearchClientPercolate = elasticsearchClientPercolate;
    }

    public record SearchResult(Long totalHits, List<Map> hits) {}

    private Integer parseSimple(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        if (obj instanceof JsonData) {
            try { return ((JsonData) obj).to(Integer.class); } catch (Exception ignored) {}
        }
        if (obj instanceof String) {
            try { return Integer.parseInt((String) obj); } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private void standarizationEs(String raw, String typeStandarization, MetadataRequest metadataRequest) {

        PercolateQuery percolateQuery;
        if (!typeStandarization.equals("content_type")) {
            PercolateRequest percolateRequest = new PercolateRequest(raw);
            percolateQuery = PercolateQuery.of(percolate -> percolate
                    .field("query")
                    .document(JsonData.of(percolateRequest)));
        } else {
            List<JsonData> percolateRequests = new ArrayList<>();
            for (String contentType: metadataRequest.getContentType()) {
                PercolateRequest percolateRequest = new PercolateRequest(contentType);
                percolateRequests.add(JsonData.of(percolateRequest));
            }
            percolateQuery = PercolateQuery.of(percolate -> percolate
                    .field("query")
                    .documents(percolateRequests));
        }
        try {
            SearchResponse<Map> response = elasticsearchClientPercolate.search(search -> search
                        .index("portal-metadata-standarization")
                        .query(query -> query
                        .bool(bool -> bool
                        .filter(filter -> filter
                        .term(term -> term.field("type").value(typeStandarization)))
                        .must(must -> must
                        .percolate(percolateQuery)))), Map.class
            );
            List<Hit<Map>>hits = response.hits().hits()
                    .stream()
                    .filter(Objects::nonNull)
                    .toList();

            for (Hit hit: hits) {
                int docSlot = 0;

                Map source = (Map) hit.source();
                JsonData slotData = (JsonData) hit.fields().get("_percolator_document_slot");
                if (slotData != null) {
                    Object slotRaw = slotData.to(Object.class);
                    if (slotRaw instanceof List) {
                        List<?> list = (List<?>) slotRaw;
                        if (!list.isEmpty()) {
                            docSlot = (int) list.get(0);
                        }
                    }
                }

                if (source != null) {
                    String standarization = source.get("standarization").toString();
                    switch (typeStandarization) {
                        case "region":
                            metadataRequest.setRegion(standarization);
                            break;
                        case "level":
                            metadataRequest.setLevel(standarization);
                            break;
                        case "crawl_method":
                            metadataRequest.setCrawlMethod(standarization);
                            break;
                        case "schedule_interval":
                            metadataRequest.setScheduleInterval(standarization);
                            break;
                        case "content_type":
                            List<String> contentType = metadataRequest.getContentType();
                            contentType.set(docSlot, standarization);
                            metadataRequest.setContentType(contentType);
                            break;
                    }
                }
                if (!typeStandarization.equals("content_type")) {
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MetadataRequest getStandarizationMetadata(MetadataRequest metadataRequest) {

        standarizationEs(metadataRequest.getRegion(), "region", metadataRequest);
        standarizationEs(metadataRequest.getCrawlMethod(), "crawl_method", metadataRequest);
        standarizationEs(metadataRequest.getScheduleInterval(), "schedule_interval", metadataRequest);
        standarizationEs(metadataRequest.getLevel(), "level", metadataRequest);
        standarizationEs(null, "content_type", metadataRequest);
        return metadataRequest;
    }

    private void queryMustClauses(List<Query> mustClauses, String value, String type) {
        if (value != null) {
                mustClauses.add(new Query.Builder()
                .term(term -> term
                .field(type)
                .value(value)
                ).build()
            );
        }
    }

    public SearchResult getIndexElasticsearch(
        String region,
        String pathData,
        String medallion,
        String source
    ) {
        try {
            List<Query> mustClauses = new ArrayList<>();

            queryMustClauses(mustClauses, region, "region");
            queryMustClauses(mustClauses, pathData, "path_data.keyword");
            queryMustClauses(mustClauses, medallion, "medallion");
            queryMustClauses(mustClauses, source, "source");

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

    public long getTotalDataset(
        String region,
        String pathData,
        String medallion,
        String source
    ) {
        try {
            List<Query> mustClauses = new ArrayList<>();

            queryMustClauses(mustClauses, region, "region");
            queryMustClauses(mustClauses, pathData, "path_data.keyword");
            queryMustClauses(mustClauses, medallion, "medallion");
            queryMustClauses(mustClauses, source, "source");

            Query finalQuery;
            if (!mustClauses.isEmpty()) {
                finalQuery = new Query.Builder()
                        .bool(bool -> bool.must(mustClauses))
                        .build();
            } else {
                finalQuery = new Query.Builder().matchAll(match -> match).build();
            }

            CountResponse response = elasticsearchClient.count(
                    CountRequest.of(count -> count
                    .index("portal-metadata-dataset")
                    .query(finalQuery))
            );
            return response.count();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
