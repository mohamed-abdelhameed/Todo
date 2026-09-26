package com.example.drift;

import org.hibernate.tool.schema.spi.SchemaManagementException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class SchemaDriftTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    DataSourceAutoConfiguration.class,
                    FlywayAutoConfiguration.class,
                    HibernateJpaAutoConfiguration.class))
            .withUserConfiguration(DriftTestApplication.class)
            .withPropertyValues(
                    "spring.datasource.url=jdbc:h2:mem:drifttest",
                    "spring.jpa.hibernate.ddl-auto=validate");

    @Test
    void startupFailsWhenAnEntityIsNotBackedByAMigration() {
        contextRunner.run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure())
                    .hasRootCauseInstanceOf(SchemaManagementException.class)
                    .hasStackTraceContaining("Schema-validation: missing table [unmigrated_records]");
        });
    }
}
