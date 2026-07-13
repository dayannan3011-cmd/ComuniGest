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
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class EncomiendaSchemaUpdater implements ApplicationRunner {
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public EncomiendaSchemaUpdater(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            if (!"H2".equalsIgnoreCase(connection.getMetaData().getDatabaseProductName())
                    || !hasTable(connection, "encomiendas")) return;
            addColumnIfMissing(connection, "destinatario", "VARCHAR(160)");
            addColumnIfMissing(connection, "empresa_repartidor", "VARCHAR(160)");
            addColumnIfMissing(connection, "turno_recepcion_id", "BIGINT");
            addColumnIfMissing(connection, "turno_entrega_id", "BIGINT");
            jdbcTemplate.update("""
                    UPDATE encomiendas
                    SET destinatario = COALESCE(destinatario, recibido_por, 'Destinatario no informado')
                    WHERE destinatario IS NULL
                    """);
            jdbcTemplate.execute("ALTER TABLE encomiendas ALTER COLUMN destinatario SET NOT NULL");
        }
    }

    private void addColumnIfMissing(Connection connection, String column, String definition) throws SQLException {
        if (!hasColumn(connection, "encomiendas", column))
            jdbcTemplate.execute("ALTER TABLE encomiendas ADD COLUMN " + column + " " + definition);
    }

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
