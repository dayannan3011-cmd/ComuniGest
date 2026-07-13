package cl.comunigest.backend.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordMigrationRunnerTest {

    @Test
    void reconocePrefijosBcryptCompatibles() {
        assertTrue(PasswordMigrationRunner.isBcrypt("$2a$valor"));
        assertTrue(PasswordMigrationRunner.isBcrypt("$2b$valor"));
        assertTrue(PasswordMigrationRunner.isBcrypt("$2y$valor"));
        assertFalse(PasswordMigrationRunner.isBcrypt("texto-plano"));
        assertFalse(PasswordMigrationRunner.isBcrypt(null));
    }
}
