package com.portal.data.api.dto.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ElasticModel {

    @Value("${ELASTIC_HOST}")
    private String clusterNodes;
    @Value("${ELASTIC_PORT}")
    private Integer port;
    @Value("${ELASTIC_USERNAME}")
    private String username;
    @Value("${ELASTIC_PASSWORD}")
    private String password;
}
