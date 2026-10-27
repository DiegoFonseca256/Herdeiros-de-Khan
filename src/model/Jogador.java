package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Jogador: recursos, tabuleiro pessoal (colunas e melhorias), votos, peão e as
 * ações que ele pode fazer no seu turno. As ações retornam false se forem inválidas.
 */
class Jogador {
	static final int YURTS_INICIAIS = 12;
	static final int MAX_MELHORIAS_POR_COLUNA = 4;
	private static final int CUSTO_MELHORIA = 2;

	private final Cor cor;
	private final Tabuleiro tabuleiro;
	private final HashMap<TipoTributo, Integer> tributos = new HashMap<>();
	private final HashMap<TipoTesouro, Integer> tesouros = new HashMap<>();
	private final HashMap<Coluna, Boolean> ativacaoUsada = new HashMap<>();
	private final HashMap<Coluna, List<Melhoria>> melhorias = new HashMap<>();
	private final HashMap<Coluna, AcoesColuna> acoesBase = new HashMap<>();
	private int yurtsDisponiveis = YURTS_INICIAIS;
	private int votos;
	private int pecasBonus;
	private Parada posicao;

	// estado do turno
	private Coluna colunaAtivada;
	private int movimentos;
	private int punhos;
	private boolean khanObrigatorio;
	private boolean khanUsado;
	private final HashMap<TipoTributo, Integer> tributosVirtuais = new HashMap<>();
	private final Set<Parada> paradasVisitadas = new HashSet<>();
	private final HashMap<Cidade, Integer> tesourosTomados = new HashMap<>();
	private final Set<Melhoria> melhoriasCompradas = new HashSet<>();

	/** Cria o jogador em Karakorum; o 1º e o 2º a jogar (ordem 0 e 1) recebem 1 moeda, os demais 2. */
	Jogador(Cor cor, Tabuleiro tabuleiro, int ordemDeJogo) {
		this.cor = cor;
		this.tabuleiro = tabuleiro;
		for (TipoTributo t : TipoTributo.values())
			tributos.put(t, 0);
		for (TipoTesouro t : TipoTesouro.values())
			tesouros.put(t, 0);
		for (Coluna c : Coluna.values()) {
			ativacaoUsada.put(c, false);
			melhorias.put(c, new ArrayList<>());
			acoesBase.put(c, AcoesColuna.padrao(c));
		}
		adicionarTributo(TipoTributo.MOEDA, tabuleiro.retirarDaReserva(TipoTributo.MOEDA, ordemDeJogo < 2 ? 1 : 2));
		moverPara(tabuleiro.getKarakorum());
		iniciarTurno();
	}

	Cor getCor() {
		return cor;
	}

	// =====================================================================
	// Ações do turno
	// =====================================================================

	/** Zera o estado do turno; o jogador começa tendo visitado a parada onde está. */
	void iniciarTurno() {
		colunaAtivada = null;
		movimentos = 0;
		punhos = 0;
		khanObrigatorio = false;
		khanUsado = false;
		for (TipoTributo t : TipoTributo.values())
			tributosVirtuais.put(t, 0);
		paradasVisitadas.clear();
		paradasVisitadas.add(posicao);
		tesourosTomados.clear();
		melhoriasCompradas.clear();
	}

	/** Ativa uma coluna livre (1 por turno). A branca recupera todas as peças. */
	boolean ativarColuna(Coluna coluna) {
		if (coluna == null || colunaAtivada != null || ativacaoUsada.get(coluna))
			return false;

		ativacaoUsada.put(coluna, true);
		AcoesColuna base = acoesBase.get(coluna);
		colunaAtivada = coluna;
		movimentos = base.getMovimentos();
		punhos = base.getPunhos();
		khanObrigatorio = base.temKhan();
		if (base.getTributoVirtual() != null)
			tributosVirtuais.merge(base.getTributoVirtual(), 1, Integer::sum);

		for (Melhoria m : melhorias.get(coluna)) {
			if (melhoriasCompradas.contains(m))
				continue; // melhorias compradas neste turno não podem ser usadas
			movimentos += m.getTipo().getMovimentos();
			punhos += m.getTipo().getPunhos();
			if (m.getTipo().getTributoVirtual() != null)
				tributosVirtuais.merge(m.getTipo().getTributoVirtual(), 1, Integer::sum);
		}

		if (base.recuperaAtivacoes())
			recuperarAtivacoes();
		return true;
	}

