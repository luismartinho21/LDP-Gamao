package ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.scene.control.CheckBox;

import rede.Cliente;
import rede.MensagemRede;
import rede.PacoteEstadoJogo;
import rede.Servidor;
import modelo.Peca;

/**
 * Ponto de entrada JavaFX para a aplicacao cliente do Gamao.
 * 
 * Responsabilidades:
 * 1. Apresentar o ecra inicial de formulado de ligacao (IP, Porta, Nome e tipo de ligacao).
 * 2. Gerir o lobby/sala de espera até ligacao de ambos os jogadores.
 * 3. Delegar o controlo grafico da partida para a classe {@link TelaJogoController}.
 */
public class ClienteMain extends Application {

    // ── Dependências de rede ───────────────────────────────────────────────
    private Stage mainStage;
    private Cliente cliente;
    private Servidor servidorLocal;

    // ── Estado do jogador local ────────────────────────────────────────────
    private String meuNome;
    private Peca.CorPeca minhaCor;
    private boolean jogoIniciado = false;
    private PacoteEstadoJogo ultimoEstado = null;

    // ── Controlador da tela de jogo (criado quando o jogo arranca) ─────────
    private TelaJogoController telaJogo;
    private CheckBox chkCarregarAnterior;

    // ── Componentes do Lobby (necessários para atualizarLobbyUI) ──────────
    private Label lblNome1;
    private Label lblCor1;
    private Circle circuloCor1;
    private Label lblPronto1;
    private HBox boxJogador2;
    private Button btnStatus;

    // ──────────────────────────────────────────────────────────────────────

    /**
     * Ponto de entrada do cliente JavaFX.
     * 
     * @param args Os argumentos da linha de comandos
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Inicializa a interface do formulário de conexao/lobby.
     * 
     * @param primaryStage O palco principal da interface JavaFX
     */
    @Override
    public void start(Stage primaryStage) {
        this.mainStage = primaryStage;
        primaryStage.setTitle("Gamão - Conectar");

        VBox root = new VBox(30);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #FEF7E8;");

        // ── Cabeçalho ──────────────────────────────────────────────────────
        VBox boxCabecalho = new VBox(5);
        boxCabecalho.setAlignment(Pos.CENTER);
        Label lblTitulo = new Label("Gamão");
        lblTitulo.setStyle("-fx-text-fill: #703005; -fx-font-size: 45px; -fx-font-weight: bold;");
        Label lblSubtitulo = new Label("Jogo Multijogador em Rede");
        lblSubtitulo.setStyle("-fx-text-fill: #D2691E; -fx-font-size: 16px;");
        boxCabecalho.getChildren().addAll(lblTitulo, lblSubtitulo);

        // ── Cartão de formulário ───────────────────────────────────────────
        VBox cartaoLogin = new VBox(15);
        cartaoLogin.setMaxWidth(350);
        cartaoLogin.setPadding(new Insets(30));
        cartaoLogin.setStyle("-fx-background-color: white; -fx-background-radius: 12;");

        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.rgb(0, 0, 0, 0.1));
        sombra.setRadius(15);
        sombra.setOffsetY(5);
        cartaoLogin.setEffect(sombra);

        Label lblTituloCartao = new Label("Configurar Nova Sala");
        lblTituloCartao.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");

        // Toggle Hospedar / Entrar
        boolean[] modoHospedar = {true};

        HBox toggleBar = new HBox(10);
        toggleBar.setAlignment(Pos.CENTER);
        toggleBar.setStyle("-fx-background-color: #F3F4F6; -fx-padding: 5; -fx-background-radius: 8;");

