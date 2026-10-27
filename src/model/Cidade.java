package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Cidade do mapa. Só pode ser atacada quando está disponível (revelada e não conquistada). */
class Cidade {
	static final int TESOUROS_INICIAIS = 4;

	enum Estado {
		OCULTA, DISPONIVEL, CONQUISTADA
	}

	private final String nome;
	private final Regiao regiao;
	private final Set<Parada> paradasAdjacentes = new HashSet<>();
	private final List<TipoTesouro> tesouros = new ArrayList<>();
	private Estado estado = Estado.OCULTA;
	private Jogador yurt; // yurt colocado pelo conquistador

	Cidade(String nome, Regiao regiao) {
		this.nome = nome;
		this.regiao = regiao;
	}

	String getNome() {
		return nome;
	}

	Regiao getRegiao() {
		return regiao;
	}

	Estado getEstado() {
		return estado;
	}

	List<TipoTesouro> getTesouros() {
		return tesouros;
	}

	void adicionarParadaAdjacente(Parada parada) {
		paradasAdjacentes.add(parada);
	}

	boolean isAdjacenteA(Collection<Parada> paradas) {
		for (Parada p : paradas)
			if (paradasAdjacentes.contains(p))
				return true;
		return false;
	}

	void revelar(List<TipoTesouro> novosTesouros) {
		tesouros.addAll(novosTesouros);
		estado = Estado.DISPONIVEL;
	}

	boolean retirarTesouro(TipoTesouro tipo) {
		return tesouros.remove(tipo);
	}

	void conquistar(Jogador conquistador, boolean colocouYurt) {
		estado = Estado.CONQUISTADA;
		if (colocouYurt)
			yurt = conquistador;
	}

	boolean temYurtDe(Jogador jogador) {
		return yurt == jogador;
	}
}
