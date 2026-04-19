package gamao.modelo;

import java.io.Serializable;

/**
 * Representa uma peça (dama) do jogo de Gamão.
 * Implementa Serializable para permitir o Save/Load do jogo e a transmissão
 * nativa via Sockets através da rede.
 */
public class Peca implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Enumeração das cores possíveis para a peça (identifica o jogador dono).
     */
    public enum CorPeca {
        BRANCO,
        PRETO
    }

    private final CorPeca cor;

    /**
     * Construtor da Peça.
     * * @param cor a cor da peça que designa a qual jogador esta pertence.
     */
    public Peca(CorPeca cor) {
        this.cor = cor;
    }

    /**
     * Obtém a cor atual desta peça.
     * * @return a cor da peça.
     */
    public CorPeca getCor() {
        return cor;
    }

    @Override
    public String toString() {
        return "Peca[" + cor + "]";
    }
}