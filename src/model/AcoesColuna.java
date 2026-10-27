package model;

/** Ações impressas na base de uma coluna do tabuleiro do jogador (valores padrão do herdeiro Tolui). */
class AcoesColuna {
	private final int movimentos;
	private final int punhos;
	private final boolean khan;
	private final boolean recupera;
	private final TipoTributo tributoVirtual;

	AcoesColuna(int movimentos, int punhos, boolean khan, boolean recupera, TipoTributo tributoVirtual) {
		this.movimentos = movimentos;
		this.punhos = punhos;
		this.khan = khan;
		this.recupera = recupera;
		this.tributoVirtual = tributoVirtual;
	}

	static AcoesColuna padrao(Coluna coluna) {
		return switch (coluna) {
			case VERMELHA -> new AcoesColuna(1, 1, false, false, TipoTributo.MOEDA);
			case VERDE -> new AcoesColuna(2, 1, false, false, null);
			case AMARELA -> new AcoesColuna(1, 2, true, false, null);
			case BRANCA -> new AcoesColuna(2, 0, true, true, null);
		};
	}

	int getMovimentos() {
		return movimentos;
	}

	int getPunhos() {
		return punhos;
	}

	boolean temKhan() {
		return khan;
	}

	boolean recuperaAtivacoes() {
		return recupera;
	}

	TipoTributo getTributoVirtual() {
		return tributoVirtual;
	}
}
