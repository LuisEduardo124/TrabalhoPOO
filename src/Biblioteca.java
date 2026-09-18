import java.util.ArrayList;
import java.util.List;

public class Biblioteca {
    private final List<ItemBiblioteca> itens = new ArrayList<>();
    private final List<Usuario> usuarios = new ArrayList<>();
    private final List<Emprestimo> emprestimos = new ArrayList<>();

    public void adicionarItem(ItemBiblioteca item) {
        if (procurarItem(item.getCodigo()) != null) {
            throw new IllegalArgumentException("Já existe um item com esse código.");
        }
        itens.add(item);
    }

    public void adicionarUsuario(Usuario usuario) {
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

    public void emprestar(String codigoItem, int idUsuario) {
        ItemBiblioteca item = procurarItem(codigoItem);
        Usuario usuario = procurarUsuario(idUsuario);

        if (item == null) {
            throw new IllegalArgumentException("Item não encontrado.");
        }
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }
        emprestimos.add(new Emprestimo(item, usuario));
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

    public void mostrarItens() {
        if (itens.isEmpty()) {
            System.out.println("Nenhum item cadastrado.");
            return;
        }
        for (ItemBiblioteca item : itens) {
            System.out.println(item);
        }
    }

    public void mostrarUsuarios() {
        if (usuarios.isEmpty()) {
            System.out.println("Nenhum usuário cadastrado.");
            return;
        }
        for (Usuario usuario : usuarios) {
            System.out.println(usuario);
        }
    }

    public void mostrarEmprestimos() {
        if (emprestimos.isEmpty()) {
            System.out.println("Nenhum empréstimo realizado.");
            return;
        }
        for (Emprestimo emprestimo : emprestimos) {
            System.out.println(emprestimo);
        }
    }
}
