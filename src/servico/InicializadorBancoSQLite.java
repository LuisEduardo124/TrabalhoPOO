package servico;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Cria a estrutura do banco SQLite a partir do script oficial do projeto.
 *
 * <p>As instruções do script são idempotentes porque o próprio
 * {@code sql/schema.sql} usa {@code CREATE TABLE IF NOT EXISTS}. Portanto,
 * executar esta classe novamente não remove nem substitui dados existentes.</p>
 */
public final class InicializadorBancoSQLite {
    private static final Path CAMINHO_SCHEMA = Path.of("sql", "schema.sql");
    private static final String RECURSO_SCHEMA = "/sql/schema.sql";
    private static final List<String> TABELAS_ESPERADAS =
            List.of("itens", "usuarios", "emprestimos");

    private InicializadorBancoSQLite() {
        // Classe utilitária.
    }

    /**
     * Inicializa as tabelas do banco padrão da aplicação.
     *
     * @throws IllegalStateException se o banco não puder ser aberto, o script
     *         não puder ser lido ou alguma tabela não for criada
     */
    public static void inicializar() {
        try (Connection conexao = ConexaoSQLite.abrir()) {
            executarSchema(conexao, lerSchema());
            verificarTabelas(conexao);
        } catch (IOException | SQLException erro) {
            throw new IllegalStateException(
                    "Não foi possível inicializar o banco SQLite: " + erro.getMessage(), erro);
        }
    }

    private static String lerSchema() throws IOException {
        if (Files.isRegularFile(CAMINHO_SCHEMA)) {
            return Files.readString(CAMINHO_SCHEMA, StandardCharsets.UTF_8);
        }

        try (InputStream entrada = InicializadorBancoSQLite.class
                .getResourceAsStream(RECURSO_SCHEMA)) {
            if (entrada == null) {
                throw new IOException("arquivo sql/schema.sql não foi encontrado");
            }
            return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void executarSchema(Connection conexao, String schema) throws SQLException {
        try (Statement comando = conexao.createStatement()) {
            String schemaSemComentarios = schema.lines()
                    .filter(linha -> !linha.trim().startsWith("--"))
                    .collect(Collectors.joining(System.lineSeparator()));
            for (String instrucao : schemaSemComentarios.split(";")) {
                String sql = instrucao.trim();
                if (!sql.isEmpty()) {
                    comando.execute(sql);
                }
            }
        }
    }

    private static void verificarTabelas(Connection conexao) throws SQLException {
        String sql = "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?";
        try (var comando = conexao.prepareStatement(sql)) {
            for (String tabela : TABELAS_ESPERADAS) {
                comando.setString(1, tabela);
                try (ResultSet resultado = comando.executeQuery()) {
                    if (!resultado.next()) {
                        throw new SQLException("a tabela obrigatória não foi criada: " + tabela);
                    }
                }
            }
        }
    }
}