	/** Move para parada adjacente ou pula um yurt próprio (1 movimento). */
	boolean mover(String nomeDestino) {
		Parada destino = tabuleiro.getParada(nomeDestino);
		if (destino == null || destino == posicao || movimentos <= 0)
			return false;

		boolean alcancavel = posicao.isVizinha(destino);
		if (!alcancavel) {
			for (Parada meio : posicao.getVizinhas()) {
				if (meio.temYurtDe(this) && meio.isVizinha(destino)) {
					alcancavel = true;
					break;
				}
			}
		}
		if (!alcancavel)
			return false;

		moverPara(destino);
		paradasVisitadas.add(destino);
		movimentos--;
		return true;
	}

	/** Pega 1 tributo de província adjacente a parada visitada (1 punho). */
	boolean pegarTributo(String nomeProvincia) {
		Provincia p = tabuleiro.getProvincia(nomeProvincia);
		if (p == null || punhos <= 0 || p.getPecas() == 0 || !p.isAdjacenteA(paradasVisitadas))
			return false;

		p.retirarPeca();
		adicionarTributo(p.getTipo(), 1);
		punhos--;
		return true;
	}

	/** Move o Khan (só com coluna que tenha o ícone dele, 1 vez por turno). */
	boolean moverKhan(String destino) {
		if (!khanObrigatorio || khanUsado || !tabuleiro.moverKhan(destino, this))
			return false;
		khanUsado = true;
		return true;
	}

	/** Toma um tesouro (1ª vez no turno: 1 espada; depois: 2). O último conquista a cidade. */
	boolean atacarCidade(String nomeCidade, TipoTesouro tesouro) {
		Cidade c = tabuleiro.getCidade(nomeCidade);
		if (c == null || tesouro == null || c.getEstado() != Cidade.Estado.DISPONIVEL)
			return false;
		if (!c.isAdjacenteA(paradasVisitadas) || !c.getTesouros().contains(tesouro))
			return false;

		int custo = tesourosTomados.getOrDefault(c, 0) == 0 ? 1 : 2;
		if (!gastar(TipoTributo.ESPADA, custo))
			return false;

		c.retirarTesouro(tesouro);
		adicionarTesouro(tesouro);
		tesourosTomados.merge(c, 1, Integer::sum);

		if (c.getTesouros().isEmpty()) {
			c.conquistar(this, usarYurt());
			tabuleiro.revelarProximaCidade();
		}
		return true;
	}

	/** Constrói yurt numa parada visitada (1 tributo de yurt). */
	boolean construirYurt(String nomeParada) {
		Parada p = tabuleiro.getParada(nomeParada);
		if (p == null || !paradasVisitadas.contains(p) || !p.podeReceberYurt() || yurtsDisponiveis == 0)
			return false;
		if (!gastar(TipoTributo.YURT, 1))
			return false;

		usarYurt();
		p.adicionarYurt(this);
		return true;
	}

	/** Compra melhoria por 2 moedas (vale a partir do próximo turno). Linha completa dá uma grátis na branca. */
	boolean comprarMelhoria(int indice) {
		AreaMelhorias area = tabuleiro.getAreaMelhorias();
		Melhoria m = area.ver(indice);
		if (m == null || getNumeroMelhorias(m.getCor()) >= MAX_MELHORIAS_POR_COLUNA)
			return false;
		if (!gastar(TipoTributo.MOEDA, CUSTO_MELHORIA))
			return false;

		area.retirar(indice);
		adicionarMelhoria(m.getCor(), m);
		melhoriasCompradas.add(m);

		if (completouLinha(getNumeroMelhorias(m.getCor()))) {
			Melhoria gratis = area.retirar(AreaMelhorias.TOPO_DA_PILHA);
			if (gratis != null && adicionarMelhoria(Coluna.BRANCA, gratis))
				melhoriasCompradas.add(gratis);
		}
		return true;
	}

	/** Entrega tesouros a um conselheiro visitado; o 1º a entregar ganha o bônus. */
	boolean entregarTesouros(String nomeParada, TipoTesouro... entrega) {
		Parada p = tabuleiro.getParada(nomeParada);
		if (p == null || p.getConselheiro() == null || !paradasVisitadas.contains(p))
			return false;

		Conselheiro conselheiro = p.getConselheiro();
		List<TipoTesouro> lista = Arrays.asList(entrega);
		if (!conselheiro.aceita(lista) || !removerTesouros(lista))
			return false;

		ganharVotos(conselheiro.getVotos());
		if (conselheiro.recolherBonus())
			pecasBonus++;
		return true;
	}

