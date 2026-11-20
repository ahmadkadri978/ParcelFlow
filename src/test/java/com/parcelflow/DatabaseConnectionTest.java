package com.parcelflow;

import com.parcelflow.support.PostgresTestConfiguration;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = "DB_PASSWORD=unused")
@Import(PostgresTestConfiguration.class)
class DatabaseConnectionTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldConnectToTestDatabase() {

        String databaseName = jdbcTemplate.queryForObject(
                "SELECT current_database()",
                String.class
        );

        assertEquals("parcelflow_test", databaseName);
    }

    @Test
    void shouldApplyFlywayMigration() {

        Integer migrations = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM flyway_schema_history
                WHERE version = '1'
                  AND success = true
                """,
                Integer.class
        );

        assertEquals(1, migrations);
    }
}
