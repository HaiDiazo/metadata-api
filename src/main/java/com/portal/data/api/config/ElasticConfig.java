package com.portal.data.api.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.portal.data.api.dto.config.ElasticModel;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticConfig {

    private final Logger logger = LoggerFactory.getLogger(ElasticConfig.class);

    @Autowired
    private ElasticModel elasticModel;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        printConfig();
        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(elasticModel.getUsername(), elasticModel.getPassword())
        );

        RestClientBuilder restClientBuilder = RestClient.builder(
                new HttpHost(elasticModel.getClusterNodes(), elasticModel.getPort(), "http")
        ).setHttpClientConfigCallback(httpClientBuilder ->
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
        );

        RestClientTransport transport = new RestClientTransport(
                restClientBuilder.build(),
                new JacksonJsonpMapper()
        );
        return new ElasticsearchClient(transport);
    }

    private void printConfig() {
        logger.info("Elastic Host: {}:{}", elasticModel.getClusterNodes(), elasticModel.getPort());
        logger.info("Elastic Username: {}", elasticModel.getUsername());
        logger.info("Elastic Password: {}", elasticModel.getPassword());
    }
}
