package com.portal.data.api.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MetadataStandarization {
    private String id;
    private String type;
    private String standarization;
    private String query;
}
