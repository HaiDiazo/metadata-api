package com.portal.data.api.dto.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class MongoModel {
    @Value("${MONGO_HOST}")
    private String host;
    @Value("${MONGO_PORT}")
    private Integer port;
    @Value("${MONGO_USERNAME}")
    private String username;
    @Value("${MONGO_PASSWORD}")
    private String password;
    @Value("${MONGO_DATABASE}")
    private String database;

    public String uri() {
        return "mongodb://%s:%s@%s:%s/%s".formatted(
            getUsername(),
            getPassword(),
            getHost(),
            getPort(),
            getDatabase()
        );
    }
}
