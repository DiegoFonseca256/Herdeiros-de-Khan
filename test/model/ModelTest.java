package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;



public class ModelTest {
	private static final List<TipoTesouro> TESOUROS_KIEV = Arrays.asList(
			TipoTesouro.PELE, TipoTesouro.PELE, TipoTesouro.FERRO, TipoTesouro.SEDA);

	private Tabuleiro tabuleiro;
	private Jogador vermelho;
	private Jogador azul;

	@Before
	public void setUp() {
		tabuleiro = novoTabuleiro(2);
		vermelho = new Jogador(Cor.VERMELHO, tabuleiro, 0);
		azul = new Jogador(Cor.AZUL, tabuleiro, 1);
	}

	// =====================================================================
	// Movimento
	// =====================================================================

	/**
	 * Objetivo: mover só para parada adjacente, gastando 1 movimento.
	 * Cenário: VERMELHO em K ativa VERDE (2 movimentos).
	 * Esperado: K->B recusado; K->A->B aceito; 3º movimento recusado.
	 */
	@Test
	public void moverExigeAdjacenciaEConsomeMovimentos() {
		assertTrue(vermelho.ativarColuna(Coluna.VERDE));
		assertEquals(2, vermelho.getMovimentos());

		assertFalse(vermelho.mover("B"));
		assertEquals(2, vermelho.getMovimentos());

		assertTrue(vermelho.mover("A"));
		assertTrue(vermelho.mover("B"));
		assertEquals(0, vermelho.getMovimentos());

		assertFalse(vermelho.mover("C"));
		assertEquals("B", vermelho.getPosicao().getNome());
	}

	/**
	 * Objetivo: pular yurt próprio custa 1 movimento e não conta como visita.
	 * Cenário: VERMELHO tem yurt em A e ativa VERMELHA (1 movimento, 1 punho).
	 * Esperado: K->B aceito; tributo de PR_ESPADA (adj. só a A) recusado.
	 */
	@Test
	public void pularYurtProprioCustaUmMovimentoENaoContaComoVisita() {
		tabuleiro.getParada("A").adicionarYurt(vermelho);
		assertTrue(vermelho.ativarColuna(Coluna.VERMELHA));

		assertTrue(vermelho.mover("B"));
		assertEquals(0, vermelho.getMovimentos());
		assertEquals("B", vermelho.getPosicao().getNome());

		assertFalse(vermelho.pegarTributo("PR_ESPADA"));
		assertEquals(1, vermelho.getPunhos());
	}

	/**
	 * Objetivo: não se pode pular yurt de outro jogador.
	 * Cenário: AZUL tem yurt em A; VERMELHO em K ativa VERDE.
	 * Esperado: K->B recusado; peão fica em K com 2 movimentos.
	 */
	@Test
	public void naoPodePularYurtDeOutroJogador() {
		tabuleiro.getParada("A").adicionarYurt(azul);
		assertTrue(vermelho.ativarColuna(Coluna.VERDE));

		assertFalse(vermelho.mover("B"));
		assertEquals("K", vermelho.getPosicao().getNome());
		assertEquals(2, vermelho.getMovimentos());
	}

	/**
	 * Objetivo: pode passar por parada simples ocupada, mas não terminar nela.
	 * Cenário: AZUL está em A; VERMELHO em K ativa VERDE.
	 * Esperado: encerrar em A não é permitido; em B (dupla, vazia) é.
	 */
	@Test
	public void naoPodeTerminarTurnoEmParadaSimplesOcupada() {
		azul.moverPara(tabuleiro.getParada("A"));
		assertTrue(vermelho.ativarColuna(Coluna.VERDE));

		assertTrue(vermelho.mover("A"));
		assertFalse(vermelho.podeEncerrarTurno());

		assertTrue(vermelho.mover("B"));
		assertTrue(vermelho.podeEncerrarTurno());
	}

