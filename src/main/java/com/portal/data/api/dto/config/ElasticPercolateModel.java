package com.portal.data.api.dto.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ElasticPercolateModel {
    @Value("${ELASTIC_PERCOLATE_HOST}")
    private String host;
    @Value(("${ELASTIC_PERCOLATE_PORT}"))
    private int port;
    @Value("${ELASTIC_PERCOLATE_USERNAME}")
    private String username;
    @Value("${ELASTIC_PERCOLATE_PASSWORD}")
    private String password;
}
