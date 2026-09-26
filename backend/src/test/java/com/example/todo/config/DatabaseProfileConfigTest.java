package com.example.todo.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseProfileConfigTest {

    @Test
    void postgresProfileDeclaresOnlyDatasourceKeys() throws IOException {
        assertThat(load("application-postgres.properties").stringPropertyNames())
                .containsExactlyInAnyOrder(
                        "spring.datasource.url",
                        "spring.datasource.username",
                        "spring.datasource.password",
                        "spring.datasource.driver-class-name",
                        "spring.h2.console.enabled");
    }

    @Test
    void postgresProfileResolvesCredentialsFromTheEnvironment() throws IOException {
        Properties postgres = load("application-postgres.properties");

        assertThat(postgres.getProperty("spring.datasource.url"))
                .isEqualTo("${POSTGRES_URL:jdbc:postgresql://localhost:5432/todo}");
        assertThat(postgres.getProperty("spring.datasource.username"))
                .isEqualTo("${POSTGRES_USER:todo}");
        assertThat(postgres.getProperty("spring.datasource.password"))
                .isEqualTo("${POSTGRES_PASSWORD:}");
    }

    @Test
    void postgresProfileSelectsPostgresAndDisablesTheH2Console() throws IOException {
        Properties postgres = load("application-postgres.properties");

        assertThat(postgres.getProperty("spring.datasource.driver-class-name"))
                .isEqualTo("org.postgresql.Driver");
        assertThat(postgres.getProperty("spring.h2.console.enabled")).isEqualTo("false");
    }

    @Test
    void defaultProfileStillTargetsInMemoryH2AndValidatesOnly() throws IOException {
        Properties base = load("application.properties");

        assertThat(base.getProperty("spring.datasource.url")).isEqualTo("jdbc:h2:mem:tododb");
        assertThat(base.getProperty("spring.datasource.driver-class-name")).isEqualTo("org.h2.Driver");
        assertThat(base.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
    }

    private Properties load(String resource) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertThat(in).as("classpath resource %s", resource).isNotNull();
            Properties properties = new Properties();
            properties.load(in);
            return properties;
        }
    }
}
