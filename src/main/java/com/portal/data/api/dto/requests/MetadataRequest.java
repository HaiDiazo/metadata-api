package com.portal.data.api.dto.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class MetadataRequest {
    private String link;
    private String source;
    private List<String> tags;
    @JsonProperty("path_data")
    private String pathData;
    private List<String> paths;
    @JsonProperty("crawling_time")
    private long crawlingTime;
    private String category;
    @JsonProperty("sub_category")
    private String subCategory;
    @JsonProperty("sub_title")
    private String subTitle;
    private String title;
    @JsonProperty("table_name")
    private String tableName;
    @JsonProperty("updated_date")
    private long updatedDate;
    @JsonProperty("created_date")
    private long createdDate;
    private String desc;
    @JsonProperty("range_data")
    private String rangeData;
    private String region;
    private String level;
    private String stage;
    @JsonProperty("schedule_interval")
    private String scheduleInterval;
    @JsonProperty("crawl_method")
    private String crawlMethod;
    @JsonProperty("content_type")
    private List<String> contentType;
}
