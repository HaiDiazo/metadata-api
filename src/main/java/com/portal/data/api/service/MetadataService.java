package com.portal.data.api.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.PercolateQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.CountRequest;
import co.elastic.clients.elasticsearch.core.CountResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.util.NamedValue;
import com.portal.data.api.dto.requests.MetadataRequest;
import com.portal.data.api.dto.requests.PercolateRequest;
import com.portal.data.api.dto.response.MetadataStandarization;
import com.portal.data.api.utils.DataUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

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

    public record StandarizationResult(boolean isStandarization, String reason) {}

    public record MetadataReasonStandarization(
            boolean isStandarization,
            String reasonStandarization,
            MetadataRequest metadataRequest
    ) {}

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

    private StandarizationResult standarizationEs(String raw, String typeStandarization, MetadataRequest metadataRequest) throws IOException {

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

        if (hits.isEmpty() && raw != null) {
            return new StandarizationResult(
                    false,
                    "Found not standarization in %s with value %s".formatted(typeStandarization, raw)
            );
        } else if (hits.isEmpty()) {
            return new StandarizationResult(
                    false,
                    "Found not standarization in %s with value %s".formatted(
                            typeStandarization,
                            metadataRequest.getContentType().toString()
                    )
            );
        }

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
        return new StandarizationResult(
                true,
                ""
        );
    }

    public MetadataReasonStandarization getStandarizationMetadata(MetadataRequest metadataRequest) throws IOException {

        StandarizationResult res;
        boolean isStandarization = true;
        StringBuilder reason = new StringBuilder();

        res = standarizationEs(metadataRequest.getRegion(), "region", metadataRequest);
        if (!res.isStandarization) {
            isStandarization = false;
            reason.append(", ").append(res.reason);
        }

        res = standarizationEs(metadataRequest.getCrawlMethod(), "crawl_method", metadataRequest);
        if (!res.isStandarization) {
            isStandarization = false;
            reason.append(", ").append(res.reason);
        }

        res = standarizationEs(metadataRequest.getScheduleInterval(), "schedule_interval", metadataRequest);
        if (!res.isStandarization) {
            isStandarization = false;
            reason.append(", ").append(res.reason);
        }

        res = standarizationEs(metadataRequest.getLevel(), "level", metadataRequest);
        if (!res.isStandarization) {
            isStandarization = false;
            reason.append(", ").append(res.reason);
        }

        res = standarizationEs(null, "content_type", metadataRequest);
        if (!res.isStandarization) {
            isStandarization = false;
            reason.append(", ").append(res.reason);
        }

        if (!reason.isEmpty()) {
            isStandarization = false;
            reason.append(", ").append("Not Sending Into Queue");
        }

        return new MetadataReasonStandarization(
                isStandarization,
                reason.toString(),
                metadataRequest
        );
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

    public List<Map<String, Object>> getTimeseriesBySource(
        String source,
        String startDate,
        String endDate
    ) {
        try {
            Query query;
            if (source != null) {
                query = new Query.Builder().bool(bool -> bool
                        .filter(filter -> filter
                        .range(range -> range
                        .field("crawling_time")
                        .gte(JsonData.of(startDate))
                        .lte(JsonData.of(endDate))
                        .format("yyyy-MM-dd HH:mm:ss")))
                        .must(must -> must
                        .term(term -> term
                        .field("source")
                        .value(source)))).build();
            } else {
                query = new Query.Builder().bool(bool -> bool
                        .filter(filter -> filter
                        .range(range -> range
                        .field("crawling_time")
                        .gte(JsonData.of(startDate))
                        .lte(JsonData.of(endDate))
                        .format("yyyy-MM-dd HH:mm:ss")))).build();
            }

            SearchResponse<Map> response = elasticsearchClient.search(search -> search
                    .index("portal-metadata-dataset")
                    .query(query)
                    .aggregations("range_date", aggregation -> aggregation
                    .dateHistogram(dateHistogram -> dateHistogram
                    .field("crawling_time")
                    .calendarInterval(CalendarInterval.Day)
                    .format("yyyy-MM-dd")
                    .order(NamedValue.of("_key", SortOrder.Desc)))),
                    Map.class
            );

            Aggregate aggregateMap = response.aggregations().get("range_date");
            DateHistogramAggregate dateHistogram = aggregateMap.dateHistogram();

            List<Map<String, Object>> results = new ArrayList<>();
            for (DateHistogramBucket bucket: dateHistogram.buckets().array()) {
                Map<String, Object> mapped = new HashMap<>();
                mapped.put("date", bucket.keyAsString());
                mapped.put("value", bucket.docCount());

                results.add(mapped);
            }

            return results;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Map<String, String>> getTypeStandarization() {
        try {
            SearchResponse<Map> response = elasticsearchClientPercolate.search(search -> search
                    .index("portal-metadata-standarization")
                    .size(0)
                    .aggregations("type_collect", aggregation -> aggregation
                    .terms(term -> term
                    .field("type"))), Map.class);

            Aggregate aggregateMap = response.aggregations().get("type_collect");
            StringTermsAggregate termsAggregate = aggregateMap.sterms();

            List<Map<String, String>> resultsMapped = new ArrayList<>();
            for (StringTermsBucket termsBucket: termsAggregate.buckets().array()) {

                Map<String, String> mapped = new HashMap<>();

                String key = termsBucket.key().stringValue();
                mapped.put("key", key);
                resultsMapped.add(mapped);
            }
            return resultsMapped;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<MetadataStandarization> getStandarization(String type) {
        try {
            Query query;
            if (type != null) {
                query = new Query.Builder().bool(bool -> bool
                        .must(must -> must
                        .term(term -> term
                        .field("type")
                        .value(type)))).build();
            } else {
                query = new Query.Builder().matchAll(match -> match).build();
            }

            SearchResponse<Map> response = elasticsearchClientPercolate.search(search -> search
                    .index("portal-metadata-standarization")
                    .size(100)
                    .query(query),
                    Map.class);


            return response.hits().hits().stream().map(mapHit -> {
                if (mapHit.source() != null) {
                    Map<String, Object> fieldQuery = (Map<String, Object>) mapHit.source().get("query");
                    Map<String, Object> queryString = (Map<String, Object>) fieldQuery.get("query_string");

                    return new MetadataStandarization(
                        mapHit.id(),
                        String.valueOf(mapHit.source().get("type")),
                        String.valueOf(mapHit.source().get("standarization")),
                        String.valueOf(queryString.get("query"))
                    );
                }
                return null;
            }).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
