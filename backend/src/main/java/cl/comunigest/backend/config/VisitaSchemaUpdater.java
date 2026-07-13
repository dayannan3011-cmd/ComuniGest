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

@Component
@Profile("h2")
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class VisitaSchemaUpdater implements ApplicationRunner {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public VisitaSchemaUpdater(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            if (!isH2(connection) || !hasTable(connection, "visitas")) {
                return;
            }

            addColumnIfMissing(connection, "documento_identidad", "VARCHAR(40)");
            addColumnIfMissing(connection, "turno_ingreso_id", "BIGINT");
            addColumnIfMissing(connection, "turno_salida_id", "BIGINT");
            addColumnIfMissing(connection, "observaciones", "VARCHAR(500)");
            addColumnIfMissing(connection, "creado_en", "TIMESTAMP");
            addColumnIfMissing(connection, "actualizado_en", "TIMESTAMP");

            if (hasColumn(connection, "visitas", "documento")) {
                jdbcTemplate.update("""
                        UPDATE visitas
                        SET documento_identidad = documento
                        WHERE documento_identidad IS NULL
                          AND documento IS NOT NULL
                        """);
            }

            jdbcTemplate.update("""
                    UPDATE visitas
                    SET creado_en = COALESCE(creado_en, fecha_ingreso, CURRENT_TIMESTAMP),
                        actualizado_en = COALESCE(actualizado_en, fecha_salida, fecha_ingreso, CURRENT_TIMESTAMP)
                    WHERE creado_en IS NULL OR actualizado_en IS NULL
                    """);
            jdbcTemplate.execute("ALTER TABLE visitas ALTER COLUMN nombre_visitante VARCHAR(160)");
            jdbcTemplate.execute("ALTER TABLE visitas ALTER COLUMN creado_en SET NOT NULL");
            jdbcTemplate.execute("ALTER TABLE visitas ALTER COLUMN actualizado_en SET NOT NULL");

            Integer missingDocuments = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM visitas WHERE documento_identidad IS NULL", Integer.class);
            if (missingDocuments != null && missingDocuments == 0) {
                jdbcTemplate.execute("ALTER TABLE visitas ALTER COLUMN documento_identidad SET NOT NULL");
            }
        }
    }

    private void addColumnIfMissing(Connection connection, String column, String definition) throws SQLException {
        if (!hasColumn(connection, "visitas", column)) {
            jdbcTemplate.execute("ALTER TABLE visitas ADD COLUMN " + column + " " + definition);
        }
    }

    private boolean isH2(Connection connection) throws SQLException {
        return connection.getMetaData().getDatabaseProductName().equalsIgnoreCase("H2");
    }

    private boolean hasTable(Connection connection, String table) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet tables = metadata.getTables(null, null, null, new String[]{"TABLE"})) {
            while (tables.next()) {
                if (table.equalsIgnoreCase(tables.getString("TABLE_NAME"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasColumn(Connection connection, String table, String column) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet columns = metadata.getColumns(null, null, null, null)) {
            while (columns.next()) {
                if (table.equalsIgnoreCase(columns.getString("TABLE_NAME"))
                        && column.equalsIgnoreCase(columns.getString("COLUMN_NAME"))) {
                    return true;
                }
            }
        }
        return false;
    }
}
