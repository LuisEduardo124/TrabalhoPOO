package interfacegrafica;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;


import modelo.Emprestimo;
import modelo.ItemBiblioteca;
import modelo.ItemIndisponivelException;
import modelo.Livro;
import modelo.Revista;
import modelo.Usuario;
import servico.Biblioteca;
import servico.RepositorioBibliotecaSQLite;

/**
 * Janela principal da biblioteca, feita em Swing.
 * Organiza os cadastros e empréstimos em abas e exibe os resultados das operações.
 */
public class MainGUI extends JFrame {
    private final Biblioteca biblioteca;
    private final JTextField campoCodigo = new JTextField();
    private final JTextField campoTitulo = new JTextField();
    private final JTextField campoAutor = new JTextField();
    private final JTextField campoIsbn = new JTextField();
    private final JTextField campoEdicao = new JTextField();
    private final JTextField campoIdUsuario = new JTextField();
    private final JTextField campoNome = new JTextField();
    private final JTextField campoEmail = new JTextField();
    private final JTextField campoCodigoEmprestimo = new JTextField();
    private final JTextField campoIdEmprestimo = new JTextField();
    private final JTextArea listaEmprestimos = criarAreaDeLista();
    private final JTextArea listaUsuarios = criarAreaDeLista();
    private final DefaultTableModel modeloItens = new DefaultTableModel(
            new String[]{"Código", "Título", "Tipo", "Disponibilidade"}, 0) {
        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return false;
        }
    };

    private final JTable tabelaItens = new JTable(modeloItens);

    /**
     * Monta as abas de itens, usuários e empréstimos e carrega as listas iniciais.
     *
     * @param biblioteca serviço usado para consultar dados e executar operações
     */
    public MainGUI(Biblioteca biblioteca) {
        this.biblioteca = biblioteca;
        configurarJanela();
        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Itens", criarPainelItens());
        abas.addTab("Usuários", criarPainelUsuarios());
        abas.addTab("Empréstimos", criarPainelEmprestimos());
        add(abas);
        atualizarListas();
    }

    private void configurarJanela() {
        setTitle("Biblioteca");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private JPanel criarPainelItens() {
        JPanel formulario = new JPanel(new BorderLayout(0, 8));

        JComboBox<String> seletorTipo = new JComboBox<>(new String[]{"Livro", "Revista"});

        JPanel camposComuns = new JPanel(new GridLayout(0, 2, 8, 8));
        camposComuns.add(new JLabel("Tipo de item:"));
        camposComuns.add(seletorTipo);
        adicionarCampo(camposComuns, "Código:", campoCodigo);
        adicionarCampo(camposComuns, "Título:", campoTitulo);

        JPanel camposLivro = new JPanel(new GridLayout(0, 2, 8, 8));
        adicionarCampo(camposLivro, "Autor:", campoAutor);
        adicionarCampo(camposLivro, "ISBN:", campoIsbn);

        JPanel camposRevista = new JPanel(new GridLayout(0, 2, 8, 8));
        adicionarCampo(camposRevista, "Edição:", campoEdicao);

        CardLayout layoutTipo = new CardLayout();
        JPanel camposPorTipo = new JPanel(layoutTipo);
        camposPorTipo.add(camposLivro, "Livro");
        camposPorTipo.add(camposRevista, "Revista");

        seletorTipo.addActionListener(event -> {
            String tipo = (String) seletorTipo.getSelectedItem();
            layoutTipo.show(camposPorTipo, tipo);
        });

        JButton botaoCadastrar = criarBotao("Cadastrar", event -> {
            if ("Livro".equals(seletorTipo.getSelectedItem())) {
                cadastrarLivro();
            } else {
                cadastrarRevista();
            }
        });

        JPanel acoes = new JPanel(new BorderLayout());
        acoes.add(botaoCadastrar, BorderLayout.LINE_END);

        formulario.add(camposComuns, BorderLayout.NORTH);
        formulario.add(camposPorTipo, BorderLayout.CENTER);
        formulario.add(acoes, BorderLayout.SOUTH);

        return montarPainel(formulario, tabelaItens);
    }

    private JPanel criarPainelUsuarios() {
        JPanel formulario = new JPanel(new GridLayout(0, 2, 8, 8));
        adicionarCampo(formulario, "ID:", campoIdUsuario);
        adicionarCampo(formulario, "Nome:", campoNome);
        adicionarCampo(formulario, "E-mail:", campoEmail);
        formulario.add(new JLabel());
        formulario.add(criarBotao("Cadastrar usuário", event -> cadastrarUsuario()));
        return montarPainel(formulario, listaUsuarios);
    }

    private JPanel criarPainelEmprestimos() {
        JPanel formulario = new JPanel(new GridLayout(0, 2, 8, 8));
        adicionarCampo(formulario, "Código do item:", campoCodigoEmprestimo);
        adicionarCampo(formulario, "ID do usuário:", campoIdEmprestimo);
        formulario.add(criarBotao("Fazer empréstimo", event -> fazerEmprestimo()));
        formulario.add(criarBotao("Fazer devolução", event -> fazerDevolucao()));
        return montarPainel(formulario, listaEmprestimos);
    }

    private JButton criarBotao(String texto, java.awt.event.ActionListener acao) {
        JButton botao = new JButton(texto);
        botao.addActionListener(acao);
        return botao;
    }

    private void adicionarCampo(JPanel formulario, String rotulo, JTextField campo) {
        formulario.add(new JLabel(rotulo));
        formulario.add(campo);
    }

    private JPanel montarPainel(JPanel formulario, JComponent lista) {
        JPanel painel = new JPanel(new BorderLayout(12, 12));
        painel.add(formulario, BorderLayout.NORTH);
        painel.add(new JScrollPane(lista), BorderLayout.CENTER);
        return painel;
    }

    private void cadastrarLivro() {
        executarOperacao(() -> biblioteca.adicionarItem(new Livro(campoCodigo.getText().trim(),
                campoTitulo.getText().trim(), campoAutor.getText().trim(), campoIsbn.getText().trim())),
                "Livro salvo.", this::limparCamposItem);
    }

    private void cadastrarRevista() {
        executarOperacao(() -> biblioteca.adicionarItem(new Revista(campoCodigo.getText().trim(),
                campoTitulo.getText().trim(), lerInteiro(campoEdicao.getText(), "Edição"))),
                "Revista salva.", this::limparCamposItem);
    }

    private void cadastrarUsuario() {
        executarOperacao(() -> biblioteca.adicionarUsuario(new Usuario(lerInteiro(campoIdUsuario.getText(), "ID"),
                campoNome.getText().trim(), campoEmail.getText().trim())),
                "Usuário salvo.", this::limparCamposUsuario);
    }

    private void fazerEmprestimo() {
        try {
            Emprestimo emprestimo = biblioteca.emprestar(campoCodigoEmprestimo.getText().trim(),
                    lerInteiro(campoIdEmprestimo.getText(), "ID do usuário"));
            mostrarMensagem("Empréstimo feito em " + emprestimo.getDataEmprestimoFormatada()
                    + ".\nDevolver até " + emprestimo.getDataLimiteFormatada() + ".");
            limparCamposEmprestimo();
            atualizarListas();
        } catch (ItemIndisponivelException erro) {
            mostrarErro(erro.getMessage());
        } catch (IllegalArgumentException erro) {
            mostrarErro(erro.getMessage());
        }
    }

    private void fazerDevolucao() {
        executarOperacao(() -> biblioteca.devolver(campoCodigoEmprestimo.getText().trim()),
                "Devolução feita.", this::limparCamposEmprestimo);
    }

    private void executarOperacao(Runnable operacao, String sucesso, Runnable limpeza) {
        try {
            operacao.run();
            mostrarMensagem(sucesso);
            limpeza.run();
            atualizarListas();
        } catch (IllegalArgumentException erro) {
            mostrarErro(erro.getMessage());
        }
    }

    private void atualizarListas() {
        modeloItens.setRowCount(0);

        for (ItemBiblioteca item : biblioteca.listarItens()) {
            modeloItens.addRow(new Object[]{
                    item.getCodigo(),
                    item.getTitulo(),
                    item.getTipo(),
                    item.isDisponivel() ? "Disponível" : "Emprestado"
            });
        }
        listaUsuarios.setText(formatarLista(biblioteca.listarUsuarios(), "Nenhum usuário cadastrado."));
        listaEmprestimos.setText(formatarLista(biblioteca.listarEmprestimos(), "Nenhum empréstimo realizado."));
    }

    private String formatarLista(Iterable<?> objetos, String mensagemVazia) {
        StringBuilder texto = new StringBuilder();
        for (Object objeto : objetos) {
            texto.append(objeto).append(System.lineSeparator());
        }
        return texto.length() == 0 ? mensagemVazia : texto.toString();
    }

    private int lerInteiro(String texto, String nomeCampo) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException erro) {
            throw new IllegalArgumentException("O campo " + nomeCampo + " deve ser um número inteiro.");
        }
    }

    private void limparCamposItem() {
        campoCodigo.setText("");
        campoTitulo.setText("");
        campoAutor.setText("");
        campoIsbn.setText("");
        campoEdicao.setText("");
    }

    private void limparCamposUsuario() {
        campoIdUsuario.setText("");
        campoNome.setText("");
        campoEmail.setText("");
    }

    private void limparCamposEmprestimo() {
        campoCodigoEmprestimo.setText("");
        campoIdEmprestimo.setText("");
    }

    private void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarMensagem(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Biblioteca", JOptionPane.INFORMATION_MESSAGE);
    }

    private static JTextArea criarAreaDeLista() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    /**
     * Inicia a aplicação com um repositório em memória e abre a janela
     * principal na thread de eventos do Swing.
     *
     * @param args argumentos da linha de comando, não utilizados
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Biblioteca biblioteca = new Biblioteca(new RepositorioBibliotecaSQLite());
            new MainGUI(biblioteca).setVisible(true);
        });
    }
}
