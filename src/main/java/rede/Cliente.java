package rede;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Gere a ligacao socket TCP do lado do cliente/jogador.
 * Estabelece a conexao com o servidor e escuta atualizacoes de estado em segundo plano.
 */
public class Cliente {
    private final String ipServidor;
    private final int portaServidor;
    private final AtualizadorInterface atualizadorInterface;

    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private Thread threadEscuta;
    private volatile boolean ligado;

    /**
     * Construtor da classe Cliente.
     * 
     * @param ipServidor O IP do servidor de jogo
     * @param portaServidor A porta TCP do servidor
     * @param atualizadorInterface O callback para atualizar a UI JavaFX
     */
    public Cliente(String ipServidor, int portaServidor, AtualizadorInterface atualizadorInterface) {
        this.ipServidor = ipServidor;
        this.portaServidor = portaServidor;
        this.atualizadorInterface = atualizadorInterface;
    }

    /**
     * Tenta estabelecer a ligacao socket TCP com o servidor de jogo.
     * Cria os fluxos de leitura e escrita e inicia a escuta em background.
     */
    public void ligar() {
        try {
            /*
             * A porta nao e fixa nesta classe: ela e recebida no construtor,
             * permitindo que o utilizador escolha o porto de ligacao.
             */
            socket = new Socket(ipServidor, portaServidor);

            /*
             * O fluxo de saida e criado primeiro para evitar problemas no
             * handshake de serializacao com o servidor.
             */
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());

            ligado = true;
            iniciarEscutaEmBackground();

            System.out.println("Cliente ligado ao servidor " + ipServidor + ":" + portaServidor + ".");
        } catch (IOException e) {
            System.err.println("Nao foi possivel ligar ao servidor: " + e.getMessage());
            fecharLigacao();
        }
    }

    /**
     * Envia assincronamente uma mensagem de rede para o servidor de jogo.
     * 
     * @param mensagem A mensagem de rede (DTO) a enviar
     */
    public synchronized void enviarMensagem(MensagemRede mensagem) {
        if (!ligado || mensagem == null) {
            return;
        }

        try {
            output.writeObject(mensagem);
            output.flush();
        } catch (IOException e) {
            System.err.println("Erro ao enviar mensagem para o servidor: " + e.getMessage());
            fecharLigacao();
        }
    }

    private void iniciarEscutaEmBackground() {
        /*
         * Esta thread fica permanentemente a escutar o servidor sem bloquear
         * a thread principal da aplicacao nem a interface grafica.
         */
        threadEscuta = new Thread(() -> {
            try {
                while (ligado && socket != null && !socket.isClosed()) {
                    Object objetoRecebido = input.readObject();

                    if (objetoRecebido instanceof PacoteEstadoJogo pacoteEstadoJogo) {
                        processarEstadoRecebido(pacoteEstadoJogo);
                    }
                }
            } catch (EOFException e) {
                System.out.println("Ligacao ao servidor terminada.");
            } catch (IOException e) {
                if (ligado) {
                    System.err.println("Erro ao receber dados do servidor: " + e.getMessage());
                }
            } catch (ClassNotFoundException e) {
                System.err.println("Objeto desconhecido recebido do servidor: " + e.getMessage());
            } finally {
                fecharLigacao();
            }
        }, "Cliente-Escuta-Servidor");

        threadEscuta.setDaemon(true);
        threadEscuta.start();
    }

    private void processarEstadoRecebido(PacoteEstadoJogo pacoteEstadoJogo) {
        /*
         * O Cliente nao deve conhecer diretamente os detalhes da interface.
         * Em vez disso, notifica um callback para que a camada de vista/controlador
         * atualize os componentes graficos com o novo estado do jogo.
         */
        if (atualizadorInterface != null) {
            atualizadorInterface.atualizarEstado(pacoteEstadoJogo);
        }
    }

    /**
     * Encerra todos os fluxos de rede (leitura/escrita) e o socket de ligacao de forma segura.
     */
    public synchronized void fecharLigacao() {
        ligado = false;

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
     * Indica se a ligacao de rede se encontra ativa.
     * 
     * @return true se estiver ligado, false caso contrario
     */
    public boolean isLigado() {
        return ligado;
    }

    /**
     * Interface de callback usada para desacoplar a camada de rede da camada visual (UI).
     */
    public interface AtualizadorInterface {
        /**
         * Metodo invocado quando e recebido um novo estado de jogo enviado pelo servidor.
         * 
         * @param pacoteEstadoJogo O novo pacote contendo o estado atualizado da partida
         */
        void atualizarEstado(PacoteEstadoJogo pacoteEstadoJogo);
    }
}
