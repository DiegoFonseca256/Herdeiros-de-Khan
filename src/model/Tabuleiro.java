package model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;

/**
 * Tabuleiro principal: mapa (montado pelos métodos adicionar/conectar), pilha de
 * tesouros, reserva de tributos, área de melhorias, Khan e trilha do Kurultai.
 */
class Tabuleiro {
	/** Destino do Khan que representa a área de melhorias. */
	static final String AREA_MELHORIAS = "MELHORIAS";
	private static final int CIDADES_INICIAIS = 3;

	private final HashMap<String, Parada> paradas = new HashMap<>();
	private final HashMap<String, Provincia> provincias = new HashMap<>();
	private final HashMap<String, Cidade> cidades = new HashMap<>();
	private final List<Cidade> pilhaCidades = new ArrayList<>(); // ordem em que as cidades são reveladas
	private Parada karakorum;

	private final List<TipoTesouro> pilhaTesouros = new ArrayList<>();
	private final HashMap<TipoTributo, Integer> reservaTributos = new HashMap<>();
	private AreaMelhorias areaMelhorias;
	private Kurultai kurultai;
	private Provincia provinciaDoKhan;
	private boolean khanNaAreaMelhorias;

	// ---- montagem do mapa ----

	Parada adicionarParada(String nome, TipoParada tipo, Regiao regiao) {
		Parada p = new Parada(nome, tipo, regiao);
		paradas.put(nome, p);
		if (tipo == TipoParada.KARAKORUM)
			karakorum = p;
		return p;
	}

	void conectar(String parada1, String parada2) {
		buscarParada(parada1).conectar(buscarParada(parada2));
	}

	/** Cria uma província com 1 peça de tributo, adjacente às paradas informadas. */
	Provincia adicionarProvincia(String nome, Regiao regiao, TipoTributo tipo, String... paradasAdjacentes) {
		Provincia p = new Provincia(nome, regiao, tipo);
		for (String nomeParada : paradasAdjacentes)
			p.adicionarParadaAdjacente(buscarParada(nomeParada));
		p.adicionarPeca();
		provincias.put(nome, p);
		return p;
	}

	void definirProvinciaDoKhan(String nome, String adjacente1, String adjacente2) {
		buscarProvincia(nome).tornarDoKhan(buscarProvincia(adjacente1), buscarProvincia(adjacente2));
	}

	/** Cria uma cidade (oculta) e a coloca no fim da pilha de cidades. */
	Cidade adicionarCidade(String nome, Regiao regiao, String... paradasAdjacentes) {
		Cidade c = new Cidade(nome, regiao);
		for (String nomeParada : paradasAdjacentes)
			c.adicionarParadaAdjacente(buscarParada(nomeParada));
		cidades.put(nome, c);
		pilhaCidades.add(c);
		return c;
	}

	void adicionarConselheiro(String parada, Conselheiro conselheiro) {
		buscarParada(parada).setConselheiro(conselheiro);
	}

	/** Prepara a partida com o mapa já montado. As pilhas devem vir embaralhadas. */
	void prepararPartida(int numeroJogadores, List<TipoTesouro> tesouros, List<Melhoria> melhorias) {
		kurultai = new Kurultai(numeroJogadores);
		areaMelhorias = new AreaMelhorias(melhorias);
		pilhaTesouros.addAll(tesouros);
		for (TipoTributo t : TipoTributo.values())
			reservaTributos.put(t, t.getTotal());
		for (Provincia p : provincias.values())
			reservaTributos.merge(p.getTipo(), -p.getPecas(), Integer::sum);
		for (int i = 0; i < CIDADES_INICIAIS; i++)
			revelarProximaCidade();
	}

	private Parada buscarParada(String nome) {
		Parada p = paradas.get(nome);
		if (p == null)
			throw new IllegalArgumentException("Parada inexistente: " + nome);
		return p;
	}

	private Provincia buscarProvincia(String nome) {
		Provincia p = provincias.get(nome);
		if (p == null)
			throw new IllegalArgumentException("Província inexistente: " + nome);
		return p;
	}

	// ---- consultas ----

	Parada getParada(String nome) {
		return paradas.get(nome);
	}

	Provincia getProvincia(String nome) {
		return provincias.get(nome);
	}

	Cidade getCidade(String nome) {
		return cidades.get(nome);
	}

	Collection<Provincia> getProvincias() {
		return provincias.values();
	}

	Parada getKarakorum() {
		return karakorum;
	}

	AreaMelhorias getAreaMelhorias() {
		return areaMelhorias;
	}

	Kurultai getKurultai() {
		return kurultai;
	}

