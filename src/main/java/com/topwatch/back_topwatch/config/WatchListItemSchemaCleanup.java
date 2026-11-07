package com.topwatch.back_topwatch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * watched / watched_date moved from watch_list_items to watch_list_view_items.
 * ddl-auto: update never drops columns, and the old NOT NULL "watched" column would
 * make every new insert into watch_list_items fail, so drop both once on startup.
 * Safe to remove once every database has been migrated.
 */
@Component
@RequiredArgsConstructor
public class WatchListItemSchemaCleanup implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        jdbcTemplate.execute("ALTER TABLE IF EXISTS watch_list_items DROP COLUMN IF EXISTS watched, DROP COLUMN IF EXISTS watched_date");
    }

}
