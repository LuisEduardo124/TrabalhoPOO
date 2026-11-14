package servico;

import java.util.List;

import modelo.Emprestimo;
import modelo.ItemBiblioteca;
import modelo.ItemIndisponivelException;
import modelo.Usuario;

/** Executa as regras de negócio sem conhecer a interface ou o tipo de armazenamento. */
public class Biblioteca {
    private final RepositorioBiblioteca repositorio;

    public Biblioteca(RepositorioBiblioteca repositorio) {
        this.repositorio = repositorio;
    }

    public List<ItemBiblioteca> listarItens() {
        atualizarEmprestimos();
        return repositorio.listarItens();
    }

    public List<Usuario> listarUsuarios() {
        return repositorio.listarUsuarios();
    }

    public List<Emprestimo> listarEmprestimos() {
        atualizarEmprestimos();
        return repositorio.listarEmprestimos();
    }

    public void adicionarItem(ItemBiblioteca item) {
        exigirItemNovo(item);
        repositorio.salvarItem(item);
    }

    public void adicionarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário inválido.");
        }
        if (repositorio.buscarUsuario(usuario.getId()) != null) {
            throw new IllegalArgumentException("Já existe um usuário com esse ID.");
        }
        repositorio.salvarUsuario(usuario);
    }

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
