package servico;
import java.util.ArrayList;
import java.util.List;

import modelo.Emprestimo;
import modelo.ItemBiblioteca;
import modelo.ItemIndisponivelException;
import modelo.Usuario;

public class Biblioteca {
    private final List<ItemBiblioteca> itens = new ArrayList<>();
    private final List<Usuario> usuarios = new ArrayList<>();
    private final List<Emprestimo> emprestimos = new ArrayList<>();

    public List<ItemBiblioteca> getItens() {
        atualizarEmprestimos();
        return new ArrayList<>(itens);
    }

    public List<Usuario> getUsuarios() {
        return new ArrayList<>(usuarios);
    }

    public List<Emprestimo> getEmprestimos() {
        atualizarEmprestimos();
        return new ArrayList<>(emprestimos);
    }

    public void adicionarItem(ItemBiblioteca item) {
        if (item == null) {
            throw new IllegalArgumentException("Item inválido.");
        }
        if (procurarItem(item.getCodigo()) != null) {
            throw new IllegalArgumentException("Já existe um item com esse código.");
        }
        itens.add(item);
    }

    public void adicionarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário inválido.");
        }
        if (procurarUsuario(usuario.getId()) != null) {
            throw new IllegalArgumentException("Já existe um usuário com esse ID.");
        }
        usuarios.add(usuario);
    }

    public ItemBiblioteca procurarItem(String codigo) {
        for (ItemBiblioteca item : itens) {
            if (item.getCodigo().equalsIgnoreCase(codigo)) {
                return item;
            }
        }
        return null;
    }

    public Usuario procurarUsuario(int id) {
        for (Usuario usuario : usuarios) {
            if (usuario.getId() == id) {
                return usuario;
            }
        }
        return null;
    }

    public Emprestimo emprestar(String codigoItem, int idUsuario) {
        ItemBiblioteca item = procurarItem(codigoItem);
        Usuario usuario = procurarUsuario(idUsuario);

        if (item == null) {
            throw new IllegalArgumentException("Item não encontrado.");
        }
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }

        atualizarEmprestimos();
        if (!item.isDisponivel()) {
            throw new ItemIndisponivelException("O item \"" + item.getTitulo() + "\" já está emprestado.");
        }

        item.emprestar();
        Emprestimo emprestimo = new Emprestimo(item, usuario);
        emprestimos.add(emprestimo);
        return emprestimo;
    }

    public void devolver(String codigoItem) {
        for (Emprestimo emprestimo : emprestimos) {
            if (emprestimo.isAtivo()
                    && emprestimo.getItem().getCodigo().equalsIgnoreCase(codigoItem)) {
                emprestimo.devolver();
                return;
            }
        }
        throw new IllegalArgumentException("Não existe empréstimo ativo para esse item.");
    }

    private void atualizarEmprestimos() {
        for (Emprestimo emprestimo : emprestimos) {
            emprestimo.atualizarStatus();
        }
    }
}
