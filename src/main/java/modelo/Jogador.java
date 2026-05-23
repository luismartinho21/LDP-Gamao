package modelo;

import java.io.Serializable;

public class Jogador implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nome;
    private final Peca.CorPeca cor;
    private int pontuacao;

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
        pontuacao += pontos;
    }

    public void resetPontuacao() {
        pontuacao = 0;
    }

    @Override
    public String toString() {
        return nome + " (" + cor + ")";
    }
}
