package gamao.modelo;

import java.util.Random;
import java.io.Serializable;

/**
 * Representa um dado de 6 faces usado no jogo.
 */
public class Dado implements Serializable {
    private static final long serialVersionUID = 1L;

    private int valor;
    private final Random gerador;

    public Dado() {
        this.gerador = new Random();
        lancar(); // Rola o dado logo quando é criado
    }

    public int lancar() {
        // Gera um número aleatório de 1 a 6
        this.valor = gerador.nextInt(6) + 1;
        return this.valor;
    }

    public int getValor() {
        return valor;
    }
}