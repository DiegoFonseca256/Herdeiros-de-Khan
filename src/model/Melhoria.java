package model;

/** Uma peça de melhoria. A cor de fundo define em qual coluna ela pode ser colocada. */
class Melhoria {
	private final TipoMelhoria tipo;
	private final Coluna cor;
	private final TipoTesouro tesouro; // só para melhorias do tipo TESOURO

	Melhoria(TipoMelhoria tipo, Coluna cor) {
		this(tipo, cor, null);
	}

	Melhoria(TipoMelhoria tipo, Coluna cor, TipoTesouro tesouro) {
		this.tipo = tipo;
		this.cor = cor;
		this.tesouro = tesouro;
	}

	TipoMelhoria getTipo() {
		return tipo;
	}

	Coluna getCor() {
		return cor;
	}

	TipoTesouro getTesouro() {
		return tesouro;
	}
}
