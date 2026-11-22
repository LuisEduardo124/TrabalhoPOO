package interfacegrafica;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.*;


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
import javax.swing.UIManager;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.AbstractBorder;

import modelo.Emprestimo;
import modelo.ItemBiblioteca;
import modelo.ItemIndisponivelException;
import modelo.Livro;
import modelo.Revista;
import modelo.Usuario;
import servico.Biblioteca;
import servico.InicializadorBancoSQLite;
import servico.RepositorioBibliotecaSQLite;

/**
 * Janela principal da biblioteca, feita em Swing.
 * Organiza os cadastros e empréstimos em abas e exibe os resultados das operações.
 */
public class MainGUI extends JFrame {
    /** Serviço de regras de negócio usado pela janela. */
    private final Biblioteca biblioteca;
    private boolean modoEscuro = false;
    private JTabbedPane abas;
    private final JButton botaoTema =
            new BotaoArredondado("☾ Modo escuro");
    /** Campos do formulário de itens e usuários. */
    private final JTextField campoCodigo = new JTextField();
    /** Campo de título. */
    private final JTextField campoTitulo = new JTextField();
    /** Campo de autor. */
    private final JTextField campoAutor = new JTextField();
    /** Campo de ISBN. */
    private final JTextField campoIsbn = new JTextField();
    /** Campo de edição. */
    private final JTextField campoEdicao = new JTextField();
    /** Campo de identificador do usuário. */
    private final JTextField campoIdUsuario = new JTextField();
    /** Campo de nome do usuário. */
    private final JTextField campoNome = new JTextField();
    /** Campo de e-mail do usuário. */
    private final JTextField campoEmail = new JTextField();
    /** Campos usados para consultar empréstimos e registrar devoluções. */
    private final JTextField campoCodigoEmprestimo = new JTextField();
    /** Campo de identificador do empréstimo. */
    private final JTextField campoIdEmprestimo = new JTextField();
    /** Áreas que exibem usuários e empréstimos. */
    private final JTextArea listaEmprestimos = criarAreaDeLista();
    /** Área de listagem de usuários. */
    private final JTextArea listaUsuarios = criarAreaDeLista();
    /** Modelo não editável da tabela de itens. */
    private final DefaultTableModel modeloItens = new DefaultTableModel(
            new String[]{"Código", "Título", "Tipo", "Disponibilidade"}, 0) {
        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return false;
        }
    };

    /** Tabela que exibe o acervo cadastrado. */
    private final JTable tabelaItens = new JTable(modeloItens);

    /**
     * Monta as abas de itens, usuários e empréstimos e carrega as listas iniciais.
     *
     * @param biblioteca serviço usado para consultar dados e executar operações
     */
    public MainGUI(Biblioteca biblioteca) {
        this.biblioteca = biblioteca;
        configurarJanela();
        abas = new JTabbedPane();
        abas.addTab("Itens", criarPainelItens());
        abas.addTab("Usuários", criarPainelUsuarios());
        abas.addTab("Empréstimos", criarPainelEmprestimos());
        JPanel cabecalho = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        cabecalho.add(botaoTema);

        botaoTema.addActionListener(e -> {
            modoEscuro = !modoEscuro;
            aplicarTema();
        });

        add(cabecalho, BorderLayout.NORTH);
        abas.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        tabelaItens.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabelaItens.setRowHeight(30);
        tabelaItens.setShowVerticalLines(false);
        tabelaItens.setShowHorizontalLines(false);
        tabelaItens.setFillsViewportHeight(true);

        tabelaItens.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );
        tabelaItens.getTableHeader().setPreferredSize(
                new Dimension(0, 36)
        );
        add(abas, BorderLayout.CENTER);

        aplicarTema();
        atualizarListas();
    }

    private Border criarBordaArredondada() {
        return new AbstractBorder() {
            @Override
            public void paintBorder(
                    Component c, Graphics g,
                    int x, int y, int width, int height) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2.setColor(modoEscuro
                        ? new Color(90, 96, 102)
                        : new Color(195, 200, 198));

                g2.drawRoundRect(
                        x, y, width - 1, height - 1,
                        16, 16
                );

                g2.dispose();
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(7, 12, 7, 12);
            }

            @Override
            public Insets getBorderInsets(Component c, Insets insets) {
                insets.set(7, 12, 7, 12);
                return insets;
            }
        };
    }

    private JButton criarBotaoArredondado(JButton botao) {
        botao.setContentAreaFilled(false);
        botao.setFocusPainted(false);
        botao.setBorderPainted(false);
        botao.setOpaque(false);

        botao.setBorder(
                BorderFactory.createEmptyBorder(9, 18, 9, 18)
        );

        return botao;
    }

    private void aplicarTema() {
        Color fundo = modoEscuro
                ? new Color(35, 38, 42)
                : new Color(248, 248, 246);

        Color campo = modoEscuro
                ? new Color(50, 54, 59)
                : Color.WHITE;

        Color texto = modoEscuro
                ? new Color(235, 235, 235)
                : new Color(35, 35, 35);

        aplicarCores(getContentPane(), fundo, campo, texto);

        botaoTema.setText(modoEscuro ? "☀ Modo claro" : "☾ Modo escuro");
        Color fundoAbas = modoEscuro
                ? new Color(43, 47, 51)
                : new Color(244, 246, 245);

        Color textoAbas = modoEscuro
                ? Color.WHITE
                : new Color(35, 35, 35);

        abas.setBackground(fundoAbas);
        abas.setForeground(textoAbas);

        for (int i = 0; i < abas.getTabCount(); i++) {
            abas.setBackgroundAt(i, fundoAbas);
            abas.setForegroundAt(i, textoAbas);
        }
        repaint();
    }

    private void aplicarCores(
            Component componente,
            Color fundo,
            Color campo,
            Color texto) {

        if (componente instanceof JTextField
                || componente instanceof JTextArea
                || componente instanceof JComboBox) {

            if (componente instanceof JTextField campoTexto) {
                campoTexto.setCaretColor(texto);
                campoTexto.setBorder(criarBordaArredondada());
            }

            componente.setBackground(campo);
            componente.setForeground(texto);
        } else if (componente instanceof JTable tabela) {

            tabela.setBackground(campo);
            tabela.setForeground(texto);

            tabela.setGridColor(
                    modoEscuro
                            ? new Color(65, 70, 75)
                            : new Color(225, 228, 230)
            );

            tabela.setSelectionBackground(
                    modoEscuro
                            ? new Color(48, 105, 91)
                            : new Color(214, 235, 227)
            );

            tabela.setSelectionForeground(
                    modoEscuro ? Color.WHITE : new Color(35, 55, 47)
            );

            tabela.getTableHeader().setBackground(
                    modoEscuro
                            ? new Color(43, 47, 51)
                            : new Color(244, 246, 245)
            );

            tabela.getTableHeader().setForeground(texto);

            tabela.getTableHeader().setBorder(
                    BorderFactory.createMatteBorder(
                            0, 0, 1, 0,
                            modoEscuro
                                    ? new Color(75, 80, 85)
                                    : new Color(215, 220, 218)
                    )
            );

        } else if (componente instanceof JButton botao) {

            botao.setBackground(new Color(55, 145, 115));
            botao.setForeground(Color.WHITE);

            if (!(botao instanceof BotaoArredondado)) {
                botao.setOpaque(true);
            }

        } else {
            componente.setBackground(fundo);
            componente.setForeground(texto);
        }

        if (componente instanceof Container container) {
            for (Component filho : container.getComponents()) {
                aplicarCores(filho, fundo, campo, texto);
            }
        }
    }

    private void configurarJanela() {
        setTitle("Biblioteca - Sistema de Gestão de Acervo");
        setSize(960, 720);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Button.font", new Font("Segoe UI", Font.PLAIN, 14));
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
        JButton botao = new BotaoArredondado(texto);
        botao.addActionListener(acao);
        botao.setPreferredSize(new Dimension(140, 36));
        return botao;
    }

    private void adicionarCampo(JPanel formulario, String rotulo, JTextField campo) {
        formulario.add(new JLabel(rotulo));
        formulario.add(campo);
    }

    private JPanel montarPainel(JPanel formulario, JComponent lista) {
        JPanel painel = new JPanel(new BorderLayout(12, 12));
        painel.setBorder(BorderFactory.createEmptyBorder(12, 14, 14, 14));

        formulario.setBorder(
                BorderFactory.createEmptyBorder(10, 8, 14, 8)
        );

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
        try {
            InicializadorBancoSQLite.inicializar();
        } catch (IllegalStateException erro) {
            JOptionPane.showMessageDialog(null, erro.getMessage(), "Erro ao iniciar", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            Biblioteca biblioteca = new Biblioteca(new RepositorioBibliotecaSQLite());
            new MainGUI(biblioteca).setVisible(true);
        });
    }
}
