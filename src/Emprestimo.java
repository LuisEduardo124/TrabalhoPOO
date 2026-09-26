import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Emprestimo {
    private final ItemBiblioteca item;
    private final Usuario usuario;
    private final LocalDateTime dataEmprestimo;
    private boolean ativo;
    private static final DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public Emprestimo(ItemBiblioteca item, Usuario usuario) {
        this.item = item;
        this.usuario = usuario;
        this.ativo = true;
        this.dataEmprestimo = LocalDateTime.now();
        item.emprestar();
    }

    public ItemBiblioteca getItem() {
        return item;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public boolean isAtivo() {
        atualizarStatus();
        return ativo;
    }

    public boolean isExpirado() {
        atualizarStatus();
        return !ativo && LocalDateTime.now().isAfter(getDataLimite());
    }

    public LocalDateTime getDataLimite() {
        return dataEmprestimo.plus(2, ChronoUnit.DAYS);
    }

    public String getDataLimiteFormatada() {
        return getDataLimite().format(formatador);
    }

    private void atualizarStatus() {
        if (ativo && !LocalDateTime.now().isBefore(getDataLimite())) {
            ativo = false;
            item.devolver();
        }
    }

    public void devolver() {
        atualizarStatus();
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