        Button btnModoHospedar = new Button("Hospedar");
        btnModoHospedar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnModoHospedar, javafx.scene.layout.Priority.ALWAYS);
        btnModoHospedar.setPrefHeight(30);

        Button btnModoEntrar = new Button("Entrar");
        btnModoEntrar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnModoEntrar, javafx.scene.layout.Priority.ALWAYS);
        btnModoEntrar.setPrefHeight(30);

        String estiloAtivo   = "-fx-background-color: #8B5A2B; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;";
        String estiloInativo = "-fx-background-color: transparent; -fx-text-fill: #555555; "
                + "-fx-background-radius: 6; -fx-cursor: hand;";

        btnModoHospedar.setStyle(estiloAtivo);
        btnModoEntrar.setStyle(estiloInativo);
        toggleBar.getChildren().addAll(btnModoHospedar, btnModoEntrar);

        // Campos de entrada
        TextField txtMeuNome = new TextField();
        txtMeuNome.setPromptText("Ex: João");
        VBox boxNome = criarBlocoInput("Seu Nome", txtMeuNome);

        TextField txtIp = new TextField("127.0.0.1");
        txtIp.setPromptText("Ex: 192.168.1.100");
        VBox boxIp = criarBlocoInput("IP de Conexão", txtIp);

        TextField txtPorta = new TextField("12025");
        txtPorta.setPromptText("Ex: 25565");
        VBox boxPortaBase = criarBlocoInput("Porta", txtPorta);
        Label lblNotaPorta = new Label("* Ambos os jogadores têm de usar a mesma porta.");
        lblNotaPorta.setStyle("-fx-text-fill: #888888; -fx-font-size: 10px; -fx-font-style: italic;");
        VBox grupoPorta = new VBox(2);
        grupoPorta.getChildren().addAll(boxPortaBase, lblNotaPorta);

        Label lblErro = new Label();
        lblErro.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        chkCarregarAnterior = new CheckBox("Carregar jogo guardado anterior");
        chkCarregarAnterior.setStyle("-fx-text-fill: #555555; -fx-font-size: 12px; -fx-cursor: hand;");

        Button btnConectar = new Button("Criar Sala ->");
        btnConectar.setMaxWidth(Double.MAX_VALUE);
        btnConectar.setPrefHeight(40);
        btnConectar.setStyle("-fx-background-color: #8B5A2B; -fx-text-fill: white; "
                + "-fx-font-size: 14px; -fx-font-weight: bold; "
                + "-fx-background-radius: 8; -fx-cursor: hand;");

        // Ações do toggle
        btnModoHospedar.setOnAction(ev -> {
            modoHospedar[0] = true;
            btnModoHospedar.setStyle(estiloAtivo);
            btnModoEntrar.setStyle(estiloInativo);
            lblTituloCartao.setText("Configurar Nova Sala");
            btnConectar.setText("Criar Sala ->");
            btnConectar.setStyle("-fx-background-color: #8B5A2B; -fx-text-fill: white; "
                    + "-fx-font-size: 14px; -fx-font-weight: bold; "
                    + "-fx-background-radius: 8; -fx-cursor: hand;");
            chkCarregarAnterior.setVisible(true);
            chkCarregarAnterior.setManaged(true);
        });

        btnModoEntrar.setOnAction(ev -> {
            modoHospedar[0] = false;
            btnModoHospedar.setStyle(estiloInativo);
            btnModoEntrar.setStyle(estiloAtivo);
            lblTituloCartao.setText("Ligar a Sala Existente");
            btnConectar.setText("Entrar na Sala ->");
            btnConectar.setStyle("-fx-background-color: #7B8594; -fx-text-fill: white; "
                    + "-fx-font-size: 14px; -fx-font-weight: bold; "
                    + "-fx-background-radius: 8; -fx-cursor: hand;");
            chkCarregarAnterior.setVisible(false);
            chkCarregarAnterior.setManaged(false);
        });

        // Ação do botão principal de ligação
        btnConectar.setOnAction(e -> {
            String ip     = txtIp.getText().trim();
            String nome   = txtMeuNome.getText().trim();
            String portaStr = txtPorta.getText().trim();

            if (ip.isEmpty() || nome.isEmpty() || portaStr.isEmpty()) {
                lblErro.setText("Preencha IP, Nome e Porta!");
                return;
            }

            try {
                int porta = Integer.parseInt(portaStr);
                this.meuNome = nome;

                // Se for o anfitrião, arranca o servidor localmente
                if (modoHospedar[0]) {
                    try {
                        if (servidorLocal != null) {
                            servidorLocal.encerrarServidor();
                        }
                        servidorLocal = new Servidor(porta);
                        if (chkCarregarAnterior.isSelected()) {
                            servidorLocal.carregarJogo("jogo_salvo.dat");
                        }
                        new Thread(servidorLocal::iniciar, "Servidor-Local").start();
                    } catch (Exception ex) {
                        lblErro.setText("Erro ao iniciar o servidor!");
                        return;
                    }
                }

                // Cria o cliente com o callback de atualização de estado
                this.cliente = new Cliente(ip, porta, pacote -> {
                    ultimoEstado = pacote;
                    if (pacote.getCorAtribuida() != null) {
                        minhaCor = pacote.getCorAtribuida();
                    }
                    if (jogoIniciado) {
                        Platform.runLater(() -> atualizarJogoUI(pacote));
                    } else {
                        atualizarLobbyUI(pacote);
                    }
                });

                // Liga o socket em background
                new Thread(() -> {
                    if (modoHospedar[0]) {
                        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                    }
                    cliente.ligar();
                    if (cliente.isLigado()) {
                        cliente.enviarMensagem(new MensagemRede(null, nome, null, null));
                    } else {
                        Platform.runLater(() -> {
                            lblErro.setText("Não foi possível ligar ao Servidor!");
                            if (servidorLocal != null) {
                                servidorLocal.encerrarServidor();
                                servidorLocal = null;
                            }
                        });
                    }
                }).start();

                mostrarSalaEspera(ip, portaStr, nome);

            } catch (NumberFormatException ex) {
                lblErro.setText("Porta inválida!");
            }
        });

        cartaoLogin.getChildren().addAll(
                lblTituloCartao, toggleBar, boxNome, boxIp, grupoPorta, chkCarregarAnterior, btnConectar, lblErro);

        // ── Cartão de ajuda ────────────────────────────────────────────────
        VBox cartaoAjuda = new VBox(10);
        cartaoAjuda.setMaxWidth(350);
        cartaoAjuda.setPadding(new Insets(20));
        cartaoAjuda.setStyle("-fx-background-color: #F0F7FF; -fx-background-radius: 10; "
                + "-fx-border-color: #D3E3FD; -fx-border-radius: 10;");
        Label lblAjudaTitulo = new Label("💡 Como jogar");
        lblAjudaTitulo.setStyle("-fx-font-weight: bold; -fx-text-fill: #1A4FA3; -fx-font-size: 14px;");
        Label lblPassos = new Label(
                "1. Inicie o servidor Java\n"
                        + "2. Digite seu nome e o IP\n"
                        + "3. Aguarde o adversário\n"
                        + "4. Comece a jogar!");
        lblPassos.setStyle("-fx-text-fill: #1A4FA3; -fx-font-size: 12px;");
        lblPassos.setWrapText(true);
        lblPassos.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        cartaoAjuda.getChildren().addAll(lblAjudaTitulo, lblPassos);

        root.getChildren().addAll(boxCabecalho, cartaoLogin, cartaoAjuda);

        Scene scene = new Scene(root, 700, 750);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> {
            if (cliente != null) {
                new Thread(() -> {
                    cliente.enviarMensagem(
                            new MensagemRede(MensagemRede.TipoMensagem.DESCONECTAR, meuNome, null, null));
                    cliente.fecharLigacao();
                }).start();
            }
            if (servidorLocal != null) {
                servidorLocal.encerrarServidor();
            }
            Platform.exit();
            System.exit(0);
        });
    }

    // ── Sala de espera (Lobby) ─────────────────────────────────────────────

    /**
     * Constrói e exibe a interface visual da sala de espera (lobby). Mostra as informações
     * de rede da ligação e cria os blocos informativos dos jogadores (atribuição de cores,
     * nomes e status de prontidão).
     * 
     * @param ipServidor O endereço IP do servidor ligado
     * @param portaServidor A porta TCP de escuta do servidor
     * @param nomeJogador O nome do jogador local conectado
     */
    private void mostrarSalaEspera(String ipServidor, String portaServidor, String nomeJogador) {
        VBox root = new VBox(30);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #FEF7E8;");

        VBox cartaoLobby = new VBox(20);
        cartaoLobby.setAlignment(Pos.TOP_CENTER);
        cartaoLobby.setMaxWidth(400);
        cartaoLobby.setPadding(new Insets(30));
        cartaoLobby.setStyle("-fx-background-color: white; -fx-background-radius: 12;");

        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.rgb(0, 0, 0, 0.1));
        sombra.setRadius(15);
        sombra.setOffsetY(5);
        cartaoLobby.setEffect(sombra);

        Label lblTitulo = new Label("Sala de Jogo");
        lblTitulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #111;");

        Label lblInfoServer = new Label(
                "Servidor:\n" + ipServidor + "\n\nPorta:\n" + portaServidor);
        lblInfoServer.setStyle(
                "-fx-text-alignment: center; -fx-font-size: 14px; "
                        + "-fx-text-fill: #333; -fx-font-weight: bold;");

        // Jogador 1
        HBox boxJogador1 = new HBox(15);
        boxJogador1.setAlignment(Pos.CENTER_LEFT);
        boxJogador1.setPadding(new Insets(10, 20, 10, 20));
        boxJogador1.setStyle("-fx-background-color: #E8F5E9; -fx-border-color: #C8E6C9; "
                + "-fx-border-radius: 8; -fx-background-radius: 8;");

        circuloCor1 = new Circle(15);
        circuloCor1.setFill(Color.WHITE);
        circuloCor1.setStroke(Color.LIGHTGRAY);

        VBox infoJ1 = new VBox(2);
        lblNome1 = new Label(nomeJogador);
        lblNome1.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        lblCor1 = new Label("A determinar...");
        lblCor1.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
        infoJ1.getChildren().addAll(lblNome1, lblCor1);

        lblPronto1 = new Label("CONECTADO");
        lblPronto1.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold; -fx-font-size: 12px;");
        HBox.setMargin(lblPronto1, new Insets(0, 0, 0, 50));
        boxJogador1.getChildren().addAll(circuloCor1, infoJ1, lblPronto1);

        // Jogador 2 (vazio até ligar)
        boxJogador2 = new HBox(15);
        boxJogador2.setAlignment(Pos.CENTER);
        boxJogador2.setPadding(new Insets(15));
        boxJogador2.setStyle("-fx-background-color: #FAFAFA; -fx-border-color: #EEEEEE; "
                + "-fx-border-radius: 8; -fx-background-radius: 8;");
        Label lblAguardando = new Label("↻ A aguardar o jogador 2...");
        lblAguardando.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
        boxJogador2.getChildren().add(lblAguardando);

        btnStatus = new Button("A aguardar a conexão do oponente");
        btnStatus.setMaxWidth(Double.MAX_VALUE);
        btnStatus.setPrefHeight(45);
        btnStatus.setStyle("-fx-background-color: #F0F7FF; -fx-text-fill: #1A4FA3; "
                + "-fx-border-color: #D3E3FD; -fx-border-radius: 6; -fx-background-radius: 6;");

        cartaoLobby.getChildren().addAll(
                lblTitulo, lblInfoServer, boxJogador1, boxJogador2, btnStatus);
        root.getChildren().add(cartaoLobby);

        Platform.runLater(() -> {
            this.mainStage.setScene(new Scene(root, 700, 700));
            this.mainStage.centerOnScreen();
        });
    }

    // ── Atualização do Lobby ───────────────────────────────────────────────

    /**
     * Atualiza as informações dos jogadores no lobby visual a partir de um pacote
     * de estado recebido do servidor. Se ambos os jogadores estiverem conectados,
     * inicia automaticamente a transição para a tela de jogo.
     * 
     * @param pacote O estado de jogo sincronizado contendo os nomes dos jogadores conectados
     */
    private void atualizarLobbyUI(PacoteEstadoJogo pacote) {
        String nomeBranco = pacote.getNomeJogadorBranco();
        String nomePreto  = pacote.getNomeJogadorPreto();

        boolean brancoPresente = nomeBranco != null && !nomeBranco.equals("A aguardar...");
        boolean pretoPresente  = nomePreto  != null && !nomePreto.equals("A aguardar...");

        // Ambos ligados logo arranca o jogo
        if (brancoPresente && pretoPresente && !jogoIniciado) {
            jogoIniciado = true;
            Platform.runLater(this::mostrarTelaJogo);
            return;
        }

        Platform.runLater(() -> {
            if (brancoPresente) {
                lblNome1.setText(nomeBranco);
                lblCor1.setText("Brancas");
                circuloCor1.setFill(Color.WHITE);
                lblPronto1.setText("PRONTO");
            }

            if (pretoPresente) {
                boxJogador2.getChildren().clear();
                boxJogador2.setAlignment(Pos.CENTER_LEFT);
                boxJogador2.setPadding(new Insets(10, 20, 10, 20));
                boxJogador2.setStyle("-fx-background-color: #E8F5E9; -fx-border-color: #C8E6C9; "
                        + "-fx-border-radius: 8; -fx-background-radius: 8;");

                Circle circuloCor2 = new Circle(15);
                circuloCor2.setFill(Color.web("#111827"));
                circuloCor2.setStroke(Color.LIGHTGRAY);

                VBox infoJ2 = new VBox(2);
                Label lblNome2 = new Label(nomePreto);
                lblNome2.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                Label lblCor2 = new Label("Pretas");
                lblCor2.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
                infoJ2.getChildren().addAll(lblNome2, lblCor2);

                Label lblPronto2 = new Label("PRONTO");
                lblPronto2.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold; -fx-font-size: 12px;");
                HBox.setMargin(lblPronto2, new Insets(0, 0, 0, 50));

                boxJogador2.getChildren().addAll(circuloCor2, infoJ2, lblPronto2);
                btnStatus.setText("A iniciar a partida...");

            } else {
                boxJogador2.getChildren().clear();
                boxJogador2.setAlignment(Pos.CENTER);
                boxJogador2.setPadding(new Insets(15));
                boxJogador2.setStyle("-fx-background-color: #FAFAFA; -fx-border-color: #EEEEEE; "
                        + "-fx-border-radius: 8; -fx-background-radius: 8;");
                Label lbl = new Label("↻ A aguardar o jogador 2...");
                lbl.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
                boxJogador2.getChildren().add(lbl);
                btnStatus.setText("A aguardar a conexão do oponente");
            }
        });
    }

    // ── Transição para o jogo ──────────────────────────────────────────────

    /**
     * Inicializa o controlador da tela de jogo {@link TelaJogoController} e exibe
     * a interface gráfica do tabuleiro de jogo no palco principal (Stage).
     * Caso exista um estado de jogo prévio, atualiza a interface de imediato.
     */
    private void mostrarTelaJogo() {
        telaJogo = new TelaJogoController(mainStage, cliente, meuNome, minhaCor);
        telaJogo.mostrar();

        if (ultimoEstado != null) {
            telaJogo.atualizar(ultimoEstado);
        }
    }

    /**
     * Encaminha o novo estado do jogo recebido da rede para o controlador ativo
     * da partida para que os elementos visuais sejam redesenhados.
     *
     * @param pacote O pacote de estado do jogo atualizado a ser processado
     */
    private void atualizarJogoUI(PacoteEstadoJogo pacote) {
        if (telaJogo != null) {
            telaJogo.atualizar(pacote);
        }
    }

    // ── Utilitário de formulário ───────────────────────────────────────────

    private VBox criarBlocoInput(String titulo, TextField campoTexto) {
        VBox box = new VBox(5);
        Label lbl = new Label(titulo);
        lbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #555;");
        campoTexto.setPrefHeight(35);
        campoTexto.setStyle("-fx-background-radius: 5; -fx-border-color: #ddd; "
                + "-fx-border-radius: 5; -fx-background-color: #fafafa;");
        box.getChildren().addAll(lbl, campoTexto);
        return box;
    }
}