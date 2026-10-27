package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Área entre as rotas que acumula peças de tributo de um único tipo. */
class Provincia {
	static final int MAX_PECAS = 3;

	private final String nome;
	private final Regiao regiao;
	private final TipoTributo tipo;
	private final Set<Parada> paradasAdjacentes = new HashSet<>();
	private int pecas;
	private boolean doKhan;
	private final List<Provincia> adjacentesDoKhan = new ArrayList<>();

	Provincia(String nome, Regiao regiao, TipoTributo tipo) {
		this.nome = nome;
		this.regiao = regiao;
		this.tipo = tipo;
	}

	String getNome() {
		return nome;
	}

	Regiao getRegiao() {
		return regiao;
	}

	TipoTributo getTipo() {
		return tipo;
	}

	int getPecas() {
		return pecas;
	}

	void adicionarParadaAdjacente(Parada parada) {
		paradasAdjacentes.add(parada);
	}

	/** Indica se a província é adjacente a alguma das paradas informadas. */
	boolean isAdjacenteA(Collection<Parada> paradas) {
		for (Parada p : paradas)
			if (paradasAdjacentes.contains(p))
				return true;
		return false;
	}

	boolean adicionarPeca() {
		if (pecas >= MAX_PECAS)
			return false;
		pecas++;
		return true;
	}

	boolean retirarPeca() {
		if (pecas == 0)
			return false;
		pecas--;
		return true;
	}

	boolean isDoKhan() {
		return doKhan;
	}

	/** Marca a província como província do Khan, com as 2 províncias indicadas pelas setas. */
	void tornarDoKhan(Provincia adjacente1, Provincia adjacente2) {
		doKhan = true;
		adjacentesDoKhan.clear();
		adjacentesDoKhan.add(adjacente1);
		adjacentesDoKhan.add(adjacente2);
	}

	List<Provincia> getAdjacentesDoKhan() {
		return adjacentesDoKhan;
	}
}
