package rede;

import java.io.Serializable;
import modelo.Peca;
import modelo.Tabuleiro;

/**
 * Objeto DTO (Data Transfer Object) de sincronizacao de estado.
 * Transmite o instantaneo completo do jogo do servidor para os clientes
 * a cada alteracao do estado da partida.
 */
public class PacoteEstadoJogo implements Serializable {
    private static final long serialVersionUID = 1L;

    protected Tabuleiro tabuleiroSnapshot;
    protected int pontuacaoBranco;
    protected int pontuacaoPreto;
    protected Peca.CorPeca turnoAtual;
    protected String nomeJogadorTurno;
    protected String nomeJogadorBranco;
    protected String nomeJogadorPreto;
    protected int valorDadoUm;
    protected int valorDadoDois;
    protected Peca.CorPeca corAtribuida;
    protected java.util.List<Integer> movimentosDisponiveis = new java.util.ArrayList<>();
    protected boolean dadosLancados;
    protected String nomeVencedor;

    /**
     * Obtem o nome do vencedor do jogo (caso a partida tenha terminado).
     * 
     * @return O nome do vencedor, ou null se a partida ainda estiver ativa
     */
    public String getNomeVencedor() {
        return nomeVencedor;
    }

    /**
     * Define o nome do vencedor do jogo.
     * 
     * @param nomeVencedor O nome do vencedor
     */
    public void setNomeVencedor(String nomeVencedor) {
        this.nomeVencedor = nomeVencedor;
    }

    /**
     * Construtor por defeito do PacoteEstadoJogo.
     */
    public PacoteEstadoJogo() {
    }

    /**
     * Construtor parcial do PacoteEstadoJogo.
     * 
     * @param tabuleiroSnapshot O instantaneo do tabuleiro
     * @param pontuacaoBranco A pontuacao do jogador Branco
     * @param pontuacaoPreto A pontuacao do jogador Preto
     * @param turnoAtual A cor do jogador que tem a vez
     * @param nomeJogadorTurno O nome do jogador que tem a vez
     */
    public PacoteEstadoJogo(Tabuleiro tabuleiroSnapshot, int pontuacaoBranco, int pontuacaoPreto,
            Peca.CorPeca turnoAtual, String nomeJogadorTurno) {
        this.tabuleiroSnapshot = tabuleiroSnapshot;
        this.pontuacaoBranco = pontuacaoBranco;
        this.pontuacaoPreto = pontuacaoPreto;
        this.turnoAtual = turnoAtual;
        this.nomeJogadorTurno = nomeJogadorTurno;
    }

    /**
     * Construtor intermedio do PacoteEstadoJogo.
     * 
     * @param tabuleiroSnapshot O instantaneo do tabuleiro
     * @param pontuacaoBranco A pontuacao do jogador Branco
     * @param pontuacaoPreto A pontuacao do jogador Preto
     * @param turnoAtual A cor do jogador que tem a vez
     * @param nomeJogadorTurno O nome do jogador que tem a vez
     * @param nomeJogadorBranco O nome do jogador Branco
     * @param nomeJogadorPreto O nome do jogador Preto
     * @param valorDadoUm O valor do primeiro dado do turno
     * @param valorDadoDois O valor do segundo dado do turno
     */
    public PacoteEstadoJogo(Tabuleiro tabuleiroSnapshot, int pontuacaoBranco, int pontuacaoPreto,
            Peca.CorPeca turnoAtual, String nomeJogadorTurno, String nomeJogadorBranco,
            String nomeJogadorPreto, int valorDadoUm, int valorDadoDois) {
        this.tabuleiroSnapshot = tabuleiroSnapshot;
        this.pontuacaoBranco = pontuacaoBranco;
        this.pontuacaoPreto = pontuacaoPreto;
        this.turnoAtual = turnoAtual;
        this.nomeJogadorTurno = nomeJogadorTurno;
        this.nomeJogadorBranco = nomeJogadorBranco;
        this.nomeJogadorPreto = nomeJogadorPreto;
        this.valorDadoUm = valorDadoUm;
        this.valorDadoDois = valorDadoDois;
        this.movimentosDisponiveis = new java.util.ArrayList<>();
        this.dadosLancados = false;
    }

    /**
     * Construtor completo do PacoteEstadoJogo.
     * 
     * @param tabuleiroSnapshot O instantaneo do tabuleiro
     * @param pontuacaoBranco A pontuacao do jogador Branco
     * @param pontuacaoPreto A pontuacao do jogador Preto
     * @param turnoAtual A cor do jogador que tem a vez
     * @param nomeJogadorTurno O nome do jogador que tem a vez
     * @param nomeJogadorBranco O nome do jogador Branco
     * @param nomeJogadorPreto O nome do jogador Preto
     * @param valorDadoUm O valor do primeiro dado do turno
     * @param valorDadoDois O valor do segundo dado do turno
     * @param movimentosDisponiveis A lista de movimentos/dados disponiveis para jogar
     * @param dadosLancados Indica se os dados ja foram lancados neste turno
     */
    public PacoteEstadoJogo(Tabuleiro tabuleiroSnapshot, int pontuacaoBranco, int pontuacaoPreto,
            Peca.CorPeca turnoAtual, String nomeJogadorTurno, String nomeJogadorBranco,
            String nomeJogadorPreto, int valorDadoUm, int valorDadoDois,
            java.util.List<Integer> movimentosDisponiveis, boolean dadosLancados) {
        this.tabuleiroSnapshot = tabuleiroSnapshot;
        this.pontuacaoBranco = pontuacaoBranco;
        this.pontuacaoPreto = pontuacaoPreto;
        this.turnoAtual = turnoAtual;
        this.nomeJogadorTurno = nomeJogadorTurno;
        this.nomeJogadorBranco = nomeJogadorBranco;
        this.nomeJogadorPreto = nomeJogadorPreto;
        this.valorDadoUm = valorDadoUm;
        this.valorDadoDois = valorDadoDois;
        this.movimentosDisponiveis = movimentosDisponiveis != null ? new java.util.ArrayList<>(movimentosDisponiveis) : new java.util.ArrayList<>();
        this.dadosLancados = dadosLancados;
    }

