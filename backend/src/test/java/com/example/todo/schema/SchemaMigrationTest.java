package com.example.todo.schema;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SchemaMigrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void baselineMigrationIsRecordedAsAppliedExactlyOnce() {
        List<Map<String, Object>> migrations = jdbc.queryForList(
                "SELECT \"version\" AS \"version\", \"description\" AS \"description\", "
                        + "\"type\" AS \"type\", \"success\" AS \"success\" FROM \"flyway_schema_history\" "
                        + "WHERE \"type\" = 'SQL'");

        assertThat(migrations).hasSize(1);
        assertThat(migrations.get(0))
                .containsEntry("version", "1")
                .containsEntry("description", "create tasks table")
                .containsEntry("type", "SQL")
                .containsEntry("success", true);
    }

    @Test
    void tasksTableMatchesTheEntityMapping() {
        List<Column> columns = jdbc.query(
                "SELECT column_name AS \"name\", data_type AS \"type\", is_nullable AS \"nullable\", "
                        + "character_maximum_length AS \"length\" FROM information_schema.columns "
                        + "WHERE table_name = 'TASKS' ORDER BY ordinal_position",
                (rs, rowNum) -> new Column(
                        rs.getString("name"),
                        rs.getString("type"),
                        "YES".equalsIgnoreCase(rs.getString("nullable")),
                        (int) rs.getLong("length")));

        assertThat(columns).extracting(Column::name).containsExactly("ID", "TITLE", "COMPLETED");
        assertThat(columns).extracting(Column::nullable).containsExactly(false, true, false);
        assertThat(Column.named(columns, "ID").type()).isEqualTo("BIGINT");
        assertThat(Column.named(columns, "TITLE").type()).isIn("VARCHAR", "CHARACTER VARYING");
        assertThat(Column.named(columns, "TITLE").length()).isEqualTo(255);
        assertThat(Column.named(columns, "COMPLETED").type()).isEqualTo("BOOLEAN");
    }

    private record Column(String name, String type, boolean nullable, int length) {

        private static Column named(List<Column> columns, String name) {
            return columns.stream()
                    .filter(column -> column.name().equals(name))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("no column named " + name + " in " + columns));
        }
    }
}
