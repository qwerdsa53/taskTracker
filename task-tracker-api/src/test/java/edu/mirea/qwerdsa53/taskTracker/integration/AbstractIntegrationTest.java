package edu.mirea.qwerdsa53.taskTracker.integration;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration profile: JDBC to {@code localhost:${DB_INTEGRATION_PORT:5433}} (Docker service {@code postgres-integration}).
 * Before tests: {@code docker compose up -d} and {@code bash scripts/fixture-db-integration.sh}.
 */
@Tag("integration")
@SpringBootTest
@ActiveProfiles("integration")
public abstract class AbstractIntegrationTest {
}
