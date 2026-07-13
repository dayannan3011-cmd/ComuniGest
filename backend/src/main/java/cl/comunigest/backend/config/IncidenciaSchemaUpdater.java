package cl.comunigest.backend.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

@Component
@Profile("h2")
@Order(Ordered.HIGHEST_PRECEDENCE + 3)
public class IncidenciaSchemaUpdater implements ApplicationRunner {
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public IncidenciaSchemaUpdater(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            if (!"H2".equalsIgnoreCase(connection.getMetaData().getDatabaseProductName())
                    || !hasTable(connection, "incidencias")) return;
            addColumnIfMissing(connection, "turno_registro_id", "BIGINT");
            addColumnIfMissing(connection, "fecha_resolucion", "TIMESTAMP");
            addColumnIfMissing(connection, "usuario_resuelve_id", "BIGINT");
            addColumnIfMissing(connection, "resolucion", "VARCHAR(1000)");
            addColumnIfMissing(connection, "creado_en", "TIMESTAMP");
            addColumnIfMissing(connection, "actualizado_en", "TIMESTAMP");

            List<StateCheck> stateChecks = findStateChecks();
            for (StateCheck check : stateChecks) {
                if (isLegacyStateCheck(check.clause())) {
                    jdbcTemplate.execute("ALTER TABLE incidencias DROP CONSTRAINT " + quote(check.name()));
                }
            }
            if (isEnumStateColumn()) {
                jdbcTemplate.execute("""
                        ALTER TABLE incidencias ALTER COLUMN estado
                        ENUM('ABIERTA', 'CERRADA', 'EN_PROCESO', 'RESUELTA') NOT NULL
                        """);
            }

            if (hasColumn(connection, "incidencias", "fecha_cierre")) {
                jdbcTemplate.update("""
                        UPDATE incidencias SET fecha_resolucion = fecha_cierre
                        WHERE fecha_resolucion IS NULL AND fecha_cierre IS NOT NULL
                        """);
            }
            jdbcTemplate.update("""
                    UPDATE incidencias
                    SET estado = 'RESUELTA',
                        resolucion = COALESCE(resolucion, 'Resolución no informada en registro legado')
                    WHERE estado = 'CERRADA'
                    """);
            if (isEnumStateColumn()) {
                jdbcTemplate.execute("ALTER TABLE incidencias ALTER COLUMN estado VARCHAR(20) NOT NULL");
            }
            if (!hasCompatibleStateCheck()) {
                jdbcTemplate.execute("""
                        ALTER TABLE incidencias ADD CONSTRAINT chk_incidencias_estado
                        CHECK (estado IN ('ABIERTA', 'EN_PROCESO', 'RESUELTA'))
                        """);
            }
            jdbcTemplate.update("""
                    UPDATE incidencias
                    SET creado_en = COALESCE(creado_en, fecha_registro, CURRENT_TIMESTAMP),
                        actualizado_en = COALESCE(actualizado_en, fecha_resolucion, fecha_registro, CURRENT_TIMESTAMP)
                    WHERE creado_en IS NULL OR actualizado_en IS NULL
                    """);
            jdbcTemplate.execute("ALTER TABLE incidencias ALTER COLUMN creado_en SET NOT NULL");
            jdbcTemplate.execute("ALTER TABLE incidencias ALTER COLUMN actualizado_en SET NOT NULL");
        }
    }

    private void addColumnIfMissing(Connection connection, String column, String definition) throws SQLException {
        if (!hasColumn(connection, "incidencias", column))
            jdbcTemplate.execute("ALTER TABLE incidencias ADD COLUMN " + column + " " + definition);
    }

    private List<StateCheck> findStateChecks() {
        return jdbcTemplate.query("""
                SELECT tc.constraint_name, cc.check_clause
                FROM information_schema.table_constraints tc
                JOIN information_schema.check_constraints cc
                  ON cc.constraint_catalog = tc.constraint_catalog
                 AND cc.constraint_schema = tc.constraint_schema
                 AND cc.constraint_name = tc.constraint_name
                WHERE LOWER(tc.table_name) = 'incidencias'
                  AND tc.constraint_type = 'CHECK'
                """, (rs, row) -> new StateCheck(rs.getString(1), rs.getString(2)));
    }

    private boolean isLegacyStateCheck(String clause) {
        String normalized = clause.toUpperCase(Locale.ROOT);
        return normalized.contains("ESTADO") && normalized.contains("ABIERTA")
                && normalized.contains("CERRADA") && !normalized.contains("EN_PROCESO");
    }

    private boolean hasCompatibleStateCheck() {
        return findStateChecks().stream().anyMatch(check -> {
            String clause = check.clause().toUpperCase(Locale.ROOT);
            return clause.contains("ESTADO") && clause.contains("ABIERTA")
                    && clause.contains("EN_PROCESO") && clause.contains("RESUELTA")
                    && !clause.contains("CERRADA");
        });
    }

    private boolean isEnumStateColumn() {
        String type = jdbcTemplate.queryForObject("""
                SELECT data_type FROM information_schema.columns
                WHERE LOWER(table_name) = 'incidencias' AND LOWER(column_name) = 'estado'
                """, String.class);
        return "ENUM".equalsIgnoreCase(type);
    }

    private String quote(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    private record StateCheck(String name, String clause) {}

    private boolean hasTable(Connection connection, String table) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet rows = metadata.getTables(null, null, null, new String[]{"TABLE"})) {
            while (rows.next()) if (table.equalsIgnoreCase(rows.getString("TABLE_NAME"))) return true;
        }
        return false;
    }

    private boolean hasColumn(Connection connection, String table, String column) throws SQLException {
        try (ResultSet rows = connection.getMetaData().getColumns(null, null, null, null)) {
            while (rows.next()) {
                if (table.equalsIgnoreCase(rows.getString("TABLE_NAME"))
                        && column.equalsIgnoreCase(rows.getString("COLUMN_NAME"))) return true;
            }
        }
        return false;
    }
}
