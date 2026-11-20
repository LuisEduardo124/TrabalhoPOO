package modelo;

/**
 * Representa um livro do acervo, identificado por seu código, autor e ISBN.
 */
public class Livro extends ItemBiblioteca {
    private String autor;
    private String isbn;

    /**
     * Cria um livro inicialmente disponível para empréstimo.
     *
     * @param codigo código numérico do item no acervo
     * @param titulo título do livro
     * @param autor nome do autor
     * @param isbn ISBN com exatamente 13 dígitos
     * @throws IllegalArgumentException se o código ou o título for inválido,
     *         se o autor estiver vazio ou contiver caracteres além de letras
     *         e espaços, ou se o ISBN não tiver exatamente 13 dígitos
     */
    public Livro(String codigo, String titulo, String autor, String isbn) {
        super(codigo, titulo);
        validarAutor(autor);
        validarIsbn(isbn);
        this.autor = autor;
        this.isbn = isbn;
    }

    /**
     * Identifica o item como um livro.
     *
     * @return {@code "Livro"}
     */
    @Override
    public String getTipo() {
        return "Livro";
    }

    /**
     * Retorna o nome do autor.
     *
     * @return o nome do autor do livro
     */
    public String getAutor() {
        return autor;
    }

    /**
     * Altera o nome do autor do livro.
     *
     * @param autor novo nome do autor
     * @throws IllegalArgumentException se o nome for nulo, estiver vazio
     *         ou contiver caracteres além de letras e espaços
     */
    public void setAutor(String autor) {
        validarAutor(autor);
        this.autor = autor;
    }

    /**
     * Retorna o ISBN do livro.
     *
     * @return o ISBN do livro
     */
    public String getIsbn() {
        return isbn;
    }

    /**
     * Altera o ISBN do livro.
     *
     * @param isbn novo ISBN com exatamente 13 dígitos
     * @throws IllegalArgumentException se o ISBN for nulo ou não tiver
     *         exatamente 13 dígitos
     */
    public void setIsbn(String isbn) {
        validarIsbn(isbn);
        this.isbn = isbn;
    }

    private static void validarAutor(String autor) {
        if (autor == null || !autor.matches("[a-zA-ZÀ-ÿ\\s]+") || autor.trim().isEmpty()) {
            throw new IllegalArgumentException("O autor deve ter apenas letras.");
        }
    }

    private static void validarIsbn(String isbn) {
        if (isbn == null || !isbn.matches("\\d{13}")) {
            throw new IllegalArgumentException("O ISBN deve ter 13 números.");
        }
    }

    /**
     * Acrescenta o autor e o ISBN à descrição do item no acervo.
     *
     * @return a descrição do livro em texto
     */
    @Override
    public String toString() {
        return super.toString() + " | autor: " + autor + " | ISBN: " + isbn;
    }
}
