package modelo;

/** Representa um item do acervo e controla sua disponibilidade. */
public abstract class ItemBiblioteca {
    private final String codigo;
    private String titulo;
    private boolean disponivel = true;

    /**
     * Inicializa um item do acervo como disponível para empréstimo.
     *
     * @param codigo código numérico do item
     * @param titulo título do item
     * @throws IllegalArgumentException se o código não contiver apenas dígitos
     *         ou se o código ou o título estiver ausente ou vazio
     */
    protected ItemBiblioteca(String codigo, String titulo) {
        validarCodigo(codigo);
        validarTitulo(titulo);
        this.codigo = codigo;
        this.titulo = titulo;
    }

    /**
     * Identifica o tipo do item no acervo.
     *
     * @return o tipo do item, como livro ou revista
     */
    public abstract String getTipo();

    /**
     * Retorna o código do item no acervo.
     *
     * @return o código do item
     */
    public String getCodigo() {
        return codigo;
    }

    /**
     * Retorna o título do item.
     *
     * @return o título atual do item
     */
    public String getTitulo() {
        return titulo;
    }

    /**
     * Altera o título do item.
     *
     * @param titulo novo título do item
     * @throws IllegalArgumentException se o título for nulo ou estiver em branco
     */
    public void setTitulo(String titulo) {
        validarTitulo(titulo);
        this.titulo = titulo;
    }

    /**
     * Verifica se o item pode ser emprestado.
     *
     * @return {@code true} se o item estiver disponível ou {@code false} caso contrário
     */
    public boolean isDisponivel() {
        return disponivel;
    }

    /**
     * Marca o item como indisponível após verificar sua disponibilidade.
     *
     * @throws ItemIndisponivelException se o item já estiver indisponível
     */
    public void emprestar() {
        if (!disponivel) {
            throw new ItemIndisponivelException("O item já está emprestado.");
        }
        disponivel = false;
    }

    /**
     * Marca o item como disponível para um novo empréstimo.
     */
    public void devolver() {
        disponivel = true;
    }

    /**
     * Garante que o código contenha apenas dígitos.
     *
     * @param codigo código informado para o item
     * @throws IllegalArgumentException se o código for nulo ou contiver
     *         caracteres que não sejam dígitos
     */
    private static void validarCodigo(String codigo) {
        if (codigo == null || !codigo.matches("\\d+")) {
            throw new IllegalArgumentException("O código deve ter apenas números.");
        }
    }

    /**
     * Garante que o título não esteja ausente nem vazio.
     *
     * @param titulo título informado para o item
     * @throws IllegalArgumentException se o título for nulo ou estiver em branco
     */
    private static void validarTitulo(String titulo) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("O título não pode ser vazio.");
        }
    }

    /**
     * Monta uma descrição do item com tipo, código, título e disponibilidade.
     *
     * @return a descrição do item em texto
     */
    @Override
    public String toString() {
        String situacao = disponivel ? "disponível" : "emprestado";
        return getTipo() + " | código: " + codigo
                + " | título: " + titulo
                + " | situação: " + situacao;
    }
}
