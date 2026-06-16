package rede;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import main.JogoBackgammon;
import modelo.Campo;
import modelo.Dado;
import modelo.Peca;
import modelo.Tabuleiro;

public class Servidor {
    private static final int PORTA = 12025;
    private static final int MAX_CLIENTES = 2;

    private final int porta;
    private final List<ClientHandler> clientes;
    private final JogoBackgammon jogoBackgammon;
    private final Tabuleiro tabuleiro;
    private final Dado dadoUm;
    private final Dado dadoDois;

    private ServerSocket serverSocket;
    private boolean servidorAtivo;
    private Peca.CorPeca turnoAtual;
    private int pontuacaoBranco;
    private int pontuacaoPreto;
    private final List<Integer> movimentosDisponiveis = Collections.synchronizedList(new ArrayList<>());
    private boolean dadosLancadosNoTurno = false;

    private LogListener logListener;

    public interface LogListener {
        void onLog(String mensagem);
    }

    public void setLogListener(LogListener listener) {
        this.logListener = listener;
    }

    private void log(String mensagem) {
        System.out.println(mensagem);
        if (logListener != null) {
            logListener.onLog(mensagem);
        }
    }

    private void logErro(String mensagem) {
        System.err.println(mensagem);
        if (logListener != null) {
            logListener.onLog("ERRO: " + mensagem);
        }
    }

    public Servidor() {
        this(PORTA);
    }

    public Servidor(int porta) {
        this.porta = porta;
        this.clientes = Collections.synchronizedList(new ArrayList<>());

        /*
         * O projeto atual tem a classe JogoBackgammon no pacote "main" e essa classe
         * representa a interface Swing. O servidor instancia essa classe para manter
         * o ponto de integração pedido no enunciado, mas o estado autoritativo da
         * partida fica no Tabuleiro abaixo, que é o objeto efetivamente difundido
         * para os clientes em rede.
         */
        this.jogoBackgammon = new JogoBackgammon();
        this.tabuleiro = new Tabuleiro();
        this.dadoUm = new Dado();
        this.dadoDois = new Dado();
        this.turnoAtual = Peca.CorPeca.BRANCO;
        this.pontuacaoBranco = 0;
        this.pontuacaoPreto = 0;
    }

