package main;

import modelo.Dado;
import modelo.Jogador;
import modelo.Peca;
import modelo.Tabuleiro;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Classe herdada baseada em Swing que representa a interface legada/Swing do jogo.
 * Serve como elemento de integracao do projeto para manter a conformidade com o enunciado inicial.
 */
public class JogoBackgammon extends JFrame {
    private final Tabuleiro tabuleiro;
    private final Dado dadoUm;
    private final Dado dadoDois;
    private final JLabel estadoJogo;
    private final JTextArea resumoTabuleiro;
    private final JTextField jogadorBrancoField;
    private final JTextField jogadorPretoField;

    /**
     * Construtor da classe JogoBackgammon.
     * Inicializa o tabuleiro local, os dados e os componentes graficos Swing.
     */
    public JogoBackgammon() {
        this.tabuleiro = new Tabuleiro();
        this.dadoUm = new Dado();
        this.dadoDois = new Dado();
        this.estadoJogo = new JLabel();
        this.resumoTabuleiro = new JTextArea();
        this.jogadorBrancoField = new JTextField("Jogador Branco");
        this.jogadorPretoField = new JTextField("Jogador Preto");

        configurarJanela();
        setContentPane(criarConteudo());
        atualizarEstadoInicial();
    }

    private void configurarJanela() {
        setTitle("Jogo Backgammon");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 640));
        setLocationRelativeTo(null);
    }

    private JPanel criarConteudo() {
        JPanel raiz = new JPanel(new BorderLayout(24, 24));
        raiz.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        raiz.setBackground(new Color(245, 238, 225));
        raiz.add(criarCabecalho(), BorderLayout.NORTH);
        raiz.add(criarCentro(), BorderLayout.CENTER);
        raiz.add(criarRodape(), BorderLayout.SOUTH);
        return raiz;
    }

    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel();
        cabecalho.setLayout(new BoxLayout(cabecalho, BoxLayout.Y_AXIS));
        cabecalho.setOpaque(false);

        JLabel titulo = new JLabel("BACKGAMMON");
        titulo.setFont(new Font("Serif", Font.BOLD, 34));
        titulo.setForeground(new Color(73, 38, 19));
        titulo.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitulo = new JLabel("Pagina principal do jogo e ponto de arranque do projeto");
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitulo.setForeground(new Color(102, 77, 59));
        subtitulo.setAlignmentX(CENTER_ALIGNMENT);

        cabecalho.add(titulo);
        cabecalho.add(Box.createVerticalStrut(8));
        cabecalho.add(subtitulo);
        return cabecalho;
    }

    private JPanel criarCentro() {
        JPanel centro = new JPanel(new GridLayout(1, 2, 24, 24));
        centro.setOpaque(false);
        centro.add(criarPainelConfiguracao());
        centro.add(criarPainelTabuleiro());
        return centro;
    }

    private JPanel criarPainelConfiguracao() {
        JPanel painel = new JPanel(new BorderLayout(16, 16));
        painel.setBackground(new Color(124, 73, 44));
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(73, 38, 19), 2),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JLabel titulo = new JLabel("Preparar partida");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        titulo.setForeground(new Color(255, 245, 232));

        JPanel formulario = new JPanel(new GridLayout(5, 1, 0, 12));
        formulario.setOpaque(false);
        formulario.add(criarCampoJogador("Nome jogador branco", jogadorBrancoField));
        formulario.add(criarCampoJogador("Nome jogador preto", jogadorPretoField));

        JButton iniciar = new JButton("Novo jogo");
        iniciar.addActionListener(evento -> iniciarNovoJogo());
        JButton regras = new JButton("Ver regras rapidas");
        regras.addActionListener(evento -> mostrarRegras());
        JButton iniciarRede = new JButton("Jogar em Rede (JavaFX)");
        iniciarRede.addActionListener(evento -> iniciarJogoRede());

        formulario.add(iniciar);
        formulario.add(regras);
        formulario.add(iniciarRede);

        JTextArea descricao = new JTextArea(
                "Esta pagina principal arranca o jogo, mostra o estado inicial do tabuleiro e deixa "
                        + "os nomes dos jogadores prontos para a proxima fase da logica.");

        descricao.setLineWrap(true);
        descricao.setWrapStyleWord(true);
        descricao.setEditable(false);
        descricao.setOpaque(false);
        descricao.setForeground(new Color(255, 245, 232));
        descricao.setFont(new Font("SansSerif", Font.PLAIN, 15));

        painel.add(titulo, BorderLayout.NORTH);
        painel.add(formulario, BorderLayout.CENTER);
        painel.add(descricao, BorderLayout.SOUTH);
        return painel;
    }

    private JPanel criarCampoJogador(String label, JTextField campo) {
        JPanel linha = new JPanel(new BorderLayout(0, 6));
        linha.setOpaque(false);

        JLabel titulo = new JLabel(label);
        titulo.setForeground(new Color(255, 245, 232));
        titulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        campo.setFont(new Font("SansSerif", Font.PLAIN, 14));

        linha.add(titulo, BorderLayout.NORTH);
        linha.add(campo, BorderLayout.CENTER);
        return linha;
    }

    private JPanel criarPainelTabuleiro() {
        JPanel painel = new JPanel(new BorderLayout(16, 16));
        painel.setBackground(new Color(246, 243, 237));
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(162, 131, 98), 2),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JLabel titulo = new JLabel("Estado inicial do tabuleiro");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        titulo.setForeground(new Color(73, 38, 19));

        resumoTabuleiro.setEditable(false);
        resumoTabuleiro.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        resumoTabuleiro.setLineWrap(true);
        resumoTabuleiro.setWrapStyleWord(true);
        resumoTabuleiro.setBackground(new Color(255, 251, 245));
        resumoTabuleiro.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JTextArea ajuda = new JTextArea(
                "Legenda: B = pecas brancas, P = pecas pretas, -- = campo vazio.\n"
                        + "A disposicao segue a abertura classica do backgammon.");
        ajuda.setEditable(false);
        ajuda.setOpaque(false);
        ajuda.setForeground(new Color(102, 77, 59));
        ajuda.setFont(new Font("SansSerif", Font.PLAIN, 14));

        painel.add(titulo, BorderLayout.NORTH);
        painel.add(new JScrollPane(resumoTabuleiro), BorderLayout.CENTER);
        painel.add(ajuda, BorderLayout.SOUTH);
        return painel;
    }

    private JLabel criarRodape() {
        estadoJogo.setOpaque(true);
        estadoJogo.setBackground(new Color(73, 38, 19));
        estadoJogo.setForeground(new Color(255, 245, 232));
        estadoJogo.setFont(new Font("SansSerif", Font.BOLD, 15));
        estadoJogo.setHorizontalAlignment(SwingConstants.CENTER);
        estadoJogo.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        return estadoJogo;
    }

    private void atualizarEstadoInicial() {
        resumoTabuleiro.setText(tabuleiro.gerarResumoVisual());
        estadoJogo.setText("Projeto organizado. Pronto para iniciar uma nova partida.");
    }

    private void iniciarNovoJogo() {
        Jogador jogadorBranco = new Jogador(validarNome(jogadorBrancoField.getText(), "Jogador Branco"), Peca.CorPeca.BRANCO);
        Jogador jogadorPreto = new Jogador(validarNome(jogadorPretoField.getText(), "Jogador Preto"), Peca.CorPeca.PRETO);

        int valorUm = dadoUm.lancar();
        int valorDois = dadoDois.lancar();
        String mensagem = jogadorBranco.getNome() + " vs " + jogadorPreto.getNome()
                + " | dados iniciais: " + valorUm + " e " + valorDois;

        estadoJogo.setText(mensagem);
        JOptionPane.showMessageDialog(this, mensagem, "Partida criada", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarRegras() {
        String regras = """
                Objetivo:
                Levar todas as pecas para fora do tabuleiro antes do adversario.

                Turno:
                Cada jogador lanca dois dados e move as pecas conforme os valores obtidos.

                Captura:
                Uma peca sozinha pode ser capturada e vai para a barra.

                Saida:
                Quando todas as pecas estiverem na zona final, ja podem sair do tabuleiro.
                """;
        JOptionPane.showMessageDialog(this, regras, "Regras rapidas", JOptionPane.PLAIN_MESSAGE);
    }

    private void iniciarJogoRede() {
        this.setVisible(false);
        this.dispose();
        new Thread(() -> {
            try {
                javafx.application.Application.launch(ui.ClienteMain.class);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private String validarNome(String nomeAtual, String nomeDefault) {
        String nome = nomeAtual == null ? "" : nomeAtual.trim();
        return nome.isEmpty() ? nomeDefault : nome;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Mantem o look and feel default se o do sistema falhar.
            }
            new JogoBackgammon().setVisible(true);
        });
    }
}
