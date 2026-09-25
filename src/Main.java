import java.util.Scanner;

public class Main {
    private static final Scanner entrada = new Scanner(System.in);
    private static final Biblioteca biblioteca = new Biblioteca();

    public static void main(String[] args) {
        int opcao;

        do {
            mostrarMenu();
            opcao = lerInteiro("Escolha uma opção: ");

            try {
                executarOpcao(opcao);
            } catch (IllegalArgumentException | IllegalStateException erro) {
                System.out.println("Erro: " + erro.getMessage());
            }
        } while (opcao != 0);

        entrada.close();
        System.out.println("Programa encerrado.");
    }

    private static void mostrarMenu() {
        System.out.println("\n===== BIBLIOTECA =====");
        System.out.println("1 - Cadastrar livro");
        System.out.println("2 - Cadastrar revista");
        System.out.println("3 - Cadastrar usuário");
        System.out.println("4 - Ver itens");
        System.out.println("5 - Ver usuários");
        System.out.println("6 - Fazer empréstimo");
        System.out.println("7 - Fazer devolução");
        System.out.println("8 - Ver empréstimos");
        System.out.println("0 - Sair");
    }

    private static void executarOpcao(int opcao) {
        switch (opcao) {
            case 1:
                cadastrarLivro();
                break;
            case 2:
                cadastrarRevista();
                break;
            case 3:
                cadastrarUsuario();
                break;
            case 4:
                biblioteca.mostrarItens();
                break;
            case 5:
                biblioteca.mostrarUsuarios();
                break;
            case 6:
                fazerEmprestimo();
                break;
            case 7:
                fazerDevolucao();
                break;
            case 8:
                biblioteca.mostrarEmprestimos();
                break;
            case 0:
                break;
            default:
                System.out.println("Opção inválida.");
        }
    }

    private static void cadastrarLivro() {
        String codigo = lerTextoApenasNumeros("Código: ");
        String titulo = lerTextoApenasLetras("Título: ");
        String autor = lerTextoApenasLetras("Autor: ");
        
        String isbn = lerTextoApenasNumeros("ISBN: ");
        while (isbn.length() != 13) {
            System.out.println("ISBN deve ter 13 números!");
            isbn = lerTextoApenasNumeros("ISBN: ");
        }

        biblioteca.adicionarItem(new Livro(codigo, titulo, autor, isbn));
        System.out.println("Livro salvo.");
    }

    private static void cadastrarRevista() {
        String codigo = lerTextoApenasNumeros("Código: ");
        String titulo = lerTextoApenasLetras("Título: ");
        int edicao = lerInteiro("Número da edição: ");

        biblioteca.adicionarItem(new Revista(codigo, titulo, edicao));
        System.out.println("Revista salva.");
    }

    private static void cadastrarUsuario() {
        int id = lerInteiro("ID: ");
        String nome = lerTexto("Nome: ");
        String email = lerTexto("E-mail: ");

        biblioteca.adicionarUsuario(new Usuario(id, nome, email));
        System.out.println("Usuário salvo.");
    }

    private static void fazerEmprestimo() {
        String codigo = lerTexto("Código do item: ");
        int idUsuario = lerInteiro("ID do usuário: ");

        biblioteca.emprestar(codigo, idUsuario);
        System.out.println("Empréstimo feito.");
    }

    private static void fazerDevolucao() {
        String codigo = lerTexto("Código do item: ");
        biblioteca.devolver(codigo);
        System.out.println("Devolução feita.");
    }

    private static String lerTexto(String mensagem) {
        System.out.print(mensagem);
        return entrada.nextLine();
    }

    private static String lerTextoApenasLetras(String mensagem) {
        while (true) {
            String texto = lerTexto(mensagem);
            if (texto.matches("[a-zA-ZÀ-ÿ\\s]+")) {
                return texto;
            }
            System.out.println("Digite apenas letras!");
        }
    }

    private static String lerTextoApenasNumeros(String mensagem) {
        while (true) {
            String texto = lerTexto(mensagem);
            if (texto.matches("\\d+")) {
                return texto;
            }
            System.out.println("Digite apenas números!");
        }
    }

    private static int lerInteiro(String mensagem) {
        while (true) {
            try {
                return Integer.parseInt(lerTexto(mensagem));
            } catch (NumberFormatException erro) {
                System.out.println("Digite um número inteiro válido.");
            }
        }
    }
}
