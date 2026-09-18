class livro {

    private String titulo;
    private String autor;
    private String isbn;
    private int anoPublicacao;
    private boolean disponivel;

    
    // get/set isbn
     public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getIsbn() {
        return isbn;
    }


    // get/set titulo
    public String getTitulo(){
        return titulo;
    }

    public void setTitulo(String valor){
        titulo = valor;
    }

    
    // get/set Autor
    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    // get/set AnoPublicação
    public int getAnoPublicacao() {
        return anoPublicacao;
    }

    public void setAnoPublicacao(int anoPublicacao) {
        this.anoPublicacao = anoPublicacao;
    }

    
    
    // get/set Disponivel
    public boolean isDisponivel() {
        return disponivel;
    }

    public void setDisponivel(boolean disponivel) {
        this.disponivel = disponivel;
    }

}