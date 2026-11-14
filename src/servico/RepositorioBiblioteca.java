package servico;

import java.util.List;

import modelo.Emprestimo;
import modelo.ItemBiblioteca;
import modelo.Usuario;

/** Abstrai o armazenamento usado pelas regras da biblioteca. */
public interface RepositorioBiblioteca {
    List<ItemBiblioteca> listarItens();

    List<Usuario> listarUsuarios();

    List<Emprestimo> listarEmprestimos();

    void salvarItem(ItemBiblioteca item);

    void salvarUsuario(Usuario usuario);

    void salvarEmprestimo(Emprestimo emprestimo);

    ItemBiblioteca buscarItem(String codigo);

    Usuario buscarUsuario(int id);
}
