package model;

/**
 * Tipos de parada do mapa. Cada tipo define quantos peões podem terminar o
 * movimento nela e quantos yurts ela comporta.
 */
enum TipoParada {
	SIMPLES(1, 1),
	DUPLA(2, 2),
	KARAKORUM(Integer.MAX_VALUE, 0);

	private final int capacidadePeoes;
	private final int capacidadeYurts;

	TipoParada(int capacidadePeoes, int capacidadeYurts) {
		this.capacidadePeoes = capacidadePeoes;
		this.capacidadeYurts = capacidadeYurts;
	}

	int getCapacidadePeoes() {
		return capacidadePeoes;
	}

	int getCapacidadeYurts() {
		return capacidadeYurts;
	}
}
