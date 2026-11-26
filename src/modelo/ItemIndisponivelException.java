package modelo;

/**
 * Sinaliza que a operação não pode continuar porque o item está indisponível.
 * Isso ocorre ao emprestar ou ao excluir um item que já está emprestado.
 */
public class ItemIndisponivelException extends RuntimeException {

    /**
     * Cria a exceção com a mensagem que explica a indisponibilidade.
     *
     * @param mensagem descrição da falha
     */
    public ItemIndisponivelException(String mensagem) {
        super(mensagem);
    }
}