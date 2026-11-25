package interfacegrafica;

import java.awt.*;

import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;


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
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;

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
    private static final Logger LOGGER = Logger.getLogger(MainGUI.class.getName());
    private static final Color COR_DESTAQUE = new Color(55, 145, 115);
    private boolean modoEscuro = false;
    private JTabbedPane abas;
        /** Botões que disparam operações; ficam desabilitados enquanto algo roda em segundo plano. */
    private final List<JButton> botoesAcao = new java.util.ArrayList<>();
    /** Quantidade de operações em segundo plano ainda em andamento. */
    private int operacoesEmAndamento = 0;
    private JButton botaoCadastrarItem;
    private JButton botaoCadastrarUsuario;
    private JButton botaoEmprestar;
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

            botaoTema.setMnemonic(KeyEvent.VK_T);
        botaoTema.addActionListener(e -> alternarTema());
        configurarAtalhos();

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
        atualizarBotaoPadrao();
        atualizarListas();
    }
    private void alternarTema() {
        modoEscuro = !modoEscuro;
        aplicarTema();
    }

    /** Registra atalhos globais: Ctrl+T alterna o tema e a troca de aba atualiza o botão do Enter. */
    private void configurarAtalhos() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK), "alternarTema");
        getRootPane().getActionMap().put("alternarTema", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evento) {
                alternarTema();
            }
        });
        abas.addChangeListener(evento -> atualizarBotaoPadrao());
    }

    /** Faz a tecla Enter acionar o botão principal da aba que está aberta. */
    private void atualizarBotaoPadrao() {
        JButton padrao = switch (abas.getSelectedIndex()) {
            case 0 -> botaoCadastrarItem;
            case 1 -> botaoCadastrarUsuario;
            default -> botaoEmprestar;
        };
        getRootPane().setDefaultButton(padrao);
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

                boolean comFoco = c.hasFocus();
                g2.setColor(comFoco ? COR_DESTAQUE : modoEscuro
                        ? new Color(90, 96, 102)
                        : new Color(195, 200, 198));
                g2.setStroke(new BasicStroke(comFoco ? 2.5f : 1f));

                g2.drawRoundRect(
                        x + 1, y + 1, width - 3, height - 3,
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
        UIManager.put("Button.disabledText", new Color(235, 240, 238));
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

                botaoCadastrarItem = criarBotao("Cadastrar", KeyEvent.VK_C, event -> {
            if ("Livro".equals(seletorTipo.getSelectedItem())) {
                cadastrarLivro();
            } else {
                cadastrarRevista();
            }
        });

        JPanel acoes = new JPanel(new BorderLayout());
        acoes.add(botaoCadastrarItem, BorderLayout.LINE_END);

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
        botaoCadastrarUsuario = criarBotao("Cadastrar usuário", KeyEvent.VK_U, event -> cadastrarUsuario());
        formulario.add(botaoCadastrarUsuario);
        return montarPainel(formulario, listaUsuarios);
    }

    private JPanel criarPainelEmprestimos() {
        JPanel formulario = new JPanel(new GridLayout(0, 2, 8, 8));
        adicionarCampo(formulario, "Código do item:", campoCodigoEmprestimo);
        adicionarCampo(formulario, "ID do usuário:", campoIdEmprestimo);
        botaoEmprestar = criarBotao("Fazer empréstimo", KeyEvent.VK_E, event -> fazerEmprestimo());
        formulario.add(botaoEmprestar);
        formulario.add(criarBotao("Fazer devolução", KeyEvent.VK_D, event -> fazerDevolucao()));
        return montarPainel(formulario, listaEmprestimos);
    }

        private JButton criarBotao(String texto, int mnemonico, java.awt.event.ActionListener acao) {
        JButton botao = new BotaoArredondado(texto);
        botao.setMnemonic(mnemonico);
        botao.addActionListener(acao);
        botao.setPreferredSize(new Dimension(140, 36));
        botoesAcao.add(botao);
        return botao;
    }

    private void adicionarCampo(JPanel formulario, String rotulo, JTextField campo) {
        JLabel legenda = new JLabel(rotulo);
        legenda.setLabelFor(campo);
        formulario.add(legenda);
        formulario.add(campo);
        campo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evento) {
                campo.repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evento) {
                campo.repaint();
            }
        });
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
        executar(() -> {
            Livro livro = new Livro(campoCodigo.getText().trim(), campoTitulo.getText().trim(),
                    campoAutor.getText().trim(), campoIsbn.getText().trim());
            return () -> {
                biblioteca.adicionarItem(livro);
                return "Livro salvo.";
            };
        }, this::limparCamposItem);
    }

    private void cadastrarRevista() {
        executar(() -> {
            Revista revista = new Revista(campoCodigo.getText().trim(), campoTitulo.getText().trim(),
                    lerInteiro(campoEdicao.getText(), "Edição"));
            return () -> {
                biblioteca.adicionarItem(revista);
                return "Revista salva.";
            };
        }, this::limparCamposItem);
    }

    private void cadastrarUsuario() {
        executar(() -> {
            Usuario usuario = new Usuario(lerInteiro(campoIdUsuario.getText(), "ID"),
                    campoNome.getText().trim(), campoEmail.getText().trim());
            return () -> {
                biblioteca.adicionarUsuario(usuario);
                return "Usuário salvo.";
            };
        }, this::limparCamposUsuario);
    }

    private void fazerEmprestimo() {
        executar(() -> {
            String codigo = campoCodigoEmprestimo.getText().trim();
            int idUsuario = lerInteiro(campoIdEmprestimo.getText(), "ID do usuário");
            return () -> {
                Emprestimo emprestimo = biblioteca.emprestar(codigo, idUsuario);
                return "Empréstimo feito em " + emprestimo.getDataEmprestimoFormatada()
                        + ".\nDevolver até " + emprestimo.getDataLimiteFormatada() + ".";
            };
        }, this::limparCamposEmprestimo);
    }

    private void fazerDevolucao() {
        executar(() -> {
            String codigo = campoCodigoEmprestimo.getText().trim();
            return () -> {
                biblioteca.devolver(codigo);
                return "Devolução feita.";
            };
        }, this::limparCamposEmprestimo);
    }

    /**
     * Executa uma operação de negócio fora da thread de eventos, para a janela não travar.
     * A leitura e a validação dos campos acontecem antes, na thread de eventos, dentro de
     * {@code preparar}; só o trabalho devolvido por ele roda em segundo plano.
     *
     * @param preparar lê os campos e devolve a operação, que produz a mensagem de sucesso
     * @param limpeza  executado na thread de eventos quando a operação termina com sucesso
     */
    private void executar(Supplier<Callable<String>> preparar, Runnable limpeza) {
        Callable<String> operacao;
        try {
            operacao = preparar.get();
        } catch (IllegalArgumentException erro) {
            mostrarErro(erro.getMessage());
            return;
        }

        definirOcupado(true);
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return operacao.call();
            }

            @Override
            protected void done() {
                definirOcupado(false);
                try {
                    String sucesso = get();
                    limpeza.run();
                    mostrarMensagem(sucesso);
                    atualizarListas();
                } catch (ExecutionException erro) {
                    tratarFalha(erro.getCause());
                } catch (InterruptedException erro) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    /**
     * Mostra ao usuário uma mensagem compreensível e preserva a causa técnica no log.
     *
     * @param causa exceção lançada pela operação em segundo plano
     */
    private void tratarFalha(Throwable causa) {
        if (causa instanceof IllegalStateException) {
            LOGGER.log(Level.SEVERE, "Falha ao acessar o banco de dados", causa);
            mostrarErro("Não foi possível acessar o banco de dados. Tente novamente.");
        } else if (causa instanceof RuntimeException) {
            mostrarErro(causa.getMessage());
        } else {
            LOGGER.log(Level.SEVERE, "Falha inesperada", causa);
            mostrarErro("Ocorreu um erro inesperado.");
        }
    }

    /**
     * Mostra o cursor de espera e desabilita os botões de ação enquanto há trabalho em andamento,
     * evitando cliques repetidos e operações simultâneas no banco.
     *
     * @param ocupado {@code true} ao iniciar uma operação, {@code false} ao terminar
     */
    private void definirOcupado(boolean ocupado) {
        operacoesEmAndamento += ocupado ? 1 : -1;
        boolean livre = operacoesEmAndamento == 0;
        Component foco = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        setCursor(Cursor.getPredefinedCursor(livre ? Cursor.DEFAULT_CURSOR : Cursor.WAIT_CURSOR));
        botoesAcao.forEach(botao -> botao.setEnabled(livre));
        if (livre && foco != null) {
            foco.requestFocusInWindow();
        }
    }

    /** Dados lidos do banco para preencher as três abas de uma só vez. */
    private record DadosTela(List<ItemBiblioteca> itens, List<Usuario> usuarios,
                             List<Emprestimo> emprestimos) {
    }

    private void atualizarListas() {
        definirOcupado(true);
        new SwingWorker<DadosTela, Void>() {
            @Override
            protected DadosTela doInBackground() {
                return new DadosTela(biblioteca.listarItens(), biblioteca.listarUsuarios(),
                        biblioteca.listarEmprestimos());
            }

            @Override
            protected void done() {
                definirOcupado(false);
                try {
                    exibir(get());
                } catch (ExecutionException erro) {
                    tratarFalha(erro.getCause());
                } catch (InterruptedException erro) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void exibir(DadosTela dados) {
        modeloItens.setRowCount(0);
        for (ItemBiblioteca item : dados.itens()) {
            modeloItens.addRow(new Object[]{
                    item.getCodigo(),
                    item.getTitulo(),
                    item.getTipo(),
                    item.isDisponivel() ? "Disponível" : "Emprestado"
            });
        }
        listaUsuarios.setText(formatarLista(dados.usuarios(), "Nenhum usuário cadastrado."));
        listaEmprestimos.setText(formatarLista(dados.emprestimos(), "Nenhum empréstimo realizado."));
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
