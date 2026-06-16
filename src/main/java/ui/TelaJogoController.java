package ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import modelo.Campo;
import modelo.Peca;
import modelo.Tabuleiro;
import rede.Cliente;
import rede.MensagemRede;
import rede.PacoteEstadoJogo;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.List;

/**
 * TelaJogoController — responsável por toda a interface gráfica da partida.
 *
 * Recebe o estado do jogo via PacoteEstadoJogo enviado pelo Servidor e
 * envia as ações do jogador (lançar dados, mover peça, passar turno) de
 * volta ao Servidor através do Cliente de rede.
 *
 * Esta classe NÃO conhece a lógica do jogo — toda a validação é feita
 * no Servidor. Aqui apenas se apresenta o estado e recolhe-se o input.
 */
public class TelaJogoController {

    // ── Dependências injetadas ─────────────────────────────────────────────
    private final Stage stage;
    private final Cliente cliente;
    private final String meuNome;
    private final Peca.CorPeca minhaCor;

    // ── Estado local (leitura apenas) ──────────────────────────────────────
    private PacoteEstadoJogo ultimoEstado;
    private Integer origemSelecionada = null;

    // ── Componentes do tabuleiro ───────────────────────────────────────────
    private Label lblTurno;
    private Label lblPlacar;
    private Label lblDado1;
    private Label lblDado2;
    private Label lblMovimentos;
    private Button btnLancarDados;
    private Button btnPassarTurno;
    private Button btnRetirarPeca;
    private GridPane boardGrid;
    private Label lblAvisoBarra;
    private Label lblPecasBrancoFora;
    private Label lblPecasPretoFora;

    // ── Estilos reutilizados ───────────────────────────────────────────────
    private static final String ESTILO_DADO =
            "-fx-font-size: 22px; -fx-font-weight: bold; "
                    + "-fx-background-color: white; -fx-border-color: #703005; "
                    + "-fx-border-radius: 6; -fx-background-radius: 6; "
                    + "-fx-padding: 6 14 6 14; -fx-min-width: 46px; -fx-alignment: center;";

    private static final String ESTILO_BTN_PRIMARIO =
            "-fx-background-color: #8B5A2B; -fx-text-fill: white; "
                    + "-fx-font-weight: bold; -fx-background-radius: 6; "
                    + "-fx-cursor: hand; -fx-padding: 8 18 8 18;";

    private static final String ESTILO_BTN_SECUNDARIO =
            "-fx-background-color: #7B8594; -fx-text-fill: white; "
                    + "-fx-font-weight: bold; -fx-background-radius: 6; "
                    + "-fx-cursor: hand; -fx-padding: 8 18 8 18;";

    private static final String ESTILO_BTN_DESATIVADO =
            "-fx-background-color: #CCCCCC; -fx-text-fill: #888888; "
                    + "-fx-font-weight: bold; -fx-background-radius: 6; "
                    + "-fx-padding: 8 18 8 18;";

    // ── Construtor ─────────────────────────────────────────────────────────

    /**
     * @param stage     Stage JavaFX onde a cena do jogo será mostrada
     * @param cliente   Cliente de rede para envio de mensagens ao Servidor
     * @param meuNome   Nome do jogador local (para distinguir de quem é o turno)
     * @param minhaCor  Cor atribuída pelo Servidor a este jogador
     */
    public TelaJogoController(Stage stage, Cliente cliente, String meuNome, Peca.CorPeca minhaCor) {
        this.stage = stage;
        this.cliente = cliente;
        this.meuNome = meuNome;
        this.minhaCor = minhaCor;
    }

    // ── Montagem da cena ───────────────────────────────────────────────────

