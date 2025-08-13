package com.portal.data.api.dto.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class JwtKey {
    @Value("${SECRET_KEY}")
    private String key;
    @Value("${EXPIRATION}")
    private long expiration;
}
