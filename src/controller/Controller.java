package controller;

import java.util.ArrayList;
import java.util.List;

import model.ModelFacade;

/**
 * Controller (Singleton): recebe as ações da View, repassa ao Model pela fachada e,
 * quando o estado muda, notifica os observadores registrados (padrão Observer).
 */
public class Controller implements ObservadoIF {
	// índices aceitos por get(int)
	public static final int EVENTO = 0;
	public static final int JOGADOR = 1; // posição da cor em ModelFacade.getCores()
	public static final int VISIVEL = 2; // 1 se a janela ficou aberta, 0 se foi fechada

	// valores de get(EVENTO)
	public static final int PARTIDA_INICIADA = 0;
	public static final int COLUNA_ATIVADA = 1;
	public static final int PEAO_MOVIDO = 2;
	public static final int TURNO_ENCERRADO = 3;
	public static final int CARTELA_ALTERNADA = 4;
	public static final int CARTAS_ALTERNADA = 5;
	public static final int KHAN_MOVIDO = 6;
	public static final int TRIBUTO_PEGO = 7;
	public static final int YURT_CONSTRUIDO = 8;
	public static final int TESOURO_TOMADO = 9;

	private static Controller instancia;

	private final ModelFacade modelo = ModelFacade.getInstancia();
	private final List<ObservadorIF> observadores = new ArrayList<>();
	private final List<String> cartelasAbertas = new ArrayList<>();
	private boolean cartasAberta;
	private int evento;
	private int jogador;
	private int visivel;

	private Controller() {
	}

	public static Controller getInstancia() {
		if (instancia == null)
			instancia = new Controller();
		return instancia;
	}

	/** Usado pelos observadores para se registrarem: Controller.getInstancia().registra(this). */
	public void registra(ObservadorIF o) {
		add(o);
	}

	// ---- ObservadoIF ----

	@Override
	public void add(ObservadorIF o) {
		if (!observadores.contains(o))
			observadores.add(o);
	}

	@Override
	public void remove(ObservadorIF o) {
		observadores.remove(o);
	}

	@Override
	public int get(int i) {
		if (i == EVENTO)
			return evento;
		if (i == JOGADOR)
			return jogador;
		if (i == VISIVEL)
			return visivel;
		return -1;
	}

	private void notificar(int novoEvento, int novoJogador, int novoVisivel) {
		evento = novoEvento;
		jogador = novoJogador;
		visivel = novoVisivel;
		for (ObservadorIF o : new ArrayList<>(observadores))
			o.notify(this);
	}

	// ---- sequência de uma partida ----

	public void novaPartida(int numeroJogadores) {
		modelo.novaPartida(numeroJogadores);
		cartelasAbertas.clear();
		cartasAberta = false;
		notificar(PARTIDA_INICIADA, -1, 0);
	}

	public boolean ativarColuna(String coluna) {
		if (!modelo.ativarColuna(coluna))
			return false;
		notificar(COLUNA_ATIVADA, indiceDaVez(), 0);
		return true;
	}

	public boolean mover(String parada) {
		if (!modelo.mover(parada))
			return false;
		notificar(PEAO_MOVIDO, indiceDaVez(), 0);
		return true;
	}

	public boolean moverKhan(String destino) {
		if (!modelo.moverKhan(destino))
			return false;
		notificar(KHAN_MOVIDO, indiceDaVez(), 0);
		return true;
	}

	public boolean pegarTributo(String provincia) {
		if (!modelo.pegarTributo(provincia))
			return false;
		notificar(TRIBUTO_PEGO, indiceDaVez(), 0);
		return true;
	}

	public boolean construirYurt(String parada) {
		if (!modelo.construirYurt(parada))
			return false;
		notificar(YURT_CONSTRUIDO, indiceDaVez(), 0);
		return true;
	}

	public boolean atacarCidade(String cidade, String tesouro) {
		if (!modelo.atacarCidade(cidade, tesouro))
			return false;
		notificar(TESOURO_TOMADO, indiceDaVez(), 0);
		return true;
	}

	public boolean encerrarTurno() {
		if (!modelo.encerrarTurno())
			return false;
		notificar(TURNO_ENCERRADO, indiceDaVez(), 0);
		return true;
	}

	/** Abre a janela da cartela do jogador se estiver fechada e fecha se estiver aberta. */
	public void alternarCartela(String cor) {
		int indice = modelo.getCores().indexOf(cor);
		if (indice < 0)
			return;
		int aberta = 0;
		if (cartelasAbertas.contains(cor)) {
			cartelasAbertas.remove(cor);
		} else {
			cartelasAbertas.add(cor);
			aberta = 1;
		}
		notificar(CARTELA_ALTERNADA, indice, aberta);
	}

	public void alternarCartas() {
		cartasAberta = !cartasAberta;
		int aberta = 0;
		if (cartasAberta)
			aberta = 1;
		notificar(CARTAS_ALTERNADA, -1, aberta);
	}

	private int indiceDaVez() {
		return modelo.getCores().indexOf(modelo.getCorDaVez());
	}
}
