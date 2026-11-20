package modelo;

/**
 * Indica que um empréstimo foi recusado porque o item está indisponível.
 */
public class ItemIndisponivelException extends RuntimeException {

    /**
     * Cria a exceção com uma mensagem que explica por que o empréstimo foi recusado.
     *
     * @param mensagem descrição da falha
     */
    public ItemIndisponivelException(String mensagem) {
        super(mensagem);
    }
}