    public void iniciar() {
        try {
            serverSocket = new ServerSocket(porta);
            servidorAtivo = true;
            log("Servidor de Gamão ativo na porta " + porta + ".");
            aguardarLigacaoDosClientes();
            manterServidorAtivo();
        } catch (IOException e) {
            logErro("Erro ao iniciar o servidor: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logErro("Thread principal do servidor interrompida.");
        } finally {
            encerrarServidor();
        }
    }

    private void aguardarLigacaoDosClientes() throws IOException {
        while (servidorAtivo && clientes.size() < MAX_CLIENTES) {
            Socket socketCliente = serverSocket.accept();
            Peca.CorPeca corAtribuida = clientes.isEmpty() ? Peca.CorPeca.BRANCO : Peca.CorPeca.PRETO;

            ClientHandler cliente = new ClientHandler(socketCliente, corAtribuida);
            clientes.add(cliente);
            cliente.start();

            log("Cliente ligado como jogador " + corAtribuida + ".");
        }

        log("Dois clientes ligados. A partida pode comecar.");
        fazerBroadcast();
    }

    private void manterServidorAtivo() throws InterruptedException {
        while (servidorAtivo) {
            Thread.sleep(200L);
        }
    }

    public synchronized void fazerBroadcast() {
        String nomeBranco = "A aguardar...";
        String nomePreto = "A aguardar...";
        synchronized (clientes) {
            for (ClientHandler cliente : clientes) {
                if (cliente.getCorJogador() == Peca.CorPeca.BRANCO) {
                    nomeBranco = cliente.getNomeJogador();
                } else if (cliente.getCorJogador() == Peca.CorPeca.PRETO) {
                    nomePreto = cliente.getNomeJogador();
                }
            }
        }

        PacoteEstadoJogo pacote = new PacoteEstadoJogo(
                tabuleiro,
                pontuacaoBranco,
                pontuacaoPreto,
                turnoAtual,
                obterNomeJogadorDoTurno(),
                nomeBranco,
                nomePreto,
                dadosLancadosNoTurno ? dadoUm.getValor() : 0,
                dadosLancadosNoTurno ? dadoDois.getValor() : 0,
                new ArrayList<>(movimentosDisponiveis),
                dadosLancadosNoTurno);

        synchronized (clientes) {
            for (ClientHandler cliente : clientes) {
                cliente.enviarPacote(pacote);
            }
        }
    }

    private synchronized void processarMensagem(ClientHandler origemCliente, MensagemRede mensagem) {
        if (mensagem == null || mensagem.getTipoMensagem() == null) {
            return;
        }

        if (origemCliente.getCorJogador() != turnoAtual) {
            log("Mensagem ignorada: nao e o turno de " + origemCliente.getCorJogador() + ".");
            return;
        }

        switch (mensagem.getTipoMensagem()) {
            case LANCAR_DADOS:
                processarLancamentoDados(origemCliente);
                break;
            case MOVER_PECA:
                processarMovimento(origemCliente, mensagem);
                break;
            case PASSAR_TURNO:
                alternarTurno();
                fazerBroadcast();
                break;
            case DESCONECTAR:
                removerCliente(origemCliente);
                break;
            case GUARDAR_JOGO:
                gravarJogo("jogo_salvo.dat");
                break;
            default:
                break;
        }
    }

    private void processarLancamentoDados(ClientHandler cliente) {
        if (dadosLancadosNoTurno) {
            log("Jogador " + cliente.getCorJogador() + " tentou lancar os dados novamente, mas ja foram lancados.");
            return;
        }

        int valorUm = dadoUm.lancar();
        int valorDois = dadoDois.lancar();
        dadosLancadosNoTurno = true;

        movimentosDisponiveis.clear();
        if (valorUm == valorDois) {
            // Se dados iguais (duplos), recebe 4 movimentos desse valor
            for (int i = 0; i < 4; i++) {
                movimentosDisponiveis.add(valorUm);
            }
        } else {
            // Se diferentes, recebe os 2 valores
            movimentosDisponiveis.add(valorUm);
            movimentosDisponiveis.add(valorDois);
        }

        log("Jogador " + cliente.getCorJogador() + " lancou os dados: " + valorUm + " e " + valorDois + " (" + movimentosDisponiveis.size() + " movimentos disponiveis).");
        fazerBroadcast();
    }

    private void processarMovimento(ClientHandler cliente, MensagemRede mensagem) {
        if (!dadosLancadosNoTurno) {
            log("Movimento ignorado: os dados ainda nao foram lancados.");
            return;

        }
        // Se o jogador tem peças na barra, só pode reintroduzir
        if (tabuleiro.temPecasNaBarra(cliente.getCorJogador())) {
            processarReintroducao(cliente, mensagem.getDestino());
            return;
        }

        Integer origem = mensagem.getOrigem();
        Integer destino = mensagem.getDestino();

        if (origem == null || destino == null) {
            log("Movimento ignorado: origem ou destino em falta.");
            return;
        }

        // Permite destino 25 (brancas saem) ou 0 (pretas saem) para bearing off
        boolean bearingOff = (cliente.getCorJogador() == Peca.CorPeca.BRANCO && destino == 25)
                || (cliente.getCorJogador() == Peca.CorPeca.PRETO && destino == 0);

        if (!posicaoValida(origem) || (!posicaoValida(destino) && !bearingOff)) {
            log("Movimento ignorado: posicao invalida.");
            return;
        }

        if (bearingOff && !tabuleiro.todasPecasNoQuadranteFinal(cliente.getCorJogador())) {
            log("Bearing off ignorado: ainda ha pecas fora do quadrante final.");
            return;
        }

        // Validação de Sentido (Direção) e Distância
        int distancia;
        if (cliente.getCorJogador() == Peca.CorPeca.BRANCO) {
            distancia = destino - origem;
        } else {
            distancia = origem - destino;
        }

        if (distancia <= 0) {
            log("Movimento ignorado: direcao de movimento incorreta para o jogador " + cliente.getCorJogador() + ".");
            return;
        }

        // Verifica se a distância do movimento é permitida pelos dados disponíveis
        boolean movimentoValidoPorDado = false;
        synchronized (movimentosDisponiveis) {
            if (movimentosDisponiveis.contains(distancia)) {
                movimentoValidoPorDado = true;
            }
        }

        if (!movimentoValidoPorDado) {
            log("Movimento ignorado: a distancia " + distancia + " nao esta entre os movimentos disponiveis " + movimentosDisponiveis + ".");
            return;
        }

        Campo campoOrigem = tabuleiro.getCampo(origem);
        Campo campoDestino = null;
        if (!bearingOff) {
            campoDestino = tabuleiro.getCampo(destino);
        }

        if (campoOrigem.isVazio()) {
            log("Movimento ignorado: campo de origem vazio.");
            return;
        }

        Peca pecaTopo = campoOrigem.espreitarTopo();
        if (pecaTopo == null || pecaTopo.getCor() != cliente.getCorJogador()) {
            log("Movimento ignorado: a peca nao pertence ao jogador atual.");
            return;
        }

        if (!bearingOff) {
            if (!campoDestino.isVazio()
                    && campoDestino.getCorDominante() != cliente.getCorJogador()
                    && campoDestino.getQuantidadePecas() > 1) {
                log("Movimento ignorado: destino bloqueado pelo adversario.");
                return;
            }

            if (!campoDestino.isVazio()
                    && campoDestino.getCorDominante() != cliente.getCorJogador()
                    && campoDestino.getQuantidadePecas() == 1) {
                Peca pecaCapturada = campoDestino.removerPeca();
                tabuleiro.getBarra(pecaCapturada.getCor()).adicionarPeca(pecaCapturada);
                atribuirPonto(cliente.getCorJogador());
                log("Jogador " + cliente.getCorJogador() + " capturou peca adversaria no campo " + destino + "! (vai para a barra)");
            }
        }

        if (bearingOff) {
            campoOrigem.removerPeca();
            log("Jogador " + cliente.getCorJogador() + " retirou uma peca do tabuleiro (bearing off).");
        } else {
            campoDestino.adicionarPeca(campoOrigem.removerPeca());
        }

        // Consome o movimento
        synchronized (movimentosDisponiveis) {
            movimentosDisponiveis.remove((Integer) distancia);
        }

        log("Movimento valido: " + origem + " -> " + destino + " (distancia " + distancia + "). Restam " + movimentosDisponiveis + ".");

        // Se consumiu todos os movimentos, passa automaticamente o turno
        if (movimentosDisponiveis.isEmpty()) {
            log("Todos os movimentos consumidos. A passar turno automaticamente.");
            alternarTurno();
        }

        fazerBroadcast();
        verificarFimJogo();
    }
    // ── Reintrodução da barra ─────────────────────────────────────────────
    private void processarReintroducao(ClientHandler cliente, int destino) {
        Peca.CorPeca corJogador = cliente.getCorJogador();

        boolean destinoValido;
        int distancia;
        if (corJogador == Peca.CorPeca.BRANCO) {
            destinoValido = destino >= 1 && destino <= 6;
            distancia = destino;
        } else {
            destinoValido = destino >= 19 && destino <= 24;
            distancia = 25 - destino;
        }

        if (!destinoValido) {
            log("Reintroducao ignorada: destino " + destino
                    + " fora da zona de entrada para " + corJogador + ".");
            return;
        }

        synchronized (movimentosDisponiveis) {
            if (!movimentosDisponiveis.contains(distancia)) {
                log("Reintroducao ignorada: distancia " + distancia
                        + " nao disponivel em " + movimentosDisponiveis + ".");
                return;
            }
            movimentosDisponiveis.remove((Integer) distancia);
        }

        Campo campoDestino = tabuleiro.getCampo(destino);

        if (!campoDestino.isVazio()
                && campoDestino.getCorDominante() != corJogador
                && campoDestino.getQuantidadePecas() > 1) {
            log("Reintroducao ignorada: casa " + destino + " bloqueada.");
            synchronized (movimentosDisponiveis) { movimentosDisponiveis.add(distancia); }
            return;
        }

        if (!campoDestino.isVazio()
                && campoDestino.getCorDominante() != corJogador
                && campoDestino.getQuantidadePecas() == 1) {
            Peca capturada = campoDestino.removerPeca();
            tabuleiro.getBarra(capturada.getCor()).adicionarPeca(capturada);
            atribuirPonto(corJogador);
            log("Reintroducao com captura na casa " + destino + "!");
        }

        Campo barra = tabuleiro.getBarra(corJogador);
        campoDestino.adicionarPeca(barra.removerPeca());
        log("Jogador " + corJogador + " reintroduziu peca na casa " + destino
                + ". Restam na barra: " + barra.getQuantidadePecas());

        if (movimentosDisponiveis.isEmpty()) {
            log("Todos os movimentos consumidos. A passar turno automaticamente.");
            alternarTurno();
        }
        verificarFimJogo();
        fazerBroadcast();
    }
    private boolean posicaoValida(int posicao) {
        return posicao >= 1 && posicao <= 24;
    }

    private void atribuirPonto(Peca.CorPeca corJogador) {
        if (corJogador == Peca.CorPeca.BRANCO) {
            pontuacaoBranco++;
        } else {
            pontuacaoPreto++;
        }
    }

    private void alternarTurno() {
        turnoAtual = turnoAtual == Peca.CorPeca.BRANCO ? Peca.CorPeca.PRETO : Peca.CorPeca.BRANCO;
        movimentosDisponiveis.clear();
        dadosLancadosNoTurno = false;
    }

    private void verificarFimJogo() {
        if (tabuleiro.jogadorVenceu(Peca.CorPeca.BRANCO)) {
            String vencedor = obterNomeJogador(Peca.CorPeca.BRANCO);
            log("Fim do jogo! Vencedor: " + vencedor);
            fazerBroadcastFimJogo(vencedor);
            servidorAtivo = false;
        } else if (tabuleiro.jogadorVenceu(Peca.CorPeca.PRETO)) {
            String vencedor = obterNomeJogador(Peca.CorPeca.PRETO);
            log("Fim do jogo! Vencedor: " + vencedor);
            fazerBroadcastFimJogo(vencedor);
            servidorAtivo = false;
        }
    }

    private String obterNomeJogador(Peca.CorPeca cor) {
        synchronized (clientes) {
            for (ClientHandler cliente : clientes) {
                if (cliente.getCorJogador() == cor) return cliente.getNomeJogador();
            }
        }
        return cor.name();
    }

    private void fazerBroadcastFimJogo(String nomeVencedor) {
        PacoteEstadoJogo pacote = new PacoteEstadoJogo(
                tabuleiro, pontuacaoBranco, pontuacaoPreto,
                turnoAtual, obterNomeJogadorDoTurno(),
                null, null, 0, 0, new ArrayList<>(), false);
        pacote.setNomeVencedor(nomeVencedor);

        synchronized (clientes) {
            for (ClientHandler cliente : clientes) {
                cliente.enviarPacote(pacote);
            }
        }
    }

    private String obterNomeJogadorDoTurno() {
        synchronized (clientes) {
            for (ClientHandler cliente : clientes) {
                if (cliente.getCorJogador() == turnoAtual) {
                    return cliente.getNomeJogador();
                }
            }
        }
        return turnoAtual.name();
    }

    private synchronized void removerCliente(ClientHandler cliente) {
        clientes.remove(cliente);
        cliente.fecharLigacao();
        log("Cliente " + cliente.getCorJogador() + " removido do servidor.");
    }

    public void encerrarServidor() {
        servidorAtivo = false;

        synchronized (clientes) {
            for (ClientHandler cliente : clientes) {
                cliente.fecharLigacao();
            }
            clientes.clear();
        }

        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logErro("Erro ao fechar o ServerSocket: " + e.getMessage());
            }
        }
    }
    // ── Gravação e carregamento do estado do jogo ─────────────────────────────
    public void gravarJogo(String caminho) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new java.io.FileOutputStream(caminho))) {
            oos.writeObject(tabuleiro);
            oos.writeObject(turnoAtual);
            oos.writeObject(pontuacaoBranco);
            oos.writeObject(pontuacaoPreto);
            log("Jogo gravado com sucesso em " + caminho);
        } catch (IOException e) {
            logErro("Erro ao gravar jogo: " + e.getMessage());
        }
    }

    public void carregarJogo(String caminho) {
        try (ObjectInputStream ois = new ObjectInputStream(
                new java.io.FileInputStream(caminho))) {
            Tabuleiro tabuleiroCarregado = (Tabuleiro) ois.readObject();
            Peca.CorPeca turnoCarregado = (Peca.CorPeca) ois.readObject();
            int pontBranco = (int) ois.readObject();
            int pontPreto = (int) ois.readObject();

            // Copia o estado carregado para o tabuleiro activo
            for (int i = 1; i <= 24; i++) {
                Campo campoOrigem = tabuleiroCarregado.getCampo(i);
                Campo campoDestino = tabuleiro.getCampo(i);
                while (!campoDestino.isVazio()) campoDestino.removerPeca();
                for (int j = 0; j < campoOrigem.getQuantidadePecas(); j++) {
                    campoDestino.adicionarPeca(new Peca(campoOrigem.getCorDominante()));
                }
            }

            // Copia o estado das barras
            Campo barraBrancoOrigem = tabuleiroCarregado.getBarraBranco();
            Campo barraBrancoDestino = tabuleiro.getBarraBranco();
            while (!barraBrancoDestino.isVazio()) barraBrancoDestino.removerPeca();
            for (int j = 0; j < barraBrancoOrigem.getQuantidadePecas(); j++) {
                barraBrancoDestino.adicionarPeca(new Peca(Peca.CorPeca.BRANCO));
            }

            Campo barraPretoOrigem = tabuleiroCarregado.getBarraPreto();
            Campo barraPretoDestino = tabuleiro.getBarraPreto();
            while (!barraPretoDestino.isVazio()) barraPretoDestino.removerPeca();
            for (int j = 0; j < barraPretoOrigem.getQuantidadePecas(); j++) {
                barraPretoDestino.adicionarPeca(new Peca(Peca.CorPeca.PRETO));
            }
            turnoAtual = turnoCarregado;
            pontuacaoBranco = pontBranco;
            pontuacaoPreto = pontPreto;
            dadosLancadosNoTurno = false;
            movimentosDisponiveis.clear();

            log("Jogo carregado com sucesso de " + caminho);
            fazerBroadcast();
        } catch (Exception e) {
            logErro("Erro ao carregar jogo: " + e.getMessage());
        }
    }
    public static void main(String[] args) {
        new Servidor().iniciar();
    }

    private class ClientHandler extends Thread {
        private final Socket socket;
        private final Peca.CorPeca corJogador;
        private ObjectInputStream input;
        private ObjectOutputStream output;
        private String nomeJogador;

        public ClientHandler(Socket socket, Peca.CorPeca corJogador) throws IOException {
            this.socket = socket;
            this.corJogador = corJogador;
            this.nomeJogador = corJogador == Peca.CorPeca.BRANCO ? "Jogador Branco" : "Jogador Preto";

            /*
             * O ObjectOutputStream e criado primeiro para evitar bloqueios no
             * handshake do fluxo de objetos entre servidor e cliente.
             */
            this.output = new ObjectOutputStream(socket.getOutputStream());
            this.output.flush();
            this.input = new ObjectInputStream(socket.getInputStream());
        }

        @Override
        public void run() {
            try {
                fazerBroadcast();

                while (servidorAtivo && !socket.isClosed()) {
                    Object objetoRecebido = input.readObject();

                    if (objetoRecebido instanceof MensagemRede mensagem) {
                        if (mensagem.getNomeJogador() != null && !mensagem.getNomeJogador().isBlank()) {
                            nomeJogador = mensagem.getNomeJogador();
                            fazerBroadcast(); // Sincroniza o lobby com o nome atualizado do jogador
                        }
                        processarMensagem(this, mensagem);
                    }
                }
            } catch (EOFException e) {
                log("Ligacao terminada pelo cliente " + corJogador + ".");
            } catch (IOException | ClassNotFoundException e) {
                logErro("Erro no cliente " + corJogador + ": " + e.getMessage());
            } finally {
                removerCliente(this);
            }
        }

        public void enviarPacote(PacoteEstadoJogo pacote) {
            try {
                synchronized (output) {
                    PacoteEstadoJogo pacoteIndividual = new PacoteEstadoJogo(
                            pacote.getTabuleiroSnapshot(),
                            pacote.getPontuacaoBranco(),
                            pacote.getPontuacaoPreto(),
                            pacote.getTurnoAtual(),
                            pacote.getNomeJogadorTurno(),
                            pacote.getNomeJogadorBranco(),
                            pacote.getNomeJogadorPreto(),
                            pacote.getValorDadoUm(),
                            pacote.getValorDadoDois(),
                            pacote.getMovimentosDisponiveis(),
                            pacote.isDadosLancados()
                    );
                    pacoteIndividual.setCorAtribuida(this.corJogador);
                    output.reset();
                    output.writeObject(pacoteIndividual);
                    output.flush();
                }
            } catch (IOException e) {
                logErro("Erro ao enviar estado ao cliente " + corJogador + ": " + e.getMessage());
            }
        }

        public void fecharLigacao() {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ignored) {
            }

            try {
                if (output != null) {
                    output.close();
                }
            } catch (IOException ignored) {
            }

            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException ignored) {
            }
        }

        public Peca.CorPeca getCorJogador() {
            return corJogador;
        }

        public String getNomeJogador() {
            return nomeJogador;
        }
    }
}
