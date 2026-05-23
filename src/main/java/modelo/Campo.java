package modelo;

import java.io.Serializable;
import java.util.Stack;

public class Campo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final Stack<Peca> pecas;

    public Campo(int id) {
        this.id = id;
        this.pecas = new Stack<>();
    }

    public void adicionarPeca(Peca peca) {
        pecas.push(peca);
    }

    public Peca removerPeca() {
        if (pecas.isEmpty()) {
            return null;
        }
        return pecas.pop();
    }

    public Peca espreitarTopo() {
        if (pecas.isEmpty()) {
            return null;
        }
        return pecas.peek();
    }

    public int getQuantidadePecas() {
        return pecas.size();
    }

    public Peca.CorPeca getCorDominante() {
        if (pecas.isEmpty()) {
            return null;
        }
        return pecas.peek().getCor();
    }

    public int getId() {
        return id;
    }

    public boolean isVazio() {
        return pecas.isEmpty();
    }
}
