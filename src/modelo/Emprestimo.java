package modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/** Registra a relação entre um usuário e um item emprestado. */
public class Emprestimo {
    private static final DateTimeFormatter FORMATADOR = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ItemBiblioteca item;
    private final Usuario usuario;
    private final LocalDateTime dataEmprestimo;
    private LocalDateTime dataDevolucao;
    private StatusEmprestimo status;

    public Emprestimo(ItemBiblioteca item, Usuario usuario) {
        this(item, usuario, LocalDateTime.now(), null, StatusEmprestimo.ATIVO);
    }

    /** Reconstitui um empréstimo persistido no banco de dados. */
    public Emprestimo(ItemBiblioteca item, Usuario usuario, LocalDateTime dataEmprestimo,
            LocalDateTime dataDevolucao, StatusEmprestimo status) {
        if (item == null || usuario == null || dataEmprestimo == null || status == null) {
            throw new IllegalArgumentException("Os dados do empréstimo são obrigatórios.");
        }
        this.item = item;
        this.usuario = usuario;
        this.status = status;
        this.dataEmprestimo = dataEmprestimo;
        this.dataDevolucao = dataDevolucao;
    }

    public ItemBiblioteca getItem() {
        return item;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public StatusEmprestimo getStatus() {
        atualizarStatus();
        return status;
    }

    /** Retorna o status armazenado sem executar a atualização automática de vencimento. */
    public StatusEmprestimo getStatusAtual() {
        return status;
    }

    public boolean isAtivo() {
        return getStatus() == StatusEmprestimo.ATIVO;
    }

    public boolean isExpirado() {
        return getStatus() == StatusEmprestimo.EXPIRADO;
    }

    public LocalDateTime getDataEmprestimo() {
        return dataEmprestimo;
    }

    public String getDataEmprestimoFormatada() {
        return dataEmprestimo.format(FORMATADOR);
    }

    public LocalDateTime getDataDevolucao() {
        return dataDevolucao;
    }

    public LocalDateTime getDataLimite() {
        return dataEmprestimo.plus(2, ChronoUnit.DAYS);
    }

    public String getDataLimiteFormatada() {
        return getDataLimite().format(FORMATADOR);
    }

    public void atualizarStatus() {
        if (status == StatusEmprestimo.ATIVO && !LocalDateTime.now().isBefore(getDataLimite())) {
            status = StatusEmprestimo.EXPIRADO;
            item.devolver();
        }
    }

    public void devolver() {
        atualizarStatus();
        if (status == StatusEmprestimo.ATIVO) {
            status = StatusEmprestimo.DEVOLVIDO;
            dataDevolucao = LocalDateTime.now();
            item.devolver();
        }
    }

    @Override
    public String toString() {
        String texto = "Empréstimo | item: " + item.getTitulo()
                + " | usuário: " + usuario.getNome()
                + " | emprestado em: " + getDataEmprestimoFormatada()
                + " | devolver até: " + getDataLimiteFormatada()
                + " | status: " + getStatus();
        if (dataDevolucao != null) {
            texto += " | devolvido em: " + dataDevolucao.format(FORMATADOR);
        }
        return texto;
    }
}
