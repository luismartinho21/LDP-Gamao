package modelo;

import java.io.Serializable;
import java.util.Random;

public class Dado implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Random gerador;
    private int valor;

    public Dado() {
        this.gerador = new Random();
        lancar();
    }

    public int lancar() {
        valor = gerador.nextInt(6) + 1;
        return valor;
    }

    public int getValor() {
        return valor;
    }
}
