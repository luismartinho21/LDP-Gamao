package rede;

import java.io.Serializable;

/**
 * Objeto DTO (Data Transfer Object) usado para enviar comandos e jogadas
 * do cliente para o servidor de jogo TCP.
 */
public class MensagemRede implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * O tipo de comando ou ação de rede a ser transmitido e interpretado pelo servidor.
     */
    protected TipoMensagem tipoMensagem;

    /**
     * O nome identificador do jogador que iniciou a ação de rede.
     */
    protected String nomeJogador;

    /**
     * A posição de origem no tabuleiro (índice de 1 a 24) da peça a mover.
     * Pode ser null se a mensagem não representar uma jogada.
     */
    protected Integer origem;

    /**
     * A posição de destino no tabuleiro (índice de 1 a 24) ou na saída (0 ou 25 para bearing off).
     * Pode ser null se a mensagem não representar uma jogada.
     */
    protected Integer destino;

    /**
     * Construtor por defeito da MensagemRede.
     */
    public MensagemRede() {
    }

    /**
     * Construtor completo da MensagemRede.
     * 
     * @param tipoMensagem O tipo de comando de rede a enviar
     * @param nomeJogador O nome do jogador que realiza a acao
     * @param origem O index da casa de origem (null se nao aplicavel)
     * @param destino O index da casa de destino (null se nao aplicavel)
     */
    public MensagemRede(TipoMensagem tipoMensagem, String nomeJogador, Integer origem, Integer destino) {
        this.tipoMensagem = tipoMensagem;
        this.nomeJogador = nomeJogador;
        this.origem = origem;
        this.destino = destino;
    }

    /**
     * Obtem o tipo de comando da mensagem.
     * 
     * @return O tipo de mensagem
     */
    public TipoMensagem getTipoMensagem() {
        return tipoMensagem;
    }

    /**
     * Define o tipo de comando da mensagem.
     * 
     * @param tipoMensagem O tipo de mensagem
     */
    public void setTipoMensagem(TipoMensagem tipoMensagem) {
        this.tipoMensagem = tipoMensagem;
    }

    /**
     * Obtem o nome do jogador remetente.
     * 
     * @return O nome do jogador
     */
    public String getNomeJogador() {
        return nomeJogador;
    }

    /**
     * Define o nome do jogador remetente.
     * 
     * @param nomeJogador O nome do jogador
     */
    public void setNomeJogador(String nomeJogador) {
        this.nomeJogador = nomeJogador;
    }

    /**
     * Obtem a casa de origem da jogada.
     * 
     * @return O ID da casa de origem, ou null
     */
    public Integer getOrigem() {
        return origem;
    }

    /**
     * Define a casa de origem da jogada.
     * 
     * @param origem O ID da casa de origem
     */
    public void setOrigem(Integer origem) {
        this.origem = origem;
    }

    /**
     * Obtem a casa de destino da jogada.
     * 
     * @return O ID da casa de destino, ou null
     */
    public Integer getDestino() {
        return destino;
    }

    /**
     * Define a casa de destino da jogada.
     * 
     * @param destino O ID da casa de destino
     */
    public void setDestino(Integer destino) {
        this.destino = destino;
    }

    /**
     * Tipos de comandos/acoes de controlo que podem ser comunicados na rede.
     */
    public enum TipoMensagem {
        /** Solicitacao para lancar os dados do turno. */
        LANCAR_DADOS,
        /** Solicitacao para deslocar uma peca. */
        MOVER_PECA,
        /** Solicitacao para passar a vez de jogar para o oponente. */
        PASSAR_TURNO,
        /** Comunicacao de desconexao voluntaria do cliente. */
        DESCONECTAR,
        /** Solicitacao para salvar o estado da partida. */
        GUARDAR_JOGO
    }
}
