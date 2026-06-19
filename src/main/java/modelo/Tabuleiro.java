package modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa o tabuleiro do jogo de Gamao, composto pelos 24 campos normais
 * e pelas barras centralizadas das pecas brancas e pretas.
 */
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

    /**
     * Construtor do Tabuleiro.
     * Instancia os 24 campos normais, as duas barras e coloca as pecas na posicao inicial.
     */
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

    /**
     * Devolve a lista de todos os 24 campos normais do tabuleiro.
     * 
     * @return Lista de campos
     */
    public List<Campo> getCampos() {
        return campos;
    }

    /**
     * Obtem um campo especifico com base na sua posicao (1 a 24).
     * 
     * @param posicao A posicao do campo (1 a 24)
     * @return O campo correspondente
     * @throws IllegalArgumentException se a posicao estiver fora do intervalo 1-24
     */
    public Campo getCampo(int posicao) {
        if (posicao < 1 || posicao > TOTAL_CAMPOS) {
            throw new IllegalArgumentException("Posicao invalida: " + posicao);
        }
        return campos.get(posicao - 1);
    }

    // ── Acesso à barra ────────────────────────────────────────────────────

    /**
     * Obtem o campo especial que representa a barra das pecas Brancas.
     * 
     * @return O campo da barra das pecas brancas (ID 0)
     */
    public Campo getBarraBranco() {
        return barraBranco;
    }

    /**
     * Obtem o campo especial que representa a barra das pecas Pretas.
     * 
     * @return O campo da barra das pecas pretas (ID 25)
     */
    public Campo getBarraPreto() {
        return barraPreto;
    }

    /**
     * Devolve a barra correspondente a cor de peca indicada.
     * 
     * @param cor A cor do jogador
     * @return O campo da barra associado a essa cor
     */
    public Campo getBarra(Peca.CorPeca cor) {
        return cor == Peca.CorPeca.BRANCO ? barraBranco : barraPreto;
    }

    /**
     * Indica se um jogador tem pecas pendentes na barra central (aguardando reintroducao).
     * Quando true, este jogador fica restrito a realizar apenas jogadas de reintroducao.
     * 
     * @param cor A cor do jogador a verificar
     * @return true se tiver pecas na barra, false caso contrario
     */
    public boolean temPecasNaBarra(Peca.CorPeca cor) {
        return !getBarra(cor).isVazio();
    }

    /**
     * Verifica se todas as pecas ativas de um jogador se encontram no seu respetivo
     * quadrante final (casas 19-24 para as Brancas, e casas 1-6 para as Pretas).
     * Esta e uma condicao obrigatoria para que o jogador possa iniciar o processo de bearing off.
     * 
     * @param cor A cor do jogador a verificar
     * @return true se todas as pecas ativas estiverem no quadrante final, false caso contrario
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
     * Verifica se um jogador venceu a partida. Um jogador ganha quando ja retirou
     * todas as suas 15 pecas do jogo (ou seja, nao tem nenhuma peca nas 24 casas nem na barra).
     * 
     * @param cor A cor do jogador a verificar
     * @return true se o jogador venceu, false caso contrario
     */
    public boolean jogadorVenceu(Peca.CorPeca cor) {
        if (temPecasNaBarra(cor)) return false;
        for (Campo campo : campos) {
            if (!campo.isVazio() && campo.getCorDominante() == cor) return false;
        }
        return true;
    }

    // ── Resumo visual (texto) ─────────────────────────────────────────────

    /**
     * Gera um resumo visual textual do estado corrente do tabuleiro.
     * Exibe o numero de pecas em cada uma das 24 casas e nas duas barras.
     * 
     * @return String contendo a representacao textual do tabuleiro
     */
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