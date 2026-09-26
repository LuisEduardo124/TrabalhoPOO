import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Emprestimo {
    private final ItemBiblioteca item;
    private final Usuario usuario;
    private boolean ativo;
    LocalTime horaEmprestimo = LocalTime.now();
    DateTimeFormatter formatador = DateTimeFormatter.ofPattern("HH:mm:ss");

    public Emprestimo(ItemBiblioteca item, Usuario usuario) {
        this.item = item;
        this.usuario = usuario;
        this.ativo = true;
        item.emprestar();
    }

    public ItemBiblioteca getItem() {
        return item;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public boolean isAtivo() {
        if (ativo) {
            System.out.println("Empréstimo ativo. Hora do empréstimo: " + horaEmprestimo.format(formatador));
        } else {
            System.out.println("Empréstimo inativo.");
        }
        return ativo;
    }

    public void devolver() {
        if (ativo) {
            ativo = false;
            item.devolver();
        }
    }

    @Override
    public String toString() {
        return "Empréstimo | item: " + item.getTitulo()
                + " | usuário: " + usuario.getNome()
                + " | ativo: " + ativo;
    }
}
