package com.portal.data.api.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.portal.data.api.dto.config.ElasticModel;
import com.portal.data.api.dto.config.ElasticPercolateModel;
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
    @Autowired
    private ElasticPercolateModel elasticPercolateModel;

    @Bean(name = "esClientMetadata")
    public ElasticsearchClient elasticsearchClient() {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        printConfig("metadata");

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

    @Bean(name = "esClientPercolate")
    public ElasticsearchClient elasticsearchClientPercolate() {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        printConfig("percolate");

        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(elasticPercolateModel.getUsername(), elasticPercolateModel.getPassword())
        );

        RestClientBuilder restClientBuilder = RestClient.builder(
                new HttpHost(elasticPercolateModel.getHost(), elasticPercolateModel.getPort(), "http")
        ).setHttpClientConfigCallback(httpAsyncClientBuilder -> httpAsyncClientBuilder
                .setDefaultCredentialsProvider(credentialsProvider)
        );

        RestClientTransport transport = new RestClientTransport(
                restClientBuilder.build(),
                new JacksonJsonpMapper()
        );
        return new ElasticsearchClient(transport);
    }

    private void printConfig(String clientType) {
        if (clientType.equals("percolate")) {
            logger.info("Elastic Host Percolate: {}:{}", elasticPercolateModel.getHost(), elasticPercolateModel.getPort());
            logger.info("Elastic Username Percolate: {}", elasticPercolateModel.getUsername());
            logger.info("Elastic Password Percolate: {}", elasticPercolateModel.getPassword());
        } else {
            logger.info("Elastic Host Metadata: {}:{}", elasticModel.getClusterNodes(), elasticModel.getPort());
            logger.info("Elastic Username: {}", elasticModel.getUsername());
            logger.info("Elastic Password: {}", elasticModel.getPassword());
        }
    }
}
