package model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Uma parada do mapa, ligada a outras paradas por rotas. */
class Parada {
	private final String nome;
	private final TipoParada tipo;
	private final Regiao regiao;
	private final Set<Parada> vizinhas = new HashSet<>();
	private final List<Jogador> yurts = new ArrayList<>();
	private final Set<Jogador> peoes = new HashSet<>();
	private Conselheiro conselheiro;

	Parada(String nome, TipoParada tipo, Regiao regiao) {
		this.nome = nome;
		this.tipo = tipo;
		this.regiao = regiao;
	}

	String getNome() {
		return nome;
	}

	TipoParada getTipo() {
		return tipo;
	}

	Regiao getRegiao() {
		return regiao;
	}

	Set<Parada> getVizinhas() {
		return vizinhas;
	}

	void conectar(Parada outra) {
		vizinhas.add(outra);
		outra.vizinhas.add(this);
	}

	boolean isVizinha(Parada outra) {
		return vizinhas.contains(outra);
	}

	Conselheiro getConselheiro() {
		return conselheiro;
	}

	void setConselheiro(Conselheiro conselheiro) {
		this.conselheiro = conselheiro;
	}

	// ---- yurts ----

	boolean podeReceberYurt() {
		return yurts.size() < tipo.getCapacidadeYurts();
	}

	void adicionarYurt(Jogador dono) {
		yurts.add(dono);
	}

	boolean temYurtDe(Jogador jogador) {
		return yurts.contains(jogador);
	}

	int contarYurtsDe(Jogador jogador) {
		int n = 0;
		for (Jogador dono : yurts)
			if (dono == jogador)
				n++;
		return n;
	}

	// ---- peões ----

	void entrar(Jogador jogador) {
		peoes.add(jogador);
	}

	void sair(Jogador jogador) {
		peoes.remove(jogador);
	}

	/** Indica se o jogador pode terminar o movimento aqui, considerando os outros peões presentes. */
	boolean comportaPeao(Jogador jogador) {
		int outros = peoes.size() - (peoes.contains(jogador) ? 1 : 0);
		return outros < tipo.getCapacidadePeoes();
	}
}
