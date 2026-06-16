package rede;

import java.io.Serializable;

public class MensagemRede implements Serializable {
    private static final long serialVersionUID = 1L;

    protected TipoMensagem tipoMensagem;
    protected String nomeJogador;
    protected Integer origem;
    protected Integer destino;

    public MensagemRede() {
    }

    public MensagemRede(TipoMensagem tipoMensagem, String nomeJogador, Integer origem, Integer destino) {
        this.tipoMensagem = tipoMensagem;
        this.nomeJogador = nomeJogador;
        this.origem = origem;
        this.destino = destino;
    }

    public TipoMensagem getTipoMensagem() {
        return tipoMensagem;
    }

    public void setTipoMensagem(TipoMensagem tipoMensagem) {
        this.tipoMensagem = tipoMensagem;
    }

    public String getNomeJogador() {
        return nomeJogador;
    }

    public void setNomeJogador(String nomeJogador) {
        this.nomeJogador = nomeJogador;
    }

    public Integer getOrigem() {
        return origem;
    }

    public void setOrigem(Integer origem) {
        this.origem = origem;
    }

    public Integer getDestino() {
        return destino;
    }

    public void setDestino(Integer destino) {
        this.destino = destino;
    }

    public enum TipoMensagem {
        LANCAR_DADOS,
        MOVER_PECA,
        PASSAR_TURNO,
        DESCONECTAR,
        GUARDAR_JOGO
    }
}
