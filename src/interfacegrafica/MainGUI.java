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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
    private static final Color COR_DESTAQUE = new Color(55, 145, 115);
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String FONTE = "Segoe UI";

    /** Serviço de regras de negócio usado pela janela. */
    private final Biblioteca biblioteca;

    private boolean modoEscuro = false;
    private Color corFundo = new Color(248, 248, 246);
    private Color corCampo = Color.WHITE;
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
        UIManager.put("Button.disabledText", new Color(235, 240, 238));
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
        painel.add(new JScrollPane(tabela), BorderLayout.CENTER);
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
        tabela.setFillsViewportHeight(true);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.getTableHeader().setFont(new Font(FONTE, Font.BOLD, 13));
        tabela.getTableHeader().setPreferredSize(new Dimension(0, 36));

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
        campo.setBackground(editavel ? corCampo : corFundo);
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
                g2.setColor(comFoco ? COR_DESTAQUE : modoEscuro
                        ? new Color(90, 96, 102)
                        : new Color(195, 200, 198));
                g2.setStroke(new BasicStroke(comFoco ? 2.5f : 1f));
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
        corFundo = modoEscuro ? new Color(35, 38, 42) : new Color(248, 248, 246);
        corCampo = modoEscuro ? new Color(50, 54, 59) : Color.WHITE;
        Color texto = modoEscuro ? new Color(235, 235, 235) : new Color(35, 35, 35);
        Color fundoAbas = modoEscuro ? new Color(43, 47, 51) : new Color(236, 239, 238);

        // Seletor bloqueado (item em edição) continua legível.
        UIManager.put("ComboBox.disabledForeground", modoEscuro ? new Color(165, 170, 175) : new Color(105, 110, 108));
        UIManager.put("ComboBox.disabledBackground", corFundo);

        // A aba selecionada usa a mesma cor do conteúdo, para parecer ligada ao painel.
        UIManager.put("TabbedPane.selected", corFundo);
        abas.updateUI();

        aplicarCores(getContentPane(), corFundo, corCampo, texto);

        botaoTema.setText(modoEscuro ? "☀ Modo claro" : "☾ Modo escuro");
        abas.setBackground(fundoAbas);
        abas.setForeground(texto);
        for (int i = 0; i < abas.getTabCount(); i++) {
            abas.setBackgroundAt(i, fundoAbas);
            abas.setForegroundAt(i, texto);
        }
        repaint();
    }

    private void aplicarCores(Component componente, Color fundo, Color campo, Color texto) {
        if (componente instanceof JTextField || componente instanceof JComboBox) {
            if (componente instanceof JTextField campoTexto) {
                campoTexto.setCaretColor(texto);
                campoTexto.setBorder(criarBordaArredondada());
                campoTexto.setBackground(campoTexto.isEditable() ? campo : fundo);
            } else {
                ((JComboBox<?>) componente).setBorder(criarBordaArredondada());
                componente.setBackground(campo);
            }
            componente.setForeground(texto);
        } else if (componente instanceof JTable tabela) {
            aplicarCoresTabela(tabela, campo, texto);
        } else if (componente instanceof JButton botao) {
            if (botao.getParent() instanceof JComboBox) {
                botao.setForeground(texto);
            } else {
                botao.setBackground(COR_DESTAQUE);
                botao.setForeground(Color.WHITE);
                if (!(botao instanceof BotaoArredondado)) {
                    botao.setOpaque(true);
                }
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

    private void aplicarCoresTabela(JTable tabela, Color campo, Color texto) {
        tabela.setBackground(campo);
        tabela.setForeground(texto);
        tabela.setGridColor(modoEscuro ? new Color(65, 70, 75) : new Color(225, 228, 230));
        tabela.setSelectionBackground(modoEscuro ? new Color(48, 105, 91) : new Color(214, 235, 227));
        tabela.setSelectionForeground(modoEscuro ? Color.WHITE : new Color(35, 55, 47));
        tabela.getTableHeader().setBackground(modoEscuro ? new Color(43, 47, 51) : new Color(244, 246, 245));
        tabela.getTableHeader().setForeground(texto);
        tabela.getTableHeader().setBorder(BorderFactory.createMatteBorder(
                0, 0, 1, 0, modoEscuro ? new Color(75, 80, 85) : new Color(215, 220, 218)));
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