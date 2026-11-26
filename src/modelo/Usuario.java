package modelo;

/**
 * Representa um usuário da biblioteca, com identificador fixo,
 * nome e e-mail.
 */
public class Usuario {
    private final int id;
    private String nome;
    private String email;

    /**
     * Cria um usuário após validar seus dados.
     *
     * @param id identificador do usuário, maior que zero
     * @param nome nome do usuário
     * @param email endereço de e-mail que deve conter {@code @gmail.com}
     * @throws IllegalArgumentException se o ID for menor ou igual a zero,
     *         se o nome for nulo ou estiver em branco, ou se o e-mail for
     *         nulo ou não contiver {@code @gmail.com}
     */
    public Usuario(int id, String nome, String email) {
        if (id <= 0) {
            throw new IllegalArgumentException("O ID deve ser maior que zero.");
        }
        validarNome(nome);
        validarEmail(email);
        this.id = id;
        this.nome = nome;
        this.email = email;
    }

    /**
     * Retorna o identificador do usuário.
     *
     * @return o ID definido na criação do usuário
     */
    public int getId() {
        return id;
    }

    /**
     * Retorna o nome do usuário.
     *
     * @return o nome atual do usuário
     */
    public String getNome() {
        return nome;
    }

    /**
     * Altera o nome do usuário.
     *
     * @param nome novo nome do usuário
     * @throws IllegalArgumentException se o nome for nulo ou estiver em branco
     */
    public void setNome(String nome) {
        validarNome(nome);
        this.nome = nome;
    }

    /**
     * Retorna o e-mail do usuário.
     *
     * @return o e-mail atual do usuário
     */
    public String getEmail() {
        return email;
    }

    /**
     * Altera o e-mail do usuário.
     *
     * @param email novo endereço de e-mail
     * @throws IllegalArgumentException se o e-mail for nulo
     *         ou não contiver {@code @gmail.com}
     */
    public void setEmail(String email) {
        validarEmail(email);
        this.email = email;
    }

    /**
     * Garante que o nome não esteja ausente nem vazio.
     *
     * @param nome nome informado
     * @throws IllegalArgumentException se o nome for nulo ou estiver em branco
     */
    private static void validarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome não pode ser vazio.");
        }
    }

    /**
     * Garante que o e-mail contenha {@code @gmail.com}.
     *
     * @param email endereço de e-mail informado
     * @throws IllegalArgumentException se o e-mail for nulo
     *         ou não contiver {@code @gmail.com}
     */
    private static void validarEmail(String email) {
        if (email == null || !email.contains("@gmail.com")) {
            throw new IllegalArgumentException("O e-mail deve conter \"@gmail.com\".");
        }
    }

    /**
     * Monta uma descrição do usuário com seu ID, nome e e-mail.
     *
     * @return a descrição do usuário em texto
     */
    @Override
    public String toString() {
        return "ID: " + id + " | nome: " + nome + " | e-mail: " + email;
    }
}