    /**
     * Obtem o instantaneo do tabuleiro.
     * 
     * @return O tabuleiro
     */
    public Tabuleiro getTabuleiroSnapshot() {
        return tabuleiroSnapshot;
    }

    public void setTabuleiroSnapshot(Tabuleiro tabuleiroSnapshot) {
        this.tabuleiroSnapshot = tabuleiroSnapshot;
    }

    /**
     * Obtem a pontuacao acumulada pelo jogador Branco.
     * 
     * @return A pontuacao do Branco
     */
    public int getPontuacaoBranco() {
        return pontuacaoBranco;
    }

    public void setPontuacaoBranco(int pontuacaoBranco) {
        this.pontuacaoBranco = pontuacaoBranco;
    }

    /**
     * Obtem a pontuacao acumulada pelo jogador Preto.
     * 
     * @return A pontuacao do Preto
     */
    public int getPontuacaoPreto() {
        return pontuacaoPreto;
    }

    public void setPontuacaoPreto(int pontuacaoPreto) {
        this.pontuacaoPreto = pontuacaoPreto;
    }

    /**
     * Obtem a cor do jogador que tem a vez ativa de jogar.
     * 
     * @return A cor do turno atual (BRANCO ou PRETO)
     */
    public Peca.CorPeca getTurnoAtual() {
        return turnoAtual;
    }

    public void setTurnoAtual(Peca.CorPeca turnoAtual) {
        this.turnoAtual = turnoAtual;
    }

    /**
     * Obtem o nome do jogador que tem a vez ativa de jogar.
     * 
     * @return O nome do jogador
     */
    public String getNomeJogadorTurno() {
        return nomeJogadorTurno;
    }

    public void setNomeJogadorTurno(String nomeJogadorTurno) {
        this.nomeJogadorTurno = nomeJogadorTurno;
    }

    /**
     * Obtem o nome do jogador Branco.
     * 
     * @return O nome do jogador Branco
     */
    public String getNomeJogadorBranco() {
        return nomeJogadorBranco;
    }

    public void setNomeJogadorBranco(String nomeJogadorBranco) {
        this.nomeJogadorBranco = nomeJogadorBranco;
    }

    /**
     * Obtem o nome do jogador Preto.
     * 
     * @return O nome do jogador Preto
     */
    public String getNomeJogadorPreto() {
        return nomeJogadorPreto;
    }

    public void setNomeJogadorPreto(String nomeJogadorPreto) {
        this.nomeJogadorPreto = nomeJogadorPreto;
    }

    /**
     * Obtem o valor do primeiro dado do turno.
     * 
     * @return O valor do primeiro dado (1-6)
     */
    public int getValorDadoUm() {
        return valorDadoUm;
    }

    public void setValorDadoUm(int valorDadoUm) {
        this.valorDadoUm = valorDadoUm;
    }

    /**
     * Obtem o valor do segundo dado do turno.
     * 
     * @return O valor do segundo dado (1-6)
     */
    public int getValorDadoDois() {
        return valorDadoDois;
    }

    public void setValorDadoDois(int valorDadoDois) {
        this.valorDadoDois = valorDadoDois;
    }

    /**
     * Obtem a cor atribuida ao jogador recetor (usado na identificacao inicial).
     * 
     * @return A cor do jogador
     */
    public Peca.CorPeca getCorAtribuida() {
        return corAtribuida;
    }

    public void setCorAtribuida(Peca.CorPeca corAtribuida) {
        this.corAtribuida = corAtribuida;
    }

    /**
     * Obtem a lista de movimentos ou dados ainda disponiveis para jogar no turno.
     * 
     * @return A lista de valores de movimentos disponiveis
     */
    public java.util.List<Integer> getMovimentosDisponiveis() {
        return movimentosDisponiveis;
    }

    public void setMovimentosDisponiveis(java.util.List<Integer> movimentosDisponiveis) {
        this.movimentosDisponiveis = movimentosDisponiveis;
    }

    /**
     * Indica se os dados ja foram lancados neste turno.
     * 
     * @return true se ja foram lancados, false caso contrario
     */
    public boolean isDadosLancados() {
        return dadosLancados;
    }

    public void setDadosLancados(boolean dadosLancados) {
        this.dadosLancados = dadosLancados;
    }
}
