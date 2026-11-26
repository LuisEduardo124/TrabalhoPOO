package interfacegrafica;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import modelo.Emprestimo;
import modelo.ItemBiblioteca;
import modelo.Livro;
import modelo.Revista;
import modelo.Usuario;
import servico.Biblioteca;
import servico.InicializadorBancoSQLite;
import servico.RepositorioBibliotecaSQLite;

/**
 * Janela principal da biblioteca, feita em Swing.
 * Organiza itens, usuários e empréstimos em abas. Cada aba tem um formulário e uma tabela:
 * ao selecionar uma linha da tabela, o formulário passa a editar aquele registro.
 */
public class MainGUI extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(MainGUI.class.getName());
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String FONTE = "Segoe UI";

    /** Serviço de regras de negócio usado pela janela. */
    private final Biblioteca biblioteca;

    private boolean modoEscuro = false;
    /** Cores do tema em uso; trocada por {@link #aplicarTema()}. */
    private Paleta paleta = Paleta.CLARO;
    private JTabbedPane abas;
    /** Quantidade de operações em segundo plano ainda em andamento. */
    private int operacoesEmAndamento = 0;

    /** Código do item que está sendo editado; {@code null} quando o formulário cadastra um item novo. */
    private String codigoItemEmEdicao;
    /** ID do usuário que está sendo editado; {@code null} quando o formulário cadastra um usuário novo. */
    private Integer idUsuarioEmEdicao;
    private final Map<String, ItemBiblioteca> itensPorCodigo = new HashMap<>();
    private final Map<Integer, Usuario> usuariosPorId = new HashMap<>();
    private List<Emprestimo> emprestimosExibidos = new ArrayList<>();

    private final JButton botaoTema = new BotaoArredondado("☾ Modo escuro");
    private JButton botaoCadastrarItem;
    private JButton botaoSalvarItem;
    private JButton botaoExcluirItem;
    private JButton botaoLimparItem;
    private JButton botaoCadastrarUsuario;
    private JButton botaoSalvarUsuario;
    private JButton botaoExcluirUsuario;
    private JButton botaoLimparUsuario;
    private JButton botaoEmprestar;
    private JButton botaoDevolver;

    private final JComboBox<String> seletorTipo = new JComboBox<>(new String[]{"Livro", "Revista"});
    private final JTextField campoCodigo = new JTextField();
    private final JTextField campoTitulo = new JTextField();
    private final JTextField campoAutor = new JTextField();
    private final JTextField campoIsbn = new JTextField();
    private final JTextField campoEdicao = new JTextField();
    private JLabel rotuloAutor;
    private JLabel rotuloIsbn;
    private JLabel rotuloEdicao;

    private final JTextField campoIdUsuario = new JTextField();
    private final JTextField campoNome = new JTextField();
    private final JTextField campoEmail = new JTextField();

    private final JTextField campoCodigoEmprestimo = new JTextField();
    private final JTextField campoIdEmprestimo = new JTextField();

    private final DefaultTableModel modeloItens =
            criarModelo("Código", "Título", "Tipo", "Disponibilidade");
    private final JTable tabelaItens = new JTable(modeloItens);
    private final DefaultTableModel modeloUsuarios =
            criarModelo("ID", "Nome", "E-mail");
    private final JTable tabelaUsuarios = new JTable(modeloUsuarios);
    private final DefaultTableModel modeloEmprestimos =
            criarModelo("Item", "Usuário", "Empréstimo", "Devolver até", "Devolvido em", "Situação");
    private final JTable tabelaEmprestimos = new JTable(modeloEmprestimos);

    /**
     * Monta as abas de itens, usuários e empréstimos e carrega as listas iniciais.
     *
     * @param biblioteca serviço usado para consultar dados e executar operações
     */
    public MainGUI(Biblioteca biblioteca) {
        this.biblioteca = biblioteca;
        configurarJanela();

        abas = new JTabbedPane();
        abas.setUI(new AbasUI());
        abas.setFont(new Font(FONTE, Font.PLAIN, 14));
        abas.addTab("Itens", criarPainelItens());
        abas.addTab("Usuários", criarPainelUsuarios());
        abas.addTab("Empréstimos", criarPainelEmprestimos());

        JPanel cabecalho = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        cabecalho.add(botaoTema);
        botaoTema.setMnemonic(KeyEvent.VK_T);
        botaoTema.addActionListener(e -> alternarTema());

        add(cabecalho, BorderLayout.NORTH);
        add(abas, BorderLayout.CENTER);

        configurarTabela(tabelaItens, 100, 380, 100, 150);
        configurarTabela(tabelaUsuarios, 70, 300, 400);
        configurarTabela(tabelaEmprestimos, 190, 140, 130, 130, 130, 80);
        configurarSelecoes();
        configurarAtalhos();
        estilizarSeletor();

        aplicarTema();
        seletorTipo.setPreferredSize(new Dimension(10, campoCodigo.getPreferredSize().height));
        atualizarEstadoBotoes();
        atualizarListas();
    }

    // ------------------------------------------------------------------ montagem das telas

    private void configurarJanela() {
        setTitle("Biblioteca - Sistema de Gestão de Acervo");
        setSize(960, 720);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        UIManager.put("Label.font", new Font(FONTE, Font.PLAIN, 14));
        UIManager.put("Button.font", new Font(FONTE, Font.PLAIN, 14));
    }

    private JPanel criarPainelItens() {
        Formulario formulario = new Formulario();
        formulario.adicionarCampo("Tipo de item:", seletorTipo);
        formulario.adicionarCampo("Código:", campoCodigo);
        formulario.adicionarCampo("Título:", campoTitulo);
        rotuloAutor = formulario.adicionarCampo("Autor:", campoAutor);
        rotuloIsbn = formulario.adicionarCampo("ISBN:", campoIsbn);
        rotuloEdicao = formulario.adicionarCampo("Edição:", campoEdicao);
        seletorTipo.addActionListener(e -> mostrarCamposDoTipo());
        mostrarCamposDoTipo();

        botaoCadastrarItem = criarBotao("Cadastrar", KeyEvent.VK_C, e -> cadastrarItem());
        botaoSalvarItem = criarBotao("Salvar alterações", KeyEvent.VK_S, e -> salvarItem());
        botaoExcluirItem = criarBotao("Excluir", KeyEvent.VK_X, e -> excluirItem());
        botaoLimparItem = criarBotao("Limpar", KeyEvent.VK_L, e -> limparFormularioItem());
        formulario.adicionarAcoes(botaoCadastrarItem, botaoSalvarItem, botaoExcluirItem, botaoLimparItem);

        return montarPainel(formulario, tabelaItens);
    }

    private JPanel criarPainelUsuarios() {
        Formulario formulario = new Formulario();
        formulario.adicionarCampo("ID:", campoIdUsuario);
        formulario.adicionarCampo("Nome:", campoNome);
        formulario.adicionarCampo("E-mail:", campoEmail);

        botaoCadastrarUsuario = criarBotao("Cadastrar", KeyEvent.VK_U, e -> cadastrarUsuario());
        botaoSalvarUsuario = criarBotao("Salvar alterações", KeyEvent.VK_S, e -> salvarUsuario());
        botaoExcluirUsuario = criarBotao("Excluir", KeyEvent.VK_X, e -> excluirUsuario());
        botaoLimparUsuario = criarBotao("Limpar", KeyEvent.VK_L, e -> limparFormularioUsuario());
        formulario.adicionarAcoes(botaoCadastrarUsuario, botaoSalvarUsuario, botaoExcluirUsuario, botaoLimparUsuario);

        return montarPainel(formulario, tabelaUsuarios);
    }

    private JPanel criarPainelEmprestimos() {
        Formulario formulario = new Formulario();
        formulario.adicionarCampo("Código do item:", campoCodigoEmprestimo);
        formulario.adicionarCampo("ID do usuário:", campoIdEmprestimo);

        botaoEmprestar = criarBotao("Emprestar", KeyEvent.VK_E, e -> fazerEmprestimo());
        botaoDevolver = criarBotao("Devolver", KeyEvent.VK_D, e -> fazerDevolucao());
        formulario.adicionarAcoes(botaoEmprestar, botaoDevolver);

        return montarPainel(formulario, tabelaEmprestimos);
    }

    private JPanel montarPainel(JPanel formulario, JTable tabela) {
        JPanel painel = new JPanel(new BorderLayout(12, 12));
        painel.setBorder(BorderFactory.createEmptyBorder(12, 14, 14, 14));
        painel.add(formulario, BorderLayout.NORTH);
        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        rolagem.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 12));
        rolagem.getVerticalScrollBar().setUnitIncrement(16);
        painel.add(rolagem, BorderLayout.CENTER);
        return painel;
    }

    private JButton criarBotao(String texto, int mnemonico, java.awt.event.ActionListener acao) {
        JButton botao = new BotaoArredondado(texto);
        botao.setMnemonic(mnemonico);
        botao.addActionListener(acao);
        botao.setPreferredSize(new Dimension(150, 36));
        return botao;
    }

    private static DefaultTableModel criarModelo(String... colunas) {
        return new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
    }

    /**
     * Aplica o visual padrão às tabelas.
     *
     * @param tabela tabela a configurar
     * @param larguras largura preferida de cada coluna, na ordem; a tabela as ajusta ao espaço disponível
     */
    private void configurarTabela(JTable tabela, int... larguras) {
        for (int i = 0; i < larguras.length; i++) {
            tabela.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }
        tabela.setFont(new Font(FONTE, Font.PLAIN, 13));
        tabela.setRowHeight(30);
        tabela.setShowVerticalLines(false);
        tabela.setShowHorizontalLines(false);
        tabela.setIntercellSpacing(new Dimension(0, 0));
        tabela.setFillsViewportHeight(true);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.getTableHeader().setPreferredSize(new Dimension(0, 36));
        tabela.setDefaultRenderer(Object.class, new RenderizadorCelula());
        tabela.getTableHeader().setDefaultRenderer(new RenderizadorCabecalho());

        // Por padrão o Tab fica preso dentro da tabela; aqui ele passa o foco para o próximo componente.
        tabela.getActionMap().put("selectNextColumnCell", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evento) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent(tabela);
            }
        });
        tabela.getActionMap().put("selectPreviousColumnCell", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evento) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().focusPreviousComponent(tabela);
            }
        });
    }

    /** Liga cada tabela ao seu formulário: selecionar uma linha carrega o registro para edição. */
    private void configurarSelecoes() {
        tabelaItens.getSelectionModel().addListSelectionListener(evento -> {
            if (evento.getValueIsAdjusting()) {
                return;
            }
            int linha = tabelaItens.getSelectedRow();
            if (linha >= 0) {
                ItemBiblioteca item = itensPorCodigo.get(String.valueOf(modeloItens.getValueAt(linha, 0)));
                if (item != null) {
                    entrarEdicaoItem(item);
                }
            } else if (codigoItemEmEdicao != null) {
                limparFormularioItem();
            }
        });

        tabelaUsuarios.getSelectionModel().addListSelectionListener(evento -> {
            if (evento.getValueIsAdjusting()) {
                return;
            }
            int linha = tabelaUsuarios.getSelectedRow();
            if (linha >= 0) {
                int id = Integer.parseInt(String.valueOf(modeloUsuarios.getValueAt(linha, 0)));
                Usuario usuario = usuariosPorId.get(id);
                if (usuario != null) {
                    entrarEdicaoUsuario(usuario);
                }
            } else if (idUsuarioEmEdicao != null) {
                limparFormularioUsuario();
            }
        });

        tabelaEmprestimos.getSelectionModel().addListSelectionListener(evento -> {
            if (evento.getValueIsAdjusting()) {
                return;
            }
            int linha = tabelaEmprestimos.getSelectedRow();
            if (linha >= 0 && linha < emprestimosExibidos.size()) {
                Emprestimo emprestimo = emprestimosExibidos.get(linha);
                campoCodigoEmprestimo.setText(emprestimo.getItem().getCodigo());
                campoIdEmprestimo.setText(String.valueOf(emprestimo.getUsuario().getId()));
            }
        });
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

    // ------------------------------------------------------------------ estado do formulário

    private boolean livroSelecionado() {
        return "Livro".equals(seletorTipo.getSelectedItem());
    }

    /** Mostra Autor e ISBN para livros e Edição para revistas. */
    private void mostrarCamposDoTipo() {
        boolean livro = livroSelecionado();
        rotuloAutor.setVisible(livro);
        campoAutor.setVisible(livro);
        rotuloIsbn.setVisible(livro);
        campoIsbn.setVisible(livro);
        rotuloEdicao.setVisible(!livro);
        campoEdicao.setVisible(!livro);
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    private void entrarEdicaoItem(ItemBiblioteca item) {
        codigoItemEmEdicao = item.getCodigo();
        seletorTipo.setSelectedItem(item.getTipo());
        campoCodigo.setText(item.getCodigo());
        campoTitulo.setText(item.getTitulo());
        campoAutor.setText("");
        campoIsbn.setText("");
        campoEdicao.setText("");
        if (item instanceof Livro livro) {
            campoAutor.setText(livro.getAutor());
            campoIsbn.setText(livro.getIsbn());
        } else if (item instanceof Revista revista) {
            campoEdicao.setText(String.valueOf(revista.getNumeroEdicao()));
        }
        definirEditavel(campoCodigo, false);
        seletorTipo.setEnabled(false);
        atualizarEstadoBotoes();
    }

    private void limparFormularioItem() {
        codigoItemEmEdicao = null;
        campoCodigo.setText("");
        campoTitulo.setText("");
        campoAutor.setText("");
        campoIsbn.setText("");
        campoEdicao.setText("");
        definirEditavel(campoCodigo, true);
        seletorTipo.setEnabled(true);
        tabelaItens.clearSelection();
        atualizarEstadoBotoes();
    }

    private void entrarEdicaoUsuario(Usuario usuario) {
        idUsuarioEmEdicao = usuario.getId();
        campoIdUsuario.setText(String.valueOf(usuario.getId()));
        campoNome.setText(usuario.getNome());
        campoEmail.setText(usuario.getEmail());
        definirEditavel(campoIdUsuario, false);
        atualizarEstadoBotoes();
    }

    private void limparFormularioUsuario() {
        idUsuarioEmEdicao = null;
        campoIdUsuario.setText("");
        campoNome.setText("");
        campoEmail.setText("");
        definirEditavel(campoIdUsuario, true);
        tabelaUsuarios.clearSelection();
        atualizarEstadoBotoes();
    }

    private void limparFormularioEmprestimo() {
        campoCodigoEmprestimo.setText("");
        campoIdEmprestimo.setText("");
        tabelaEmprestimos.clearSelection();
    }

    /** Campos bloqueados (como o código de um item em edição) ficam com o fundo da janela. */
    private void definirEditavel(JTextField campo, boolean editavel) {
        campo.setEditable(editavel);
        campo.setBackground(editavel ? paleta.campo() : paleta.fundo());
    }

    /**
     * Habilita cada botão conforme o momento: tudo fica bloqueado enquanto há operação em andamento,
     * Cadastrar só vale para registro novo e Salvar e Excluir só valem com um registro selecionado.
     */
    private void atualizarEstadoBotoes() {
        boolean livre = operacoesEmAndamento == 0;
        boolean editandoItem = codigoItemEmEdicao != null;
        boolean editandoUsuario = idUsuarioEmEdicao != null;

        botaoCadastrarItem.setEnabled(livre && !editandoItem);
        botaoSalvarItem.setEnabled(livre && editandoItem);
        botaoExcluirItem.setEnabled(livre && editandoItem);
        botaoLimparItem.setEnabled(livre);

        botaoCadastrarUsuario.setEnabled(livre && !editandoUsuario);
        botaoSalvarUsuario.setEnabled(livre && editandoUsuario);
        botaoExcluirUsuario.setEnabled(livre && editandoUsuario);
        botaoLimparUsuario.setEnabled(livre);

        botaoEmprestar.setEnabled(livre);
        botaoDevolver.setEnabled(livre);
        atualizarBotaoPadrao();
    }

    /** Faz a tecla Enter acionar o botão principal da aba aberta (Salvar quando há registro em edição). */
    private void atualizarBotaoPadrao() {
        JButton padrao = switch (abas.getSelectedIndex()) {
            case 0 -> codigoItemEmEdicao != null ? botaoSalvarItem : botaoCadastrarItem;
            case 1 -> idUsuarioEmEdicao != null ? botaoSalvarUsuario : botaoCadastrarUsuario;
            default -> botaoEmprestar;
        };
        getRootPane().setDefaultButton(padrao);
    }

    // ------------------------------------------------------------------ operações de negócio

    private void cadastrarItem() {
        if (livroSelecionado()) {
            cadastrarLivro();
        } else {
            cadastrarRevista();
        }
    }

    private void cadastrarLivro() {
        executar(() -> {
            Livro livro = new Livro(campoCodigo.getText().trim(), campoTitulo.getText().trim(),
                    campoAutor.getText().trim(), campoIsbn.getText().trim());
            return () -> {
                biblioteca.adicionarItem(livro);
                return "Livro salvo.";
            };
        }, this::limparFormularioItem);
    }

    private void cadastrarRevista() {
        executar(() -> {
            Revista revista = new Revista(campoCodigo.getText().trim(), campoTitulo.getText().trim(),
                    lerInteiro(campoEdicao.getText(), "Edição"));
            return () -> {
                biblioteca.adicionarItem(revista);
                return "Revista salva.";
            };
        }, this::limparFormularioItem);
    }

    private void salvarItem() {
        String codigo = codigoItemEmEdicao;
        if (codigo == null) {
            return;
        }
        executar(() -> {
            String titulo = campoTitulo.getText().trim();
            if (livroSelecionado()) {
                String autor = campoAutor.getText().trim();
                String isbn = campoIsbn.getText().trim();
                return () -> {
                    biblioteca.editarLivro(codigo, titulo, autor, isbn);
                    return "Livro atualizado.";
                };
            }
            int edicao = lerInteiro(campoEdicao.getText(), "Edição");
            return () -> {
                biblioteca.editarRevista(codigo, titulo, edicao);
                return "Revista atualizada.";
            };
        }, this::limparFormularioItem);
    }

    private void excluirItem() {
        String codigo = codigoItemEmEdicao;
        if (codigo == null || !confirmar("Excluir o item " + codigo + "?\nEsta ação não pode ser desfeita.")) {
            return;
        }
        executar(() -> () -> {
            biblioteca.excluirItem(codigo);
            return "Item excluído.";
        }, this::limparFormularioItem);
    }

    private void cadastrarUsuario() {
        executar(() -> {
            Usuario usuario = new Usuario(lerInteiro(campoIdUsuario.getText(), "ID"),
                    campoNome.getText().trim(), campoEmail.getText().trim());
            return () -> {
                biblioteca.adicionarUsuario(usuario);
                return "Usuário salvo.";
            };
        }, this::limparFormularioUsuario);
    }

    private void salvarUsuario() {
        Integer id = idUsuarioEmEdicao;
        if (id == null) {
            return;
        }
        executar(() -> {
            String nome = campoNome.getText().trim();
            String email = campoEmail.getText().trim();
            return () -> {
                biblioteca.editarUsuario(id, nome, email);
                return "Usuário atualizado.";
            };
        }, this::limparFormularioUsuario);
    }

    private void excluirUsuario() {
        Integer id = idUsuarioEmEdicao;
        if (id == null || !confirmar("Excluir o usuário " + id + "?\nEsta ação não pode ser desfeita.")) {
            return;
        }
        executar(() -> () -> {
            biblioteca.excluirUsuario(id);
            return "Usuário excluído.";
        }, this::limparFormularioUsuario);
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
        }, this::limparFormularioEmprestimo);
    }

    private void fazerDevolucao() {
        executar(() -> {
            String codigo = campoCodigoEmprestimo.getText().trim();
            return () -> {
                biblioteca.devolver(codigo);
                return "Devolução feita.";
            };
        }, this::limparFormularioEmprestimo);
    }

    private boolean confirmar(String mensagem) {
        int resposta = JOptionPane.showConfirmDialog(this, mensagem, "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return resposta == JOptionPane.YES_OPTION;
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
        atualizarEstadoBotoes();
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
        itensPorCodigo.clear();
        modeloItens.setRowCount(0);
        for (ItemBiblioteca item : dados.itens()) {
            itensPorCodigo.put(item.getCodigo(), item);
            modeloItens.addRow(new Object[]{
                    item.getCodigo(),
                    item.getTitulo(),
                    item.getTipo(),
                    item.isDisponivel() ? "Disponível" : "Emprestado"
            });
        }

        usuariosPorId.clear();
        modeloUsuarios.setRowCount(0);
        for (Usuario usuario : dados.usuarios()) {
            usuariosPorId.put(usuario.getId(), usuario);
            modeloUsuarios.addRow(new Object[]{usuario.getId(), usuario.getNome(), usuario.getEmail()});
        }

        emprestimosExibidos = new ArrayList<>(dados.emprestimos());
        Collections.reverse(emprestimosExibidos);
        modeloEmprestimos.setRowCount(0);
        for (Emprestimo emprestimo : emprestimosExibidos) {
            modeloEmprestimos.addRow(new Object[]{
                    emprestimo.getItem().getCodigo() + " - " + emprestimo.getItem().getTitulo(),
                    emprestimo.getUsuario().getId() + " - " + emprestimo.getUsuario().getNome(),
                    emprestimo.getDataEmprestimoFormatada(),
                    emprestimo.getDataLimiteFormatada(),
                    emprestimo.getDataDevolucao() == null ? "-" : emprestimo.getDataDevolucao().format(FORMATO_DATA),
                    descreverSituacao(emprestimo)
            });
        }
    }

    private String descreverSituacao(Emprestimo emprestimo) {
        return switch (emprestimo.getStatusAtual()) {
            case ATIVO -> "Ativo";
            case DEVOLVIDO -> "Devolvido";
            case EXPIRADO -> "Expirado";
        };
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
        JOptionPane.showMessageDialog(this, mensagem, "Biblioteca", JOptionPane.INFORMATION_MESSAGE);
    }

    // ------------------------------------------------------------------ aparência

    private void alternarTema() {
        modoEscuro = !modoEscuro;
        aplicarTema();
    }

    /** Troca o botão de seta do seletor por um mais simples, combinando com os campos arredondados. */
    private void estilizarSeletor() {
        seletorTipo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton seta = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(getForeground());
                        int centroX = getWidth() / 2;
                        int centroY = getHeight() / 2;
                        g2.fillPolygon(new int[]{centroX - 5, centroX + 5, centroX},
                                new int[]{centroY - 2, centroY - 2, centroY + 3}, 3);
                        g2.dispose();
                    }
                };
                seta.setBorder(BorderFactory.createEmptyBorder());
                seta.setContentAreaFilled(false);
                seta.setFocusable(false);
                seta.setPreferredSize(new Dimension(32, 20));
                return seta;
            }
        });
        seletorTipo.setFont(new Font(FONTE, Font.PLAIN, 14));
    }

    private Border criarBordaArredondada() {
        return new AbstractBorder() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean comFoco = c.hasFocus();
                g2.setColor(comFoco ? paleta.foco() : paleta.borda());
                g2.setStroke(new BasicStroke(comFoco ? 2f : 1f));
                g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, 16, 16);
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

    private void aplicarTema() {
        paleta = modoEscuro ? Paleta.ESCURO : Paleta.CLARO;

        // Seletor bloqueado (item em edição) continua legível.
        UIManager.put("ComboBox.disabledForeground", paleta.textoSecundario());
        UIManager.put("ComboBox.disabledBackground", paleta.fundo());

        aplicarCores(getContentPane());
        estilizarListaDoSeletor();

        botaoTema.setText(modoEscuro ? "☀ Modo claro" : "☾ Modo escuro");
        repaint();
    }

    private void aplicarCores(Component componente) {
        if (componente instanceof JScrollBar) {
            return; // as barras de rolagem são tratadas junto com o JScrollPane
        }

        if (componente instanceof JTextField campoTexto) {
            campoTexto.setCaretColor(paleta.texto());
            campoTexto.setBorder(criarBordaArredondada());
            campoTexto.setBackground(campoTexto.isEditable() ? paleta.campo() : paleta.fundo());
            campoTexto.setForeground(paleta.texto());
            campoTexto.setSelectionColor(paleta.selecao());
            campoTexto.setSelectedTextColor(paleta.textoSelecao());
        } else if (componente instanceof JComboBox<?> seletor) {
            seletor.setBorder(criarBordaArredondada());
            seletor.setBackground(paleta.campo());
            seletor.setForeground(paleta.texto());
        } else if (componente instanceof JButton botao) {
            if (botao.getParent() instanceof JComboBox) {
                botao.setForeground(paleta.texto()); // seta do seletor
            } else {
                botao.setBackground(paleta.botao());
                botao.setForeground(Color.WHITE);
                if (botao instanceof BotaoArredondado arredondado) {
                    arredondado.setCoresDesabilitado(paleta.botaoDesabilitado(), paleta.textoBotaoDesabilitado());
                } else {
                    botao.setOpaque(true);
                }
            }
        } else if (componente instanceof JTable tabela) {
            tabela.setBackground(paleta.campo());
            tabela.setForeground(paleta.texto());
            tabela.setSelectionBackground(paleta.selecao());
            tabela.setSelectionForeground(paleta.textoSelecao());
        } else {
            componente.setBackground(paleta.fundo());
            componente.setForeground(paleta.texto());
        }

        if (componente instanceof Container container) {
            for (Component filho : container.getComponents()) {
                aplicarCores(filho);
            }
        }

        if (componente instanceof JScrollPane rolagem) {
            estilizarRolagem(rolagem);
        }
    }

    /** Borda discreta, fundo da área vazia e barras de rolagem no tom do tema. */
    private void estilizarRolagem(JScrollPane rolagem) {
        rolagem.setBorder(BorderFactory.createLineBorder(paleta.divisoria()));
        rolagem.getViewport().setBackground(paleta.campo());
        rolagem.getVerticalScrollBar().setUI(new BarraRolagemUI());
        rolagem.getHorizontalScrollBar().setUI(new BarraRolagemUI());
        JPanel canto = new JPanel();
        canto.setBackground(paleta.cabecalho());
        rolagem.setCorner(JScrollPane.UPPER_RIGHT_CORNER, canto);
    }

    /** A lista que abre no seletor não faz parte da janela, então recebe as cores à parte. */
    private void estilizarListaDoSeletor() {
        if (seletorTipo.getUI().getAccessibleChild(seletorTipo, 0) instanceof BasicComboPopup popup) {
            JList<Object> lista = popup.getList();
            lista.setBackground(paleta.campo());
            lista.setForeground(paleta.texto());
            lista.setSelectionBackground(paleta.selecao());
            lista.setSelectionForeground(paleta.textoSelecao());
            popup.setBorder(BorderFactory.createLineBorder(paleta.borda()));
        }
    }

    /**
     * Conjunto de cores de um tema. As combinações de texto e fundo foram escolhidas para ter
     * contraste de pelo menos 4,5:1 (recomendação WCAG para texto comum).
     */
    private record Paleta(Color fundo, Color campo, Color cabecalho, Color linhaAlternada,
                          Color texto, Color textoSecundario, Color borda, Color divisoria, Color foco,
                          Color botao, Color botaoDesabilitado, Color textoBotaoDesabilitado,
                          Color selecao, Color textoSelecao,
                          Color sucesso, Color aviso, Color erro, Color barraRolagem) {

        static final Paleta CLARO = new Paleta(
                new Color(246, 247, 246), Color.WHITE, new Color(236, 240, 238), new Color(246, 249, 247),
                new Color(30, 33, 36), new Color(95, 103, 100), new Color(150, 158, 155),
                new Color(214, 219, 217), new Color(35, 125, 95),
                new Color(35, 125, 95), new Color(218, 223, 221), new Color(115, 123, 120),
                new Color(200, 230, 218), new Color(18, 48, 38),
                new Color(25, 120, 80), new Color(155, 95, 0), new Color(185, 40, 35),
                new Color(190, 197, 194));

        static final Paleta ESCURO = new Paleta(
                new Color(28, 31, 35), new Color(40, 44, 50), new Color(34, 38, 43), new Color(46, 51, 57),
                new Color(232, 235, 238), new Color(165, 172, 180), new Color(118, 126, 135),
                new Color(58, 64, 71), new Color(92, 200, 160),
                new Color(33, 123, 93), new Color(52, 57, 63), new Color(135, 142, 150),
                new Color(30, 95, 76), Color.WHITE,
                new Color(110, 210, 160), new Color(240, 190, 90), new Color(255, 135, 125),
                new Color(80, 87, 95));
    }

    /** Células com espaçamento lateral, linhas alternadas e cor de destaque para as situações. */
    private final class RenderizadorCelula extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionada,
                                                       boolean comFoco, int linha, int coluna) {
            super.getTableCellRendererComponent(tabela, valor, selecionada, false, linha, coluna);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            if (selecionada) {
                setBackground(paleta.selecao());
                setForeground(paleta.textoSelecao());
            } else {
                setBackground(linha % 2 == 0 ? paleta.campo() : paleta.linhaAlternada());
                setForeground(corDoValor(valor));
            }
            return this;
        }

        private Color corDoValor(Object valor) {
            return switch (String.valueOf(valor)) {
                case "Disponível", "Ativo" -> paleta.sucesso();
                case "Emprestado" -> paleta.aviso();
                case "Expirado" -> paleta.erro();
                case "-", "Devolvido" -> paleta.textoSecundario();
                default -> paleta.texto();
            };
        }
    }

    /** Cabeçalho plano, no tom do tema, no lugar do cabeçalho em relevo do Metal. */
    private final class RenderizadorCabecalho extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionada,
                                                       boolean comFoco, int linha, int coluna) {
            super.getTableCellRendererComponent(tabela, valor, false, false, linha, coluna);
            setFont(new Font(FONTE, Font.BOLD, 13));
            setBackground(paleta.cabecalho());
            setForeground(paleta.texto());
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, paleta.divisoria()),
                    BorderFactory.createEmptyBorder(0, 10, 0, 10)));
            return this;
        }
    }

    /**
     * Abas planas: a selecionada ganha uma barra na cor de destaque e texto principal; as demais
     * usam texto secundário. Substitui o visual do Metal, que desenha contornos sempre brancos.
     */
    private final class AbasUI extends BasicTabbedPaneUI {
        @Override
        protected void installDefaults() {
            super.installDefaults();
            tabInsets = new Insets(8, 18, 8, 18);
            selectedTabPadInsets = new Insets(0, 0, 0, 0);
            tabAreaInsets = new Insets(4, 14, 0, 14);
            contentBorderInsets = new Insets(1, 0, 0, 0);
        }

        @Override
        protected void paintTabBackground(Graphics g, int posicao, int indice,
                                          int x, int y, int largura, int altura, boolean selecionada) {
            g.setColor(selecionada ? paleta.cabecalho() : paleta.fundo());
            g.fillRect(x, y, largura, altura);
        }

        @Override
        protected void paintTabBorder(Graphics g, int posicao, int indice,
                                      int x, int y, int largura, int altura, boolean selecionada) {
            if (selecionada) {
                g.setColor(paleta.foco());
                g.fillRect(x, y + altura - 3, largura, 3);
            }
        }

        @Override
        protected void paintText(Graphics g, int posicao, Font fonte, FontMetrics medidas, int indice,
                                 String titulo, Rectangle area, boolean selecionada) {
            g.setFont(fonte);
            g.setColor(selecionada ? paleta.texto() : paleta.textoSecundario());
            BasicGraphicsUtils.drawStringUnderlineCharAt(tabPane, (Graphics2D) g, titulo,
                    tabPane.getDisplayedMnemonicIndexAt(indice), area.x, area.y + medidas.getAscent());
        }

        @Override
        protected void paintFocusIndicator(Graphics g, int posicao, Rectangle[] retangulos, int indice,
                                           Rectangle areaIcone, Rectangle areaTexto, boolean selecionada) {
            if (tabPane.hasFocus() && selecionada) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(paleta.foco());
                Rectangle r = retangulos[indice];
                g2.drawRoundRect(r.x + 3, r.y + 3, r.width - 7, r.height - 10, 8, 8);
                g2.dispose();
            }
        }

        @Override
        protected void paintContentBorder(Graphics g, int posicao, int indiceSelecionado) {
            int topo = calculateTabAreaHeight(posicao, runCount, maxTabHeight);
            g.setColor(paleta.divisoria());
            g.fillRect(0, topo, tabPane.getWidth(), 1);
        }
    }

    /** Barra de rolagem fina, sem setas, com o polegar arredondado. */
    private final class BarraRolagemUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = paleta.barraRolagem();
            trackColor = paleta.campo();
        }

        @Override
        protected JButton createDecreaseButton(int orientacao) {
            return botaoVazio();
        }

        @Override
        protected JButton createIncreaseButton(int orientacao) {
            return botaoVazio();
        }

        private JButton botaoVazio() {
            JButton botao = new JButton();
            Dimension zero = new Dimension(0, 0);
            botao.setPreferredSize(zero);
            botao.setMinimumSize(zero);
            botao.setMaximumSize(zero);
            return botao;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle area) {
            g.setColor(trackColor);
            g.fillRect(area.x, area.y, area.width, area.height);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle area) {
            if (area.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isThumbRollover() || isDragging ? paleta.textoSecundario() : thumbColor);
            g2.fillRoundRect(area.x + 3, area.y + 3, area.width - 6, area.height - 6, 8, 8);
            g2.dispose();
        }
    }

    /**
     * Inicia a aplicação: cria o banco SQLite se for preciso e abre a janela principal
     * na thread de eventos do Swing.
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

    /**
     * Painel de formulário com rótulos à esquerda e campos à direita, alinhados em duas colunas,
     * seguido de uma linha de botões alinhada à direita.
     */
    private static final class Formulario extends JPanel {
        private int linha = 0;

        Formulario() {
            super(new GridBagLayout());
            setBorder(BorderFactory.createEmptyBorder(6, 4, 10, 4));
        }

        JLabel adicionarCampo(String texto, JComponent campo) {
            JLabel rotulo = new JLabel(texto);
            rotulo.setLabelFor(campo);
            rotulo.setPreferredSize(new Dimension(140, rotulo.getPreferredSize().height));

            GridBagConstraints c = new GridBagConstraints();
            c.gridy = linha++;
            c.anchor = GridBagConstraints.WEST;

            c.gridx = 0;
            c.insets = new Insets(4, 0, 4, 12);
            add(rotulo, c);

            c.gridx = 1;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(4, 0, 4, 0);
            add(campo, c);

            if (campo instanceof JTextField campoTexto) {
                campoTexto.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent evento) {
                        campoTexto.repaint();
                    }

                    @Override
                    public void focusLost(FocusEvent evento) {
                        campoTexto.repaint();
                    }
                });
            }
            return rotulo;
        }

        void adicionarAcoes(JButton... botoes) {
            JPanel faixa = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            for (JButton botao : botoes) {
                faixa.add(botao);
            }

            GridBagConstraints c = new GridBagConstraints();
            c.gridy = linha++;
            c.gridx = 0;
            c.gridwidth = 2;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(10, 0, 0, 0);
            add(faixa, c);
        }
    }
}