	/** O fim é disparado quando o Kurultai fica completo ou todas as cidades são conquistadas. */
	boolean isFimDisparado() {
		return kurultai.isCompleto() || todasCidadesConquistadas();
	}

	// ---- reserva de tributos ----

	/** Retira até a quantidade pedida da reserva e retorna quantas peças foram retiradas. */
	int retirarDaReserva(TipoTributo tipo, int quantidade) {
		int disponivel = Math.min(quantidade, reservaTributos.getOrDefault(tipo, 0));
		reservaTributos.merge(tipo, -disponivel, Integer::sum);
		return disponivel;
	}

	void devolverAReserva(TipoTributo tipo, int quantidade) {
		reservaTributos.merge(tipo, quantidade, Integer::sum);
	}

	// ---- Khan ----

	/**
	 * Move o Khan para outra província do Khan (que recebe tributo junto com as 2 adjacentes)
	 * ou para a área de melhorias (o jogador ganha moeda/espada pelos espaços vazios).
	 */
	boolean moverKhan(String destino, Jogador jogador) {
		if (destino == null)
			return false;
		if (destino.equals(AREA_MELHORIAS)) {
			if (khanNaAreaMelhorias)
				return false;
			khanNaAreaMelhorias = true;
			provinciaDoKhan = null;
			if (areaMelhorias.isEspacoVazio(AreaMelhorias.ESPACO_MOEDA))
				jogador.adicionarTributo(TipoTributo.MOEDA, retirarDaReserva(TipoTributo.MOEDA, 1));
			if (areaMelhorias.isEspacoVazio(AreaMelhorias.ESPACO_ESPADA))
				jogador.adicionarTributo(TipoTributo.ESPADA, retirarDaReserva(TipoTributo.ESPADA, 1));
			areaMelhorias.reabastecer();
			return true;
		}
		Provincia p = provincias.get(destino);
		if (p == null || !p.isDoKhan() || p == provinciaDoKhan)
			return false;
		provinciaDoKhan = p;
		khanNaAreaMelhorias = false;
		abastecer(p);
		for (Provincia adjacente : p.getAdjacentesDoKhan())
			abastecer(adjacente);
		return true;
	}

	/** Província do Khan, {@link #AREA_MELHORIAS} ou null (fora do tabuleiro). */
	String getPosicaoKhan() {
		if (khanNaAreaMelhorias)
			return AREA_MELHORIAS;
		return provinciaDoKhan == null ? null : provinciaDoKhan.getNome();
	}

	/** Coloca 1 peça de tributo da reserva na província (máximo de 3 por província). */
	private void abastecer(Provincia p) {
		if (reservaTributos.get(p.getTipo()) > 0 && p.adicionarPeca())
			reservaTributos.merge(p.getTipo(), -1, Integer::sum);
	}

	// ---- cidades ----

	/** Revela a próxima cidade oculta da pilha, colocando nela até 4 tesouros. Retorna null se não houver. */
	Cidade revelarProximaCidade() {
		for (Cidade c : pilhaCidades) {
			if (c.getEstado() == Cidade.Estado.OCULTA) {
				List<TipoTesouro> novos = new ArrayList<>();
				while (novos.size() < Cidade.TESOUROS_INICIAIS && !pilhaTesouros.isEmpty())
					novos.add(pilhaTesouros.remove(0));
				c.revelar(novos);
				return c;
			}
		}
		return null;
	}

	boolean todasCidadesConquistadas() {
		for (Cidade c : cidades.values())
			if (c.getEstado() != Cidade.Estado.CONQUISTADA)
				return false;
		return true;
	}

	/** Yurts do jogador numa região: os das paradas e os colocados em cidades conquistadas. */
	int contarYurts(Jogador jogador, Regiao regiao) {
		int total = 0;
		for (Parada p : paradas.values())
			if (p.getRegiao() == regiao)
				total += p.contarYurtsDe(jogador);
		for (Cidade c : cidades.values())
			if (c.getRegiao() == regiao && c.temYurtDe(jogador))
				total++;
		return total;
	}

	/** Menor número de rotas entre duas paradas (busca em largura). Retorna -1 se não houver caminho. */
	int distancia(Parada origem, Parada destino) {
		HashMap<Parada, Integer> dist = new HashMap<>();
		Queue<Parada> fila = new ArrayDeque<>();
		dist.put(origem, 0);
		fila.add(origem);
		while (!fila.isEmpty()) {
			Parada atual = fila.remove();
			if (atual == destino)
				return dist.get(atual);
			for (Parada v : atual.getVizinhas()) {
				if (!dist.containsKey(v)) {
					dist.put(v, dist.get(atual) + 1);
					fila.add(v);
				}
			}
		}
		return -1;
	}
}
