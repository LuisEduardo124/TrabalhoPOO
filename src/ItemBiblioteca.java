public abstract class ItemBiblioteca {
    private final String codigo;
    private String titulo;
    private boolean disponivel = true;

    protected ItemBiblioteca(String codigo, String titulo) {
        this.codigo = codigo;
        this.titulo = titulo;
    }

    public abstract String getTipo();

    public String getCodigo() {
        return codigo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    public void emprestar() {
        if (!disponivel) {
            throw new IllegalStateException("O item já está emprestado.");
        }
        disponivel = false;
    }

    public void devolver() {
        disponivel = true;
    }

    @Override
    public String toString() {
        String situacao = disponivel ? "disponível" : "emprestado";
        return getTipo() + " | código: " + codigo
                + " | título: " + titulo
                + " | situação: " + situacao;
    }
}
