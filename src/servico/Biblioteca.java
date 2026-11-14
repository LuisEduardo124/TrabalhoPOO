package servico;

import java.util.List;

import modelo.Emprestimo;
import modelo.ItemBiblioteca;
import modelo.ItemIndisponivelException;
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
        Emprestimo emprestimo = repositorio.listarEmprestimos().stream()
                .filter(registro -> registro.isAtivo())
                .filter(registro -> registro.getItem().getCodigo().equals(codigoItem))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Não existe empréstimo ativo para esse item."));
        emprestimo.devolver();
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
        repositorio.listarEmprestimos().forEach(Emprestimo::atualizarStatus);
    }
}
