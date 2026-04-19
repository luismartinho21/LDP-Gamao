package gamao.modelo;

import java.io.Serializable;
import java.util.Stack;

/**
 * Representa um triângulo ou casa do tabuleiro.
 * Utiliza uma Stack para garantir que as peças são empilhadas e removidas por ordem.
 */
public class Campo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final Stack<Peca> pecas;

    public Campo(int id) {
        this.id = id;
        this.pecas = new Stack<>();
    }

    public void adicionarPeca(Peca p) {
        pecas.push(p);
    }

    public Peca removerPeca() {
        if (!pecas.isEmpty()) {
            return pecas.pop();
        }
        return null;
    }

    public Peca espreitarTopo() {
        if (!pecas.isEmpty()) {
            return pecas.peek();
        }
        return null;
    }

    public int getQuantidadePecas() {
        return pecas.size();
    }

    public Peca.CorPeca getCorDominante() {
        if (pecas.isEmpty()) return null;
        return pecas.peek().getCor();
    }

    public int getId() {
        return id;
    }

    public boolean isVazio() {
        return pecas.isEmpty();
    }
}