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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;

import rede.Cliente;
import rede.MensagemRede;
import rede.PacoteEstadoJogo;
import rede.Servidor;
import modelo.Peca;
import modelo.Tabuleiro;
import modelo.Campo;


public class ClienteMain extends Application {

    private Stage mainStage;
    private Cliente cliente;
    private Servidor servidorLocal;
    private String meuNome;
    private Peca.CorPeca minhaCor;
    private boolean jogoIniciado = false;
    private Integer pontoOrigemSelecionado = null;
    private PacoteEstadoJogo ultimoEstado = null;

    // Componentes do Lobby
    private Label lblNome1;
    private Label lblCor1;
    private Circle circuloCor1;
    private Label lblPronto1;
    private HBox boxJogador2;
    private Button btnStatus;

    // Componentes do Tabuleiro
    private Label lblEstadoTurno;
    private Label lblPlacar;
    private Label lblDado1;
    private Label lblDado2;
    private Button btnLancarDados;
    private Button btnPassarTurno;
    private GridPane boardGrid;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.mainStage = primaryStage;
        primaryStage.setTitle("Gamão - Conectar");

        // Fundo Principal (Bege)
        VBox root = new VBox(30);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #FEF7E8;");

        // Cabeçalho
        VBox boxCabecalho = new VBox(5);
        boxCabecalho.setAlignment(Pos.CENTER);
        Label lblTitulo = new Label("Gamão");
        lblTitulo.setStyle("-fx-text-fill: #703005; -fx-font-size: 45px; -fx-font-weight: bold;");
        Label lblSubtitulo = new Label("Jogo Multijogador em Rede");
        lblSubtitulo.setStyle("-fx-text-fill: #D2691E; -fx-font-size: 16px;");
        boxCabecalho.getChildren().addAll(lblTitulo, lblSubtitulo);

        // Cartão Branco Central (Formulário)
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

        // Segmented Control para selecionar modo (Hospedar vs Entrar)
        boolean[] modoHospedar = {true}; // Usamos array para poder modificar dentro das lambdas

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

        // Estilos
        String estiloAtivo = "-fx-background-color: #8B5A2B; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;";
        String estiloInativo = "-fx-background-color: transparent; -fx-text-fill: #555555; -fx-background-radius: 6; -fx-cursor: hand;";

        btnModoHospedar.setStyle(estiloAtivo);
        btnModoEntrar.setStyle(estiloInativo);

        toggleBar.getChildren().addAll(btnModoHospedar, btnModoEntrar);

        TextField txtMeuNome = new TextField();
        txtMeuNome.setPromptText("Ex: João");
        VBox boxNome = criarBlocoInput("Seu Nome", txtMeuNome);

        TextField txtIp = new TextField("127.0.0.1");
        txtIp.setPromptText("Ex: 192.168.1.100");
        VBox boxIp = criarBlocoInput("IP de Conexão", txtIp);

        // Porta do Servidor
        TextField txtPorta = new TextField("12025");
        txtPorta.setPromptText("Ex: 25565");
        VBox boxPortaBase = criarBlocoInput("Porta", txtPorta);
        Label lblNotaPorta = new Label("* Ambos os jogadores têm de usar a mesma porta.");
        lblNotaPorta.setStyle("-fx-text-fill: #888888; -fx-font-size: 10px; -fx-font-style: italic;");

        VBox grupoPorta = new VBox(2);
        grupoPorta.getChildren().addAll(boxPortaBase, lblNotaPorta);

        Label lblErro = new Label();
        lblErro.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        Button btnConectar = new Button("Criar Sala ->");
        btnConectar.setMaxWidth(Double.MAX_VALUE);
        btnConectar.setPrefHeight(40);
        btnConectar.setStyle("-fx-background-color: #8B5A2B; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");