	/**
	 * Objetivo: parada dupla comporta 2 peões; Karakorum, todos.
	 * Cenário: 3 jogadores; AZUL e depois VERDE vão para B.
	 * Esperado: B aceita o 2º peão, mas não o 3º; K aceita.
	 */
	@Test
	public void paradaDuplaComportaApenasDoisPeoes() {
		Tabuleiro t = novoTabuleiro(3);
		Jogador verm = new Jogador(Cor.VERMELHO, t, 0);
		Jogador az = new Jogador(Cor.AZUL, t, 1);
		Jogador verde = new Jogador(Cor.VERDE, t, 2);
		Parada b = t.getParada("B");

		az.moverPara(b);
		assertTrue(b.comportaPeao(verm));

		verde.moverPara(b);
		assertFalse(b.comportaPeao(verm));
		assertTrue(t.getKarakorum().comportaPeao(verm));
	}

	// =====================================================================
	// Tributos e Khan
	// =====================================================================

	/**
	 * Objetivo: tributo exige província adjacente a parada visitada, peças e punhos.
	 * Cenário: VERMELHO em K ativa AMARELA (1 movimento, 2 punhos).
	 * Esperado: PR_ESPADA recusada em K; PR_MOEDA aceita e depois vazia;
	 * em A, PR_ESPADA aceita e a 2ª retirada recusada por falta de punhos.
	 */
	@Test
	public void pegarTributoRespeitaAdjacenciaPecasEPunhos() {
		assertTrue(vermelho.ativarColuna(Coluna.AMARELA));
		assertEquals(2, vermelho.getPunhos());

		assertFalse(vermelho.pegarTributo("PR_ESPADA"));

		assertTrue(vermelho.pegarTributo("PR_MOEDA"));
		assertEquals(2, vermelho.getTributo(TipoTributo.MOEDA));
		assertEquals(0, pecas("PR_MOEDA"));
		assertFalse(vermelho.pegarTributo("PR_MOEDA"));

		assertTrue(vermelho.mover("A"));
		tabuleiro.getProvincia("PR_ESPADA").adicionarPeca();
		assertTrue(vermelho.pegarTributo("PR_ESPADA"));
		assertEquals(0, vermelho.getPunhos());
		assertFalse(vermelho.pegarTributo("PR_ESPADA"));

		assertEquals(1, vermelho.getTributo(TipoTributo.ESPADA));
		assertEquals(1, pecas("PR_ESPADA"));
	}

	/**
	 * Objetivo: Khan obrigatório, sem repetir lugar, abastecendo até 3 peças.
	 * Cenário: províncias com 1 peça; Khan vai a PR_ESPADA, PR_KHAN2 e PR_ESPADA.
	 * Esperado: encerrar sem Khan e repetir lugar não são permitidos;
	 * a província e as 2 adjacentes ganham 1 peça, sem passar de 3.
	 */
	@Test
	public void khanObrigatorioNaoRepeteLugarEAbasteceAteTres() {
		assertTrue(vermelho.ativarColuna(Coluna.AMARELA));
		assertFalse(vermelho.podeEncerrarTurno());
		assertFalse(vermelho.moverKhan("PR_YURT"));
		assertTrue(vermelho.moverKhan("PR_ESPADA"));
		assertFalse(vermelho.moverKhan("PR_KHAN2"));
		assertEquals(2, pecas("PR_ESPADA"));
		assertEquals(2, pecas("PR_YURT"));
		assertEquals(2, pecas("PR_MOEDA"));
		assertTrue(vermelho.podeEncerrarTurno());

		assertTrue(azul.ativarColuna(Coluna.AMARELA));
		assertFalse(azul.moverKhan("PR_ESPADA"));
		assertTrue(azul.moverKhan("PR_KHAN2"));
		assertEquals(2, pecas("PR_KHAN2"));
		assertEquals(3, pecas("PR_YURT"));
		assertEquals(3, pecas("PR_MOEDA"));

		vermelho.iniciarTurno();
		assertTrue(vermelho.ativarColuna(Coluna.BRANCA));
		assertTrue(vermelho.moverKhan("PR_ESPADA"));
		assertEquals("PR_ESPADA", tabuleiro.getPosicaoKhan());
		assertEquals(3, pecas("PR_ESPADA"));
		assertEquals(3, pecas("PR_YURT"));
		assertEquals(3, pecas("PR_MOEDA"));
	}

