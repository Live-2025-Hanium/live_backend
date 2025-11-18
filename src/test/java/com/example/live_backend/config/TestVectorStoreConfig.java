package com.example.live_backend.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.Collections;
import java.util.List;

@Configuration
@Profile("test")
public class TestVectorStoreConfig {

    @Bean
    @Primary
    public VectorStore mockVectorStore() {
        return new VectorStore() {
            @Override
            public void add(List<Document> documents) {
            }

            @Override
            public void delete(List<String> idList) {

            }

            @Override
            public void delete(Filter.Expression filterExpression) {

            }

            @Override
            public List<Document> similaritySearch(SearchRequest request) {
                return Collections.emptyList();
            }

            @Override
            public List<Document> similaritySearch(String query) {
                return Collections.emptyList();
            }
        };
    }
}
