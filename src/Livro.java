public class Livro extends ItemBiblioteca {
    private String autor;
    private String isbn;

    public Livro(String codigo, String titulo, String autor, String isbn) {
        super(codigo, titulo);
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
        this.autor = autor;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    @Override
    public String toString() {
        return super.toString() + " | autor: " + autor + " | ISBN: " + isbn;
    }
}
