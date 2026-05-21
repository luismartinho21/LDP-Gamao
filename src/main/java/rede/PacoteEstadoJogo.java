package rede;

import java.io.Serializable;
import modelo.Peca;
import modelo.Tabuleiro;

public class PacoteEstadoJogo implements Serializable {
    private static final long serialVersionUID = 1L;

    protected Tabuleiro tabuleiroSnapshot;
    protected int pontuacaoBranco;
    protected int pontuacaoPreto;
    protected Peca.CorPeca turnoAtual;
    protected String nomeJogadorTurno;

    public PacoteEstadoJogo() {
    }

    public PacoteEstadoJogo(Tabuleiro tabuleiroSnapshot, int pontuacaoBranco, int pontuacaoPreto,
            Peca.CorPeca turnoAtual, String nomeJogadorTurno) {
        this.tabuleiroSnapshot = tabuleiroSnapshot;
        this.pontuacaoBranco = pontuacaoBranco;
        this.pontuacaoPreto = pontuacaoPreto;
        this.turnoAtual = turnoAtual;
        this.nomeJogadorTurno = nomeJogadorTurno;
    }

    public Tabuleiro getTabuleiroSnapshot() {
        return tabuleiroSnapshot;
    }

    public void setTabuleiroSnapshot(Tabuleiro tabuleiroSnapshot) {
        this.tabuleiroSnapshot = tabuleiroSnapshot;
    }

    public int getPontuacaoBranco() {
        return pontuacaoBranco;
    }

    public void setPontuacaoBranco(int pontuacaoBranco) {
        this.pontuacaoBranco = pontuacaoBranco;
    }

    public int getPontuacaoPreto() {
        return pontuacaoPreto;
    }

    public void setPontuacaoPreto(int pontuacaoPreto) {
        this.pontuacaoPreto = pontuacaoPreto;
    }

    public Peca.CorPeca getTurnoAtual() {
        return turnoAtual;
    }

    public void setTurnoAtual(Peca.CorPeca turnoAtual) {
        this.turnoAtual = turnoAtual;
    }

    public String getNomeJogadorTurno() {
        return nomeJogadorTurno;
    }

    public void setNomeJogadorTurno(String nomeJogadorTurno) {
        this.nomeJogadorTurno = nomeJogadorTurno;
    }
}