	/** O turno só pode acabar com coluna ativada, Khan usado (se obrigatório) e peão numa parada que o comporte. */
	boolean podeEncerrarTurno() {
		return colunaAtivada != null && (!khanObrigatorio || khanUsado) && posicao.comportaPeao(this);
	}

	int getMovimentos() {
		return movimentos;
	}

	int getPunhos() {
		return punhos;
	}

	/** Gasta tributos, usando primeiro os virtuais do turno; os reais voltam para a reserva. */
	private boolean gastar(TipoTributo tipo, int quantidade) {
		int virtuais = tributosVirtuais.get(tipo);
		if (virtuais + tributos.get(tipo) < quantidade)
			return false;
		int doTurno = Math.min(virtuais, quantidade);
		tributosVirtuais.merge(tipo, -doTurno, Integer::sum);
		tributos.merge(tipo, doTurno - quantidade, Integer::sum);
		tabuleiro.devolverAReserva(tipo, quantidade - doTurno);
		return true;
	}

	/** Linha (1 = a de baixo) completa nas 3 colunas coloridas e ainda sem prêmio? */
	private boolean completouLinha(int linha) {
		for (Coluna c : Coluna.values())
			if (c.isColorida() && getNumeroMelhorias(c) < linha)
				return false;
		return getNumeroMelhorias(Coluna.BRANCA) < linha;
	}

	// =====================================================================
	// Recursos
	// =====================================================================

	int getTributo(TipoTributo tipo) {
		return tributos.get(tipo);
	}

	void adicionarTributo(TipoTributo tipo, int quantidade) {
		tributos.merge(tipo, quantidade, Integer::sum);
	}

	int getTesouro(TipoTesouro tipo) {
		return tesouros.get(tipo);
	}

	int getTotalTesouros() {
		int total = 0;
		for (int n : tesouros.values())
			total += n;
		return total;
	}

	void adicionarTesouro(TipoTesouro tipo) {
		tesouros.merge(tipo, 1, Integer::sum);
	}

	boolean possuiTesouros(List<TipoTesouro> lista) {
		HashMap<TipoTesouro, Integer> necessarios = new HashMap<>();
		for (TipoTesouro t : lista)
			necessarios.merge(t, 1, Integer::sum);
		for (TipoTesouro t : necessarios.keySet())
			if (tesouros.get(t) < necessarios.get(t))
				return false;
		return true;
	}

	boolean removerTesouros(List<TipoTesouro> lista) {
		if (!possuiTesouros(lista))
			return false;
		for (TipoTesouro t : lista)
			tesouros.merge(t, -1, Integer::sum);
		return true;
	}

	int getYurtsDisponiveis() {
		return yurtsDisponiveis;
	}

	boolean usarYurt() {
		if (yurtsDisponiveis == 0)
			return false;
		yurtsDisponiveis--;
		return true;
	}

	// ---- votos e bônus ----

	int getVotos() {
		return votos;
	}

	/** Votos ganhos durante a partida: também avançam a ficha neutra do Kurultai. */
	void ganharVotos(int n) {
		votos += n;
		tabuleiro.getKurultai().registrarVotos(n);
	}

	/** Votos de final de jogo (não avançam a ficha neutra). */
	void adicionarVotos(int n) {
		votos += n;
	}

	int getPecasBonus() {
		return pecasBonus;
	}

	// ---- peão ----

	Parada getPosicao() {
		return posicao;
	}

	void moverPara(Parada destino) {
		if (posicao != null)
			posicao.sair(this);
		posicao = destino;
		destino.entrar(this);
	}

	// ---- colunas e melhorias ----

	boolean isColunaDisponivel(Coluna coluna) {
		return !ativacaoUsada.get(coluna);
	}

	void recuperarAtivacoes() {
		for (Coluna c : Coluna.values())
			ativacaoUsada.put(c, false);
	}

	List<Melhoria> getMelhorias(Coluna coluna) {
		return melhorias.get(coluna);
	}

	int getNumeroMelhorias(Coluna coluna) {
		return melhorias.get(coluna).size();
	}

	/** Coloca a melhoria no próximo espaço livre da coluna (de baixo para cima). */
	boolean adicionarMelhoria(Coluna coluna, Melhoria melhoria) {
		List<Melhoria> lista = melhorias.get(coluna);
		if (lista.size() >= MAX_MELHORIAS_POR_COLUNA)
			return false;
		lista.add(melhoria);
		return true;
	}
}
