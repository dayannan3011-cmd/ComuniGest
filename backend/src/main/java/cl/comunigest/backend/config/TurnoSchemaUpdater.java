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
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TurnoSchemaUpdater implements ApplicationRunner {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public TurnoSchemaUpdater(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            if (!isH2(connection) || !hasTable(connection, "turnos")) {
                return;
            }

            addColumnIfMissing(connection, "observaciones_inicio", "VARCHAR(500)");
            addColumnIfMissing(connection, "observaciones_cierre", "VARCHAR(500)");
            addColumnIfMissing(connection, "creado_en", "TIMESTAMP");
            addColumnIfMissing(connection, "actualizado_en", "TIMESTAMP");

            if (hasColumn(connection, "turnos", "observaciones")) {
                jdbcTemplate.update("""
                        UPDATE turnos
                        SET observaciones_inicio = observaciones
                        WHERE observaciones_inicio IS NULL
                          AND observaciones IS NOT NULL
                        """);
            }

            jdbcTemplate.update("""
                    UPDATE turnos
                    SET creado_en = COALESCE(creado_en, fecha_inicio, CURRENT_TIMESTAMP),
                        actualizado_en = COALESCE(actualizado_en, fecha_cierre, fecha_inicio, CURRENT_TIMESTAMP)
                    WHERE creado_en IS NULL OR actualizado_en IS NULL
                    """);
            jdbcTemplate.update("""
                    UPDATE turnos
                    SET estado = 'CERRADO',
                        fecha_cierre = COALESCE(fecha_cierre, CURRENT_TIMESTAMP),
                        actualizado_en = CURRENT_TIMESTAMP
                    WHERE estado = 'ABIERTO'
                      AND EXISTS (
                          SELECT 1
                          FROM usuarios u
                          JOIN perfiles p ON p.id = u.perfil_id
                          WHERE u.id = turnos.usuario_id
                            AND UPPER(p.nombre) <> 'CONSERJE'
                      )
                    """);
            jdbcTemplate.execute("ALTER TABLE turnos ALTER COLUMN creado_en SET NOT NULL");
            jdbcTemplate.execute("ALTER TABLE turnos ALTER COLUMN actualizado_en SET NOT NULL");
        }
    }

    private void addColumnIfMissing(Connection connection, String column, String definition) throws SQLException {
        if (!hasColumn(connection, "turnos", column)) {
            jdbcTemplate.execute("ALTER TABLE turnos ADD COLUMN " + column + " " + definition);
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
