package servico;

import java.util.ArrayList;
import java.util.List;

import modelo.Emprestimo;
import modelo.ItemBiblioteca;
import modelo.Usuario;

/** Implementação em memória usada por esta versão sem banco de dados. */
public class RepositorioBibliotecaMemoria implements RepositorioBiblioteca {
    private final List<ItemBiblioteca> itens = new ArrayList<>();
    private final List<Usuario> usuarios = new ArrayList<>();
    private final List<Emprestimo> emprestimos = new ArrayList<>();

    @Override
    public List<ItemBiblioteca> listarItens() {
        return new ArrayList<>(itens);
    }

    @Override
    public List<Usuario> listarUsuarios() {
        return new ArrayList<>(usuarios);
    }

    @Override
    public List<Emprestimo> listarEmprestimos() {
        return new ArrayList<>(emprestimos);
    }

    @Override
    public void salvarItem(ItemBiblioteca item) {
        itens.add(item);
    }

    @Override
    public void salvarUsuario(Usuario usuario) {
        usuarios.add(usuario);
    }

    @Override
    public void salvarEmprestimo(Emprestimo emprestimo) {
        emprestimos.add(emprestimo);
    }

    @Override
    public ItemBiblioteca buscarItem(String codigo) {
        return itens.stream()
                .filter(item -> item.getCodigo().equals(codigo))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Usuario buscarUsuario(int id) {
        return usuarios.stream()
                .filter(usuario -> usuario.getId() == id)
                .findFirst()
                .orElse(null);
    }
}
