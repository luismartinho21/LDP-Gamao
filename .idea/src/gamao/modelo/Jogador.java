package gamao.modelo;

import java.io.Serializable;

/**
 * Representa um jogador no jogo de Gamão.
 * Armazena a identidade, a cor das peças e o progresso no match.
 */
public class Jogador implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nome;
    private final Peca.CorPeca cor;
    private int pontuacao;

    /**
     * Constrói um jogador com um nome e cor da peça.
     * * @param nome O nome do jogador.
     * @param cor  A cor das peças (BRANCO ou PRETO).
     */
    public Jogador(String nome, Peca.CorPeca cor) {
        this.nome = nome;
        this.cor = cor;
        this.pontuacao = 0;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Peca.CorPeca getCor() {
        return cor;
    }

    public int getPontuacao() {
        return pontuacao;
    }

    public void adicionarPontos(int pontos) {
        this.pontuacao += pontos;
    }

    public void resetPontuacao() {
        this.pontuacao = 0;
    }

    @Override
    public String toString() {
        return nome + " (" + cor + ")";
    }
}