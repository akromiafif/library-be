package com.samsung.library.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Database Initializer that loads sample data on application startup
 * Only runs in development and testing profiles
 */
@Component
@Profile({"dev", "development", "test"})
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${app.database.initialize-sample-data:true}")
    private boolean initializeSampleData;

    @Value("${app.database.sample-data-file:data.sql}")
    private String sampleDataFile;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (!initializeSampleData) {
            logger.info("Sample data initialization is disabled");
            return;
        }

        try {
            // Check if data already exists
            Integer authorCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM authors", Integer.class);

            if (authorCount != null && authorCount > 0) {
                logger.info("Database already contains {} authors. Skipping sample data initialization.", authorCount);
                return;
            }

            logger.info("Initializing database with sample data...");
            loadSampleData();

            // Verify data was loaded
            verifyDataLoaded();

            logger.info("✅ Database initialized successfully with sample data!");

        } catch (Exception e) {
            logger.error("❌ Failed to initialize database with sample data", e);
            throw e;
        }
    }

    /**
     * Load sample data from SQL file
     */
    private void loadSampleData() {
        try {
            ClassPathResource resource = new ClassPathResource(sampleDataFile);

            if (!resource.exists()) {
                logger.warn("Sample data file '{}' not found. Skipping sample data loading.", sampleDataFile);
                return;
            }

            String sqlContent = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))
                    .lines()
                    .filter(line -> !line.trim().isEmpty() && !line.trim().startsWith("--"))
                    .collect(Collectors.joining("\n"));

            // Split by semicolon and execute each statement
            String[] statements = sqlContent.split(";");
            int executedStatements = 0;

            for (String statement : statements) {
                String trimmedStatement = statement.trim();
                if (!trimmedStatement.isEmpty()) {
                    try {
                        jdbcTemplate.execute(trimmedStatement);
                        executedStatements++;
                    } catch (DataAccessException e) {
                        logger.warn("Failed to execute SQL statement: {}", trimmedStatement);
                        logger.warn("Error: {}", e.getMessage());
                    }
                }
            }

            logger.info("Executed {} SQL statements from {}", executedStatements, sampleDataFile);

        } catch (Exception e) {
            logger.error("Error loading sample data from file: {}", sampleDataFile, e);
            throw new RuntimeException("Failed to load sample data", e);
        }
    }

    /**
     * Verify that sample data was loaded correctly
     */
    private void verifyDataLoaded() {
        try {
            Integer authorCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM authors", Integer.class);
            Integer bookCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM books", Integer.class);
            Integer memberCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM members", Integer.class);
            Integer borrowedCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM borrowed_books", Integer.class);

            logger.info("📊 Sample data loaded successfully:");
            logger.info("   - Authors: {}", authorCount);
            logger.info("   - Books: {}", bookCount);
            logger.info("   - Members: {}", memberCount);
            logger.info("   - Borrowed Books: {}", borrowedCount);

            if (authorCount == 0 || bookCount == 0 || memberCount == 0) {
                logger.warn("⚠️  Some tables appear to be empty. Sample data may not have loaded correctly.");
            }

        } catch (Exception e) {
            logger.error("Failed to verify sample data", e);
        }
    }
}