	/**
	 * Objetivo: Khan só pode ser usado com coluna que tenha o ícone dele.
	 * Cenário: VERMELHO ativa VERDE (sem Khan).
	 * Esperado: mover o Khan recusado; turno pode ser encerrado.
	 */
	@Test
	public void khanSoPodeSerUsadoComColunaDoKhan() {
		assertTrue(vermelho.ativarColuna(Coluna.VERDE));
		assertFalse(vermelho.moverKhan("PR_ESPADA"));
		assertNull(tabuleiro.getPosicaoKhan());
		assertTrue(vermelho.podeEncerrarTurno());
	}

	// =====================================================================
	// Cidades e yurts
	// =====================================================================

	/**
	 * Objetivo: ataque exige adjacência; custa 1 espada e depois 2.
	 * Cenário: VERMELHO com 3 espadas; Kiev (adj. a A) tem PELE, PELE, FERRO, SEDA.
	 * Esperado: ataque de K recusado; de A, PELE custa 1, FERRO custa 2;
	 * GADO (inexistente) e SEDA (sem espadas) recusados.
	 */
	@Test
	public void ataqueCustaUmaEspadaDepoisDuas() {
		vermelho.adicionarTributo(TipoTributo.ESPADA, 3);
		assertTrue(vermelho.ativarColuna(Coluna.VERDE));

		assertFalse(vermelho.atacarCidade("Kiev", TipoTesouro.PELE));

		assertTrue(vermelho.mover("A"));
		assertTrue(vermelho.atacarCidade("Kiev", TipoTesouro.PELE));
		assertEquals(2, vermelho.getTributo(TipoTributo.ESPADA));

		assertFalse(vermelho.atacarCidade("Kiev", TipoTesouro.GADO));

		assertTrue(vermelho.atacarCidade("Kiev", TipoTesouro.FERRO));
		assertEquals(0, vermelho.getTributo(TipoTributo.ESPADA));

		assertFalse(vermelho.atacarCidade("Kiev", TipoTesouro.SEDA));
		assertEquals(Arrays.asList(TipoTesouro.PELE, TipoTesouro.SEDA), tabuleiro.getCidade("Kiev").getTesouros());
		assertEquals(1, vermelho.getTesouro(TipoTesouro.PELE));
		assertEquals(1, vermelho.getTesouro(TipoTesouro.FERRO));
	}

	/**
	 * Objetivo: tomar o último tesouro conquista a cidade.
	 * Cenário: VERMELHO em A com 7 espadas; Sarai é a próxima da pilha.
	 * Esperado: Kiev conquistada com yurt do VERMELHO (1 na Rússia);
	 * Sarai revelada com 4 tesouros; novo ataque a Kiev recusado.
	 */
	@Test
	public void tomarUltimoTesouroConquistaCidadeERevelaProxima() {
		vermelho.adicionarTributo(TipoTributo.ESPADA, 7);
		assertTrue(vermelho.ativarColuna(Coluna.VERDE));
		assertTrue(vermelho.mover("A"));
		assertEquals(Cidade.Estado.OCULTA, tabuleiro.getCidade("Sarai").getEstado());

		for (TipoTesouro t : TESOUROS_KIEV)
			assertTrue(vermelho.atacarCidade("Kiev", t));

		assertEquals(0, vermelho.getTributo(TipoTributo.ESPADA));
		assertEquals(Cidade.Estado.CONQUISTADA, tabuleiro.getCidade("Kiev").getEstado());
		assertEquals(Jogador.YURTS_INICIAIS - 1, vermelho.getYurtsDisponiveis());
		assertEquals(1, tabuleiro.contarYurts(vermelho, Regiao.RUSSIA));

		assertEquals(Cidade.Estado.DISPONIVEL, tabuleiro.getCidade("Sarai").getEstado());
		assertEquals(4, tabuleiro.getCidade("Sarai").getTesouros().size());

		assertFalse(vermelho.atacarCidade("Kiev", TipoTesouro.PELE));
	}

