package modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Tabuleiro implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int TOTAL_CAMPOS = 24;

    private final List<Campo> campos;

    // ── Barra central (peças capturadas aguardam reintrodução) ────────────
    // Cada cor tem a sua própria barra — Campo com id 0 (brancas) e 25 (pretas)
    // Os ids 0 e 25 são convenção clássica do backgammon e não colidem com os
    // 24 campos normais.
    private final Campo barraBranco;
    private final Campo barraPreto;

    public Tabuleiro() {
        this.campos = new ArrayList<>(TOTAL_CAMPOS);
        for (int i = 1; i <= TOTAL_CAMPOS; i++) {
            campos.add(new Campo(i));
        }
        this.barraBranco = new Campo(0);
        this.barraPreto  = new Campo(25);
        configurarPosicaoInicial();
    }

    // ── Acesso aos 24 campos normais ──────────────────────────────────────

    public List<Campo> getCampos() {
        return campos;
    }

    public Campo getCampo(int posicao) {
        if (posicao < 1 || posicao > TOTAL_CAMPOS) {
            throw new IllegalArgumentException("Posicao invalida: " + posicao);
        }
        return campos.get(posicao - 1);
    }

    // ── Acesso à barra ────────────────────────────────────────────────────

    public Campo getBarraBranco() {
        return barraBranco;
    }

    public Campo getBarraPreto() {
        return barraPreto;
    }

    /**
     * Devolve a barra da cor indicada.
     * Conveniência para o Servidor não precisar de fazer if/else.
     */
    public Campo getBarra(Peca.CorPeca cor) {
        return cor == Peca.CorPeca.BRANCO ? barraBranco : barraPreto;
    }

    /**
     * Indica se um jogador tem peças na barra.
     * Quando true, esse jogador SÓ pode jogar a reintrodução.
     */
    public boolean temPecasNaBarra(Peca.CorPeca cor) {
        return !getBarra(cor).isVazio();
    }
    /**
     * Verifica se todas as peças de um jogador estão no quadrante final.
     * Brancas: casas 19-24 | Pretas: casas 1-6
     * Condição necessária para poder fazer bearing off.
     */
    public boolean todasPecasNoQuadranteFinal(Peca.CorPeca cor) {
        if (temPecasNaBarra(cor)) return false;

        for (Campo campo : campos) {
            if (!campo.isVazio() && campo.getCorDominante() == cor) {
                int id = campo.getId();
                if (cor == Peca.CorPeca.BRANCO && id < 19) return false;
                if (cor == Peca.CorPeca.PRETO  && id > 6)  return false;
            }
        }
        return true;
    }

    /**
     * Verifica se um jogador ganhou — sem peças no tabuleiro nem na barra.
     */
    public boolean jogadorVenceu(Peca.CorPeca cor) {
        if (temPecasNaBarra(cor)) return false;
        for (Campo campo : campos) {
            if (!campo.isVazio() && campo.getCorDominante() == cor) return false;
        }
        return true;
    }

    // ── Resumo visual (texto) ─────────────────────────────────────────────

    public String gerarResumoVisual() {
        StringBuilder resumo = new StringBuilder();

        // Barra das brancas
        resumo.append("BARRA B[")
                .append(barraBranco.getQuantidadePecas())
                .append("]  ");

        for (Campo campo : campos) {
            String conteudo = "--";
            if (!campo.isVazio()) {
                String cor = campo.getCorDominante() == Peca.CorPeca.BRANCO ? "B" : "P";
                conteudo = cor + campo.getQuantidadePecas();
            }
            resumo.append(String.format("%02d[%s]", campo.getId(), conteudo));
            if (campo.getId() < TOTAL_CAMPOS) {
                resumo.append(campo.getId() == 12 ? System.lineSeparator() : " ");
            }
        }

        // Barra das pretas
        resumo.append("  BARRA P[")
                .append(barraPreto.getQuantidadePecas())
                .append("]");

        return resumo.toString();
    }

    // ── Posição inicial ───────────────────────────────────────────────────

    private void configurarPosicaoInicial() {
        colocarPecas(1,  Peca.CorPeca.BRANCO, 2);
        colocarPecas(12, Peca.CorPeca.BRANCO, 5);
        colocarPecas(17, Peca.CorPeca.BRANCO, 3);
        colocarPecas(19, Peca.CorPeca.BRANCO, 5);

        colocarPecas(24, Peca.CorPeca.PRETO, 2);
        colocarPecas(13, Peca.CorPeca.PRETO, 5);
        colocarPecas(8,  Peca.CorPeca.PRETO, 3);
        colocarPecas(6,  Peca.CorPeca.PRETO, 5);
    }

    private void colocarPecas(int posicao, Peca.CorPeca cor, int quantidade) {
        Campo campo = getCampo(posicao);
        for (int i = 0; i < quantidade; i++) {
            campo.adicionarPeca(new Peca(cor));
        }
    }
}