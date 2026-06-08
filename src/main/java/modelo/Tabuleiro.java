package modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Tabuleiro implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int TOTAL_CAMPOS = 24;

    private final List<Campo> campos;
    private int pecasBarraBranco = 0;
    private int pecasBarraPreto = 0;

    public Tabuleiro() {
        this.campos = new ArrayList<>(TOTAL_CAMPOS);
        for (int i = 1; i <= TOTAL_CAMPOS; i++) {
            campos.add(new Campo(i));
        }
        configurarPosicaoInicial();
    }

    public List<Campo> getCampos() {
        return campos;
    }

    public Campo getCampo(int posicao) {
        if (posicao < 1 || posicao > TOTAL_CAMPOS) {
            throw new IllegalArgumentException("Posicao invalida: " + posicao);
        }
        return campos.get(posicao - 1);
    }

    public String gerarResumoVisual() {
        StringBuilder resumo = new StringBuilder();
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
        return resumo.toString();
    }

    private void configurarPosicaoInicial() {
        colocarPecas(1, Peca.CorPeca.BRANCO, 2);
        colocarPecas(12, Peca.CorPeca.BRANCO, 5);
        colocarPecas(17, Peca.CorPeca.BRANCO, 3);
        colocarPecas(19, Peca.CorPeca.BRANCO, 5);

        colocarPecas(24, Peca.CorPeca.PRETO, 2);
        colocarPecas(13, Peca.CorPeca.PRETO, 5);
        colocarPecas(8, Peca.CorPeca.PRETO, 3);
        colocarPecas(6, Peca.CorPeca.PRETO, 5);
    }

    private void colocarPecas(int posicao, Peca.CorPeca cor, int quantidade) {
        Campo campo = getCampo(posicao);
        for (int i = 0; i < quantidade; i++) {
            campo.adicionarPeca(new Peca(cor));
        }
    }

    public int getPecasBarraBranco() {
        return pecasBarraBranco;
    }

    public void setPecasBarraBranco(int pecasBarraBranco) {
        this.pecasBarraBranco = pecasBarraBranco;
    }

    public int getPecasBarraPreto() {
        return pecasBarraPreto;
    }

    public void setPecasBarraPreto(int pecasBarraPreto) {
        this.pecasBarraPreto = pecasBarraPreto;
    }

    public void adicionarPecaBarra(Peca.CorPeca cor) {
        if (cor == Peca.CorPeca.BRANCO) {
            pecasBarraBranco++;
        } else if (cor == Peca.CorPeca.PRETO) {
            pecasBarraPreto++;
        }
    }

    public void removerPecaBarra(Peca.CorPeca cor) {
        if (cor == Peca.CorPeca.BRANCO) {
            if (pecasBarraBranco > 0) {
                pecasBarraBranco--;
            }
        } else if (cor == Peca.CorPeca.PRETO) {
            if (pecasBarraPreto > 0) {
                pecasBarraPreto--;
            }
        }
    }

    public boolean temPecasBarra(Peca.CorPeca cor) {
        return cor == Peca.CorPeca.BRANCO ? pecasBarraBranco > 0 : pecasBarraPreto > 0;
    }
}
