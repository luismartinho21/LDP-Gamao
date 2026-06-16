package modelo;

import java.io.Serializable;
import java.util.Stack;

/**
 * Representa uma casa do tabuleiro de Gamao (ponto/casa normal ou barra).
 * Usa uma pilha interna para gerir as pecas empilhadas naquela posicao.
 */
public class Campo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final Stack<Peca> pecas;

    /**
     * Construtor do Campo.
     * 
     * @param id O identificador unico da casa (1-24 para casas normais, 0 ou 25 para as barras)
     */
    public Campo(int id) {
        this.id = id;
        this.pecas = new Stack<>();
    }

    /**
     * Adiciona uma peca ao topo desta casa.
     * 
     * @param peca A peca a empilhar
     */
    public void adicionarPeca(Peca peca) {
        pecas.push(peca);
    }

    /**
     * Remove e devolve a peca que esta no topo desta casa.
     * 
     * @return A peca removida, ou null se a casa estiver vazia
     */
    public Peca removerPeca() {
        if (pecas.isEmpty()) {
            return null;
        }
        return pecas.pop();
    }

    /**
     * Permite inspecionar a peca do topo sem a remover.
     * 
     * @return A peca no topo, ou null se a casa estiver vazia
     */
    public Peca espreitarTopo() {
        if (pecas.isEmpty()) {
            return null;
        }
        return pecas.peek();
    }

    /**
     * Devolve a quantidade de pecas atualmente empilhadas nesta casa.
     * 
     * @return O numero total de pecas
     */
    public int getQuantidadePecas() {
        return pecas.size();
    }

    /**
     * Obtem a cor dominante das pecas nesta casa (a cor da peca do topo).
     * 
     * @return A cor dominante, ou null se a casa estiver vazia
     */
    public Peca.CorPeca getCorDominante() {
        if (pecas.isEmpty()) {
            return null;
        }
        return pecas.peek().getCor();
    }

    /**
     * Obtem o identificador unico desta casa.
     * 
     * @return O ID da casa
     */
    public int getId() {
        return id;
    }

    /**
     * Indica se a casa nao contem qualquer peca.
     * 
     * @return true se estiver vazia, false caso contrario
     */
    public boolean isVazio() {
        return pecas.isEmpty();
    }
}
