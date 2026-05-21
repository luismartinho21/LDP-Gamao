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
    private static final int PORTA = 8080;
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
            System.out.println("Servidor de Gamão ativo na porta " + porta + ".");
            aguardarLigacaoDosClientes();
            manterServidorAtivo();
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread principal do servidor interrompida.");
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

            System.out.println("Cliente ligado como jogador " + corAtribuida + ".");
        }

        System.out.println("Dois clientes ligados. A partida pode comecar.");
        fazerBroadcast();
    }

    private void manterServidorAtivo() throws InterruptedException {
        while (servidorAtivo) {
            Thread.sleep(200L);
        }
    }

    public synchronized void fazerBroadcast() {
        PacoteEstadoJogo pacote = new PacoteEstadoJogo(
                tabuleiro,
                pontuacaoBranco,
                pontuacaoPreto,
                turnoAtual,
                obterNomeJogadorDoTurno());

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
            System.out.println("Mensagem ignorada: nao e o turno de " + origemCliente.getCorJogador() + ".");
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
            default:
                break;
        }
    }

    private void processarLancamentoDados(ClientHandler cliente) {
        int valorUm = dadoUm.lancar();
        int valorDois = dadoDois.lancar();
        System.out.println("Jogador " + cliente.getCorJogador() + " lancou os dados: " + valorUm + " e " + valorDois + ".");
        fazerBroadcast();
    }

    private void processarMovimento(ClientHandler cliente, MensagemRede mensagem) {
        Integer origem = mensagem.getOrigem();
        Integer destino = mensagem.getDestino();

        if (origem == null || destino == null) {
            System.out.println("Movimento ignorado: origem ou destino em falta.");
            return;
        }

        if (!posicaoValida(origem) || !posicaoValida(destino)) {
            System.out.println("Movimento ignorado: posicao invalida.");
            return;
        }

        Campo campoOrigem = tabuleiro.getCampo(origem);
        Campo campoDestino = tabuleiro.getCampo(destino);

        if (campoOrigem.isVazio()) {
            System.out.println("Movimento ignorado: campo de origem vazio.");
            return;
        }

        Peca pecaTopo = campoOrigem.espreitarTopo();
        if (pecaTopo == null || pecaTopo.getCor() != cliente.getCorJogador()) {
            System.out.println("Movimento ignorado: a peca nao pertence ao jogador atual.");
            return;
        }

        /*
         * Regra simplificada de transporte:
         * - permite mover para campo vazio;
         * - permite mover para campo com pecas da mesma cor;
         * - permite capturar se existir exatamente uma peca adversaria.
         *
         * O refinamento completo das regras de Gamão pode ser acrescentado depois
         * na camada de modelo, mantendo o mesmo protocolo de rede.
         */
        if (!campoDestino.isVazio()
                && campoDestino.getCorDominante() != cliente.getCorJogador()
                && campoDestino.getQuantidadePecas() > 1) {
            System.out.println("Movimento ignorado: destino bloqueado pelo adversario.");
            return;
        }

        if (!campoDestino.isVazio()
                && campoDestino.getCorDominante() != cliente.getCorJogador()
                && campoDestino.getQuantidadePecas() == 1) {
            campoDestino.removerPeca();
            atribuirPonto(cliente.getCorJogador());
        }

        campoDestino.adicionarPeca(campoOrigem.removerPeca());
        alternarTurno();
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
        System.out.println("Cliente " + cliente.getCorJogador() + " removido do servidor.");
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
                System.err.println("Erro ao fechar o ServerSocket: " + e.getMessage());
            }
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
                enviarPacote(new PacoteEstadoJogo(
                        tabuleiro,
                        pontuacaoBranco,
                        pontuacaoPreto,
                        turnoAtual,
                        obterNomeJogadorDoTurno()));

                while (servidorAtivo && !socket.isClosed()) {
                    Object objetoRecebido = input.readObject();

                    if (objetoRecebido instanceof MensagemRede mensagem) {
                        if (mensagem.getNomeJogador() != null && !mensagem.getNomeJogador().isBlank()) {
                            nomeJogador = mensagem.getNomeJogador();
                        }
                        processarMensagem(this, mensagem);
                    }
                }
            } catch (EOFException e) {
                System.out.println("Ligacao terminada pelo cliente " + corJogador + ".");
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Erro no cliente " + corJogador + ": " + e.getMessage());
            } finally {
                removerCliente(this);
            }
        }

        public void enviarPacote(PacoteEstadoJogo pacote) {
            try {
                synchronized (output) {
                    output.reset();
                    output.writeObject(pacote);
                    output.flush();
                }
            } catch (IOException e) {
                System.err.println("Erro ao enviar estado ao cliente " + corJogador + ": " + e.getMessage());
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
