package model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fachada do Model: único ponto de acesso da View. Expõe só tipos simples
 * (nomes de cores, colunas e paradas) e controla a ordem dos turnos.
 */
public class ModelFacade {
	public static final String KARAKORUM = "KARAKORUM";
	private static final int PARADAS_POR_REGIAO = 11;
	private static final List<String> DUPLAS = List.of("R4", "R11", "P1", "P8", "C7", "C9");

	/** Rotas do mapa real. Paradas: R = Rússia, P = Pérsia, C = China. */
	private static final String[][] ROTAS = {
		// Rússia
		{ "R1", "R2" }, { "R1", "R3" }, { "R2", "R3" }, { "R2", "R4" }, { "R2", "R5" },
		{ "R4", "R6" }, { "R5", "R6" }, { "R5", "R7" }, { "R6", "R7" }, { "R6", "R8" },
		{ "R8", "R9" }, { "R8", "R10" }, { "R9", "R10" }, { "R10", "R11" },
		// Pérsia
		{ "P1", "P2" }, { "P1", "P5" }, { "P2", "P3" }, { "P2", "P4" }, { "P3", "P4" },
		{ "P3", "P5" }, { "P5", "P6" }, { "P5", "P7" }, { "P6", "P7" }, { "P6", "P8" },
		{ "P4", "P9" }, { "P8", "P9" }, { "P9", "P10" }, { "P9", "P11" }, { "P10", "P11" },
		// China
		{ "C1", "C2" }, { "C1", "C3" }, { "C2", "C3" }, { "C3", "C4" }, { "C3", "C7" },
		{ "C4", "C5" }, { "C4", "C6" }, { "C5", "C6" }, { "C5", "C9" }, { "C6", "C8" },
		{ "C7", "C8" }, { "C8", "C10" }, { "C8", "C11" }, { "C10", "C11" }, { "C9", "C10" },
		// entre regiões
		{ "R3", "P1" }, { "R5", "P1" }, { "R7", "P2" }, { "R11", "C1" }, { "P9", "C5" }, { "P11", "C9" },
		// Karakorum
		{ "R8", KARAKORUM }, { "R11", KARAKORUM }, { "C4", KARAKORUM }, { "P4", KARAKORUM },
	};

	private static ModelFacade instancia;

	private Tabuleiro tabuleiro;
	private final List<Jogador> jogadores = new ArrayList<>();
	private int vez;
	private boolean colunaAtivadaNoTurno;

	private ModelFacade() {
	}

	public static ModelFacade getInstancia() {
		if (instancia == null)
			instancia = new ModelFacade();
		return instancia;
	}

	/** Inicia uma partida com 2 a 5 jogadores, todos em Karakorum. */
	public void novaPartida(int numeroJogadores) {
		tabuleiro = new Tabuleiro();
		montarMapa();
		// sem tesouros e melhorias por enquanto: a 2ª iteração só usa o movimento
		tabuleiro.prepararPartida(numeroJogadores, new ArrayList<>(), new ArrayList<>());
		jogadores.clear();
		for (int i = 0; i < numeroJogadores; i++)
			jogadores.add(new Jogador(Cor.values()[i], tabuleiro, i));
		vez = 0;
		colunaAtivadaNoTurno = false;
	}

	private void montarMapa() {
		adicionarParadas("R", Regiao.RUSSIA);
		adicionarParadas("P", Regiao.PERSIA);
		adicionarParadas("C", Regiao.CHINA);
		tabuleiro.adicionarParada(KARAKORUM, TipoParada.KARAKORUM, null);
		for (String[] rota : ROTAS)
			tabuleiro.conectar(rota[0], rota[1]);
	}

	private void adicionarParadas(String prefixo, Regiao regiao) {
		for (int i = 1; i <= PARADAS_POR_REGIAO; i++) {
			String nome = prefixo + i;
			tabuleiro.adicionarParada(nome, DUPLAS.contains(nome) ? TipoParada.DUPLA : TipoParada.SIMPLES, regiao);
		}
	}

	// ---- consultas ----

	public List<String> getCores() {
		List<String> cores = new ArrayList<>();
		for (Jogador j : jogadores)
			cores.add(j.getCor().name());
		return cores;
	}

	public String getCorDaVez() {
		return jogadorDaVez().getCor().name();
	}

