package cl.comunigest.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class PasswordMigrationRunner implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordMigrationRunner.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public PasswordMigrationRunner(DataSource dataSource, JdbcTemplate jdbcTemplate,
                                   PasswordEncoder passwordEncoder) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            if (!hasTable(connection, "usuarios")) return;
        }

        List<UserPassword> users = jdbcTemplate.query(
                "SELECT id, password FROM usuarios",
                (rs, row) -> new UserPassword(rs.getLong("id"), rs.getString("password")));
        int migrated = 0;
        for (UserPassword user : users) {
            if (user.password() == null || isBcrypt(user.password())) continue;
            String hash = passwordEncoder.encode(user.password());
            migrated += jdbcTemplate.update(
                    "UPDATE usuarios SET password = ? WHERE id = ? AND password = ?",
                    hash, user.id(), user.password());
        }
        LOGGER.info("Migración BCrypt finalizada: {} contraseña(s) migrada(s).", migrated);
    }

    static boolean isBcrypt(String value) {
        return value != null && (value.startsWith("$2a$")
                || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }

    private boolean hasTable(Connection connection, String table) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet rows = metadata.getTables(null, null, null, new String[]{"TABLE"})) {
            while (rows.next()) if (table.equalsIgnoreCase(rows.getString("TABLE_NAME"))) return true;
        }
        return false;
    }

    private record UserPassword(Long id, String password) {}
}
