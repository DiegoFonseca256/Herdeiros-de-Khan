package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fachada do Model: único ponto de acesso da View. Expõe só tipos simples
 * (nomes de cores, colunas e paradas) e controla a ordem dos turnos.
 */
public class ModelFacade {
	public static final String KARAKORUM = "KARAKORUM";
	public static final String AREA_MELHORIAS = Tabuleiro.AREA_MELHORIAS;
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

	/** Províncias do mapa real: nome, região, tributo e paradas do contorno. */
	private static final String[][] PROVINCIAS = {
		{ "YURT_KIEV", "RUSSIA", "YURT", "R2", "R5", "P1", "R3" },
		{ "ESPADA_MOSCOU", "RUSSIA", "ESPADA", "R2", "R4", "R6", "R5" },
		{ "MOEDA_MOSCOU", "RUSSIA", "MOEDA", "P1", "R5", "R7", "P2" },
		{ "YURT_MOSCOU", "RUSSIA", "YURT", "R7", "R6", "R8", KARAKORUM, "P4", "P2" },
		{ "MOEDA_SARAI", "RUSSIA", "MOEDA", "R8", "R10", "R11", KARAKORUM },
		{ "ESPADA_KAESONG", "CHINA", "ESPADA", "R11", "C1", "C3", "C4", KARAKORUM },
		{ "ESPADA_SAMARCANDA", "PERSIA", "ESPADA", "P1", "P2", "P3", "P5" },
		{ "MOEDA_BAGDA", "PERSIA", "MOEDA", "P3", "P4", "P9", "P8", "P6", "P5" },
		{ "YURT_PEQUIM", "PERSIA", "YURT", "P4", KARAKORUM, "C4", "C5", "P9" },
		{ "ESPADA_CABUL", "CHINA", "ESPADA", "P9", "C5", "C9", "P11" },
		{ "YURT_CANTAO", "CHINA", "YURT", "C5", "C6", "C8", "C10", "C9" },
		{ "MOEDA_PEQUIM", "CHINA", "MOEDA", "C4", "C3", "C7", "C8", "C6" },
	};

	/** Província do Khan (onde fica o rosto) e as 2 apontadas pelas outras setas. */
	private static final String[][] PROVINCIAS_DO_KHAN = {
		{ "YURT_KIEV", "ESPADA_MOSCOU", "MOEDA_MOSCOU" },
		{ "MOEDA_SARAI", "YURT_MOSCOU", "ESPADA_KAESONG" },
		{ "MOEDA_BAGDA", "ESPADA_SAMARCANDA", "YURT_PEQUIM" },
		{ "YURT_CANTAO", "ESPADA_CABUL", "MOEDA_PEQUIM" },
	};

	/** Cidades: nome, região e as 3 paradas em volta. */
	private static final String[][] CIDADES = {
		{ "KIEV", "RUSSIA", "R1", "R2", "R3" },
		{ "MOSCOU", "RUSSIA", "R5", "R6", "R7" },
		{ "SARAI", "RUSSIA", "R8", "R9", "R10" },
		{ "KAESONG", "CHINA", "C1", "C2", "C3" },
		{ "PEQUIM", "CHINA", "C4", "C5", "C6" },
		{ "CANTAO", "CHINA", "C8", "C10", "C11" },
		{ "SAMARCANDA", "PERSIA", "P2", "P3", "P4" },
		{ "BAGDA", "PERSIA", "P5", "P6", "P7" },
		{ "CABUL", "PERSIA", "P9", "P10", "P11" },
	};

	private static ModelFacade instancia;