	/**
	 * Objetivo: yurt exige tributo e parada visitada, respeitando a capacidade.
	 * Cenário: VERMELHO vai de K a B passando por A.
	 * Esperado: sem tributo, em K e em C (não visitada) é recusado;
	 * A aceita 1 yurt e B (dupla) aceita 2.
	 */
	@Test
	public void construirYurtRespeitaTributoVisitaECapacidade() {
		assertTrue(vermelho.ativarColuna(Coluna.VERDE));
		assertTrue(vermelho.mover("A"));
		assertTrue(vermelho.mover("B"));

		assertFalse(vermelho.construirYurt("A"));
		vermelho.adicionarTributo(TipoTributo.YURT, 4);

		assertFalse(vermelho.construirYurt("K"));
		assertFalse(vermelho.construirYurt("C"));

		assertTrue(vermelho.construirYurt("A"));
		assertFalse(vermelho.construirYurt("A"));

		assertTrue(vermelho.construirYurt("B"));
		assertTrue(vermelho.construirYurt("B"));
		assertFalse(vermelho.construirYurt("B"));

		assertEquals(1, vermelho.getTributo(TipoTributo.YURT));
		assertEquals(Jogador.YURTS_INICIAIS - 3, vermelho.getYurtsDisponiveis());
		assertEquals(3, tabuleiro.contarYurts(vermelho, Regiao.RUSSIA));
	}

	// =====================================================================
	// Colunas de ativação e melhorias
	// =====================================================================

	/**
	 * Objetivo: 1 coluna por turno; coluna usada só volta com a branca.
	 * Cenário: VERMELHO usa VERDE; no turno seguinte tenta VERDE e ativa BRANCA.
	 * Esperado: 2ª ativação e VERDE repetida recusadas; BRANCA libera todas.
	 */
	@Test
	public void colunaUsadaSoVoltaComColunaBranca() {
		assertTrue(vermelho.ativarColuna(Coluna.VERDE));
		assertFalse(vermelho.ativarColuna(Coluna.VERMELHA));

		vermelho.iniciarTurno();
		assertFalse(vermelho.isColunaDisponivel(Coluna.VERDE));
		assertFalse(vermelho.ativarColuna(Coluna.VERDE));

		assertTrue(vermelho.ativarColuna(Coluna.BRANCA));
		assertTrue(vermelho.isColunaDisponivel(Coluna.VERDE));
		assertTrue(vermelho.isColunaDisponivel(Coluna.BRANCA));
	}

	/**
	 * Objetivo: melhoria custa 2 moedas e só vale no turno seguinte.
	 * Cenário: VERMELHO (4 moedas) compra CAVALOS amarela (+2 movimentos).
	 * Esperado: sobram 2 moedas; AMARELA dá 1 movimento no turno da compra e 3 depois.
	 */
	@Test
	public void melhoriaCustaDuasMoedasESoValeNoTurnoSeguinte() {
		vermelho.adicionarTributo(TipoTributo.MOEDA, 3);

		assertTrue(vermelho.comprarMelhoria(0));
		assertEquals(2, vermelho.getTributo(TipoTributo.MOEDA));
		assertEquals(1, vermelho.getNumeroMelhorias(Coluna.AMARELA));

		assertTrue(vermelho.ativarColuna(Coluna.AMARELA));
		assertEquals(1, vermelho.getMovimentos());
		assertTrue(vermelho.moverKhan("PR_ESPADA"));

		vermelho.iniciarTurno();
		assertTrue(vermelho.ativarColuna(Coluna.BRANCA)); // recupera as peças
		assertTrue(vermelho.moverKhan("PR_KHAN2"));

		vermelho.iniciarTurno();
		assertTrue(vermelho.ativarColuna(Coluna.AMARELA));
		assertEquals(3, vermelho.getMovimentos());
	}

	/**
	 * Objetivo: compra desloca a área para a direita; linha completa dá melhoria grátis.
	 * Cenário: VERMELHO (6 moedas) compra uma melhoria de cada cor.
	 * Esperado: espaços da esquerda ficam vazios; BRANCA ganha 1 melhoria;
	 * sem moedas, a 4ª compra é recusada.
	 */
	@Test
	public void compraDeslocaMelhoriasELinhaCompletaDaMelhoriaGratis() {
		vermelho.adicionarTributo(TipoTributo.MOEDA, 5);
		AreaMelhorias area = tabuleiro.getAreaMelhorias();
		Melhoria espada = area.ver(3);

		assertTrue(vermelho.comprarMelhoria(0));
		assertNull(area.ver(0));
		assertFalse(vermelho.comprarMelhoria(0));

		assertTrue(vermelho.comprarMelhoria(1)); // PUNHO vermelho
		assertTrue(vermelho.comprarMelhoria(2)); // MOEDA verde
		assertNull(area.ver(1));
		assertSame(espada, area.ver(3));

		assertEquals(1, vermelho.getNumeroMelhorias(Coluna.VERMELHA));
		assertEquals(1, vermelho.getNumeroMelhorias(Coluna.VERDE));
		assertEquals(1, vermelho.getNumeroMelhorias(Coluna.AMARELA));
		assertEquals(1, vermelho.getNumeroMelhorias(Coluna.BRANCA));

		assertEquals(0, vermelho.getTributo(TipoTributo.MOEDA));
		assertFalse(vermelho.comprarMelhoria(3));
	}

