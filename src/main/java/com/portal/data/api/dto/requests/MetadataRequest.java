package com.portal.data.api.dto.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class MetadataRequest {
    @NotNull(message = "link cannot be null")
    private String link;
    @NotNull(message = "source cannot be null")
    private String source;
    private List<String> tags;
    @NotNull(message = "path_data cannot be null")
    @JsonProperty("path_data")
    private String pathData;
    private List<String> paths;
    @NotNull(message = "crawling_time cannot be null")
    @JsonProperty("crawling_time")
    private long crawlingTime;
    @NotNull(message = "category cannot be null")
    private String category;
    @JsonProperty("sub_category")
    private String subCategory;
    @JsonProperty("sub_title")
    private String subTitle;
    @NotNull(message = "title cannot be null")
    private String title;
    @JsonProperty("updated_date")
    private long updatedDate;
    @JsonProperty("created_date")
    private long createdDate;
    private String desc;
    @JsonProperty("range_data")
    private String rangeData;
    @NotNull(message = "region cannot be null")
    private String region;
    @NotNull(message = "level cannot be null")
    private String level;
    @NotNull(message = "stage cannot be null")
    private String stage;
    @NotNull(message = "schedule_interval cannot be null")
    @JsonProperty("schedule_interval")
    private String scheduleInterval;
    @NotNull(message = "crawl_method cannot be null")
    @JsonProperty("crawl_method")
    private String crawlMethod;
    @NotNull(message = "content_type cannot be null")
    @JsonProperty("content_type")
    private List<String> contentType;
}
