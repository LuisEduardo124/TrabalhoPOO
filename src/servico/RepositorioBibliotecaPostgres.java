
package servico;

import modelo.Emprestimo;
import modelo.ItemBiblioteca;
import modelo.Livro;
import modelo.Revista;
import modelo.StatusEmprestimo;
import modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação do repositório da biblioteca utilizando PostgreSQL.
 * As conexões são abertas por operação e fechadas automaticamente.
 */
public class RepositorioBibliotecaPostgres implements RepositorioBiblioteca {

    /**
     * Cria o repositório PostgreSQL.
     */
    public RepositorioBibliotecaPostgres() {
    }

    /**
     * Grava um livro ou uma revista na tabela de itens do PostgreSQL.
     *
     * @param item item a armazenar
     * @throws IllegalArgumentException se o tipo do item não for livro nem revista
     * @throws IllegalStateException se a senha do banco não estiver configurada
     *         ou se a gravação falhar
     */
    @Override
    public void salvarItem(ItemBiblioteca item) {
        String sql = """
                INSERT INTO itens
                (codigo, titulo, tipo, autor, isbn, edicao, disponivel)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conexao = ConexaoPostgres.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, item.getCodigo());
            comando.setString(2, item.getTitulo());
            comando.setString(3, item.getTipo());

            if (item instanceof Livro livro) {
                comando.setString(4, livro.getAutor());
                comando.setString(5, livro.getIsbn());
                comando.setNull(6, Types.INTEGER);

            } else if (item instanceof Revista revista) {
                comando.setNull(4, Types.VARCHAR);
                comando.setNull(5, Types.VARCHAR);
                comando.setInt(6, revista.getNumeroEdicao());

            } else {
                throw new IllegalArgumentException(
                        "Tipo de item não suportado."
                );
            }

            comando.setInt(7, item.isDisponivel() ? 1 : 0);
            comando.executeUpdate();

        } catch (SQLException erro) {
            throw new IllegalStateException(
                    "Erro ao salvar item no PostgreSQL.", erro
            );
        }
    }

    /**
     * Lista os itens do acervo, ordenados pelo código.
     *
     * @return os itens cadastrados ou uma lista vazia se não houver itens
     * @throws IllegalStateException se a senha do banco não estiver configurada
     *         ou se a consulta falhar
     */
    @Override
    public List<ItemBiblioteca> listarItens() {
        List<ItemBiblioteca> itens = new ArrayList<>();

        String sql = "SELECT * FROM itens ORDER BY codigo";

        try (Connection conexao = ConexaoPostgres.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {

            while (resultado.next()) {
                itens.add(converterItem(resultado));
            }

        } catch (SQLException erro) {
            throw new IllegalStateException(
                    "Erro ao listar itens no PostgreSQL.", erro
            );
        }

        return itens;
    }

    /**
     * Busca um item do acervo pelo código.
     *
     * @param codigo código do item procurado
     * @return o item encontrado ou {@code null} se o código não estiver cadastrado
     * @throws IllegalStateException se a senha do banco não estiver configurada
     *         ou se a consulta falhar
     */
    @Override
    public ItemBiblioteca buscarItem(String codigo) {
        String sql = "SELECT * FROM itens WHERE codigo = ?";

        try (Connection conexao = ConexaoPostgres.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, codigo);

            try (ResultSet resultado = comando.executeQuery()) {
                if (resultado.next()) {
                    return converterItem(resultado);
                }
            }

        } catch (SQLException erro) {
            throw new IllegalStateException(
                    "Erro ao buscar item no PostgreSQL.", erro
            );
        }

        return null;
    }

    /**
     * Monta um livro ou uma revista a partir de uma linha da tabela de itens.
     *
     * @param resultado linha lida do banco
     * @return o item correspondente, já marcado como emprestado quando estiver indisponível
     * @throws SQLException se a leitura das colunas falhar
     * @throws IllegalStateException se o tipo gravado não for livro nem revista
     */
    private ItemBiblioteca converterItem(ResultSet resultado)
            throws SQLException {

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
            throw new IllegalStateException(
                    "Tipo desconhecido: " + tipo
            );
        }

        if (resultado.getInt("disponivel") == 0) {
            item.emprestar();
        }

        return item;
    }

    /**
     * Grava um novo usuário na tabela de usuários.
     *
     * @param usuario usuário a armazenar
     * @throws IllegalStateException se a senha do banco não estiver configurada
     *         ou se a gravação falhar
     */
    @Override
    public void salvarUsuario(Usuario usuario) {
        String sql = """
                INSERT INTO usuarios (id, nome, email)
                VALUES (?, ?, ?)
                """;

        try (Connection conexao = ConexaoPostgres.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, usuario.getId());
            comando.setString(2, usuario.getNome());
            comando.setString(3, usuario.getEmail());

            comando.executeUpdate();

        } catch (SQLException erro) {
            throw new IllegalStateException(
                    "Erro ao salvar usuário no PostgreSQL.", erro
            );
        }
    }

    /**
     * Lista os usuários cadastrados, ordenados pelo identificador.
     *
     * @return os usuários cadastrados ou uma lista vazia se não houver usuários
     * @throws IllegalStateException se a senha do banco não estiver configurada
     *         ou se a consulta falhar
     */
    @Override
    public List<Usuario> listarUsuarios() {
        List<Usuario> usuarios = new ArrayList<>();

        String sql = """
                SELECT id, nome, email
                FROM usuarios
                ORDER BY id
                """;

        try (Connection conexao = ConexaoPostgres.conectar();
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
            throw new IllegalStateException(
                    "Erro ao listar usuários no PostgreSQL.", erro
            );
        }

        return usuarios;
    }

    /**
     * Busca um usuário pelo identificador.
     *
     * @param id identificador do usuário procurado
     * @return o usuário encontrado ou {@code null} se o ID não estiver cadastrado
     * @throws IllegalStateException se a senha do banco não estiver configurada
     *         ou se a consulta falhar
     */
    @Override
    public Usuario buscarUsuario(int id) {
        String sql = """
                SELECT id, nome, email
                FROM usuarios
                WHERE id = ?
                """;

        try (Connection conexao = ConexaoPostgres.conectar();
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
            throw new IllegalStateException(
                    "Erro ao buscar usuário no PostgreSQL.", erro
            );
        }

        return null;
    }

    /**
     * Lista o histórico de empréstimos, incluindo os devolvidos e os expirados.
     *
     * @return os empréstimos registrados ou uma lista vazia se não houver registros
     * @throws IllegalStateException se a senha do banco não estiver configurada
     *         ou se a consulta falhar
     */
    @Override
    public List<Emprestimo> listarEmprestimos() {
        List<Emprestimo> emprestimos = new ArrayList<>();

        String sql = """
                SELECT codigo_item, id_usuario,
                       data_emprestimo, data_devolucao, status
                FROM emprestimos
                ORDER BY id
                """;

        try (Connection conexao = ConexaoPostgres.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {

            while (resultado.next()) {

                ItemBiblioteca item = buscarItem(
                        resultado.getString("codigo_item")
                );

                Usuario usuario = buscarUsuario(
                        resultado.getInt("id_usuario")
                );

                LocalDateTime dataEmprestimo =
                        LocalDateTime.parse(
                                resultado.getString("data_emprestimo")
                        );

                String textoDevolucao =
                        resultado.getString("data_devolucao");

                LocalDateTime dataDevolucao =
                        textoDevolucao == null
                                ? null
                                : LocalDateTime.parse(textoDevolucao);

                StatusEmprestimo status = StatusEmprestimo.valueOf(
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
                    "Erro ao listar empréstimos no PostgreSQL.", erro
            );
        }

        return emprestimos;
    }

    /**
     * Grava um empréstimo e marca o item como indisponível na mesma transação.
     *
     * @param emprestimo empréstimo a armazenar
     * @throws IllegalStateException se o item já estiver indisponível, se a senha
     *         do banco não estiver configurada ou se a gravação falhar
     */
    @Override
    public void salvarEmprestimo(Emprestimo emprestimo) {

        String sqlItem = """
                UPDATE itens
                SET disponivel = 0
                WHERE codigo = ? AND disponivel = 1
                """;

        String sqlEmprestimo = """
                INSERT INTO emprestimos
                (codigo_item, id_usuario, data_emprestimo, status)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conexao = ConexaoPostgres.conectar()) {
            conexao.setAutoCommit(false);

            try {
                try (PreparedStatement comando =
                             conexao.prepareStatement(sqlItem)) {

                    comando.setString(
                            1, emprestimo.getItem().getCodigo()
                    );

                    if (comando.executeUpdate() != 1) {
                        throw new IllegalStateException(
                                "Item indisponível para empréstimo."
                        );
                    }
                }

                try (PreparedStatement comando =
                             conexao.prepareStatement(sqlEmprestimo)) {

                    comando.setString(
                            1, emprestimo.getItem().getCodigo()
                    );
                    comando.setInt(
                            2, emprestimo.getUsuario().getId()
                    );
                    comando.setString(
                            3, emprestimo.getDataEmprestimo().toString()
                    );
                    comando.setString(
                            4, emprestimo.getStatusAtual().name()
                    );

                    comando.executeUpdate();
                }

                conexao.commit();

            } catch (SQLException | RuntimeException erro) {
                conexao.rollback();
                throw erro;
            }

        } catch (SQLException erro) {
            throw new IllegalStateException(
                    "Erro ao salvar empréstimo no PostgreSQL.", erro
            );
        }
    }

    /**
     * Atualiza o status e a data de devolução de um empréstimo ativo
     * e marca o item como disponível, na mesma transação.
     *
     * @param emprestimo empréstimo com o status atualizado
     * @throws IllegalStateException se o empréstimo ativo ou o item não existir no banco,
     *         se a senha do banco não estiver configurada ou se a atualização falhar
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

        try (Connection conexao = ConexaoPostgres.conectar()) {
            conexao.setAutoCommit(false);

            try {
                try (PreparedStatement comando =
                             conexao.prepareStatement(sqlEmprestimo)) {

                    comando.setString(
                            1, emprestimo.getStatusAtual().name()
                    );

                    if (emprestimo.getDataDevolucao() == null) {
                        comando.setNull(2, Types.VARCHAR);
                    } else {
                        comando.setString(
                                2,
                                emprestimo.getDataDevolucao().toString()
                        );
                    }

                    comando.setString(
                            3, emprestimo.getItem().getCodigo()
                    );

                    comando.setString(
                            4, emprestimo.getDataEmprestimo().toString()
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
                            1, emprestimo.getItem().getCodigo()
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
                    "Erro ao atualizar empréstimo no PostgreSQL.", erro
            );
        }
    }

    /**
     * Grava título, autor, ISBN ou edição de um item já armazenado.
     * A disponibilidade do item não é alterada por este método.
     *
     * @param item item com os dados novos, identificado pelo código
     * @throws IllegalArgumentException se o tipo do item não for livro nem revista
     * @throws IllegalStateException se o item não existir no banco, se a senha
     *         do banco não estiver configurada ou se a atualização falhar
     */
    @Override
    public void atualizarItem(ItemBiblioteca item) {
        String sql = """
                UPDATE itens
                SET titulo = ?, autor = ?, isbn = ?, edicao = ?
                WHERE codigo = ?
                """;

        try (Connection conexao = ConexaoPostgres.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, item.getTitulo());

            if (item instanceof Livro livro) {
                comando.setString(2, livro.getAutor());
                comando.setString(3, livro.getIsbn());
                comando.setNull(4, Types.INTEGER);

            } else if (item instanceof Revista revista) {
                comando.setNull(2, Types.VARCHAR);
                comando.setNull(3, Types.VARCHAR);
                comando.setInt(4, revista.getNumeroEdicao());

            } else {
                throw new IllegalArgumentException(
                        "Tipo de item não suportado."
                );
            }

            comando.setString(5, item.getCodigo());

            if (comando.executeUpdate() != 1) {
                throw new IllegalStateException(
                        "Item não encontrado no banco."
                );
            }

        } catch (SQLException erro) {
            throw new IllegalStateException(
                    "Erro ao atualizar item no PostgreSQL.", erro
            );
        }
    }

    /**
     * Remove um item do acervo.
     *
     * @param codigo código do item a remover
     * @throws IllegalStateException se o item não existir no banco, se a senha
     *         do banco não estiver configurada ou se a exclusão falhar
     */
    @Override
    public void excluirItem(String codigo) {
        String sql = "DELETE FROM itens WHERE codigo = ?";

        try (Connection conexao = ConexaoPostgres.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, codigo);

            if (comando.executeUpdate() != 1) {
                throw new IllegalStateException(
                        "Item não encontrado no banco."
                );
            }

        } catch (SQLException erro) {
            throw new IllegalStateException(
                    "Erro ao excluir item no PostgreSQL.", erro
            );
        }
    }

    /**
     * Grava o nome e o e-mail de um usuário já armazenado.
     *
     * @param usuario usuário com os dados novos, identificado pelo ID
     * @throws IllegalStateException se o usuário não existir no banco, se a senha
     *         do banco não estiver configurada ou se a atualização falhar
     */
    @Override
    public void atualizarUsuario(Usuario usuario) {
        String sql = """
                UPDATE usuarios
                SET nome = ?, email = ?
                WHERE id = ?
                """;

        try (Connection conexao = ConexaoPostgres.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, usuario.getNome());
            comando.setString(2, usuario.getEmail());
            comando.setInt(3, usuario.getId());

            if (comando.executeUpdate() != 1) {
                throw new IllegalStateException(
                        "Usuário não encontrado no banco."
                );
            }

        } catch (SQLException erro) {
            throw new IllegalStateException(
                    "Erro ao atualizar usuário no PostgreSQL.", erro
            );
        }
    }

    /**
     * Remove um usuário da biblioteca.
     *
     * @param id identificador do usuário a remover
     * @throws IllegalStateException se o usuário não existir no banco, se a senha
     *         do banco não estiver configurada ou se a exclusão falhar
     */
    @Override
    public void excluirUsuario(int id) {
        String sql = "DELETE FROM usuarios WHERE id = ?";

        try (Connection conexao = ConexaoPostgres.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, id);

            if (comando.executeUpdate() != 1) {
                throw new IllegalStateException(
                        "Usuário não encontrado no banco."
                );
            }

        } catch (SQLException erro) {
            throw new IllegalStateException(
                    "Erro ao excluir usuário no PostgreSQL.", erro
            );
        }
    }
}
