package modelo;

import java.io.Serializable;

/**
 * Representa uma peca individual do jogo de Gamao.
 * Armazena a cor associada ao jogador proprietario.
 */
public class Peca implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Cores possiveis das pecas no jogo.
     */
    public enum CorPeca {
        BRANCO,
        PRETO
    }

    private final CorPeca cor;

    /**
     * Construtor da peca com uma cor definida.
     * 
     * @param cor A cor atribuida a peca (BRANCO ou PRETO)
     */
    public Peca(CorPeca cor) {
        this.cor = cor;
    }

    /**
     * Devolve a cor da peca.
     * 
     * @return A cor da peca (BRANCO ou PRETO)
     */
    public CorPeca getCor() {
        return cor;
    }

    @Override
    public String toString() {
        return "Peca[" + cor + "]";
    }
}
