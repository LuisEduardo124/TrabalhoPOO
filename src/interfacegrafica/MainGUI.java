package interfacegrafica;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import modelo.Emprestimo;
import modelo.ItemBiblioteca;
import modelo.ItemIndisponivelException;
import modelo.Livro;
import modelo.Revista;
import modelo.Usuario;
import servico.Biblioteca;

public class MainGUI extends JFrame {
    private final Biblioteca biblioteca = new Biblioteca();

    private JTextField campoCodigo;
    private JTextField campoTitulo;
    private JTextField campoAutor;
    private JTextField campoIsbn;
    private JTextField campoEdicao;

    private JTextField campoIdUsuario;
    private JTextField campoNome;
    private JTextField campoEmail;

    private JTextField campoCodigoEmprestimo;
    private JTextField campoIdEmprestimo;

    private JTextArea listaItens;
    private JTextArea listaUsuarios;
    private JTextArea listaEmprestimos;

    public MainGUI() {
        setTitle("Biblioteca");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Itens", criarPainelItens());
        abas.addTab("Usuários", criarPainelUsuarios());
        abas.addTab("Empréstimos", criarPainelEmprestimos());
        add(abas);

        atualizarListas();
    }

    public static void main(String[] args) {
        MainGUI janela = new MainGUI();
        janela.setVisible(true);
    }

