package model;

/** Peças de tributo e a quantidade de cada uma que vem no jogo (10 espadas, 10 yurts, 15 moedas). */
enum TipoTributo {
	ESPADA(10), YURT(10), MOEDA(15);

	private final int total;

	TipoTributo(int total) {
		this.total = total;
	}

	int getTotal() {
		return total;
	}
}