	/** Nome da parada onde está o peão da cor informada, ou null se a cor não estiver na partida. */
	public String getPosicao(String cor) {
		Jogador j = buscarJogador(cor);
		return j == null ? null : j.getPosicao().getNome();
	}

	public List<String> getColunas() {
		List<String> colunas = new ArrayList<>();
		for (Coluna c : Coluna.values())
			colunas.add(c.name());
		return colunas;
	}

	/** A coluna pode ser ativada se nenhuma foi ativada neste turno e ela não estiver usada. */
	public boolean podeAtivarColuna(String coluna) {
		Coluna c = buscarColuna(coluna);
		return c != null && !colunaAtivadaNoTurno && jogadorDaVez().isColunaDisponivel(c);
	}

	public int getMovimentos() {
		return jogadorDaVez().getMovimentos();
	}

	// ---- ações do jogador da vez ----

	public boolean ativarColuna(String coluna) {
		if (!podeAtivarColuna(coluna) || !jogadorDaVez().ativarColuna(buscarColuna(coluna)))
			return false;
		colunaAtivadaNoTurno = true;
		return true;
	}

	public boolean mover(String parada) {
		return jogadorDaVez().mover(parada);
	}

	/**
	 * Exige coluna ativada e peão numa parada que o comporte. A exigência de mover
	 * o Khan (Jogador.podeEncerrarTurno) volta na 3ª iteração, junto com as províncias.
	 */
	public boolean podeEncerrarTurno() {
		Jogador j = jogadorDaVez();
		return colunaAtivadaNoTurno && j.getPosicao().comportaPeao(j);
	}

	public boolean encerrarTurno() {
		if (!podeEncerrarTurno())
			return false;
		vez = (vez + 1) % jogadores.size();
		jogadorDaVez().iniciarTurno();
		colunaAtivadaNoTurno = false;
		return true;
	}

	// ---- status de um jogador (janela da cartela) ----

	/** Quantidade de cada tributo do jogador, na ordem de TipoTributo. */
	public Map<String, Integer> getTributos(String cor) {
		Jogador j = buscarJogador(cor);
		LinkedHashMap<String, Integer> tributos = new LinkedHashMap<>();
		for (TipoTributo t : TipoTributo.values())
			tributos.put(t.name(), j.getTributo(t));
		return tributos;
	}

	/** Quantidade de cada tesouro do jogador, na ordem de TipoTesouro. */
	public Map<String, Integer> getTesouros(String cor) {
		Jogador j = buscarJogador(cor);
		LinkedHashMap<String, Integer> tesouros = new LinkedHashMap<>();
		for (TipoTesouro t : TipoTesouro.values())
			tesouros.put(t.name(), j.getTesouro(t));
		return tesouros;
	}

	/**
	 * Melhorias da coluna, de baixo para cima, identificadas por TIPO-COR
	 * (ex.: "CAVALOS-AMARELA"). Nas de tesouro, o tipo é o tesouro ("GADO-AMARELA").
	 */
	public List<String> getMelhorias(String cor, String coluna) {
		List<String> ids = new ArrayList<>();
		for (Melhoria m : buscarJogador(cor).getMelhorias(buscarColuna(coluna)))
			ids.add(identificador(m));
		return ids;
	}

	public boolean isColunaUsada(String cor, String coluna) {
		return !buscarJogador(cor).isColunaDisponivel(buscarColuna(coluna));
	}

	public int getVotos(String cor) {
		return buscarJogador(cor).getVotos();
	}

	public int getYurtsDisponiveis(String cor) {
		return buscarJogador(cor).getYurtsDisponiveis();
	}

	private static String identificador(Melhoria m) {
		String tipo = m.getTipo() == TipoMelhoria.TESOURO ? m.getTesouro().name() : m.getTipo().name();
		return tipo + "-" + m.getCor().name();
	}

	private Jogador buscarJogador(String cor) {
		for (Jogador j : jogadores)
			if (j.getCor().name().equals(cor))
				return j;
		return null;
	}

	private Jogador jogadorDaVez() {
		return jogadores.get(vez);
	}

	private static Coluna buscarColuna(String nome) {
		for (Coluna c : Coluna.values())
			if (c.name().equals(nome))
				return c;
		return null;
	}
}
