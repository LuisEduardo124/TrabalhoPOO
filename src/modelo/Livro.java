package modelo;
public class Livro extends ItemBiblioteca {
    private String autor;
    private String isbn;

    public Livro(String codigo, String titulo, String autor, String isbn) {
        super(codigo, titulo);
        validarAutor(autor);
        validarIsbn(isbn);
        this.autor = autor;
        this.isbn = isbn;
    }

    @Override
    public String getTipo() {
        return "Livro";
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        validarAutor(autor);
        this.autor = autor;
    }

    public String getIsbn() {
        return isbn;
    }

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

    @Override
    public String toString() {
        return super.toString() + " | autor: " + autor + " | ISBN: " + isbn;
    }
}