    private JPanel criarPainelItens() {
        campoCodigo = new JTextField();
        campoTitulo = new JTextField();
        campoAutor = new JTextField();
        campoIsbn = new JTextField();
        campoEdicao = new JTextField();

        JPanel formulario = new JPanel(new GridLayout(0, 2, 5, 5));
        formulario.add(new JLabel("Código:"));
        formulario.add(campoCodigo);
        formulario.add(new JLabel("Título:"));
        formulario.add(campoTitulo);
        formulario.add(new JLabel("Autor (só livro):"));
        formulario.add(campoAutor);
        formulario.add(new JLabel("ISBN (só livro):"));
        formulario.add(campoIsbn);
        formulario.add(new JLabel("Edição (só revista):"));
        formulario.add(campoEdicao);

        JButton botaoLivro = new JButton("Cadastrar livro");
        botaoLivro.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cadastrarLivro();
            }
        });

        JButton botaoRevista = new JButton("Cadastrar revista");
        botaoRevista.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cadastrarRevista();
            }
        });

        formulario.add(botaoLivro);
        formulario.add(botaoRevista);

        listaItens = new JTextArea();
        return montarPainel(formulario, listaItens);
    }

    private JPanel criarPainelUsuarios() {
        campoIdUsuario = new JTextField();
        campoNome = new JTextField();
        campoEmail = new JTextField();

        JPanel formulario = new JPanel(new GridLayout(0, 2, 5, 5));
        formulario.add(new JLabel("ID:"));
        formulario.add(campoIdUsuario);
        formulario.add(new JLabel("Nome:"));
        formulario.add(campoNome);
        formulario.add(new JLabel("E-mail:"));
        formulario.add(campoEmail);

        JButton botaoUsuario = new JButton("Cadastrar usuário");
        botaoUsuario.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cadastrarUsuario();
            }
        });

        formulario.add(new JLabel());
        formulario.add(botaoUsuario);

        listaUsuarios = new JTextArea();
        return montarPainel(formulario, listaUsuarios);
    }

    private JPanel criarPainelEmprestimos() {
        campoCodigoEmprestimo = new JTextField();
        campoIdEmprestimo = new JTextField();

        JPanel formulario = new JPanel(new GridLayout(0, 2, 5, 5));
        formulario.add(new JLabel("Código do item:"));
        formulario.add(campoCodigoEmprestimo);
        formulario.add(new JLabel("ID do usuário (só empréstimo):"));
        formulario.add(campoIdEmprestimo);

        JButton botaoEmprestar = new JButton("Fazer empréstimo");
        botaoEmprestar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fazerEmprestimo();
            }
        });

        JButton botaoDevolver = new JButton("Fazer devolução");
        botaoDevolver.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fazerDevolucao();
            }
        });

        formulario.add(botaoEmprestar);
        formulario.add(botaoDevolver);

        listaEmprestimos = new JTextArea();
        return montarPainel(formulario, listaEmprestimos);
    }

    private JPanel montarPainel(JPanel formulario, JTextArea lista) {
        lista.setEditable(false);

        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.add(formulario, BorderLayout.NORTH);
        painel.add(new JScrollPane(lista), BorderLayout.CENTER);
        return painel;
    }

    private void cadastrarLivro() {
        try {
            Livro livro = new Livro(campoCodigo.getText(), campoTitulo.getText(),
                    campoAutor.getText(), campoIsbn.getText());
            biblioteca.adicionarItem(livro);
            mostrarMensagem("Livro salvo.");
            limparCamposItem();
            atualizarListas();
        } catch (IllegalArgumentException erro) {
            mostrarErro(erro.getMessage());
        }
    }

    private void cadastrarRevista() {
        try {
            int edicao = lerInteiro(campoEdicao.getText(), "Edição");
            Revista revista = new Revista(campoCodigo.getText(), campoTitulo.getText(), edicao);
            biblioteca.adicionarItem(revista);
            mostrarMensagem("Revista salva.");
            limparCamposItem();
            atualizarListas();
        } catch (IllegalArgumentException erro) {
            mostrarErro(erro.getMessage());
        }
    }

    private void cadastrarUsuario() {
        try {
            int id = lerInteiro(campoIdUsuario.getText(), "ID");
            Usuario usuario = new Usuario(id, campoNome.getText(), campoEmail.getText());
            biblioteca.adicionarUsuario(usuario);
            mostrarMensagem("Usuário salvo.");
            campoIdUsuario.setText("");
            campoNome.setText("");
            campoEmail.setText("");
            atualizarListas();
        } catch (IllegalArgumentException erro) {
            mostrarErro(erro.getMessage());
        }
    }

    private void fazerEmprestimo() {
        try {
            int idUsuario = lerInteiro(campoIdEmprestimo.getText(), "ID do usuário");
            Emprestimo emprestimo = biblioteca.emprestar(campoCodigoEmprestimo.getText(), idUsuario);
            mostrarMensagem("Empréstimo feito em " + emprestimo.getDataEmprestimoFormatada()
                    + ".\nDevolver até " + emprestimo.getDataLimiteFormatada() + ".");
            campoCodigoEmprestimo.setText("");
            campoIdEmprestimo.setText("");
            atualizarListas();
        } catch (ItemIndisponivelException erro) {
            JOptionPane.showMessageDialog(this, erro.getMessage(), "Item indisponível",
                    JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException erro) {
            mostrarErro(erro.getMessage());
        }
    }

    private void fazerDevolucao() {
        try {
            biblioteca.devolver(campoCodigoEmprestimo.getText());
            mostrarMensagem("Devolução feita.");
            campoCodigoEmprestimo.setText("");
            campoIdEmprestimo.setText("");
            atualizarListas();
        } catch (IllegalArgumentException erro) {
            mostrarErro(erro.getMessage());
        }
    }

    private void limparCamposItem() {
        campoCodigo.setText("");
        campoTitulo.setText("");
        campoAutor.setText("");
        campoIsbn.setText("");
        campoEdicao.setText("");
    }

    private void atualizarListas() {
        String textoItens = "";
        for (ItemBiblioteca item : biblioteca.getItens()) {
            textoItens += item + "\n";
        }
        if (textoItens.isEmpty()) {
            textoItens = "Nenhum item cadastrado.";
        }
        listaItens.setText(textoItens);

        String textoUsuarios = "";
        for (Usuario usuario : biblioteca.getUsuarios()) {
            textoUsuarios += usuario + "\n";
        }
        if (textoUsuarios.isEmpty()) {
            textoUsuarios = "Nenhum usuário cadastrado.";
        }
        listaUsuarios.setText(textoUsuarios);

        String textoEmprestimos = "";
        for (Emprestimo emprestimo : biblioteca.getEmprestimos()) {
            textoEmprestimos += emprestimo + "\n";
        }
        if (textoEmprestimos.isEmpty()) {
            textoEmprestimos = "Nenhum empréstimo realizado.";
        }
        listaEmprestimos.setText(textoEmprestimos);
    }

    private int lerInteiro(String texto, String nomeCampo) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException erro) {
            throw new IllegalArgumentException("O campo " + nomeCampo + " deve ser um número inteiro.");
        }
    }

    private void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarMensagem(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem);
    }
}
