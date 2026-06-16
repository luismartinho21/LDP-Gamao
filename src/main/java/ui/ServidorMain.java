package ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import rede.Servidor;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ServidorMain extends Application {

    private Servidor servidor;
    private Thread threadServidor;
    private boolean servidorAtivo = false;

    // Componentes UI
    private TextField txtPorta;
    private Button btnIniciar;
    private Button btnParar;
    private TextArea areaLogs;
    private Label lblEstadoTexto;
    private Circle indicadorEstado;
    private Label lblJogador1;
    private Label lblJogador2;
    private Label lblContadorLigacoes;

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Gamão — Painel do Servidor");
        stage.setResizable(false);

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #1A1A2E;");

        // ── Cabeçalho ──────────────────────────────────────────────────────────
        HBox cabecalho = new HBox(12);
        cabecalho.setAlignment(Pos.CENTER_LEFT);

        Label lblTitulo = new Label("Servidor do Gamão");
        lblTitulo.setStyle("-fx-text-fill: #E8D5B7; -fx-font-size: 26px; -fx-font-weight: bold;");

        Label lblVersao = new Label("v1.0");
        lblVersao.setStyle("-fx-text-fill: #8B7355; -fx-font-size: 13px; -fx-padding: 4 8 4 8; "
                + "-fx-background-color: #2A2A3E; -fx-background-radius: 10;");

        cabecalho.getChildren().addAll(lblTitulo, lblVersao);

        // ── Cartão de Configuração ─────────────────────────────────────────────
        VBox cartaoConfig = new VBox(16);
        cartaoConfig.setPadding(new Insets(20));
        cartaoConfig.setStyle("-fx-background-color: #2A2A3E; -fx-background-radius: 10;");

        Label lblConfigTitulo = new Label("Configuração");
        lblConfigTitulo.setStyle("-fx-text-fill: #A0A0B0; -fx-font-size: 12px; -fx-font-weight: bold;");

        HBox linhaPorta = new HBox(12);
        linhaPorta.setAlignment(Pos.CENTER_LEFT);

        VBox blocoPorta = new VBox(5);
        Label lblPortaLabel = new Label("PORTA");
        lblPortaLabel.setStyle("-fx-text-fill: #6A6A7A; -fx-font-size: 10px; -fx-font-weight: bold;");
        txtPorta = new TextField("12025");
        txtPorta.setPrefWidth(110);
        txtPorta.setPrefHeight(36);
        txtPorta.setStyle("-fx-background-color: #1A1A2E; -fx-text-fill: #E8D5B7; "
                + "-fx-border-color: #3A3A5E; -fx-border-radius: 6; -fx-background-radius: 6; "
                + "-fx-font-size: 14px;");
        blocoPorta.getChildren().addAll(lblPortaLabel, txtPorta);

        btnIniciar = new Button("▶  Iniciar Servidor");
        btnIniciar.setPrefHeight(36);
        btnIniciar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-font-size: 13px; "
                + "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 0 20 0 20;");
        btnIniciar.setOnAction(e -> iniciarServidor());

        btnParar = new Button("⏹  Parar Servidor");
        btnParar.setPrefHeight(36);
        btnParar.setDisable(true);
        btnParar.setStyle("-fx-background-color: #E53935; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-font-size: 13px; "
                + "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 0 20 0 20;");
        btnParar.setOnAction(e -> pararServidor());

        linhaPorta.getChildren().addAll(blocoPorta, btnIniciar, btnParar);
        cartaoConfig.getChildren().addAll(lblConfigTitulo, linhaPorta);

        // ── Cartão de Estado ───────────────────────────────────────────────────
        HBox cartaoEstado = new HBox(20);
        cartaoEstado.setPadding(new Insets(16, 20, 16, 20));
        cartaoEstado.setAlignment(Pos.CENTER_LEFT);
        cartaoEstado.setStyle("-fx-background-color: #2A2A3E; -fx-background-radius: 10;");

        // Indicador visual (círculo colorido)
        VBox blocoIndicador = new VBox(6);
        blocoIndicador.setAlignment(Pos.CENTER);
        indicadorEstado = new Circle(8);
        indicadorEstado.setFill(Color.web("#555566"));
        lblEstadoTexto = new Label("Parado");
        lblEstadoTexto.setStyle("-fx-text-fill: #6A6A7A; -fx-font-size: 11px; -fx-font-weight: bold;");
        blocoIndicador.getChildren().addAll(indicadorEstado, lblEstadoTexto);

        // Separador vertical
        Label sep1 = new Label("|");
        sep1.setStyle("-fx-text-fill: #3A3A5E; -fx-font-size: 24px;");

        // Jogadores ligados
        VBox blocoJogadores = new VBox(6);
        Label lblJogadoresTitulo = new Label("JOGADORES");
        lblJogadoresTitulo.setStyle("-fx-text-fill: #6A6A7A; -fx-font-size: 10px; -fx-font-weight: bold;");

        lblJogador1 = new Label("○  A aguardar...");
        lblJogador1.setStyle("-fx-text-fill: #8A8A9A; -fx-font-size: 12px;");
        lblJogador2 = new Label("○  A aguardar...");
        lblJogador2.setStyle("-fx-text-fill: #8A8A9A; -fx-font-size: 12px;");
        blocoJogadores.getChildren().addAll(lblJogadoresTitulo, lblJogador1, lblJogador2);

        // Separador vertical
        Label sep2 = new Label("|");
        sep2.setStyle("-fx-text-fill: #3A3A5E; -fx-font-size: 24px;");

        // Contador de ligações
        VBox blocoContador = new VBox(6);
        Label lblContadorTitulo = new Label("LIGAÇÕES");
        lblContadorTitulo.setStyle("-fx-text-fill: #6A6A7A; -fx-font-size: 10px; -fx-font-weight: bold;");
        lblContadorLigacoes = new Label("0 / 2");
        lblContadorLigacoes.setStyle("-fx-text-fill: #E8D5B7; -fx-font-size: 20px; -fx-font-weight: bold;");
        blocoContador.getChildren().addAll(lblContadorTitulo, lblContadorLigacoes);

        cartaoEstado.getChildren().addAll(blocoIndicador, sep1, blocoJogadores, sep2, blocoContador);

        // ── Área de Logs ───────────────────────────────────────────────────────
        VBox cartaoLogs = new VBox(10);
        VBox.setVgrow(cartaoLogs, Priority.ALWAYS);
        cartaoLogs.setPadding(new Insets(16, 20, 16, 20));
        cartaoLogs.setStyle("-fx-background-color: #2A2A3E; -fx-background-radius: 10;");

        HBox cabecalhoLogs = new HBox();
        cabecalhoLogs.setAlignment(Pos.CENTER_LEFT);
        Label lblLogsTitulo = new Label("Registo de Atividade");
        lblLogsTitulo.setStyle("-fx-text-fill: #A0A0B0; -fx-font-size: 12px; -fx-font-weight: bold;");

        Button btnLimpar = new Button("Limpar");
        btnLimpar.setStyle("-fx-background-color: transparent; -fx-text-fill: #6A6A7A; "
                + "-fx-font-size: 11px; -fx-cursor: hand; -fx-border-color: #3A3A5E; "
                + "-fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 2 8 2 8;");
        btnLimpar.setOnAction(e -> areaLogs.clear());
        HBox.setHgrow(lblLogsTitulo, Priority.ALWAYS);

        // ──Botões de gravação e carregamento do estado do jogo ──
        Button btnGravar = new Button("Gravar");
        btnGravar.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; "
                + "-fx-font-size: 11px; -fx-cursor: hand; -fx-border-radius: 4; "
                + "-fx-background-radius: 4; -fx-padding: 2 8 2 8;");
        btnGravar.setOnAction(e -> {
            if (servidor != null) {
                servidor.gravarJogo("jogo_salvo.dat");
                adicionarLog("SISTEMA", "Jogo gravado em jogo_salvo.dat");
            } else {
                adicionarLog("ERRO", "Servidor não está activo.");
            }
        });

        Button btnCarregar = new Button("Carregar");
        btnCarregar.setStyle("-fx-background-color: #1565C0; -fx-text-fill: white; "
                + "-fx-font-size: 11px; -fx-cursor: hand; -fx-border-radius: 4; "
                + "-fx-background-radius: 4; -fx-padding: 2 8 2 8;");
        btnCarregar.setOnAction(e -> {
            if (servidor != null) {
                servidor.carregarJogo("jogo_salvo.dat");
                adicionarLog("SISTEMA", "Jogo carregado de jogo_salvo.dat");
            } else {
                adicionarLog("ERRO", "Servidor não está activo.");
            }
        });
        cabecalhoLogs.getChildren().addAll(lblLogsTitulo, btnGravar, btnCarregar, btnLimpar);

        areaLogs = new TextArea();
        areaLogs.setEditable(false);
        areaLogs.setPrefHeight(280);
        areaLogs.setStyle("-fx-control-inner-background: #1A1A2E; -fx-text-fill: #C0C0D0; "
                + "-fx-font-family: 'Monospaced'; -fx-font-size: 12px; "
                + "-fx-border-color: #3A3A5E; -fx-border-radius: 6; -fx-background-radius: 6;");

        ScrollPane scrollLogs = new ScrollPane(areaLogs);
        scrollLogs.setFitToWidth(true);
        scrollLogs.setFitToHeight(true);
        VBox.setVgrow(scrollLogs, Priority.ALWAYS);
        scrollLogs.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        cartaoLogs.getChildren().addAll(cabecalhoLogs, scrollLogs);

        // ── Rodapé ─────────────────────────────────────────────────────────────
        Label lblRodape = new Label("Os clientes ligam-se ao IP desta máquina na porta configurada acima.");
        lblRodape.setStyle("-fx-text-fill: #4A4A5A; -fx-font-size: 11px; -fx-font-style: italic;");

        root.getChildren().addAll(cabecalho, cartaoConfig, cartaoEstado, cartaoLogs, lblRodape);

        DropShadow sombraJanela = new DropShadow();
        sombraJanela.setColor(Color.rgb(0, 0, 0, 0.6));
        sombraJanela.setRadius(30);
        root.setEffect(sombraJanela);

        Scene scene = new Scene(root, 600, 640);
        stage.setScene(scene);
        stage.show();

        adicionarLog("Sistema", "Painel do servidor pronto. Configure a porta e clique em Iniciar.");

        stage.setOnCloseRequest(e -> {
            pararServidor();
            Platform.exit();
            System.exit(0);
        });
    }

    // ── Lógica do Servidor ─────────────────────────────────────────────────────

    private void iniciarServidor() {
        String portaStr = txtPorta.getText().trim();
        int porta;
        try {
            porta = Integer.parseInt(portaStr);
            if (porta < 1024 || porta > 65535) {
                adicionarLog("ERRO", "Porta inválida. Use um valor entre 1024 e 65535.");
                return;
            }
        } catch (NumberFormatException e) {
            adicionarLog("ERRO", "Porta inválida: \"" + portaStr + "\".");
            return;
        }

        servidor = new Servidor(porta);

        servidor.setLogListener(mensagem -> Platform.runLater(() -> {
            adicionarLog("SERVIDOR", mensagem);
            atualizarEstadoJogadores(mensagem);
        }));

        threadServidor = new Thread(() -> servidor.iniciar(), "Servidor-Principal");
        threadServidor.setDaemon(true);
        threadServidor.start();

        servidorAtivo = true;
        atualizarControlosAtivo(true, porta);
        adicionarLog("SISTEMA", "Servidor iniciado na porta " + porta + ". À espera de jogadores...");
    }

    private void pararServidor() {
        if (servidor != null) {
            servidor.encerrarServidor();
            servidor = null;
        }
        if (threadServidor != null) {
            threadServidor.interrupt();
            threadServidor = null;
        }
        servidorAtivo = false;
        atualizarControlosAtivo(false, 0);
        adicionarLog("SISTEMA", "Servidor encerrado.");
    }

    private void atualizarControlosAtivo(boolean ativo, int porta) {
        Platform.runLater(() -> {
            btnIniciar.setDisable(ativo);
            btnParar.setDisable(!ativo);
            txtPorta.setDisable(ativo);

            if (ativo) {
                indicadorEstado.setFill(Color.web("#4CAF50"));
                lblEstadoTexto.setText("Ativo  :" + porta);
                lblEstadoTexto.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 11px; -fx-font-weight: bold;");
            } else {
                indicadorEstado.setFill(Color.web("#555566"));
                lblEstadoTexto.setText("Parado");
                lblEstadoTexto.setStyle("-fx-text-fill: #6A6A7A; -fx-font-size: 11px; -fx-font-weight: bold;");
                lblJogador1.setText("○  A aguardar...");
                lblJogador1.setStyle("-fx-text-fill: #8A8A9A; -fx-font-size: 12px;");
                lblJogador2.setText("○  A aguardar...");
                lblJogador2.setStyle("-fx-text-fill: #8A8A9A; -fx-font-size: 12px;");
                lblContadorLigacoes.setText("0 / 2");
            }
        });
    }

    /**
     * Analisa as mensagens de log do servidor para atualizar o painel de jogadores.
     * O Servidor.log() emite padrões reconhecíveis que aqui são interpretados.
     */
    private void atualizarEstadoJogadores(String mensagem) {
        String msg = mensagem.toLowerCase();

        if (msg.contains("cliente ligado como jogador branco")) {
            lblJogador1.setText("●  Brancas — ligado");
            lblJogador1.setStyle("-fx-text-fill: #E8E8E8; -fx-font-size: 12px;");
            lblContadorLigacoes.setText("1 / 2");
        } else if (msg.contains("cliente ligado como jogador preto")) {
            lblJogador2.setText("●  Pretas — ligado");
            lblJogador2.setStyle("-fx-text-fill: #909090; -fx-font-size: 12px;");
            lblContadorLigacoes.setText("2 / 2");
        } else if (msg.contains("removido") && msg.contains("branco")) {
            lblJogador1.setText("○  A aguardar...");
            lblJogador1.setStyle("-fx-text-fill: #8A8A9A; -fx-font-size: 12px;");
            lblContadorLigacoes.setText("1 / 2");
        } else if (msg.contains("removido") && msg.contains("preto")) {
            lblJogador2.setText("○  A aguardar...");
            lblJogador2.setStyle("-fx-text-fill: #8A8A9A; -fx-font-size: 12px;");
            lblContadorLigacoes.setText("1 / 2");
        }
    }

    private void adicionarLog(String categoria, String mensagem) {
        String hora = LocalTime.now().format(FORMATO_HORA);
        String linha = String.format("[%s] %-8s %s%n", hora, categoria, mensagem);
        Platform.runLater(() -> {
            areaLogs.appendText(linha);
            areaLogs.setScrollTop(Double.MAX_VALUE);
        });
    }
}