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

    /**
     * Cria um empréstimo ativo com a data e a hora atuais.
     *
     * @param item item emprestado
     * @param usuario usuário que recebe o item
     * @throws IllegalArgumentException se o item ou o usuário for nulo
     */
    public Emprestimo(ItemBiblioteca item, Usuario usuario) {
        this(item, usuario, LocalDateTime.now(), null, StatusEmprestimo.ATIVO);
    }

    /**
     * Reconstitui um empréstimo com as datas e o status fornecidos.
     *
     * @param item item emprestado
     * @param usuario usuário que recebeu o item
     * @param dataEmprestimo data e hora do empréstimo
     * @param dataDevolucao data e hora da devolução ou {@code null}
     *        se não houver devolução registrada
     * @param status status armazenado do empréstimo
     * @throws IllegalArgumentException se o item, o usuário, a data
     *         do empréstimo ou o status for nulo
     */
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

    /**
     * Retorna o item vinculado ao empréstimo.
     *
     * @return o item emprestado
     */
    public ItemBiblioteca getItem() {
        return item;
    }

    /**
     * Retorna o usuário vinculado ao empréstimo.
     *
     * @return o usuário que recebeu o item
     */
    public Usuario getUsuario() {
        return usuario;
    }

    /**
     * Atualiza a situação do empréstimo antes de consultar seu status.
     * Se o prazo tiver vencido e o empréstimo estiver ativo, marca-o
     * como expirado e libera o item.
     *
     * @return o status após a verificação do prazo
     */
    public StatusEmprestimo getStatus() {
        atualizarStatus();
        return status;
    }

    /**
     * Consulta o status armazenado sem verificar o vencimento.
     *
     * @return o status armazenado do empréstimo
     */
    public StatusEmprestimo getStatusAtual() {
        return status;
    }

    /**
     * Verifica se o empréstimo está ativo após atualizar seu status.
     *
     * @return {@code true} se o empréstimo estiver ativo
     */
    public boolean isAtivo() {
        return getStatus() == StatusEmprestimo.ATIVO;
    }

    /**
     * Verifica se o empréstimo expirou após atualizar seu status.
     *
     * @return {@code true} se o empréstimo estiver expirado
     */
    public boolean isExpirado() {
        return getStatus() == StatusEmprestimo.EXPIRADO;
    }

    /**
     * Retorna a data em que o empréstimo foi registrado.
     *
     * @return a data e a hora do empréstimo
     */
    public LocalDateTime getDataEmprestimo() {
        return dataEmprestimo;
    }

    /**
     * Formata a data e a hora do empréstimo para exibição.
     *
     * @return a data do empréstimo no formato {@code dd/MM/yyyy HH:mm}
     */
    public String getDataEmprestimoFormatada() {
        return dataEmprestimo.format(FORMATADOR);
    }

    /**
     * Consulta a data em que o item foi devolvido.
     *
     * @return a data e a hora da devolução ou {@code null} se não foi registrada
     */
    public LocalDateTime getDataDevolucao() {
        return dataDevolucao;
    }

    /**
     * Calcula o prazo de devolução adicionando dois dias à data do empréstimo.
     *
     * @return a data e a hora limite para devolução
     */
    public LocalDateTime getDataLimite() {
        return dataEmprestimo.plus(2, ChronoUnit.DAYS);
    }

    /**
     * Formata o prazo de devolução para exibição.
     *
     * @return o prazo no formato {@code dd/MM/yyyy HH:mm}
     */
    public String getDataLimiteFormatada() {
        return getDataLimite().format(FORMATADOR);
    }

    /**
     * Marca um empréstimo ativo como expirado e libera o item
     * quando o prazo de devolução é atingido ou ultrapassado.
     */
    public void atualizarStatus() {
        if (status == StatusEmprestimo.ATIVO && !LocalDateTime.now().isBefore(getDataLimite())) {
            status = StatusEmprestimo.EXPIRADO;
            item.devolver();
        }
    }

    /**
     * Verifica o vencimento e, se o empréstimo continuar ativo,
     * registra a data da devolução, marca-o como devolvido e libera o item.
     */
    public void devolver() {
        atualizarStatus();
        if (status == StatusEmprestimo.ATIVO) {
            status = StatusEmprestimo.DEVOLVIDO;
            dataDevolucao = LocalDateTime.now();
            item.devolver();
        }
    }

    /**
     * Monta uma descrição do empréstimo após verificar seu status.
     *
     * @return o resumo em texto com item, usuário, datas e status
     */
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
