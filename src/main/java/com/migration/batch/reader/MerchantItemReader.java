package com.migration.batch.reader;

import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import com.migration.batch.model.Merchant;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Reader for Merchant data from SQL Server.
 * Implements JdbcPagingItemReader for efficient, scalable reading of Merchant records.
 * Uses cursor-based reading for large datasets to reduce memory overhead.
 */
@Component
public class MerchantItemReader {

    /**
     * Creates a JdbcCursorItemReader to read Merchant data from SQL Server.
     * Cursor-based reading is optimal for large datasets with continuous DB connection.
     * 
     * @param dataSource The DataSource for database connection
     * @return Configured JdbcCursorItemReader for Merchant entities
     */
    public JdbcCursorItemReader<Merchant> reader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Merchant>()
                .name("merchantItemReader")
                .dataSource(dataSource)
                .sqlQuery("SELECT MerchantID, MerchantName, CreditLimit, IsActive, RegistrationDate FROM dbo.Merchant")
                .rowMapper(new BeanPropertyRowMapper<>(Merchant.class))
                .maxRows(1000) // Configured for high-volume processing
                .build();
    }
}
