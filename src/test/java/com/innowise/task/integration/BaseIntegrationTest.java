package com.innowise.task.integration;

import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegrationTest
{
    public static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("user")
            .withPassword("password");

    public static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7")
            .withExposedPorts(6379);

    static {
        POSTGRES.start();
        REDIS.start();
    }

    @LocalServerPort
    protected int port;

    protected TestRestTemplate restTemplate;

    protected String userBaseUrl;
    protected String cardBaseUrl;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry)
    {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);

        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        registry.add("spring.data.redis.password", () -> "");

        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.flyway.enabled", () -> "false");

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    protected void initUrls(String userPath, String cardPath)
    {
        this.userBaseUrl = "http://localhost:" + port + userPath;
        if (cardPath != null)
        {
            this.cardBaseUrl = "http://localhost:" + port + cardPath;
        }
        this.restTemplate = new TestRestTemplate();
    }
}