	/**
	 * Objetivo: Khan na área de melhorias dá moeda/espada se o espaço estiver vazio.
	 * Cenário: espaço da moeda vazio e espaço da espada ocupado.
	 * Esperado: ganha 1 moeda e nenhuma espada; a área é reabastecida.
	 */
	@Test
	public void khanNaAreaDeMelhoriasDaMoedaEReabastece() {
		vermelho.adicionarTributo(TipoTributo.MOEDA, 3);
		AreaMelhorias area = tabuleiro.getAreaMelhorias();

		assertTrue(vermelho.comprarMelhoria(0));
		assertTrue(vermelho.comprarMelhoria(3));
		assertTrue(area.isEspacoVazio(AreaMelhorias.ESPACO_MOEDA));
		assertFalse(area.isEspacoVazio(AreaMelhorias.ESPACO_ESPADA));
		assertEquals(0, vermelho.getTributo(TipoTributo.MOEDA));

		assertTrue(vermelho.ativarColuna(Coluna.AMARELA));
		assertTrue(vermelho.moverKhan(Tabuleiro.AREA_MELHORIAS));

		assertEquals(1, vermelho.getTributo(TipoTributo.MOEDA));
		assertEquals(0, vermelho.getTributo(TipoTributo.ESPADA));
		assertFalse(area.isEspacoVazio(0));
		assertFalse(area.isEspacoVazio(1));
	}

	// =====================================================================
	// Conselheiros, Kurultai e vencedor
	// =====================================================================

	/**
	 * Objetivo: entrega dá votos; bônus só para o 1º; ficha neutra avança.
	 * Cenário: VERMELHO (4 peles) entrega em B; AZUL entrega em Karakorum.
	 * Esperado: entregas inválidas recusadas; cada um com 2 votos e 1 bônus;
	 * ficha neutra em 4.
	 */
	@Test
	public void entregaAosConselheirosDaVotosEBonusAoPrimeiro() {
		for (int i = 0; i < 4; i++)
			vermelho.adicionarTesouro(TipoTesouro.PELE);
		azul.adicionarTesouro(TipoTesouro.PELE);
		azul.adicionarTesouro(TipoTesouro.FERRO);
		azul.adicionarTesouro(TipoTesouro.SEDA);

		assertTrue(vermelho.ativarColuna(Coluna.VERDE));
		assertFalse(vermelho.entregarTesouros("B", TipoTesouro.PELE, TipoTesouro.PELE));
		assertTrue(vermelho.mover("A"));
		assertTrue(vermelho.mover("B"));
		assertFalse(vermelho.entregarTesouros("B", TipoTesouro.PELE, TipoTesouro.FERRO));
		assertTrue(vermelho.entregarTesouros("B", TipoTesouro.PELE, TipoTesouro.PELE));
		assertTrue(vermelho.entregarTesouros("B", TipoTesouro.PELE, TipoTesouro.PELE));
		assertFalse(vermelho.entregarTesouros("B", TipoTesouro.PELE, TipoTesouro.PELE));
		assertEquals(2, vermelho.getVotos());
		assertEquals(1, vermelho.getPecasBonus());

		assertTrue(azul.entregarTesouros("K", TipoTesouro.SEDA, TipoTesouro.PELE, TipoTesouro.FERRO));
		assertEquals(2, azul.getVotos());
		assertEquals(1, azul.getPecasBonus());
		assertEquals(0, azul.getTotalTesouros());

		assertEquals(4, tabuleiro.getKurultai().getFichaNeutra());
	}

