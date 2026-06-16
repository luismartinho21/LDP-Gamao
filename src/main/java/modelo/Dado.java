package modelo;

import java.io.Serializable;
import java.util.Random;

/**
 * Representa um dado de seis faces utilizado para determinar o numero
 * de casas que as pecas se podem deslocar no jogo de Gamao.
 */
public class Dado implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Random gerador;
    private int valor;

    /**
     * Construtor do Dado. Inicializa o gerador aleatorio e realiza
     * um lancamento inicial.
     */
    public Dado() {
        this.gerador = new Random();
        lancar();
    }

    /**
     * Simula o lancamento do dado, gerando um valor pseudo-aleatorio entre 1 e 6.
     * 
     * @return O valor gerado (entre 1 e 6)
     */
    public int lancar() {
        valor = gerador.nextInt(6) + 1;
        return valor;
    }

    /**
     * Obtem o valor atual do dado obtido no ultimo lancamento.
     * 
     * @return O valor atual do dado (1 a 6)
     */
    public int getValor() {
        return valor;
    }
}
