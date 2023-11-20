package com.nordeus.jobfair.auctionservice.auctionservice.integration;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.*;

@SpringBootTest
public class DatabaseIntegrationTests {

    private final Connection dbConn;

    public DatabaseIntegrationTests(@Value("${spring.datasource.url}") String dbUrl,
                                    @Value("${spring.datasource.username}") String dbUserName,
                                    @Value("${spring.datasource.password}") String dbUserPassword) throws SQLException {

        this.dbConn = DriverManager.getConnection(dbUrl, dbUserName, dbUserPassword);
    }

    @Test
    public void connectivityTest() throws SQLException {
        String query = "SELECT 1";

        Statement stmt = this.dbConn.createStatement();
        ResultSet results = stmt.executeQuery(query);

        assertThat(results.next()).isEqualTo(true);
        assertThat(results.getString(1)).isEqualTo("1");
        assertThat(results.next()).isEqualTo(false);
    }

    @ParameterizedTest
    @ValueSource(strings = {"auction"})
    void tablesExistenceTest(String tableName) throws SQLException {
        String queryTemplate = "SELECT table_name FROM information_schema.tables WHERE table_name = '%s'",
                query = String.format(queryTemplate, tableName);

        Statement stmt = this.dbConn.createStatement();
        ResultSet results = stmt.executeQuery(query);

        assertThat(results.next()).isEqualTo(true);
        assertThat(results.getString(1)).isEqualTo(tableName);
        assertThat(results.next()).isEqualTo(false);
    }
}
