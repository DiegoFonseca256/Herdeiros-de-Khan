package model;

/**
 * Tipos de peça de melhoria. As melhorias "virtuais" (moeda, espada, yurt)
 * valem como um tributo que só existe no turno em que a coluna é ativada.
 */
enum TipoMelhoria {
	MOEDA(0, 0, TipoTributo.MOEDA),
	ESPADA(0, 0, TipoTributo.ESPADA),
	YURT(0, 0, TipoTributo.YURT),
	TESOURO(0, 0, null),
	PUNHO(0, 1, null),
	CAVALOS(2, 0, null);

	private final int movimentos;
	private final int punhos;
	private final TipoTributo tributoVirtual;

	TipoMelhoria(int movimentos, int punhos, TipoTributo tributoVirtual) {
		this.movimentos = movimentos;
		this.punhos = punhos;
		this.tributoVirtual = tributoVirtual;
	}

	int getMovimentos() {
		return movimentos;
	}

	int getPunhos() {
		return punhos;
	}

	TipoTributo getTributoVirtual() {
		return tributoVirtual;
	}
}
