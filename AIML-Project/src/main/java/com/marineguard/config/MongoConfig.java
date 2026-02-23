package com.marineguard.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.marineguard.repository")
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.host:localhost}")
    private String mongoHost;

    @Value("${spring.data.mongodb.port:27017}")
    private int mongoPort;

    @Value("${spring.data.mongodb.database:marineguard}")
    private String mongoDatabase;

    @Override
    protected String getDatabaseName() {
        return mongoDatabase;
    }

    @Override
    @Bean
    public MongoClient mongoClient() {
        String connectionString = String.format("mongodb://%s:%d", mongoHost, mongoPort);
        return MongoClients.create(connectionString);
    }

    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient(), getDatabaseName());

        // Remove _class field from documents (optional)
        MappingMongoConverter converter = (MappingMongoConverter) mongoTemplate.getConverter();
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));

        return mongoTemplate;
    }

    @Override
    protected boolean autoIndexCreation() {
        return true;
    }
}