	/**
	 * Objetivo: com 10 votos (2 jogadores) na ficha neutra, o fim é disparado.
	 * Cenário: ficha neutra em 9; VERMELHO entrega 2 peles em B.
	 * Esperado: fim não disparado antes da entrega e disparado depois.
	 */
	@Test
	public void kurultaiCompletoDisparaFimDoJogo() {
		tabuleiro.getKurultai().registrarVotos(9);
		vermelho.adicionarTesouro(TipoTesouro.PELE);
		vermelho.adicionarTesouro(TipoTesouro.PELE);

		assertTrue(vermelho.ativarColuna(Coluna.VERDE));
		assertTrue(vermelho.mover("A"));
		assertTrue(vermelho.mover("B"));
		assertFalse(tabuleiro.isFimDisparado());

		assertTrue(vermelho.entregarTesouros("B", TipoTesouro.PELE, TipoTesouro.PELE));
		assertTrue(tabuleiro.isFimDisparado());
	}

	/**
	 * Objetivo: influência por região (empate em 1º anula o 2º; branca não conta).
	 * Cenário: Rússia empatada (VERMELHO x AZUL); Pérsia VERMELHO 1º, AZUL 2º; China só VERDE.
	 * Esperado: VERMELHO 2+1, AZUL 1+2, VERDE 3.
	 */
	@Test
	public void pontuacaoDeInfluenciaPorRegiao() {
		Tabuleiro t = novoTabuleiro(3);
		Jogador verm = new Jogador(Cor.VERMELHO, t, 0);
		Jogador az = new Jogador(Cor.AZUL, t, 1);
		Jogador verde = new Jogador(Cor.VERDE, t, 2);

		t.getParada("A").adicionarYurt(verm);
		t.getParada("B").adicionarYurt(verm);
		t.getParada("B").adicionarYurt(az);
		t.getParada("C").adicionarYurt(az);
		t.getParada("F").adicionarYurt(verm);
		t.getParada("G").adicionarYurt(verm);
		t.getCidade("Bagda").conquistar(az, true);
		t.getParada("E").adicionarYurt(verde);

		adicionarMelhorias(verm, Coluna.AMARELA, 2);
		adicionarMelhorias(verm, Coluna.BRANCA, 4);
		adicionarMelhorias(az, Coluna.VERDE, 3);
		adicionarMelhorias(verde, Coluna.VERMELHA, 4);

		HashMap<Jogador, Integer> votos = PontuacaoInfluencia.calcular(t, Arrays.asList(verm, az, verde));
		assertEquals(3, (int) votos.get(verm));
		assertEquals(3, (int) votos.get(az));
		assertEquals(3, (int) votos.get(verde));
	}

	/**
	 * Objetivo: desempate pela proximidade de Karakorum.
	 * Cenário: 5 votos cada, sem tesouros; VERMELHO em A, AZUL em K.
	 * Esperado: AZUL vence.
	 */
	@Test
	public void desempatePorProximidadeDeKarakorum() {
		vermelho.adicionarVotos(5);
		azul.adicionarVotos(5);
		vermelho.moverPara(tabuleiro.getParada("A"));

		assertEquals(Arrays.asList(azul), Kurultai.vencedores(Arrays.asList(vermelho, azul), tabuleiro));
	}

	/**
	 * Objetivo: desempate por tesouros e vitória compartilhada.
	 * Cenário: 5 votos cada, ambos em K; (a) sem tesouros; (b) AZUL com 1 tesouro.
	 * Esperado: (a) os dois vencem; (b) AZUL vence.
	 */
	@Test
	public void desempatePorTesourosEVitoriaCompartilhada() {
		vermelho.adicionarVotos(5);
		azul.adicionarVotos(5);
		List<Jogador> jogadores = Arrays.asList(vermelho, azul);

		assertEquals(jogadores, Kurultai.vencedores(jogadores, tabuleiro));

		azul.adicionarTesouro(TipoTesouro.GADO);
		assertEquals(Arrays.asList(azul), Kurultai.vencedores(jogadores, tabuleiro));
	}

	// =====================================================================
	// Auxiliares
	// =====================================================================

