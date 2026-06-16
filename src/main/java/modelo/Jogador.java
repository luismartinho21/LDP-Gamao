package modelo;

import java.io.Serializable;

/**
 * Representa um jogador participante na partida de Gamao.
 * Armazena o nome, a cor atribuida e a pontuacao obtida.
 */
public class Jogador implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nome;
    private final Peca.CorPeca cor;
    private int pontuacao;

    /**
     * Construtor da classe Jogador.
     * 
     * @param nome O nome do jogador
     * @param cor A cor atribuida ao jogador (BRANCO ou PRETO)
     */
    public Jogador(String nome, Peca.CorPeca cor) {
        this.nome = nome;
        this.cor = cor;
        this.pontuacao = 0;
    }

    /**
     * Obtem o nome do jogador.
     * 
     * @return O nome do jogador
     */
    public String getNome() {
        return nome;
    }

    /**
     * Define um novo nome para o jogador.
     * 
     * @param nome O novo nome
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Obtem a cor associada a este jogador.
     * 
     * @return A cor do jogador
     */
    public Peca.CorPeca getCor() {
        return cor;
    }

    /**
     * Obtem a pontuacao acumulada pelo jogador.
     * 
     * @return A pontuacao atual
     */
    public int getPontuacao() {
        return pontuacao;
    }

    /**
     * Incrementa a pontuacao do jogador com uma determinada quantidade de pontos.
     * 
     * @param pontos A quantidade de pontos a adicionar
     */
    public void adicionarPontos(int pontos) {
        pontuacao += pontos;
    }

    /**
     * Reseta a pontuacao do jogador para zero.
     */
    public void resetPontuacao() {
        pontuacao = 0;
    }

    @Override
    public String toString() {
        return nome + " (" + cor + ")";
    }
}
