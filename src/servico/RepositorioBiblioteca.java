package servico;

import java.util.List;

import modelo.Emprestimo;
import modelo.ItemBiblioteca;
import modelo.Usuario;

/**
 * Define as operações de armazenamento usadas pelo serviço da biblioteca.
 * O serviço usa este contrato sem precisar saber onde os dados são guardados.
 */
public interface RepositorioBiblioteca {

    /**
     * Lista os itens cadastrados no acervo.
     *
     * @return os itens cadastrados ou uma lista vazia se não houver itens
     */
    List<ItemBiblioteca> listarItens();

    /**
     * Lista os usuários cadastrados na biblioteca.
     *
     * @return os usuários cadastrados ou uma lista vazia se não houver usuários
     */
    List<Usuario> listarUsuarios();

    /**
     * Lista o histórico de empréstimos, incluindo os devolvidos e os expirados.
     *
     * @return os empréstimos registrados ou uma lista vazia se não houver registros
     */
    List<Emprestimo> listarEmprestimos();

    /**
     * Armazena um novo item no acervo.
     *
     * @param item item a armazenar
     */
    void salvarItem(ItemBiblioteca item);

    /**
     * Armazena um novo usuário.
     *
     * @param usuario usuário a armazenar
     */
    void salvarUsuario(Usuario usuario);

    /**
     * Armazena um novo registro de empréstimo.
     *
     * @param emprestimo empréstimo a armazenar
     */
    void salvarEmprestimo(Emprestimo emprestimo);

    /**
     * Busca um item do acervo pelo código.
     *
     * @param codigo código do item procurado
     * @return o item encontrado ou {@code null} se o código não estiver cadastrado
     */
    ItemBiblioteca buscarItem(String codigo);

    /**
     * Busca um usuário pelo identificador.
     *
     * @param id identificador do usuário procurado
     * @return o usuário encontrado ou {@code null} se o ID não estiver cadastrado
     */
    Usuario buscarUsuario(int id);

    /**
     * Atualiza o status e a data de devolução de um empréstimo,
     * além da disponibilidade do item correspondente.
     *
     * @param emprestimo empréstimo com os dados atualizados
     */
    void atualizarEmprestimo(Emprestimo emprestimo);

    /**
     * Grava os dados alterados de um item já armazenado (título, autor, ISBN ou edição).
     * A disponibilidade do item não é alterada por este método.
     *
     * @param item item com os dados novos, identificado pelo código
     */
    void atualizarItem(ItemBiblioteca item);

    /**
     * Remove um item do acervo.
     *
     * @param codigo código do item a remover
     */
    void excluirItem(String codigo);

    /**
     * Grava os dados alterados de um usuário já armazenado (nome ou e-mail).
     *
     * @param usuario usuário com os dados novos, identificado pelo ID
     */
    void atualizarUsuario(Usuario usuario);

    /**
     * Remove um usuário da biblioteca.
     *
     * @param id identificador do usuário a remover
     */
    void excluirUsuario(int id);
}