	private Tabuleiro tabuleiro;
	private final List<Jogador> jogadores = new ArrayList<>();
	private int vez;
	private boolean colunaAtivadaNoTurno;
	private Coluna colunaDoTurno;
	private boolean khanMovidoNoTurno;

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
		// sem melhorias por enquanto (fase 5 da 3ª iteração)
		tabuleiro.prepararPartida(numeroJogadores, tesourosEmbaralhados(), new ArrayList<>());
		jogadores.clear();
		for (int i = 0; i < numeroJogadores; i++)
			jogadores.add(new Jogador(Cor.values()[i], tabuleiro, i));
		vez = 0;
		iniciarTurnoDaFachada();
	}

	/** Zera o que a fachada guarda sobre o turno do jogador da vez. */
	private void iniciarTurnoDaFachada() {
		colunaAtivadaNoTurno = false;
		colunaDoTurno = null;
		khanMovidoNoTurno = false;
	}

	private void montarMapa() {
		adicionarParadas("R", Regiao.RUSSIA);
		adicionarParadas("P", Regiao.PERSIA);
		adicionarParadas("C", Regiao.CHINA);
		tabuleiro.adicionarParada(KARAKORUM, TipoParada.KARAKORUM, null);
		for (String[] rota : ROTAS)
			tabuleiro.conectar(rota[0], rota[1]);
		for (String[] p : PROVINCIAS)
			tabuleiro.adicionarProvincia(p[0], Regiao.valueOf(p[1]), TipoTributo.valueOf(p[2]),
					Arrays.copyOfRange(p, 3, p.length));
		for (String[] k : PROVINCIAS_DO_KHAN)
			tabuleiro.definirProvinciaDoKhan(k[0], k[1], k[2]);
		adicionarCidadesEmbaralhadas();
		adicionarConselheiros();
	}

	/** A ordem da pilha de cidades define quais são reveladas primeiro. */
	private void adicionarCidadesEmbaralhadas() {
		List<String[]> cidades = new ArrayList<>(Arrays.asList(CIDADES));
		Collections.shuffle(cidades);
		for (String[] c : cidades)
			tabuleiro.adicionarCidade(c[0], Regiao.valueOf(c[1]), Arrays.copyOfRange(c, 2, c.length));
	}

	/** Os pergaminhos do mapa: 5 pares, 1 trio e o de Karakorum. */
	private void adicionarConselheiros() {
		tabuleiro.adicionarConselheiro("R4", Conselheiro.par(TipoTesouro.SEDA));
		tabuleiro.adicionarConselheiro("R11", Conselheiro.par(TipoTesouro.FERRO));
		tabuleiro.adicionarConselheiro("P1", Conselheiro.par(TipoTesouro.PELE));
		tabuleiro.adicionarConselheiro("P8", Conselheiro.par(TipoTesouro.ESPECIARIA));
		tabuleiro.adicionarConselheiro("C7", Conselheiro.par(TipoTesouro.GADO));
		tabuleiro.adicionarConselheiro("C9", Conselheiro.trio());
		tabuleiro.adicionarConselheiro(KARAKORUM,
				Conselheiro.karakorum(TipoTesouro.FERRO, TipoTesouro.PELE, TipoTesouro.GADO));
	}

	/** Pilha de tesouros: 8 peças de cada tipo, embaralhadas. */
	private static List<TipoTesouro> tesourosEmbaralhados() {
		List<TipoTesouro> tesouros = new ArrayList<>();
		for (TipoTesouro t : TipoTesouro.values())
			for (int i = 0; i < TipoTesouro.PECAS_POR_TIPO; i++)
				tesouros.add(t);
		Collections.shuffle(tesouros);
		return tesouros;
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
		colunaDoTurno = buscarColuna(coluna);
		return true;
	}

	public boolean mover(String parada) {
		return jogadorDaVez().mover(parada);
	}

	/** Move o Khan para uma província do Khan ou para AREA_MELHORIAS (só com coluna que tenha o ícone dele). */
	public boolean moverKhan(String destino) {
		if (!jogadorDaVez().moverKhan(destino))
			return false;
		khanMovidoNoTurno = true;
		return true;
	}

	/** Pega 1 tributo de província vizinha a uma parada visitada no turno (gasta 1 punho). */
	public boolean pegarTributo(String provincia) {
		return jogadorDaVez().pegarTributo(provincia);
	}

	/** Constrói yurt numa parada visitada no turno (gasta 1 tributo de yurt). */
	public boolean construirYurt(String parada) {
		return jogadorDaVez().construirYurt(parada);
	}

	/** Toma um tesouro de cidade disponível vizinha a parada visitada (1ª vez no turno: 1 espada; depois: 2). */
	public boolean atacarCidade(String cidade, String tesouro) {
		TipoTesouro tipo = null;
		for (TipoTesouro t : TipoTesouro.values())
			if (t.name().equals(tesouro))
				tipo = t;
		return jogadorDaVez().atacarCidade(cidade, tipo);
	}

	/** Exige coluna ativada, Khan movido (se a coluna pedir) e peão numa parada que o comporte. */
	public boolean podeEncerrarTurno() {
		return colunaAtivadaNoTurno && jogadorDaVez().podeEncerrarTurno();
	}

	public boolean encerrarTurno() {
		if (!podeEncerrarTurno())
			return false;
		vez = (vez + 1) % jogadores.size();
		jogadorDaVez().iniciarTurno();
		iniciarTurnoDaFachada();
		return true;
	}

	// ---- províncias e Khan ----

	/** Nomes das 12 províncias, na ordem de PROVINCIAS. */
	public List<String> getProvincias() {
		List<String> nomes = new ArrayList<>();
		for (String[] p : PROVINCIAS)
			nomes.add(p[0]);
		return nomes;
	}

	/** Tributo da província: "YURT", "ESPADA" ou "MOEDA". */
	public String getTributoProvincia(String provincia) {
		return tabuleiro.getProvincia(provincia).getTipo().name();
	}

	public int getPecasProvincia(String provincia) {
		return tabuleiro.getProvincia(provincia).getPecas();
	}

	/** As 4 províncias do Khan e AREA_MELHORIAS. */
	public List<String> getDestinosKhan() {
		List<String> destinos = new ArrayList<>();
		for (String[] k : PROVINCIAS_DO_KHAN)
			destinos.add(k[0]);
		destinos.add(AREA_MELHORIAS);
		return destinos;
	}

	/** Província do Khan, AREA_MELHORIAS ou null (ainda fora do tabuleiro). */
	public String getPosicaoKhan() {
		return tabuleiro.getPosicaoKhan();
	}

	// ---- cidades ----

	/** Nomes das 9 cidades, na ordem de CIDADES. */
	public List<String> getCidades() {
		List<String> nomes = new ArrayList<>();
		for (String[] c : CIDADES)
			nomes.add(c[0]);
		return nomes;
	}

	/** "OCULTA", "DISPONIVEL" ou "CONQUISTADA". */
	public String getEstadoCidade(String cidade) {
		return tabuleiro.getCidade(cidade).getEstado().name();
	}

	/** Tesouros que ainda estão na cidade (ex.: PELE, SEDA, GADO, GADO). */
	public List<String> getTesourosCidade(String cidade) {
		List<String> tesouros = new ArrayList<>();
		for (TipoTesouro t : tabuleiro.getCidade(cidade).getTesouros())
			tesouros.add(t.name());
		return tesouros;
	}

	/** Cor de quem colocou yurt na cidade ao conquistá-la, ou null. */
	public String getYurtCidade(String cidade) {
		Cidade c = tabuleiro.getCidade(cidade);
		for (Jogador j : jogadores)
			if (c.temYurtDe(j))
				return j.getCor().name();
		return null;
	}

	// ---- yurts ----

	/** Cores dos donos dos yurts da parada (uma entrada por yurt), na ordem dos jogadores. */
	public List<String> getYurts(String parada) {
		List<String> donos = new ArrayList<>();
		Parada p = tabuleiro.getParada(parada);
		for (Jogador j : jogadores)
			for (int i = 0; i < p.contarYurtsDe(j); i++)
				donos.add(j.getCor().name());
		return donos;
	}

	public int getPunhos() {
		return jogadorDaVez().getPunhos();
	}

	/** A coluna do turno tem o ícone do Khan e ele ainda não foi movido (todos são Tolui). */
	public boolean isKhanPendente() {
		return colunaDoTurno != null && AcoesColuna.padrao(colunaDoTurno).temKhan() && !khanMovidoNoTurno;
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
