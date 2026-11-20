package servico;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gerencia a abertura de conexões com o banco SQLite.
 */
public class ConexaoSQLite {

    private static final String URL = "jdbc:sqlite:biblioteca.db";

    /**
     * Abre uma conexão com o banco e ativa as chaves estrangeiras.
     *
     * @return conexão aberta com o SQLite
     * @throws SQLException se ocorrer um erro de conexão
     */
    public static Connection abrir() throws SQLException {
        Connection conexao = DriverManager.getConnection(URL);

        try (Statement comando = conexao.createStatement()) {
            comando.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException erro) {
            conexao.close();
            throw erro;
        }

        return conexao;
    }
}