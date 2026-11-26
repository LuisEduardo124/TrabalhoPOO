package modelo;

/**
 * Representa uma revista do acervo com seu número de edição.
 */
public class Revista extends ItemBiblioteca {
    private int numeroEdicao;

    /**
     * Cria uma revista inicialmente disponível para empréstimo.
     *
     * @param codigo código numérico do item no acervo
     * @param titulo título da revista
     * @param numeroEdicao número da edição, maior que zero
     * @throws IllegalArgumentException se o código ou o título for inválido
     *         ou se o número da edição for menor ou igual a zero
     */
    public Revista(String codigo, String titulo, int numeroEdicao) {
        super(codigo, titulo);
        validarNumeroEdicao(numeroEdicao);
        this.numeroEdicao = numeroEdicao;
    }

    /**
     * Identifica o item como uma revista.
     *
     * @return {@code "Revista"}
     */
    @Override
    public String getTipo() {
        return "Revista";
    }

    /**
     * Retorna o número da edição da revista.
     *
     * @return o número da edição
     */
    public int getNumeroEdicao() {
        return numeroEdicao;
    }

    /**
     * Altera o número da edição da revista.
     *
     * @param numeroEdicao novo número da edição, maior que zero
     * @throws IllegalArgumentException se o número da edição for menor ou igual a zero
     */
    public void setNumeroEdicao(int numeroEdicao) {
        validarNumeroEdicao(numeroEdicao);
        this.numeroEdicao = numeroEdicao;
    }

    /**
     * Garante que o número da edição seja maior que zero.
     *
     * @param numeroEdicao número da edição informado
     * @throws IllegalArgumentException se o número da edição for menor ou igual a zero
     */
    private static void validarNumeroEdicao(int numeroEdicao) {
        if (numeroEdicao <= 0) {
            throw new IllegalArgumentException("O número da edição deve ser maior que zero.");
        }
    }

    /**
     * Acrescenta o número da edição à descrição do item no acervo.
     *
     * @return a descrição da revista em texto
     */
    @Override
    public String toString() {
        return super.toString() + " | edição: " + numeroEdicao;
    }
}
