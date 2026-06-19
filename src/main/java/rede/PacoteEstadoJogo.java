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

    /**
     * O instantâneo completo do tabuleiro de jogo, contendo o estado de todas as casas e peças.
     */
    protected Tabuleiro tabuleiroSnapshot;

    /**
     * A pontuação corrente acumulada pelo jogador de peças brancas.
     */
    protected int pontuacaoBranco;

    /**
     * A pontuação corrente acumulada pelo jogador de peças pretas.
     */
    protected int pontuacaoPreto;

    /**
     * A cor do jogador que detém a vez ativa de jogar (BRANCO ou PRETO).
     */
    protected Peca.CorPeca turnoAtual;

    /**
     * O nome do jogador que detém a vez ativa no turno atual.
     */
    protected String nomeJogadorTurno;

    /**
     * O nome registado do jogador que está a controlar as peças brancas.
     */
    protected String nomeJogadorBranco;

    /**
     * O nome registado do jogador que está a controlar as peças pretas.
     */
    protected String nomeJogadorPreto;

    /**
     * O valor do primeiro dado lançado neste turno (0 se ainda não lançado).
     */
    protected int valorDadoUm;

    /**
     * O valor do segundo dado lançado neste turno (0 se ainda não lançado).
     */
    protected int valorDadoDois;

    /**
     * A cor atribuída pelo servidor ao cliente recetor específico desta instância do pacote.
     */
    protected Peca.CorPeca corAtribuida;

    /**
     * A lista de valores de movimentos ou dados individuais ainda disponíveis para jogar neste turno.
     */
    protected java.util.List<Integer> movimentosDisponiveis = new java.util.ArrayList<>();

    /**
     * Flag que indica se os dados do turno corrente já foram lançados.
     */
    protected boolean dadosLancados;

    /**
     * O nome do jogador vencedor da partida, caso o jogo tenha terminado (null caso contrário).
     */
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

    /**
     * Define o instantaneo do tabuleiro.
     * 
     * @param tabuleiroSnapshot O tabuleiro
     */
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

    /**
     * Define a pontuacao do jogador Branco.
     * 
     * @param pontuacaoBranco A pontuacao do Branco
     */
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

    /**
     * Define a pontuacao do jogador Preto.
     * 
     * @param pontuacaoPreto A pontuacao do Preto
     */
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

    /**
     * Define a cor do jogador que tem a vez ativa de jogar.
     * 
     * @param turnoAtual A cor do turno atual (BRANCO ou PRETO)
     */
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

    /**
     * Define o nome do jogador que tem a vez ativa de jogar.
     * 
     * @param nomeJogadorTurno O nome do jogador
     */
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

    /**
     * Define o nome do jogador Branco.
     * 
     * @param nomeJogadorBranco O nome do jogador Branco
     */
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

    /**
     * Define o nome do jogador Preto.
     * 
     * @param nomeJogadorPreto O nome do jogador Preto
     */
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

    /**
     * Define o valor do primeiro dado do turno.
     * 
     * @param valorDadoUm O valor do primeiro dado (1-6)
     */
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

    /**
     * Define o valor do segundo dado do turno.
     * 
     * @param valorDadoDois O valor do segundo dado (1-6)
     */
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

    /**
     * Define a cor atribuida ao jogador recetor.
     * 
     * @param corAtribuida A cor do jogador
     */
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

    /**
     * Define a lista de movimentos ou dados ainda disponiveis para jogar no turno.
     * 
     * @param movimentosDisponiveis A lista de valores de movimentos disponiveis
     */
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

    /**
     * Define se os dados ja foram lancados neste turno.
     * 
     * @param dadosLancados true se ja foram lancados, false caso contrario
     */
    public void setDadosLancados(boolean dadosLancados) {
        this.dadosLancados = dadosLancados;
    }
}
