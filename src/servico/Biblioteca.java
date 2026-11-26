package servico;

import java.util.List;

import modelo.Emprestimo;
import modelo.ItemBiblioteca;
import modelo.ItemIndisponivelException;
import modelo.Livro;
import modelo.Revista;
import modelo.Livro;
import modelo.Revista;
import modelo.Usuario;

/** Executa as regras de negócio sem conhecer a interface ou o tipo de armazenamento. */
public class Biblioteca {
    private final RepositorioBiblioteca repositorio;

    /**
     * Cria o serviço da biblioteca com o repositório fornecido.
     *
     * @param repositorio repositório usado para consultar e salvar os dados
     */

    public Biblioteca(RepositorioBiblioteca repositorio) {
        this.repositorio = repositorio;
    }
    /**
     * Atualiza os empréstimos vencidos antes de listar os itens do acervo.
     *
     * @return lista dos itens cadastrados
     */
    public List<ItemBiblioteca> listarItens() {
        atualizarEmprestimos();
        return repositorio.listarItens();
    }
    /**
     * Lista os usuários cadastrados na biblioteca.
     *
     * @return lista dos usuários cadastrados
     */
    public List<Usuario> listarUsuarios() {
        return repositorio.listarUsuarios();
    }
    /**
     * Atualiza os empréstimos vencidos e retorna o histórico de empréstimos.
     *
     * @return lista dos empréstimos registrados
     */
    public List<Emprestimo> listarEmprestimos() {
        atualizarEmprestimos();
        return repositorio.listarEmprestimos();
    }
    /**
     * Cadastra um item cujo código ainda não está em uso no acervo.
     *
     * @param item item a cadastrar
     * @throws IllegalArgumentException se o item for nulo ou o código já estiver cadastrado
     */
    public void adicionarItem(ItemBiblioteca item) {
        exigirItemNovo(item);
        repositorio.salvarItem(item);
    }
    /**
     * Cadastra um usuário cujo ID ainda não está em uso.
     *
     * @param usuario usuário a cadastrar
     * @throws IllegalArgumentException se o usuário for nulo ou o ID já estiver cadastrado
     */
    public void adicionarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário inválido.");
        }
        if (repositorio.buscarUsuario(usuario.getId()) != null) {
            throw new IllegalArgumentException("Já existe um usuário com esse ID.");
        }
        repositorio.salvarUsuario(usuario);
    }
    /**
     * Registra o empréstimo de um item disponível para um usuário cadastrado.
     *
     * @param codigoItem código do item a emprestar
     * @param idUsuario identificador do usuário que receberá o item
     * @return o empréstimo registrado
     * @throws IllegalArgumentException se o item ou o usuário não for encontrado
     * @throws ItemIndisponivelException se o item não estiver disponível
     */
    public Emprestimo emprestar(String codigoItem, int idUsuario) {
        atualizarEmprestimos();
        ItemBiblioteca item = exigirItem(codigoItem);
        Usuario usuario = exigirUsuario(idUsuario);
        if (!item.isDisponivel()) {
            throw new ItemIndisponivelException("O item já está emprestado.");
        }
        item.emprestar();
        Emprestimo emprestimo = new Emprestimo(item, usuario);
        repositorio.salvarEmprestimo(emprestimo);
        return emprestimo;
    }
    /**
     * Registra a devolução de um item com empréstimo ativo.
     *
     * @param codigoItem código do item a devolver
     * @throws IllegalArgumentException se não houver empréstimo ativo para o item
     */
    public void devolver(String codigoItem) {
        atualizarEmprestimos();

        Emprestimo emprestimo = repositorio.listarEmprestimos().stream()
                .filter(registro -> registro.isAtivo())
                .filter(registro -> registro.getItem().getCodigo().equals(codigoItem))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Não existe empréstimo ativo para esse item."));
        emprestimo.devolver();
        repositorio.atualizarEmprestimo(emprestimo);
    }
        /**
     * Altera os dados de um livro já cadastrado. O código não muda.
     *
     * @param codigo código do livro a alterar
     * @param titulo novo título
     * @param autor  novo autor
     * @param isbn   novo ISBN
     * @throws IllegalArgumentException se o item não existir, não for um livro ou os dados forem inválidos
     */
    public void editarLivro(String codigo, String titulo, String autor, String isbn) {
        ItemBiblioteca item = exigirItem(codigo);
        if (!(item instanceof Livro livro)) {
            throw new IllegalArgumentException("O item " + codigo + " não é um livro.");
        }
        Livro validado = new Livro(codigo, titulo, autor, isbn);
        livro.setTitulo(validado.getTitulo());
        livro.setAutor(validado.getAutor());
        livro.setIsbn(validado.getIsbn());
        repositorio.atualizarItem(livro);
    }

    /**
     * Altera os dados de uma revista já cadastrada. O código não muda.
     *
     * @param codigo       código da revista a alterar
     * @param titulo       novo título
     * @param numeroEdicao novo número da edição
     * @throws IllegalArgumentException se o item não existir, não for uma revista ou os dados forem inválidos
     */
    public void editarRevista(String codigo, String titulo, int numeroEdicao) {
        ItemBiblioteca item = exigirItem(codigo);
        if (!(item instanceof Revista revista)) {
            throw new IllegalArgumentException("O item " + codigo + " não é uma revista.");
        }
        Revista validada = new Revista(codigo, titulo, numeroEdicao);
        revista.setTitulo(validada.getTitulo());
        revista.setNumeroEdicao(validada.getNumeroEdicao());
        repositorio.atualizarItem(revista);
    }

    /**
     * Exclui um item do acervo. Itens emprestados ou com histórico de empréstimos não são excluídos,
     * para que o histórico continue consistente.
     *
     * @param codigo código do item a excluir
     * @throws IllegalArgumentException  se o item não existir ou tiver histórico de empréstimos
     * @throws ItemIndisponivelException se o item estiver emprestado
     */
    public void excluirItem(String codigo) {
        atualizarEmprestimos();
        ItemBiblioteca item = exigirItem(codigo);
        if (!item.isDisponivel()) {
            throw new ItemIndisponivelException("O item está emprestado. Faça a devolução antes de excluir.");
        }
        boolean temHistorico = repositorio.listarEmprestimos().stream()
                .anyMatch(registro -> registro.getItem().getCodigo().equals(codigo));
        if (temHistorico) {
            throw new IllegalArgumentException("Não é possível excluir: o item possui histórico de empréstimos.");
        }
        repositorio.excluirItem(codigo);
    }

    /**
     * Altera o nome e o e-mail de um usuário já cadastrado. O ID não muda.
     *
     * @param id    identificador do usuário a alterar
     * @param nome  novo nome
     * @param email novo e-mail
     * @throws IllegalArgumentException se o usuário não existir ou os dados forem inválidos
     */
    public void editarUsuario(int id, String nome, String email) {
        Usuario usuario = exigirUsuario(id);
        Usuario validado = new Usuario(id, nome, email);
        usuario.setNome(validado.getNome());
        usuario.setEmail(validado.getEmail());
        repositorio.atualizarUsuario(usuario);
    }

    /**
     * Exclui um usuário. Usuários com empréstimos registrados não são excluídos,
     * para que o histórico continue consistente.
     *
     * @param id identificador do usuário a excluir
     * @throws IllegalArgumentException se o usuário não existir ou tiver empréstimos registrados
     */
    public void excluirUsuario(int id) {
        atualizarEmprestimos();
        exigirUsuario(id);
        var emprestimosDoUsuario = repositorio.listarEmprestimos().stream()
                .filter(registro -> registro.getUsuario().getId() == id)
                .toList();
        if (emprestimosDoUsuario.stream().anyMatch(registro -> registro.isAtivo())) {
            throw new IllegalArgumentException("Não é possível excluir: o usuário tem empréstimo ativo.");
        }
        if (!emprestimosDoUsuario.isEmpty()) {
            throw new IllegalArgumentException("Não é possível excluir: o usuário possui histórico de empréstimos.");
        }
        repositorio.excluirUsuario(id);
    }

    private void exigirItemNovo(ItemBiblioteca item) {
        if (item == null) {
            throw new IllegalArgumentException("Item inválido.");
        }
        if (repositorio.buscarItem(item.getCodigo()) != null) {
            throw new IllegalArgumentException("Já existe um item com esse código.");
        }
    }

    private ItemBiblioteca exigirItem(String codigo) {
        ItemBiblioteca item = repositorio.buscarItem(codigo);
        if (item == null) {
            throw new IllegalArgumentException("Item não encontrado.");
        }
        return item;
    }

    private Usuario exigirUsuario(int id) {
        Usuario usuario = repositorio.buscarUsuario(id);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }
        return usuario;
    }

    private void atualizarEmprestimos() {
        for (Emprestimo emprestimo : repositorio.listarEmprestimos()) {
            modelo.StatusEmprestimo statusAnterior = emprestimo.getStatusAtual();

            emprestimo.atualizarStatus();

            if (statusAnterior != emprestimo.getStatusAtual()) {
                repositorio.atualizarEmprestimo(emprestimo);
            }
        }
    }
}
