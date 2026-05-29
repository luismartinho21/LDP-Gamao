package ui;

import javafx.application.Application;
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

/**
 * Classe principal da Interface Gráfica do Cliente.
 * Responsável por gerir a transição de ecrãs (Login, Lobby e Jogo)
 * e recolher os dados iniciais de conexão do utilizador.
 */
public class ClienteMain extends Application {

    private Stage mainStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.mainStage = primaryStage;
        primaryStage.setTitle("Gamão - Conectar");

        // 1. Fundo Principal (Bege)
        VBox root = new VBox(30);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #FEF7E8;");

        // 2. Cabeçalho
        VBox boxCabecalho = new VBox(5);
        boxCabecalho.setAlignment(Pos.CENTER);
        Label lblTitulo = new Label("Gamão");
        lblTitulo.setStyle("-fx-text-fill: #703005; -fx-font-size: 45px; -fx-font-weight: bold;");
        Label lblSubtitulo = new Label("Jogo Multijogador em Rede");
        lblSubtitulo.setStyle("-fx-text-fill: #D2691E; -fx-font-size: 16px;");
        boxCabecalho.getChildren().addAll(lblTitulo, lblSubtitulo);

        // 3. Cartão Branco Central (Formulário)
        VBox cartaoLogin = new VBox(15);
        cartaoLogin.setMaxWidth(350);
        cartaoLogin.setPadding(new Insets(30));
        cartaoLogin.setStyle("-fx-background-color: white; -fx-background-radius: 12;");

        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.rgb(0, 0, 0, 0.1));
        sombra.setRadius(15);
        sombra.setOffsetY(5);
        cartaoLogin.setEffect(sombra);

        Label lblTituloCartao = new Label("Conectar ao Servidor");
        lblTituloCartao.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");

        TextField txtMeuNome = new TextField();
        txtMeuNome.setPromptText("Ex: João");
        VBox boxNome = criarBlocoInput("Seu Nome", txtMeuNome);

        TextField txtIp = new TextField("127.0.0.1");
        txtIp.setPromptText("Ex: 192.168.1.100");
        VBox boxIp = criarBlocoInput("IP do Servidor", txtIp);

        // Porta do Servidor
        TextField txtPorta = new TextField("12025");
        txtPorta.setPromptText("Ex: 25565");
        VBox boxPortaBase = criarBlocoInput("Insira a porta desejada para o servidor:", txtPorta);
        Label lblNotaPorta = new Label("* O 2º jogador tem de usar a mesma porta para a conexão.");
        lblNotaPorta.setStyle("-fx-text-fill: #888888; -fx-font-size: 10px; -fx-font-style: italic;");

        // Caixa da porta e nota pequena
        VBox grupoPorta = new VBox(2); // 2 pixels de espaçamento
        grupoPorta.getChildren().addAll(boxPortaBase, lblNotaPorta);

        Label lblErro = new Label();
        lblErro.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        Button btnConectar = new Button("Conectar ->");
        btnConectar.setMaxWidth(Double.MAX_VALUE);
        btnConectar.setPrefHeight(40);
        btnConectar.setStyle("-fx-background-color: #7B8594; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");

        // Lógica puramente visual
        btnConectar.setOnAction((e) -> {
            String ip = txtIp.getText().trim();
            String nome = txtMeuNome.getText().trim();
            if (!ip.isEmpty() && !nome.isEmpty()) {
                // Como ainda não foi desenvolvido a rede, avançamos direto para a Sala de Espera para testar o design
                mostrarSalaEspera(ip, nome, true);
            } else {
                lblErro.setText("Preencha IP e o Nome!");
            }
        });

        cartaoLogin.getChildren().addAll(lblTituloCartao, boxNome, boxIp, grupoPorta, btnConectar, lblErro);

        // 4. Cartão de Instruções
        VBox cartaoAjuda = new VBox(10);
        cartaoAjuda.setMaxWidth(350);
        cartaoAjuda.setPadding(new Insets(20));
        cartaoAjuda.setStyle("-fx-background-color: #F0F7FF; -fx-background-radius: 10; -fx-border-color: #D3E3FD; -fx-border-radius: 10;");
        Label lblAjudaTitulo = new Label("\uD83D\uDCA1 Como jogar");
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
    }
    /**
     * Método auxiliar para criar rapidamente blocos de texto estilizados.
     * * @param titulo O texto que aparece por cima da caixa
     * @param campoTexto O TextField que será encapsulado
     * @return Um VBox que contem a Label e o TextField formatados
     */
    private VBox criarBlocoInput(String titulo, TextField campoTexto) {
        VBox box = new VBox(5);
        Label lbl = new Label(titulo);
        lbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #555;");
        campoTexto.setPrefHeight(35);
        campoTexto.setStyle("-fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-color: #fafafa;");
        box.getChildren().addAll(lbl, campoTexto);
        return box;
    }
    /**
     * Substitui o ecrã atual pela Sala de Espera (Lobby).
     * Nota: Atualmente contém lógica simulada para efeitos de design.
     * * @param ipServidor IP introduzido pelo utilizador
     * @param meuNome Nome escolhido pelo utilizador
     * @param isBrancas Define a cor da peça do jogador (temporário)
     */
    private void mostrarSalaEspera(String ipServidor, String meuNome, boolean isBrancas) {
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

        Label lblInfoServer = new Label("Servidor:\n" + ipServidor + "\n\nPorta:\n12025");
        lblInfoServer.setStyle("-fx-text-alignment: center; -fx-font-size: 14px; -fx-text-fill: #333; -fx-font-weight: bold;");

        HBox boxJogador1 = new HBox(15);
        boxJogador1.setAlignment(Pos.CENTER_LEFT);
        boxJogador1.setPadding(new Insets(10, 20, 10, 20));
        boxJogador1.setStyle("-fx-background-color: #E8F5E9; -fx-border-color: #C8E6C9; -fx-border-radius: 8; -fx-background-radius: 8;");

        Circle circuloCor1 = new Circle(15);
        circuloCor1.setFill(isBrancas ? Color.WHITE : Color.web("#111827"));
        circuloCor1.setStroke(Color.LIGHTGRAY);

        VBox infoJ1 = new VBox(2);
        Label lblNome1 = new Label(meuNome);
        lblNome1.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label lblCor1 = new Label(isBrancas ? "Brancas" : "Pretas");
        lblCor1.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
        infoJ1.getChildren().addAll(lblNome1, lblCor1);

        Label lblPronto1 = new Label("PRONTO");
        lblPronto1.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold; -fx-font-size: 12px;");
        HBox.setMargin(lblPronto1, new Insets(0, 0, 0, 50));

        boxJogador1.getChildren().addAll(circuloCor1, infoJ1, lblPronto1);

        HBox boxJogador2 = new HBox(15);
        boxJogador2.setAlignment(Pos.CENTER);
        boxJogador2.setPadding(new Insets(15));
        boxJogador2.setStyle("-fx-background-color: #FAFAFA; -fx-border-color: #EEEEEE; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label lblAguardando = new Label("↻ A aguardar o jogador 2...");
        lblAguardando.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
        boxJogador2.getChildren().add(lblAguardando);

        Button btnStatus = new Button("A aguardar a conexão do oponente");
        btnStatus.setMaxWidth(Double.MAX_VALUE);
        btnStatus.setPrefHeight(45);
        btnStatus.setStyle("-fx-background-color: #F0F7FF; -fx-text-fill: #1A4FA3; -fx-border-color: #D3E3FD; -fx-border-radius: 6; -fx-background-radius: 6;");

        cartaoLobby.getChildren().addAll(lblTitulo, lblInfoServer, boxJogador1, boxJogador2, btnStatus);
        root.getChildren().add(cartaoLobby);

        Scene cenaLobby = new Scene(root, 700, 700);
        this.mainStage.setScene(cenaLobby);
    }
}