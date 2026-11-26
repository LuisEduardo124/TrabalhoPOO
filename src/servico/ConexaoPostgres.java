
package servico;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gerencia a conexão com o banco PostgreSQL.
 * O PostgreSQL está executando no Docker.
 */
public class ConexaoPostgres {

    private static final String URL =
            "jdbc:postgresql://localhost:5432/biblioteca";

    private static final String USUARIO = "postgres";

    /**
     * Abre uma conexão com o PostgreSQL usando a senha da variável
     * de ambiente {@code BIBLIOTECA_DB_PASSWORD}.
     *
     * @return conexão JDBC aberta
     * @throws SQLException se ocorrer um erro na conexão
     * @throws IllegalStateException se a variável de ambiente não estiver definida
     */
    public static Connection conectar() throws SQLException {

        String senha = System.getenv("BIBLIOTECA_DB_PASSWORD");

        if (senha == null || senha.isBlank()) {
            throw new IllegalStateException(
                    "Configure a variável BIBLIOTECA_DB_PASSWORD."
            );
        }

        return DriverManager.getConnection(
                URL,
                USUARIO,
                senha
        );
    }
}
