package servico;

import modelo.Emprestimo;
import modelo.ItemBiblioteca;
import modelo.Livro;
import modelo.Revista;
import modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação do repositório da biblioteca usando SQLite.
 */
public class RepositorioBibliotecaSQLite implements RepositorioBiblioteca {

    /**
     * Cria um repositório que persiste os dados no banco SQLite padrão.
     */
    public RepositorioBibliotecaSQLite() {
        // A conexão é aberta por operação para manter os recursos fechados.
    }

    @Override
    public void salvarItem(ItemBiblioteca item) {
        String sql = """
                INSERT INTO itens
                (codigo, titulo, tipo, autor, isbn, edicao, disponivel)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conexao = ConexaoSQLite.abrir();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, item.getCodigo());
            comando.setString(2, item.getTitulo());
            comando.setString(3, item.getTipo());

            if (item instanceof Livro livro) {
                comando.setString(4, livro.getAutor());
                comando.setString(5, livro.getIsbn());
                comando.setNull(6, java.sql.Types.INTEGER);
            } else if (item instanceof Revista revista) {
                comando.setNull(4, java.sql.Types.VARCHAR);
                comando.setNull(5, java.sql.Types.VARCHAR);
                comando.setInt(6, revista.getNumeroEdicao());
            } else {
                throw new IllegalArgumentException("Tipo de item não suportado.");
            }

            comando.setInt(7, item.isDisponivel() ? 1 : 0);
            comando.executeUpdate();

        } catch (SQLException erro) {
            throw new IllegalStateException("Erro ao salvar item no SQLite.", erro);
        }
    }

    @Override
    public List<ItemBiblioteca> listarItens() {
        List<ItemBiblioteca> itens = new ArrayList<>();

        String sql = "SELECT * FROM itens ORDER BY codigo";

        try (Connection conexao = ConexaoSQLite.abrir();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {

            while (resultado.next()) {
                itens.add(converterItem(resultado));
            }

        } catch (SQLException erro) {
            throw new IllegalStateException("Erro ao listar itens do SQLite.", erro);
        }

        return itens;
    }

    @Override
    public ItemBiblioteca buscarItem(String codigo) {
        String sql = "SELECT * FROM itens WHERE codigo = ?";

        try (Connection conexao = ConexaoSQLite.abrir();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, codigo);

            try (ResultSet resultado = comando.executeQuery()) {
                if (resultado.next()) {
                    return converterItem(resultado);
                }
            }

        } catch (SQLException erro) {
            throw new IllegalStateException("Erro ao buscar item no SQLite.", erro);
        }

        return null;
    }

    private ItemBiblioteca converterItem(ResultSet resultado) throws SQLException {
        String codigo = resultado.getString("codigo");
        String titulo = resultado.getString("titulo");
        String tipo = resultado.getString("tipo");

        ItemBiblioteca item;

        if ("Livro".equals(tipo)) {
            item = new Livro(
                    codigo,
                    titulo,
                    resultado.getString("autor"),
                    resultado.getString("isbn")
            );
        } else if ("Revista".equals(tipo)) {
            item = new Revista(
                    codigo,
                    titulo,
                    resultado.getInt("edicao")
            );
        } else {
            throw new IllegalStateException("Tipo desconhecido: " + tipo);
        }

        if (resultado.getInt("disponivel") == 0) {
            item.emprestar();
        }

        return item;
    }

    @Override
    public List<Usuario> listarUsuarios() {
        List<Usuario> usuarios = new ArrayList<>();

        String sql = "SELECT id, nome, email FROM usuarios ORDER BY id";

        try (Connection conexao = ConexaoSQLite.abrir();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {

            while (resultado.next()) {
                usuarios.add(new Usuario(
                        resultado.getInt("id"),
                        resultado.getString("nome"),
                        resultado.getString("email")
                ));
            }

        } catch (SQLException erro) {
            throw new IllegalStateException("Erro ao listar usuários no SQLite.", erro);
        }

        return usuarios;
    }

    @Override
    public List<Emprestimo> listarEmprestimos() {
        List<Emprestimo> emprestimos = new ArrayList<>();

        String sql = """
            SELECT codigo_item, id_usuario,
                   data_emprestimo, data_devolucao, status
            FROM emprestimos
            ORDER BY id
            """;

        try (Connection conexao = ConexaoSQLite.abrir();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {

            while (resultado.next()) {
                ItemBiblioteca item = buscarItem(
                        resultado.getString("codigo_item")
                );

                Usuario usuario = buscarUsuario(
                        resultado.getInt("id_usuario")
                );

                java.time.LocalDateTime dataEmprestimo =
                        java.time.LocalDateTime.parse(
                                resultado.getString("data_emprestimo")
                        );

                String textoDevolucao = resultado.getString("data_devolucao");

                java.time.LocalDateTime dataDevolucao =
                        textoDevolucao == null
                                ? null
                                : java.time.LocalDateTime.parse(textoDevolucao);

                modelo.StatusEmprestimo status =
                        modelo.StatusEmprestimo.valueOf(
                                resultado.getString("status")
                        );

                emprestimos.add(new Emprestimo(
                        item,
                        usuario,
                        dataEmprestimo,
                        dataDevolucao,
                        status
                ));
            }

        } catch (SQLException erro) {
            throw new IllegalStateException(
                    "Erro ao listar empréstimos no SQLite.", erro
            );
        }

        return emprestimos;
    }

    @Override
    public void salvarUsuario(Usuario usuario) {
        String sql = "INSERT INTO usuarios (id, nome, email) VALUES (?, ?, ?)";

        try (Connection conexao = ConexaoSQLite.abrir();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, usuario.getId());
            comando.setString(2, usuario.getNome());
            comando.setString(3, usuario.getEmail());

            comando.executeUpdate();

        } catch (SQLException erro) {
            throw new IllegalStateException("Erro ao salvar usuário no SQLite.", erro);
        }
    }

    @Override
    public Usuario buscarUsuario(int id) {
        String sql = "SELECT id, nome, email FROM usuarios WHERE id = ?";

        try (Connection conexao = ConexaoSQLite.abrir();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, id);

            try (ResultSet resultado = comando.executeQuery()) {
                if (resultado.next()) {
                    return new Usuario(
                            resultado.getInt("id"),
                            resultado.getString("nome"),
                            resultado.getString("email")
                    );
                }
            }

        } catch (SQLException erro) {
            throw new IllegalStateException("Erro ao buscar usuário no SQLite.", erro);
        }

        return null;
    }

    @Override
    public void salvarEmprestimo(Emprestimo emprestimo) {
        String sqlEmprestimo = """
            
                INSERT INTO emprestimos
            (codigo_item, id_usuario, data_emprestimo, status)
            VALUES (?, ?, ?, ?)
            """;

        String sqlItem = """
        UPDATE itens
        SET disponivel = 0
        WHERE codigo = ? AND disponivel = 1
        """;

        try (Connection conexao = ConexaoSQLite.abrir()) {
            conexao.setAutoCommit(false);

            try {
                try (PreparedStatement comando = conexao.prepareStatement(sqlItem)) {
                    comando.setString(1, emprestimo.getItem().getCodigo());

                    if (comando.executeUpdate() != 1) {
                        throw new IllegalStateException("Item indisponível para empréstimo.");
                    }
                }

                try (PreparedStatement comando = conexao.prepareStatement(sqlEmprestimo)) {
                    comando.setString(1, emprestimo.getItem().getCodigo());
                    comando.setInt(2, emprestimo.getUsuario().getId());
                    comando.setString(3, emprestimo.getDataEmprestimo().toString());
                    comando.setString(4, emprestimo.getStatusAtual().name());

                    comando.executeUpdate();
                }

                conexao.commit();

            } catch (SQLException | RuntimeException erro) {
                conexao.rollback();
                throw erro;
            }

        } catch (SQLException erro) {
            throw new IllegalStateException("Erro ao salvar empréstimo no SQLite.", erro);
        }
    }

    /**
     * Atualiza o status de um empréstimo no SQLite e libera
     * o item quando ele for devolvido ou expirar.
     *
     * @param emprestimo empréstimo com o status atualizado
     */
    @Override
    public void atualizarEmprestimo(Emprestimo emprestimo) {
        String sqlEmprestimo = """
            UPDATE emprestimos
            SET status = ?, data_devolucao = ?
             WHERE codigo_item = ?
              AND data_emprestimo = ?
              AND status = 'ATIVO'
            """;

        String sqlItem = """
            UPDATE itens
            SET disponivel = 1
            WHERE codigo = ?
            """;

        try (Connection conexao = ConexaoSQLite.abrir()) {
            conexao.setAutoCommit(false);

            try {
                try (PreparedStatement comando =
                             conexao.prepareStatement(sqlEmprestimo)) {

                    comando.setString(1, emprestimo.getStatusAtual().name());

                    if (emprestimo.getDataDevolucao() == null) {
                        comando.setNull(2, java.sql.Types.VARCHAR);
                    } else {
                        comando.setString(
                                2,
                                emprestimo.getDataDevolucao().toString()
                        );
                    }

                    comando.setString(
                            3,
                            emprestimo.getItem().getCodigo()
                    );

                    comando.setString(
                            4,
                            emprestimo.getDataEmprestimo().toString()
                    );

                    if (comando.executeUpdate() != 1) {
                        throw new IllegalStateException(
                                "Empréstimo ativo não encontrado no banco."
                        );
                    }
                }

                try (PreparedStatement comando =
                             conexao.prepareStatement(sqlItem)) {

                    comando.setString(
                            1,
                            emprestimo.getItem().getCodigo()
                    );

                    if (comando.executeUpdate() != 1) {
                        throw new IllegalStateException(
                                "Item não encontrado no banco."
                        );
                    }
                }

                conexao.commit();

            } catch (SQLException | RuntimeException erro) {
                conexao.rollback();
                throw erro;
            }

        } catch (SQLException erro) {
            throw new IllegalStateException(
                    "Erro ao atualizar empréstimo no SQLite.", erro
            );
        }
    }
        @Override
    public void atualizarItem(ItemBiblioteca item) {
        String sql = """
                UPDATE itens
                SET titulo = ?, autor = ?, isbn = ?, edicao = ?
                WHERE codigo = ?
                """;

        try (Connection conexao = ConexaoSQLite.abrir();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, item.getTitulo());

            if (item instanceof Livro livro) {
                comando.setString(2, livro.getAutor());
                comando.setString(3, livro.getIsbn());
                comando.setNull(4, java.sql.Types.INTEGER);
            } else if (item instanceof Revista revista) {
                comando.setNull(2, java.sql.Types.VARCHAR);
                comando.setNull(3, java.sql.Types.VARCHAR);
                comando.setInt(4, revista.getNumeroEdicao());
            } else {
                throw new IllegalArgumentException("Tipo de item não suportado.");
            }

            comando.setString(5, item.getCodigo());

            if (comando.executeUpdate() != 1) {
                throw new IllegalStateException("Item não encontrado no banco.");
            }

        } catch (SQLException erro) {
            throw new IllegalStateException("Erro ao atualizar item no SQLite.", erro);
        }
    }

    @Override
    public void excluirItem(String codigo) {
        String sql = "DELETE FROM itens WHERE codigo = ?";

        try (Connection conexao = ConexaoSQLite.abrir();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, codigo);

            if (comando.executeUpdate() != 1) {
                throw new IllegalStateException("Item não encontrado no banco.");
            }

        } catch (SQLException erro) {
            throw new IllegalStateException("Erro ao excluir item no SQLite.", erro);
        }
    }

    @Override
    public void atualizarUsuario(Usuario usuario) {
        String sql = "UPDATE usuarios SET nome = ?, email = ? WHERE id = ?";

        try (Connection conexao = ConexaoSQLite.abrir();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, usuario.getNome());
            comando.setString(2, usuario.getEmail());
            comando.setInt(3, usuario.getId());

            if (comando.executeUpdate() != 1) {
                throw new IllegalStateException("Usuário não encontrado no banco.");
            }

        } catch (SQLException erro) {
            throw new IllegalStateException("Erro ao atualizar usuário no SQLite.", erro);
        }
    }

    @Override
    public void excluirUsuario(int id) {
        String sql = "DELETE FROM usuarios WHERE id = ?";

        try (Connection conexao = ConexaoSQLite.abrir();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, id);

            if (comando.executeUpdate() != 1) {
                throw new IllegalStateException("Usuário não encontrado no banco.");
            }

        } catch (SQLException erro) {
            throw new IllegalStateException("Erro ao excluir usuário no SQLite.", erro);
        }
    }

}
