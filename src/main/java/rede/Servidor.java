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

/**
 * Servidor autoritativo de jogo TCP.
 * Valida movimentos de pecas, gere a conexao de jogadores em threads,
 * efetua o broadcast do estado de jogo e suporta persistencia de dados.
 */
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

    /**
     * Interface de callback usada para desviar mensagens de log do servidor para a UI.
     */
    public interface LogListener {
        /**
         * Metodo invocado quando o servidor emite uma mensagem de log.
         * 
         * @param mensagem A string contendo a mensagem de log
         */
        void onLog(String mensagem);
    }

    /**
     * Define o listener de log do servidor.
     * 
     * @param listener O listener de log a registar
     */
    public void setLogListener(LogListener listener) {
        this.logListener = listener;
    }

    private void log(String mensagem) {
        System.out.println(mensagem);
        if (logListener != null) {
            logListener.onLog(mensagem);
        }
    }

    private void logErro(String message) {
        System.err.println(message);
        if (logListener != null) {
            logListener.onLog("ERRO: " + message);
        }
    }

    /**
     * Construtor por defeito do Servidor. Usa a porta TCP padrao (12025).
     */
    public Servidor() {
        this(PORTA);
    }

    /**
     * Construtor do Servidor que aceita uma porta especifica.
     * 
     * @param porta O porto TCP a escutar
     */
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

    /**
     * Inicializa o ServerSocket TCP na porta indicada e inicia a escuta de clientes.
     */
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

    /**
     * Entra num ciclo de espera bloqueante aguardando conexões de novos clientes TCP
     * até atingir o número máximo de jogadores configurados (2). Associa a cada cliente
     * uma cor de peças (BRANCO para o primeiro, PRETO para o segundo) e inicia o seu handler.
     * 
     * @throws IOException Se ocorrer algum erro ao aceitar a ligação no ServerSocket
     */
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

    /**
     * Mantém a thread principal do servidor ativa em ciclo de espera (sleep) enquando
     * a flag de atividade do servidor for verdadeira.
     * 
     * @throws InterruptedException Se a thread for interrompida durante o sleep
     */
    private void manterServidorAtivo() throws InterruptedException {
        while (servidorAtivo) {
            Thread.sleep(200L);
        }
    }

    /**
     * Envia o estado completo e atualizado da partida (PacoteEstadoJogo)
     * a todos os clientes conectados.
     */
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

    /**
     * Trata o processamento centralizado de mensagens/comandos de rede recebidos
     * de um cliente específico. Valida se a mensagem provém do jogador do turno corrente
     * antes de executar a ação correspondente (lançar dados, mover peça, passar turno, etc.).
     * 
     * @param origemCliente O manipulador de cliente que enviou a mensagem
     * @param mensagem A mensagem/comando de rede recebido
     */
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

    /**
     * Efetua o lançamento de dados do turno atual. Gera dois números aleatórios de 1 a 6
     * (representando os dois dados do jogo) e calcula os movimentos disponíveis. Se os dados
     * forem iguais (duplos), atribui quatro movimentos desse valor; caso contrário, atribui
     * dois movimentos correspondentes aos valores individuais.
     * 
     * @param cliente O manipulador do cliente que solicitou o lançamento dos dados
     */
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

    /**
     * Valida e executa o movimento de uma peça no tabuleiro do jogo. Verifica se os dados
     * já foram lançados, se há peças na barra, a direção e a distância correta do movimento
     * de acordo com as regras do Gamão, e se a casa de destino está disponível ou contém peças
     * a serem capturadas.
     * 
     * @param cliente O cliente que está a jogar e que enviou o movimento
     * @param mensagem A mensagem contendo a casa de origem e destino
     */
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
    /**
     * Tenta reintroduzir uma peça do jogador que se encontra na barra central de volta
     * ao tabuleiro (no quadrante inicial do adversário correspondente). Consome o valor
     * do dado apropriado e trata de capturas caso a casa de destino contenha apenas uma
     * peça adversária.
     * 
     * @param cliente O cliente que está a jogar e que tem peças na barra
     * @param destino A casa de destino pretendida para a reintrodução
     */
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
    /**
     * Auxiliar que valida se uma determinada posição de tabuleiro está dentro do intervalo
     * regulamentar das casas de jogo (1 a 24).
     * 
     * @param posicao O índice da casa a verificar
     * @return true se a posição for válida, false caso contrário
     */
    private boolean posicaoValida(int posicao) {
        return posicao >= 1 && posicao <= 24;
    }

    /**
     * Incrementa a pontuação do jogador correspondente à cor fornecida.
     * 
     * @param corJogador A cor do jogador que marcou o ponto
     */
    private void atribuirPonto(Peca.CorPeca corJogador) {
        if (corJogador == Peca.CorPeca.BRANCO) {
            pontuacaoBranco++;
        } else {
            pontuacaoPreto++;
        }
    }

    /**
     * Altera o turno ativo do jogo para o outro jogador, limpando todos os movimentos
     * que restavam da jogada anterior e redefinindo a flag de dados lançados do turno.
     */
    private void alternarTurno() {
        turnoAtual = turnoAtual == Peca.CorPeca.BRANCO ? Peca.CorPeca.PRETO : Peca.CorPeca.BRANCO;
        movimentosDisponiveis.clear();
        dadosLancadosNoTurno = false;
    }

    /**
     * Verifica se algum dos jogadores já retirou todas as peças do tabuleiro (vitória).
     * Caso um jogador tenha vencido, inicia os procedimentos de fim de jogo e desativa o servidor.
     */
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

    /**
     * Obtém o nome legível registado do cliente com base na cor de peças indicada.
     * 
     * @param cor A cor do jogador
     * @return O nome do jogador ou o nome padrão da cor caso não seja encontrado
     */
    private String obterNomeJogador(Peca.CorPeca cor) {
        synchronized (clientes) {
            for (ClientHandler cliente : clientes) {
                if (cliente.getCorJogador() == cor) return cliente.getNomeJogador();
            }
        }
        return cor.name();
    }

    /**
     * Difunde a mensagem de fim de jogo para ambos os clientes, indicando o nome do vencedor.
     * 
     * @param nomeVencedor O nome do jogador que ganhou a partida
     */
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

    /**
     * Obtém o nome do jogador que detém a vez ativa de jogar no turno corrente.
     * 
     * @return O nome do jogador ativo
     */
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

    /**
     * Remove um cliente da lista ativa de ligações do servidor e encerra a sua
     * ligação de rede individual.
     * 
     * @param cliente O manipulador de cliente a remover
     */
    private synchronized void removerCliente(ClientHandler cliente) {
        clientes.remove(cliente);
        cliente.fecharLigacao();
        log("Cliente " + cliente.getCorJogador() + " removido do servidor.");
    }

    /**
     * Solicita o encerramento completo e ordenado do servidor: desativa o loop de atividade,
     * fecha as ligações de rede de todos os clientes ligados e encerra o socket TCP de escuta.
     */
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

    /**
     * Serializa o estado completo da partida (tabuleiro, turno e pontuacoes)
     * e guarda-o num ficheiro no caminho fornecido.
     * 
     * @param caminho O caminho do ficheiro de destino (ex: "jogo_salvo.dat")
     */
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

    /**
     * Deserializa o estado da partida a partir do ficheiro indicado, restaurando
     * as pecas do tabuleiro, as barras centrais, pontuacoes e a vez do jogador ativo.
     * Realiza depois o broadcast do novo estado aos clientes ligados.
     * 
     * @param caminho O caminho do ficheiro de origem
     */
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

    /**
     * Classe interna que representa o manipulador de ligação de cada cliente (jogador).
     * Trata da receção de mensagens do cliente numa thread dedicada e do envio
     * de atualizações do estado do jogo.
     */
    private class ClientHandler extends Thread {
        /** O socket de ligação TCP do cliente. */
        private final Socket socket;
        /** A cor de peças atribuída ao jogador deste cliente. */
        private final Peca.CorPeca corJogador;
        /** O fluxo de entrada de objetos para ler mensagens do cliente. */
        private ObjectInputStream input;
        /** O fluxo de saída de objetos para enviar o estado do jogo ao cliente. */
        private ObjectOutputStream output;
        /** O nome legível do jogador, configurado pelo utilizador na interface. */
        private String nomeJogador;

        /**
         * Construtor do ClientHandler. Estabelece os fluxos de entrada e saída com o socket TCP.
         * 
         * @param socket O socket de comunicação com o cliente
         * @param corJogador A cor atribuída ao jogador correspondente
         * @throws IOException Caso ocorra um erro de I/O na inicialização dos fluxos
         */
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

        /**
         * Loop principal da thread de escuta do cliente.
         * Lê continuamente objetos serializeis {@link MensagemRede} enviados pelo cliente
         * e encaminha-os para o tratamento de mensagens no servidor.
         */
        @Override
        public void run() {
            try {
                fazerBroadcast();

                while (servidorAtivo && !socket.isClosed()) {
                    Object objetoRecebido = input.readObject();

                    if (objetoRecebido instanceof MensagemRede mensagem) {
                        if (mensagem.getNomeJogador() != null && !mensagem.getNomeJogador().isBlank()) {
                            nomeJogador = messageNomeJogador(mensagem);
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

        private String messageNomeJogador(MensagemRede mensagem) {
            return mensagem.getNomeJogador();
        }

        /**
         * Envia um pacote de estado do jogo atualizado especificamente para este cliente.
         * Personaliza o campo 'corAtribuida' do pacote para que o cliente saiba
         * qual é o seu lado no jogo.
         * 
         * @param pacote O pacote contendo o estado completo do jogo
         */
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

        /**
         * Encerra de forma segura todos os fluxos de rede (InputStream e OutputStream)
         * e fecha o socket associado a este cliente.
         */
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

        /**
         * Obtém a cor de peças atribuída a este cliente.
         * 
         * @return A cor do jogador
         */
        public Peca.CorPeca getCorJogador() {
            return corJogador;
        }

        /**
         * Obtém o nome registado para o jogador deste cliente.
         * 
         * @return O nome do jogador
         */
        public String getNomeJogador() {
            return nomeJogador;
        }
    }
}
