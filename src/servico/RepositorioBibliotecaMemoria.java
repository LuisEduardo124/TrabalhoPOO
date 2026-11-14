package servico;

import java.util.ArrayList;
import java.util.List;

import modelo.Emprestimo;
import modelo.ItemBiblioteca;
import modelo.Usuario;

/**
 * Armazena os dados da biblioteca em memória durante a execução.
 * Os registros são perdidos quando a aplicação é encerrada.
 * As listagens retornam cópias das listas, mas mantêm as referências
 * aos mesmos objetos.
 * Implementação em memória usada por esta versão sem banco de dados.
 */
public class RepositorioBibliotecaMemoria implements RepositorioBiblioteca {
    private final List<ItemBiblioteca> itens = new ArrayList<>();
    private final List<Usuario> usuarios = new ArrayList<>();
    private final List<Emprestimo> emprestimos = new ArrayList<>();

    /** {@inheritDoc} */
    @Override
    public List<ItemBiblioteca> listarItens() {
        return new ArrayList<>(itens);
    }

    /** {@inheritDoc} */
    @Override
    public List<Usuario> listarUsuarios() {
        return new ArrayList<>(usuarios);
    }


    /** {@inheritDoc} */
    @Override
    public List<Emprestimo> listarEmprestimos() {
        return new ArrayList<>(emprestimos);
    }


    /** {@inheritDoc} */
    @Override
    public void salvarItem(ItemBiblioteca item) {
        itens.add(item);
    }

    /** {@inheritDoc} */
    @Override
    public void salvarUsuario(Usuario usuario) {
        usuarios.add(usuario);
    }


    /** {@inheritDoc} */
    @Override
    public void salvarEmprestimo(Emprestimo emprestimo) {
        emprestimos.add(emprestimo);
    }


    /** {@inheritDoc} */
    @Override
    public ItemBiblioteca buscarItem(String codigo) {
        return itens.stream()
                .filter(item -> item.getCodigo().equals(codigo))
                .findFirst()
                .orElse(null);
    }

    /** {@inheritDoc} */
    @Override
    public Usuario buscarUsuario(int id) {
        return usuarios.stream()
                .filter(usuario -> usuario.getId() == id)
                .findFirst()
                .orElse(null);
    }
}
