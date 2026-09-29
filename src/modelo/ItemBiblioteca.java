package modelo;

public abstract class ItemBiblioteca {
    private final String codigo;
    private String titulo;
    private boolean disponivel = true;

    protected ItemBiblioteca(String codigo, String titulo) {
        validarCodigo(codigo);
        validarTitulo(titulo);
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
        validarTitulo(titulo);
        this.titulo = titulo;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    public void emprestar() {
        if (!disponivel) {
            throw new ItemIndisponivelException("O item já está emprestado.");
        }
        disponivel = false;
    }

    public void devolver() {
        disponivel = true;
    }

    private static void validarCodigo(String codigo) {
        if (codigo == null || !codigo.matches("\\d+")) {
            throw new IllegalArgumentException("O código deve ter apenas números.");
        }
    }

    private static void validarTitulo(String titulo) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("O título não pode ser vazio.");
        }
    }

    @Override
    public String toString() {
        String situacao = disponivel ? "disponível" : "emprestado";
        return getTipo() + " | código: " + codigo
                + " | título: " + titulo
                + " | situação: " + situacao;
    }
}
