package modelo;

/** Indica que um item não pode ser emprestado no estado atual. */
public class ItemIndisponivelException extends RuntimeException {

    public ItemIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
