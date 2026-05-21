package modelo;

import java.io.Serializable;

public class Peca implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum CorPeca {
        BRANCO,
        PRETO
    }

    private final CorPeca cor;

    public Peca(CorPeca cor) {
        this.cor = cor;
    }

    public CorPeca getCor() {
        return cor;
    }

    @Override
    public String toString() {
        return "Peca[" + cor + "]";
    }
}
