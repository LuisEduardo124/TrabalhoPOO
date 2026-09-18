public class Revista extends ItemBiblioteca {
    private int numeroEdicao;

    public Revista(String codigo, String titulo, int numeroEdicao) {
        super(codigo, titulo);
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
        this.numeroEdicao = numeroEdicao;
    }

    @Override
    public String toString() {
        return super.toString() + " | edição: " + numeroEdicao;
    }
}
