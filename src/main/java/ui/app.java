package ui;

/**
 * Ponto de entrada secundario/auxiliar da aplicacao.
 * Serve como um launcher alternativo para contornar limitacoes do runtime JavaFX
 * ao iniciar a aplicacao sem especificar o module-path.
 */
public class app {
    /**
     * Metodo principal de arranque que delega a inicializacao para a classe {@link ClienteMain}.
     * 
     * @param args Os argumentos passados pela linha de comandos
     */
    public static void main(String[] args) {
        // Apenas serve para enganar o Java e arrancar a interface visual.
        ClienteMain.main(args);
    }
}