    /**
     * Constrói e apresenta a cena do jogo no Stage.
     * Deve ser chamado na JavaFX Application Thread.
     */
    public void mostrar() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(16));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #FEF7E8;");

        root.getChildren().addAll(
                criarBarraTopo(),
                criarTabuleiro(),
                criarAviso(),
                criarBarraControlos(),
                criarRodape()
        );

        Scene cena = new Scene(root, 820, 580);
        stage.setScene(cena);
        stage.setResizable(true);
        stage.setMinWidth(780);
        stage.setMinHeight(540);
        stage.centerOnScreen();
    }

    // ── Barra de topo (turno + placar) ─────────────────────────────────────

    private HBox criarBarraTopo() {
        HBox barra = new HBox(40);
        barra.setAlignment(Pos.CENTER);
        barra.setPadding(new Insets(0, 0, 4, 0));

        lblTurno = new Label("A iniciar...");
        lblTurno.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #703005;");

        lblPlacar = new Label("Branco: 0   |   Preto: 0");
        lblPlacar.setStyle("-fx-font-size: 14px; -fx-text-fill: #8B5A2B;");

        // ── Contadores de peças retiradas (bearing off) ───────────────────────
        lblPecasBrancoFora = new Label("Brancas fora: 0/15");
        lblPecasBrancoFora.setStyle("-fx-font-size: 13px; -fx-text-fill: #8B5A2B;");

        lblPecasPretoFora = new Label("Pretas fora: 0/15");
        lblPecasPretoFora.setStyle("-fx-font-size: 13px; -fx-text-fill: #8B5A2B;");

        barra.getChildren().addAll(lblTurno, lblPlacar, lblPecasBrancoFora, lblPecasPretoFora);
        return barra;

    }
    private Label criarAviso() {
        lblAvisoBarra = new Label();
        lblAvisoBarra.setStyle(
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #B71C1C; "
                        + "-fx-background-color: #FFEBEE; -fx-background-radius: 6; "
                        + "-fx-padding: 6 14 6 14; -fx-border-color: #EF9A9A; -fx-border-radius: 6;");
        lblAvisoBarra.setVisible(false);
        lblAvisoBarra.setManaged(false);
        return lblAvisoBarra;
    }

    // ── Tabuleiro (GridPane 12 colunas × 2 linhas) ─────────────────────────

    private StackPane criarTabuleiro() {
        boardGrid = new GridPane();
        boardGrid.setAlignment(Pos.CENTER);
        boardGrid.setHgap(3);
        boardGrid.setVgap(8);
        boardGrid.setPadding(new Insets(10));
        boardGrid.setStyle(
                "-fx-background-color: #C8A96A; "
                        + "-fx-border-color: #5C3317; -fx-border-width: 4; "
                        + "-fx-border-radius: 10; -fx-background-radius: 10;");

        StackPane wrapper = new StackPane(boardGrid);
        wrapper.setAlignment(Pos.CENTER);

        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.rgb(0, 0, 0, 0.35));
        sombra.setRadius(18);
        sombra.setOffsetY(4);
        wrapper.setEffect(sombra);

        return wrapper;
    }

    // ── Barra de controlos ─────────────────────────────────────────────────

    private HBox criarBarraControlos() {
        HBox barra = new HBox(16);
        barra.setAlignment(Pos.CENTER);
        barra.setPadding(new Insets(6, 0, 0, 0));

        Label lblDadoLabel = new Label("Dados:");
        lblDadoLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #5C3317;");

        lblDado1 = new Label("?");
        lblDado1.setStyle(ESTILO_DADO);

        lblDado2 = new Label("?");
        lblDado2.setStyle(ESTILO_DADO);

        lblMovimentos = new Label("");
        lblMovimentos.setStyle("-fx-font-size: 11px; -fx-text-fill: #8B5A2B; -fx-font-style: italic;");

        btnLancarDados = new Button("Lançar Dados");
        btnLancarDados.setStyle(ESTILO_BTN_PRIMARIO);
        btnLancarDados.setOnAction(e -> enviarLancarDados());

        btnPassarTurno = new Button("Passar Turno");
        btnPassarTurno.setStyle(ESTILO_BTN_SECUNDARIO);
        btnPassarTurno.setOnAction(e -> enviarPassarTurno());

        btnRetirarPeca = new Button("Retirar Peça");
        btnRetirarPeca.setStyle(ESTILO_BTN_DESATIVADO);
        btnRetirarPeca.setDisable(true);
        btnRetirarPeca.setOnAction(e -> realizarBearingOff());

        barra.getChildren().addAll(lblDadoLabel, lblDado1, lblDado2, lblMovimentos, btnLancarDados, btnPassarTurno, btnRetirarPeca);
        return barra;
    }

    // ── Rodapé informativo ─────────────────────────────────────────────────

    private Label criarRodape() {
        String corNome = minhaCor == Peca.CorPeca.BRANCO ? "Brancas" : "Pretas";
        Label lbl = new Label("Você joga com as " + corNome
                + "  ·  Clique numa peça sua para a selecionar, depois clique no destino.");
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #8B7355; -fx-font-style: italic;");
        return lbl;
    }

    // ── Atualização do estado (chamada pelo ClienteMain) ───────────────────

    /**
     * Atualiza toda a interface com o novo estado de jogo recebido do servidor.
     * Este método DEVE ser chamado na JavaFX Application Thread (Platform.runLater).
     *
     * @param pacote Estado completo do jogo enviado pelo Servidor
     */

    // Atualização do estado (chamada pelo ClienteMain), verifica se há vencedor
    public void atualizar(PacoteEstadoJogo pacote) {
        this.ultimoEstado = pacote;
        this.origemSelecionada = null;

        if (pacote.getNomeVencedor() != null) {
            mostrarFimJogo("🏆 " + pacote.getNomeVencedor() + " venceu o jogo!");
            return;
        }
        boolean meuTurno = pacote.getNomeJogadorTurno() != null
                && pacote.getNomeJogadorTurno().equalsIgnoreCase(meuNome);

        atualizarTurno(pacote, meuTurno);
        atualizarPlacar(pacote);
        atualizarDados(pacote, meuTurno);
        atualizarControlos(pacote, meuTurno);
        atualizarAvisoBarra(pacote, meuTurno);
        atualizarContadoresBearingOff(pacote);
        desenharTabuleiro(pacote.getTabuleiroSnapshot());
    }
    // ── Métodos privados de atualização ───────────────────────────────────

    private void atualizarTurno(PacoteEstadoJogo pacote, boolean meuTurno) {
        if (meuTurno) {
            lblTurno.setText("★  A sua vez de jogar!");
            lblTurno.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");
        } else {
            String nomeOponente = pacote.getNomeJogadorTurno() != null
                    ? pacote.getNomeJogadorTurno() : "oponente";
            lblTurno.setText("Turno de " + nomeOponente + "...");
            lblTurno.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #B71C1C;");
        }
    }

    private void atualizarPlacar(PacoteEstadoJogo pacote) {
        lblPlacar.setText(String.format(
                "Branco: %d capturas   |   Preto: %d capturas",
                pacote.getPontuacaoBranco(),
                pacote.getPontuacaoPreto()));
    }

    private void atualizarDados(PacoteEstadoJogo pacote, boolean meuTurno) {
        if (pacote.isDadosLancados()) {
            lblDado1.setText(String.valueOf(pacote.getValorDadoUm()));
            lblDado2.setText(String.valueOf(pacote.getValorDadoDois()));

            List<Integer> movs = pacote.getMovimentosDisponiveis();
            if (movs != null && !movs.isEmpty()) {
                lblMovimentos.setText("Movimentos restantes: " + movs);
            } else {
                lblMovimentos.setText("Sem movimentos disponíveis");
            }
        } else {
            lblDado1.setText("?");
            lblDado2.setText("?");
            lblMovimentos.setText(meuTurno ? "Lance os dados para jogar" : "");
        }
    }

    private void atualizarControlos(PacoteEstadoJogo pacote, boolean meuTurno) {
        boolean podelancar = meuTurno && !pacote.isDadosLancados();
        boolean podePassar = meuTurno && pacote.isDadosLancados();

        btnLancarDados.setDisable(!podelancar);
        btnLancarDados.setStyle(podelancar ? ESTILO_BTN_PRIMARIO : ESTILO_BTN_DESATIVADO);

        btnPassarTurno.setDisable(!podePassar);
        btnPassarTurno.setStyle(podePassar ? ESTILO_BTN_SECUNDARIO : ESTILO_BTN_DESATIVADO);

        atualizarBtnRetirarPeca();
    }
    private void atualizarAvisoBarra(PacoteEstadoJogo pacote, boolean meuTurno) {
        Tabuleiro t = pacote.getTabuleiroSnapshot();
        if (t == null) return;

        boolean temNaBarra = meuTurno && t.temPecasNaBarra(minhaCor);
        lblAvisoBarra.setVisible(temNaBarra);
        lblAvisoBarra.setManaged(temNaBarra);

        if (temNaBarra) {
            int qtd = t.getBarra(minhaCor).getQuantidadePecas();
            String zona = minhaCor == Peca.CorPeca.BRANCO ? "casas 1-6" : "casas 19-24";
            lblAvisoBarra.setText("⚠  Tem " + qtd + " peça(s) na barra! "
                    + "Deve reintroduzir nas " + zona + " antes de mover outras peças.");
        }
    }
    // Contadores de peças retiradas (bearing off)
    private void atualizarContadoresBearingOff(PacoteEstadoJogo pacote) {
        Tabuleiro t = pacote.getTabuleiroSnapshot();
        if (t == null) return;

        int pecasBrancoNoJogo = 0;
        int pecasPretoNoJogo = 0;

        for (Campo campo : t.getCampos()) {
            if (!campo.isVazio()) {
                if (campo.getCorDominante() == Peca.CorPeca.BRANCO) {
                    pecasBrancoNoJogo += campo.getQuantidadePecas();
                } else {
                    pecasPretoNoJogo += campo.getQuantidadePecas();
                }
            }
        }

        pecasBrancoNoJogo += t.getBarraBranco().getQuantidadePecas();
        pecasPretoNoJogo  += t.getBarraPreto().getQuantidadePecas();

        int brancoFora = 15 - pecasBrancoNoJogo;
        int pretoFora  = 15 - pecasPretoNoJogo;

        lblPecasBrancoFora.setText("Brancas fora: " + brancoFora + "/15");
        lblPecasPretoFora.setText("Pretas fora: "  + pretoFora  + "/15");
    }

    // Desenho do tabuleiro

    private void desenharTabuleiro(Tabuleiro tabuleiro) {
        if (tabuleiro == null) return;
        boardGrid.getChildren().clear();

        // Metade esquerda: casas 13-18 (sup) e 12-7 (inf)
        for (int col = 0; col < 6; col++) {
            boardGrid.add(criarCelula(13 + col, tabuleiro.getCampo(13 + col), false), col, 0);
            boardGrid.add(criarCelula(12 - col,  tabuleiro.getCampo(12 - col),  true),  col, 1);
        }

        // Barra central: coluna 6, ocupa as 2 linhas
        boardGrid.add(criarCelulaBarra(tabuleiro), 6, 0, 1, 2);

        // Metade direita: casas 19-24 (sup) e 6-1 (inf)
        for (int col = 0; col < 6; col++) {
            boardGrid.add(criarCelula(19 + col, tabuleiro.getCampo(19 + col), false), col + 7, 0);
            boardGrid.add(criarCelula(6 - col,  tabuleiro.getCampo(6 - col), true),  col + 7, 1);
        }
    }

    private StackPane criarCelula(int id, Campo campo, boolean apontaCima) {
        StackPane cell = new StackPane();
        cell.setPrefSize(54, 185);

        // Destaque da célula selecionada
        boolean selecionada = (origemSelecionada != null && origemSelecionada == id);

        // Fundo de seleção
        if (selecionada) {
            Rectangle destaque = new Rectangle(54, 185);
            destaque.setFill(Color.rgb(255, 215, 0, 0.25));
            destaque.setStroke(Color.GOLD);
            destaque.setStrokeWidth(2.5);
            destaque.setArcWidth(6);
            destaque.setArcHeight(6);
            cell.getChildren().add(destaque);
        }

        // Destaque de destino possível
        if (eDestinoPossivel(id)) {
            Rectangle destaqueDestino = new Rectangle(54, 185);
            destaqueDestino.setFill(Color.rgb(144, 238, 144, 0.2)); // verde claro transparente
            destaqueDestino.setStroke(Color.LIGHTGREEN);
            destaqueDestino.setStrokeWidth(2.0);
            destaqueDestino.setArcWidth(6);
            destaqueDestino.setArcHeight(6);
            cell.getChildren().add(destaqueDestino);
        }

        // Triângulo do ponto
        Polygon triangulo = criarTriangulo(id, apontaCima);
        cell.getChildren().add(triangulo);

        // Número da casa (pequeno, no canto)
        Label lblId = new Label(String.valueOf(id));
        lblId.setStyle("-fx-font-size: 9px; -fx-text-fill: rgba(80,40,10,0.5);");
        StackPane.setAlignment(lblId, apontaCima ? Pos.BOTTOM_CENTER : Pos.TOP_CENTER);

        // Peças
        VBox vboxPecas = criarVboxPecas(campo, apontaCima);

        cell.getChildren().addAll(vboxPecas, lblId);
        cell.setOnMouseClicked(e -> tratarClique(id, campo));

        // Cursor de ponteiro quando é jogável
        if (ultimoEstado != null) {
            boolean meuTurno = ultimoEstado.getNomeJogadorTurno() != null
                    && ultimoEstado.getNomeJogadorTurno().equalsIgnoreCase(meuNome);
            if (meuTurno) {
                cell.setStyle("-fx-cursor: hand;");
            }
        }

        return cell;
    }
    private VBox criarCelulaBarra(Tabuleiro tabuleiro) {
        VBox barra = new VBox(4);
        barra.setAlignment(Pos.CENTER);
        barra.setPrefSize(52, 378);
        barra.setStyle("-fx-background-color: #8B5A2B; "
                + "-fx-border-color: #5C3317; -fx-border-width: 0 2 0 2;");
        barra.setPadding(new Insets(8, 4, 8, 4));

        VBox pilhaPreto  = criarPilhaBarra(tabuleiro.getBarraPreto(),  Peca.CorPeca.PRETO,  false);
        Rectangle div = new Rectangle(40, 3);
        div.setFill(Color.web("#5C3317"));
        VBox pilhaBranco = criarPilhaBarra(tabuleiro.getBarraBranco(), Peca.CorPeca.BRANCO, true);

        Label lblBarra = new Label("B\nA\nR\nR\nA");
        lblBarra.setStyle("-fx-text-fill: rgba(255,245,220,0.4); -fx-font-size: 9px; "
                + "-fx-font-weight: bold; -fx-text-alignment: center;");

        barra.getChildren().addAll(pilhaPreto, div, pilhaBranco, lblBarra);
        VBox.setVgrow(pilhaPreto,  javafx.scene.layout.Priority.ALWAYS);
        VBox.setVgrow(pilhaBranco, javafx.scene.layout.Priority.ALWAYS);
        return barra;
    }

    private VBox criarPilhaBarra(Campo campo, Peca.CorPeca cor, boolean apontaCima) {
        VBox vbox = new VBox(-6);
        vbox.setAlignment(apontaCima ? Pos.BOTTOM_CENTER : Pos.TOP_CENTER);
        vbox.setPadding(new Insets(4, 0, 4, 0));

        int total = campo.getQuantidadePecas();
        if (total == 0) return vbox;

        int mostrar = Math.min(total, 5);
        for (int i = 0; i < mostrar; i++) {
            Circle c = criarCirculoPeca(cor);
            c.setEffect(new DropShadow(6, Color.RED));

            if (i == mostrar - 1 && total > 5) {
                StackPane sp = new StackPane();
                Label lbl = new Label("+" + (total - 4));
                lbl.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: "
                        + (cor == Peca.CorPeca.BRANCO ? "#333" : "#FFF") + ";");
                sp.getChildren().addAll(c, lbl);
                vbox.getChildren().add(sp);
            } else {
                vbox.getChildren().add(c);
            }
        }
        return vbox;
    }

    private Polygon criarTriangulo(int id, boolean apontaCima) {
        double w = 52, h = 183;
        Polygon t = new Polygon();

        if (apontaCima) {
            t.getPoints().addAll(w / 2, 4.0, 2.0, h - 4, w - 2.0, h - 4);
        } else {
            t.getPoints().addAll(2.0, 4.0, w - 2.0, 4.0, w / 2, h - 4);
        }

        // Alterna cor dos triângulos: escuro nos pares, claro nos ímpares
        Color corTriangulo = (id % 2 == 0)
                ? Color.web("#7B3F00")   // castanho escuro
                : Color.web("#D2A679");  // castanho claro

        t.setFill(corTriangulo);
        t.setStroke(Color.web("#5C3317"));
        t.setStrokeWidth(0.8);
        return t;
    }

    private VBox criarVboxPecas(Campo campo, boolean apontaCima) {
        VBox vbox = new VBox(-8);
        vbox.setAlignment(apontaCima ? Pos.BOTTOM_CENTER : Pos.TOP_CENTER);
        vbox.setPadding(new Insets(6, 0, 6, 0));

        int total = campo.getQuantidadePecas();
        if (total == 0) return vbox;

        Peca.CorPeca cor = campo.getCorDominante();
        int mostrar = Math.min(total, 5);

        for (int i = 0; i < mostrar; i++) {
            boolean isUltima = (i == mostrar - 1);
            boolean temExcesso = (total > 5);

            Circle circulo = criarCirculoPeca(cor);

            if (isUltima && temExcesso) {
                StackPane stack = new StackPane();
                Label lblMais = new Label("+" + (total - 4));
                lblMais.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: "
                        + (cor == Peca.CorPeca.BRANCO ? "#333333" : "#FFFFFF") + ";");
                stack.getChildren().addAll(circulo, lblMais);
                vbox.getChildren().add(stack);
            } else {
                vbox.getChildren().add(circulo);
            }
        }
        return vbox;
    }

    private Circle criarCirculoPeca(Peca.CorPeca cor) {
        Circle c = new Circle(12);
        if (cor == Peca.CorPeca.BRANCO) {
            c.setFill(Color.WHITE);
            c.setStroke(Color.web("#BBBBBB"));
        } else {
            c.setFill(Color.web("#1C1C1C"));
            c.setStroke(Color.web("#555555"));
        }
        c.setStrokeWidth(2);

        DropShadow ds = new DropShadow(3, Color.rgb(0, 0, 0, 0.45));
        c.setEffect(ds);
        return c;
    }

    // ── Lógica de clique nas casas ─────────────────────────────────────────

    private void tratarClique(int id, Campo campo) {
        if (ultimoEstado == null) return;

        boolean meuTurno = ultimoEstado.getNomeJogadorTurno() != null
                && ultimoEstado.getNomeJogadorTurno().equalsIgnoreCase(meuNome);

        if (!meuTurno) return;

        if (!ultimoEstado.isDadosLancados()) {
            mostrarAviso("Lance os dados antes de mover uma peça.");
            return;
        }
        // Com peças na barra: qualquer clique é destino de reintrodução
        Tabuleiro t = ultimoEstado.getTabuleiroSnapshot();
        if (t != null && t.temPecasNaBarra(minhaCor)) {
            enviarMovimento(-1, id);
            return;
        }

        if (origemSelecionada == null) {
            // Selecionar origem
            if (campo.isVazio() || campo.getCorDominante() != minhaCor) {
                mostrarAviso("Selecione uma casa com as suas peças.");
                return;
            }
            origemSelecionada = id;
            destacarOrigem(id);

        } else {
            // Confirmar destino
            int origem = origemSelecionada;
            int destino = id;
            origemSelecionada = null;

            if (origem == destino) {
                // Clicou na mesma casa: cancela seleção
                destacarOrigem(-1);
                return;
            }

            enviarMovimento(origem, destino);
        }
    }

    /**
     * Redesenha o tabuleiro apenas para mostrar o destaque da origem selecionada.
     * O estado real não mudou — só a visualização local.
     */
    private void destacarOrigem(int idSelecionado) {
        desenharTabuleiro(ultimoEstado.getTabuleiroSnapshot());
        atualizarBtnRetirarPeca();
        atualizarDestaqueBearingOff();
    }

    private boolean eDestinoPossivel(int destinoId) {
        if (ultimoEstado == null || !ultimoEstado.isDadosLancados()) return false;

        boolean meuTurno = ultimoEstado.getNomeJogadorTurno() != null
                && ultimoEstado.getNomeJogadorTurno().equalsIgnoreCase(meuNome);
        if (!meuTurno) return false;

        Tabuleiro t = ultimoEstado.getTabuleiroSnapshot();
        if (t == null) return false;

        // Caso haja peças na barra, o jogador só pode reintroduzir
        if (t.temPecasNaBarra(minhaCor)) {
            boolean destinoValido;
            int distancia;
            if (minhaCor == Peca.CorPeca.BRANCO) {
                destinoValido = destinoId >= 1 && destinoId <= 6;
                distancia = destinoId;
            } else {
                destinoValido = destinoId >= 19 && destinoId <= 24;
                distancia = 25 - destinoId;
            }
            if (!destinoValido) return false;

            List<Integer> movs = ultimoEstado.getMovimentosDisponiveis();
            if (movs == null || !movs.contains(distancia)) return false;

            Campo campoDestino = t.getCampo(destinoId);
            return campoDestino.isVazio()
                    || campoDestino.getCorDominante() == minhaCor
                    || campoDestino.getQuantidadePecas() <= 1;
        }

        // Se não houver peças na barra, tem de haver uma peça selecionada
        if (origemSelecionada == null) return false;

        int origem = origemSelecionada;
        int distancia;
        if (minhaCor == Peca.CorPeca.BRANCO) {
            distancia = destinoId - origem;
        } else {
            distancia = origem - destinoId;
        }

        if (distancia <= 0) return false;

        List<Integer> movs = ultimoEstado.getMovimentosDisponiveis();
        if (movs == null || !movs.contains(distancia)) return false;

        Campo campoDestino = t.getCampo(destinoId);
        return campoDestino.isVazio()
                || campoDestino.getCorDominante() == minhaCor
                || campoDestino.getQuantidadePecas() <= 1;
    }

    private boolean podeFazerBearingOffComOrigem() {
        if (ultimoEstado == null || origemSelecionada == null) return false;
        Tabuleiro t = ultimoEstado.getTabuleiroSnapshot();
        if (t == null) return false;
        if (!t.todasPecasNoQuadranteFinal(minhaCor)) return false;

        int distancia = minhaCor == Peca.CorPeca.BRANCO ? (25 - origemSelecionada) : origemSelecionada;
        List<Integer> movs = ultimoEstado.getMovimentosDisponiveis();
        return movs != null && movs.contains(distancia);
    }

    private void atualizarBtnRetirarPeca() {
        if (btnRetirarPeca == null) return;
        boolean podeRetirar = podeFazerBearingOffComOrigem();
        btnRetirarPeca.setDisable(!podeRetirar);
        btnRetirarPeca.setStyle(podeRetirar ? ESTILO_BTN_PRIMARIO : ESTILO_BTN_DESATIVADO);
    }

    private void atualizarDestaqueBearingOff() {
        if (ultimoEstado == null) return;
        boolean podeRetirar = podeFazerBearingOffComOrigem();
        if (minhaCor == Peca.CorPeca.BRANCO) {
            lblPecasBrancoFora.setStyle("-fx-font-size: 13px; -fx-text-fill: "
                    + (podeRetirar ? "#2E7D32; -fx-font-weight: bold;" : "#8B5A2B;"));
        } else {
            lblPecasPretoFora.setStyle("-fx-font-size: 13px; -fx-text-fill: "
                    + (podeRetirar ? "#2E7D32; -fx-font-weight: bold;" : "#8B5A2B;"));
        }
    }

    private void realizarBearingOff() {
        if (podeFazerBearingOffComOrigem()) {
            int destino = minhaCor == Peca.CorPeca.BRANCO ? 25 : 0;
            int origem = origemSelecionada;
            origemSelecionada = null;
            enviarMovimento(origem, destino);
        }
    }

    // ── Envio de mensagens ao servidor ─────────────────────────────────────

    private void enviarLancarDados() {
        new Thread(() -> cliente.enviarMensagem(
                new MensagemRede(MensagemRede.TipoMensagem.LANCAR_DADOS, meuNome, null, null)
        ), "Thread-LancarDados").start();
    }

    private void enviarPassarTurno() {
        origemSelecionada = null;
        new Thread(() -> cliente.enviarMensagem(
                new MensagemRede(MensagemRede.TipoMensagem.PASSAR_TURNO, meuNome, null, null)
        ), "Thread-PassarTurno").start();
    }

    private void enviarMovimento(int origem, int destino) {
        new Thread(() -> cliente.enviarMensagem(
                new MensagemRede(MensagemRede.TipoMensagem.MOVER_PECA, meuNome, origem, destino)
        ), "Thread-Mover").start();
    }

    // ── Utilitários de UI ──────────────────────────────────────────────────

    private void mostrarAviso(String mensagem) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ação inválida");
            alert.setHeaderText(null);
            alert.setContentText(mensagem);
            alert.showAndWait();
        });
    }

    /**
     * Mostra um diálogo de fim de jogo.
     * Pode ser chamado pelo ClienteMain quando detetar condição de vitória.
     *
     * @param mensagem Texto a apresentar ao jogador
     */
    public void mostrarFimJogo(String mensagem) {
        Platform.runLater(() -> {
            try {
                String som = getClass().getResource("/Tada_sound_Effect___SFX.mp3").toExternalForm();
                Media media = new Media(som);
                MediaPlayer mediaPlayer = new MediaPlayer(media);
                mediaPlayer.play();
            } catch (Exception e) {
                System.err.println("Erro ao tocar som: " + e.getMessage());
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Fim do Jogo");
            alert.setHeaderText("Partida terminada!");
            alert.setContentText(mensagem);
            alert.showAndWait();
        });
    }
}