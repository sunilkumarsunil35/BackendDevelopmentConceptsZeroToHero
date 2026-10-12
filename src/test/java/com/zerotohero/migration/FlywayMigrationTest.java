package com.zerotohero.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

/** Module 11: every migration applied in order, and the schema matches the entities (ddl-auto=validate). */
@DataJpaTest
class FlywayMigrationTest {

    @Autowired
    Flyway flyway;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void allMigrationsAreApplied() {
        MigrationInfo[] applied = flyway.info().applied();
        assertThat(Arrays.stream(applied).map(MigrationInfo::getScript))
                .containsExactly("V1__create_catalog.sql", "V2__seed_catalog.sql", "V3__create_orders.sql",
                        "V4__create_outbox.sql");
        assertThat(flyway.info().pending()).isEmpty();
    }

    @Test
    void seedDataIsPresentAndConstraintsHold() {
        assertThat(jdbc.queryForObject("select count(*) from books", Integer.class)).isEqualTo(10);
        assertThat(jdbc.queryForObject("select count(*) from authors", Integer.class)).isEqualTo(10);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> jdbc.update("update books set stock = -1 where id = 1"))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }
}
