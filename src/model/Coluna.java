package model;

/**
 * Colunas do tabuleiro do jogador. Cada coluna colorida está associada a uma
 * região do mapa (usada na pontuação de influência); a branca não tem região.
 */
enum Coluna {
	VERMELHA(Regiao.CHINA),
	VERDE(Regiao.PERSIA),
	AMARELA(Regiao.RUSSIA),
	BRANCA(null);

	private final Regiao regiao;

	Coluna(Regiao regiao) {
		this.regiao = regiao;
	}

	Regiao getRegiao() {
		return regiao;
	}

	boolean isColorida() {
		return regiao != null;
	}

	static Coluna daRegiao(Regiao regiao) {
		for (Coluna c : values())
			if (c.regiao == regiao)
				return c;
		return null;
	}
}