	private int pecas(String provincia) {
		return tabuleiro.getProvincia(provincia).getPecas();
	}

	private static Tabuleiro novoTabuleiro(int numeroJogadores) {
		Tabuleiro t = new Tabuleiro();
		t.adicionarParada("K", TipoParada.KARAKORUM, null);
		t.adicionarParada("A", TipoParada.SIMPLES, Regiao.RUSSIA);
		t.adicionarParada("B", TipoParada.DUPLA, Regiao.RUSSIA);
		t.adicionarParada("C", TipoParada.SIMPLES, Regiao.RUSSIA);
		t.adicionarParada("E", TipoParada.SIMPLES, Regiao.CHINA);
		t.adicionarParada("F", TipoParada.SIMPLES, Regiao.PERSIA);
		t.adicionarParada("G", TipoParada.SIMPLES, Regiao.PERSIA);
		t.conectar("K", "A");
		t.conectar("A", "B");
		t.conectar("B", "C");
		t.conectar("C", "E");
		t.conectar("E", "K");
		t.conectar("K", "F");
		t.conectar("F", "G");

		t.adicionarProvincia("PR_ESPADA", Regiao.RUSSIA, TipoTributo.ESPADA, "A");
		t.adicionarProvincia("PR_YURT", Regiao.CHINA, TipoTributo.YURT, "C", "E");
		t.adicionarProvincia("PR_MOEDA", Regiao.PERSIA, TipoTributo.MOEDA, "K", "F", "G");
		t.adicionarProvincia("PR_KHAN2", Regiao.CHINA, TipoTributo.MOEDA, "E");
		t.definirProvinciaDoKhan("PR_ESPADA", "PR_YURT", "PR_MOEDA");
		t.definirProvinciaDoKhan("PR_KHAN2", "PR_YURT", "PR_MOEDA");

		t.adicionarCidade("Kiev", Regiao.RUSSIA, "A");
		t.adicionarCidade("Pequim", Regiao.CHINA, "E");
		t.adicionarCidade("Bagda", Regiao.PERSIA, "G");
		t.adicionarCidade("Sarai", Regiao.RUSSIA, "C");

		t.adicionarConselheiro("B", Conselheiro.par(TipoTesouro.PELE));
		t.adicionarConselheiro("K", Conselheiro.karakorum(TipoTesouro.PELE, TipoTesouro.FERRO, TipoTesouro.SEDA));

		List<TipoTesouro> tesouros = Arrays.asList(
				TipoTesouro.PELE, TipoTesouro.PELE, TipoTesouro.FERRO, TipoTesouro.SEDA, // Kiev
				TipoTesouro.GADO, TipoTesouro.GADO, TipoTesouro.ESPECIARIA, TipoTesouro.SEDA, // Pequim
				TipoTesouro.FERRO, TipoTesouro.FERRO, TipoTesouro.PELE, TipoTesouro.GADO, // Bagda
				TipoTesouro.SEDA, TipoTesouro.SEDA, TipoTesouro.SEDA, TipoTesouro.ESPECIARIA); // Sarai

		// os 5 primeiros vão para a área de melhorias; os 3 últimos ficam na pilha
		List<Melhoria> melhorias = Arrays.asList(
				new Melhoria(TipoMelhoria.CAVALOS, Coluna.AMARELA),
				new Melhoria(TipoMelhoria.PUNHO, Coluna.VERMELHA),
				new Melhoria(TipoMelhoria.MOEDA, Coluna.VERDE),
				new Melhoria(TipoMelhoria.ESPADA, Coluna.VERMELHA),
				new Melhoria(TipoMelhoria.YURT, Coluna.VERDE),
				new Melhoria(TipoMelhoria.CAVALOS, Coluna.VERDE),
				new Melhoria(TipoMelhoria.PUNHO, Coluna.AMARELA),
				new Melhoria(TipoMelhoria.MOEDA, Coluna.VERMELHA));

		t.prepararPartida(numeroJogadores, tesouros, melhorias);
		return t;
	}

	private static void adicionarMelhorias(Jogador j, Coluna coluna, int quantidade) {
		for (int i = 0; i < quantidade; i++)
			j.adicionarMelhoria(coluna, new Melhoria(TipoMelhoria.CAVALOS, coluna));
	}
}
