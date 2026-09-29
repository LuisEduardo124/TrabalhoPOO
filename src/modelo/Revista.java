package modelo;

public class Revista extends ItemBiblioteca {
    private int numeroEdicao;

    public Revista(String codigo, String titulo, int numeroEdicao) {
        super(codigo, titulo);
        validarNumeroEdicao(numeroEdicao);
        this.numeroEdicao = numeroEdicao;
    }

    @Override
    public String getTipo() {
        return "Revista";
    }

    public int getNumeroEdicao() {
        return numeroEdicao;
    }

    public void setNumeroEdicao(int numeroEdicao) {
        validarNumeroEdicao(numeroEdicao);
        this.numeroEdicao = numeroEdicao;
    }

    private static void validarNumeroEdicao(int numeroEdicao) {
        if (numeroEdicao <= 0) {
            throw new IllegalArgumentException("O número da edição deve ser maior que zero.");
        }
    }

    @Override
    public String toString() {
        return super.toString() + " | edição: " + numeroEdicao;
    }
}