        // Ações do Toggle
        btnModoHospedar.setOnAction(ev -> {
            modoHospedar[0] = true;
            btnModoHospedar.setStyle(estiloAtivo);
            btnModoEntrar.setStyle(estiloInativo);
            lblTituloCartao.setText("Configurar Nova Sala");
            btnConectar.setText("Criar Sala ->");
            btnConectar.setStyle("-fx-background-color: #8B5A2B; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        });

        btnModoEntrar.setOnAction(ev -> {
            modoHospedar[0] = false;
            btnModoHospedar.setStyle(estiloInativo);
            btnModoEntrar.setStyle(estiloAtivo);
            lblTituloCartao.setText("Ligar a Sala Existente");
            btnConectar.setText("Entrar na Sala ->");
            btnConectar.setStyle("-fx-background-color: #7B8594; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        });

        btnConectar.setOnAction((e) -> {
            String ip = txtIp.getText().trim();
            String nome = txtMeuNome.getText().trim();
            String portaStr = txtPorta.getText().trim();
            if (!ip.isEmpty() && !nome.isEmpty() && !portaStr.isEmpty()) {
                try {
                    int porta = Integer.parseInt(portaStr);
                    this.meuNome = nome;

                    if (modoHospedar[0]) {
                        // Inicia o servidor local
                        try {
                            if (servidorLocal != null) {
                                servidorLocal.encerrarServidor();
                            }
                            servidorLocal = new Servidor(porta);
                            new Thread(() -> {
                                servidorLocal.iniciar();
                            }, "Servidor-Local").start();
                        } catch (Exception ex) {
                            lblErro.setText("Erro ao iniciar o servidor!");
                            return;
                        }
                    }

                    // Inicializa a conexão de rede sockets
                    this.cliente = new Cliente(ip, porta, new Cliente.AtualizadorInterface() {
                        @Override
                        public void atualizarEstado(PacoteEstadoJogo pacote) {
                            ultimoEstado = pacote;
                            if (pacote.getCorAtribuida() != null) {
                                minhaCor = pacote.getCorAtribuida();
                            }

                            if (jogoIniciado) {
                                Platform.runLater(() -> atualizarJogoUI(pacote));
                            } else {
                                atualizarLobbyUI(pacote);
                            }
                        }
                    });

                    // Liga o socket em background thread
                    new Thread(() -> {
                        if (modoHospedar[0]) {
                            try {
                                Thread.sleep(500); // Dá tempo para o Servidor arrancar a escuta
                            } catch (InterruptedException ignored) {}
                        }
                        cliente.ligar();
                        if (cliente.isLigado()) {
                            // Envia o nome inicial para registo no servidor
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
            } else {
                lblErro.setText("Preencha IP, Nome e Porta!");
            }
        });

        cartaoLogin.getChildren().addAll(lblTituloCartao, toggleBar, boxNome, boxIp, grupoPorta, btnConectar, lblErro);

        // Cartão de Instruções
        VBox cartaoAjuda = new VBox(10);
        cartaoAjuda.setMaxWidth(350);
        cartaoAjuda.setPadding(new Insets(20));
        cartaoAjuda.setStyle("-fx-background-color: #F0F7FF; -fx-background-radius: 10; -fx-border-color: #D3E3FD; -fx-border-radius: 10;");
        Label lblAjudaTitulo = new Label("💡 Como jogar");
        lblAjudaTitulo.setStyle("-fx-font-weight: bold; -fx-text-fill: #1A4FA3; -fx-font-size: 14px;");
        Label lblPassos = new Label("1. Inicie o servidor Java\n2. Digite seu nome e o IP\n3. Aguarde o adversário\n4. Comece a jogar!");
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
                    cliente.enviarMensagem(new MensagemRede(MensagemRede.TipoMensagem.DESCONECTAR, meuNome, null, null));
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

    private VBox criarBlocoInput(String titulo, TextField campoTexto) {
        VBox box = new VBox(5);
        Label lbl = new Label(titulo);
        lbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #555;");
        campoTexto.setPrefHeight(35);
        campoTexto.setStyle("-fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-color: #fafafa;");
        box.getChildren().addAll(lbl, campoTexto);
        return box;
    }

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

        Label lblInfoServer = new Label("Servidor:\n" + ipServidor + "\n\nPorta:\n" + portaServidor);
        lblInfoServer.setStyle("-fx-text-alignment: center; -fx-font-size: 14px; -fx-text-fill: #333; -fx-font-weight: bold;");

        // Jogador 1 (Brancas por defeito ou atualizado)
        HBox boxJogador1 = new HBox(15);
        boxJogador1.setAlignment(Pos.CENTER_LEFT);
        boxJogador1.setPadding(new Insets(10, 20, 10, 20));
        boxJogador1.setStyle("-fx-background-color: #E8F5E9; -fx-border-color: #C8E6C9; -fx-border-radius: 8; -fx-background-radius: 8;");

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

        // Jogador 2 (Vazio inicialmente)
        boxJogador2 = new HBox(15);
        boxJogador2.setAlignment(Pos.CENTER);
        boxJogador2.setPadding(new Insets(15));
        boxJogador2.setStyle("-fx-background-color: #FAFAFA; -fx-border-color: #EEEEEE; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label lblAguardando = new Label("↻ A aguardar o jogador 2...");
        lblAguardando.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
        boxJogador2.getChildren().add(lblAguardando);

        btnStatus = new Button("A aguardar a conexão do oponente");
        btnStatus.setMaxWidth(Double.MAX_VALUE);
        btnStatus.setPrefHeight(45);
        btnStatus.setStyle("-fx-background-color: #F0F7FF; -fx-text-fill: #1A4FA3; -fx-border-color: #D3E3FD; -fx-border-radius: 6; -fx-background-radius: 6;");

        cartaoLobby.getChildren().addAll(lblTitulo, lblInfoServer, boxJogador1, boxJogador2, btnStatus);
        root.getChildren().add(cartaoLobby);

        Scene cenaLobby = new Scene(root, 700, 700);
        this.mainStage.setScene(cenaLobby);
        this.mainStage.centerOnScreen();
    }

    private void atualizarLobbyUI(PacoteEstadoJogo pacote) {
        String nomeBranco = pacote.getNomeJogadorBranco();
        String nomePreto = pacote.getNomeJogadorPreto();

        if (nomeBranco != null && !nomeBranco.equals("A aguardar...") &&
            nomePreto != null && !nomePreto.equals("A aguardar...")) {
            
            if (!jogoIniciado) {
                jogoIniciado = true;
                Platform.runLater(() -> mostrarTelaJogo());
            }
            return;
        }

        Platform.runLater(() -> {
            if (nomeBranco != null && !nomeBranco.equals("A aguardar...")) {
                lblNome1.setText(nomeBranco);
                lblCor1.setText("Brancas");
                circuloCor1.setFill(Color.WHITE);
                lblPronto1.setText("PRONTO");
            }

            if (nomePreto != null && !nomePreto.equals("A aguardar...")) {
                boxJogador2.getChildren().clear();
                boxJogador2.setAlignment(Pos.CENTER_LEFT);
                boxJogador2.setPadding(new Insets(10, 20, 10, 20));

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

                boxJogador2.setStyle("-fx-background-color: #E8F5E9; -fx-border-color: #C8E6C9; -fx-border-radius: 8; -fx-background-radius: 8;");
                boxJogador2.getChildren().addAll(circuloCor2, infoJ2, lblPronto2);

                btnStatus.setText("A iniciar a partida...");
            } else {
                boxJogador2.getChildren().clear();
                boxJogador2.setAlignment(Pos.CENTER);
                boxJogador2.setPadding(new Insets(15));
                Label lblAguardando = new Label("↻ A aguardar o jogador 2...");
                lblAguardando.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
                boxJogador2.setStyle("-fx-background-color: #FAFAFA; -fx-border-color: #EEEEEE; -fx-border-radius: 8; -fx-background-radius: 8;");
                boxJogador2.getChildren().add(lblAguardando);

                btnStatus.setText("A aguardar a conexão do oponente");
            }
        });
    }

    private void mostrarTelaJogo() {
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #FEF7E8;");

        lblEstadoTurno = new Label("A iniciar jogo...");
        lblEstadoTurno.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #703005;");

        lblPlacar = new Label("B: 0   vs   P: 0");
        lblPlacar.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #D2691E;");

        HBox topoBox = new HBox(50);
        topoBox.setAlignment(Pos.CENTER);
        topoBox.getChildren().addAll(lblEstadoTurno, lblPlacar);

        boardGrid = new GridPane();
        boardGrid.setAlignment(Pos.CENTER);
        boardGrid.setHgap(3);
        boardGrid.setVgap(10);
        boardGrid.setPadding(new Insets(10));
        boardGrid.setStyle("-fx-background-color: #F4E8C1; -fx-border-color: #703005; -fx-border-width: 3; -fx-border-radius: 8; -fx-background-radius: 8;");

        HBox controlBox = new HBox(20);
        controlBox.setAlignment(Pos.CENTER);

        Label lblDadosTitulo = new Label("Dados:");
        lblDadosTitulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        lblDado1 = new Label("?");
        lblDado1.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-background-color: white; -fx-border-color: #703005; -fx-border-radius: 5; -fx-padding: 5 10 5 10;");

        lblDado2 = new Label("?");
        lblDado2.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-background-color: white; -fx-border-color: #703005; -fx-border-radius: 5; -fx-padding: 5 10 5 10;");

        btnLancarDados = new Button("Lançar Dados");
        btnLancarDados.setStyle("-fx-background-color: #8B5A2B; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 8 15 8 15;");
        btnLancarDados.setOnAction(e -> handleLancarDados());

        btnPassarTurno = new Button("Passar Turno");
        btnPassarTurno.setStyle("-fx-background-color: #7B8594; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 8 15 8 15;");
        btnPassarTurno.setOnAction(e -> handlePassarTurno());

        controlBox.getChildren().addAll(lblDadosTitulo, lblDado1, lblDado2, btnLancarDados, btnPassarTurno);

        Label lblInfoInfo = new Label();
        if (minhaCor == Peca.CorPeca.BRANCO) {
            lblInfoInfo.setText("Você joga com as Brancas (Círculos Brancos).");
        } else {
            lblInfoInfo.setText("Você joga com as Pretas (Círculos Escuros).");
        }
        lblInfoInfo.setStyle("-fx-font-style: italic; -fx-text-fill: #555; -fx-font-size: 12px;");

        root.getChildren().addAll(topoBox, boardGrid, controlBox, lblInfoInfo);

        Scene cenaJogo = new Scene(root, 780, 560);
        this.mainStage.setScene(cenaJogo);
        this.mainStage.setResizable(true);
        this.mainStage.centerOnScreen();

        if (ultimoEstado != null) {
            atualizarJogoUI(ultimoEstado);
        }
    }

    private void atualizarJogoUI(PacoteEstadoJogo pacote) {
        boolean meuTurno = pacote.getNomeJogadorTurno() != null && pacote.getNomeJogadorTurno().equalsIgnoreCase(meuNome);

        lblEstadoTurno.setText(meuTurno ? "★ A sua vez de jogar!" : "Aguarde pela jogada de " + pacote.getNomeJogadorTurno() + "...");
        lblEstadoTurno.setStyle("-fx-text-fill: " + (meuTurno ? "#2E7D32" : "#B71C1C") + ";");

        lblPlacar.setText(String.format("Branco (B): %d   vs   Preto (P): %d", pacote.getPontuacaoBranco(), pacote.getPontuacaoPreto()));

        lblDado1.setText(pacote.getValorDadoUm() > 0 ? String.valueOf(pacote.getValorDadoUm()) : "?");
        lblDado2.setText(pacote.getValorDadoDois() > 0 ? String.valueOf(pacote.getValorDadoDois()) : "?");

        btnLancarDados.setDisable(!meuTurno);
        btnPassarTurno.setDisable(!meuTurno);

        desenharTabuleiro(pacote.getTabuleiroSnapshot());
    }

    private void desenharTabuleiro(Tabuleiro tabuleiro) {
        boardGrid.getChildren().clear();

        for (int i = 0; i < 12; i++) {
            // Superior (Casas 13 a 24)
            int casaSuperiorId = 13 + i;
            StackPane stackSuperior = criarCelulaCasa(casaSuperiorId, tabuleiro.getCampo(casaSuperiorId), false);
            boardGrid.add(stackSuperior, i, 0);

            // Inferior (Casas 1 a 12)
            int casaInferiorId = i + 1;
            StackPane stackInferior = criarCelulaCasa(casaInferiorId, tabuleiro.getCampo(casaInferiorId), true);
            boardGrid.add(stackInferior, i, 1);
        }
    }

    private StackPane criarCelulaCasa(int id, Campo campo, boolean apontaParaCima) {
        StackPane cell = new StackPane();
        cell.setPrefSize(52, 180);

        if (pontoOrigemSelecionado != null && pontoOrigemSelecionado == id) {
            cell.setStyle("-fx-border-color: #FFD700; -fx-border-width: 3; -fx-border-radius: 4; -fx-background-color: rgba(255, 215, 0, 0.2);");
        } else {
            cell.setStyle("-fx-border-color: #703005; -fx-border-width: 1; -fx-border-radius: 4;");
        }

        Polygon triangulo = new Polygon();
        double width = 50;
        double height = 178;

        if (apontaParaCima) {
            triangulo.getPoints().addAll(
                    width / 2.0, 5.0,
                    2.0, height - 5.0,
                    width - 2.0, height - 5.0
            );
        } else {
            triangulo.getPoints().addAll(
                    2.0, 5.0,
                    width - 2.0, 5.0,
                    width / 2.0, height - 5.0
            );
        }

        Color corTriangulo = (id % 2 == 0) ? Color.web("#D2B48C") : Color.web("#8B5A2B");
        triangulo.setFill(corTriangulo);
        triangulo.setStroke(Color.web("#703005"));
        triangulo.setStrokeWidth(1);

        cell.getChildren().add(triangulo);

        VBox vboxPecas = new VBox(-10);
        vboxPecas.setAlignment(apontaParaCima ? Pos.BOTTOM_CENTER : Pos.TOP_CENTER);
        vboxPecas.setPadding(new Insets(5, 0, 5, 0));

        int totalPecas = campo.getQuantidadePecas();
        Peca.CorPeca corD = campo.getCorDominante();

        int pecasAMostrar = Math.min(totalPecas, 5);
        for (int p = 0; p < pecasAMostrar; p++) {
            Circle circuloPeca = new Circle(11);
            if (corD == Peca.CorPeca.BRANCO) {
                circuloPeca.setFill(Color.WHITE);
                circuloPeca.setStroke(Color.LIGHTGRAY);
            } else {
                circuloPeca.setFill(Color.web("#1A202C"));
                circuloPeca.setStroke(Color.web("#4A5568"));
            }
            circuloPeca.setStrokeWidth(2);

            DropShadow ds = new DropShadow(2, Color.rgb(0, 0, 0, 0.4));
            circuloPeca.setEffect(ds);

            if (p == pecasAMostrar - 1 && totalPecas > 5) {
                StackPane stackMaisPeca = new StackPane();
                Label lblMais = new Label("+" + (totalPecas - 4));
                lblMais.setStyle("-fx-text-fill: " + (corD == Peca.CorPeca.BRANCO ? "black" : "white") + "; -fx-font-size: 9px; -fx-font-weight: bold;");
                stackMaisPeca.getChildren().addAll(circuloPeca, lblMais);
                vboxPecas.getChildren().add(stackMaisPeca);
            } else {
                vboxPecas.getChildren().add(circuloPeca);
            }
        }

        cell.getChildren().add(vboxPecas);

        cell.setOnMouseClicked(e -> tratarCliqueCasa(id, campo));

        return cell;
    }

    private void tratarCliqueCasa(int id, Campo campo) {
        boolean meuTurno = ultimoEstado != null && ultimoEstado.getNomeJogadorTurno() != null && ultimoEstado.getNomeJogadorTurno().equalsIgnoreCase(meuNome);
        if (!meuTurno) {
            return;
        }

        if (pontoOrigemSelecionado == null) {
            if (campo.isVazio() || campo.getCorDominante() != minhaCor) {
                return;
            }
            pontoOrigemSelecionado = id;
            desenharTabuleiro(ultimoEstado.getTabuleiroSnapshot());
        } else {
            int origem = pontoOrigemSelecionado;
            int destino = id;
            pontoOrigemSelecionado = null;

            if (origem == destino) {
                desenharTabuleiro(ultimoEstado.getTabuleiroSnapshot());
                return;
            }

            new Thread(() -> {
                cliente.enviarMensagem(new MensagemRede(
                        MensagemRede.TipoMensagem.MOVER_PECA,
                        meuNome,
                        origem,
                        destino
                ));
            }).start();
        }
    }

    private void handleLancarDados() {
        new Thread(() -> {
            cliente.enviarMensagem(new MensagemRede(
                    MensagemRede.TipoMensagem.LANCAR_DADOS,
                    meuNome,
                    null,
                    null
            ));
        }).start();
    }

    private void handlePassarTurno() {
        new Thread(() -> {
            cliente.enviarMensagem(new MensagemRede(
                    MensagemRede.TipoMensagem.PASSAR_TURNO,
                    meuNome,
                    null,
                    null
            ));
        }).start();
    }
}