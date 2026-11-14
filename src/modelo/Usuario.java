package modelo;
/** Pessoa autorizada a realizar empréstimos. */
public class Usuario {
    private final int id;
    private String nome;
    private String email;

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

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        validarNome(nome);
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        validarEmail(email);
        this.email = email;
    }

    private static void validarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome não pode ser vazio.");
        }
    }

    private static void validarEmail(String email) {
        if (email == null || !email.contains("@gmail.com")) {
            throw new IllegalArgumentException("O e-mail deve conter \"@gmail.com\".");
        }
    }

    @Override
    public String toString() {
        return "ID: " + id + " | nome: " + nome + " | e-mail: " + email;
    }
}
