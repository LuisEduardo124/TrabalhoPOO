package modelo;

/**
 * Representa os estados de um empréstimo da biblioteca.
 */
public enum StatusEmprestimo {

    /**
     * Empréstimo em andamento.
     */
    ATIVO,

    /**
     * Empréstimo encerrado com a devolução registrada.
     */
    DEVOLVIDO,

    /**
     * Empréstimo que atingiu o prazo sem uma devolução registrada.
     */
    EXPIRADO
}