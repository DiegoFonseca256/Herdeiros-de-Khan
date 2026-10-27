package model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Conselheiro: PAR (2 iguais, 1 voto), TRIO (3 quaisquer, 1 voto)
 * ou KARAKORUM (3 específicos, 2 votos).
 */
class Conselheiro {
	enum Tipo {
		PAR, TRIO, KARAKORUM
	}

	private final Tipo tipo;
	private final List<TipoTesouro> exigidos;
	private boolean bonusDisponivel = true;

	private Conselheiro(Tipo tipo, List<TipoTesouro> exigidos) {
		this.tipo = tipo;
		this.exigidos = exigidos;
	}

	static Conselheiro par(TipoTesouro tesouro) {
		return new Conselheiro(Tipo.PAR, Arrays.asList(tesouro, tesouro));
	}

	static Conselheiro trio() {
		return new Conselheiro(Tipo.TRIO, List.of());
	}

	static Conselheiro karakorum(TipoTesouro t1, TipoTesouro t2, TipoTesouro t3) {
		return new Conselheiro(Tipo.KARAKORUM, Arrays.asList(t1, t2, t3));
	}

	Tipo getTipo() {
		return tipo;
	}

	int getVotos() {
		return tipo == Tipo.KARAKORUM ? 2 : 1;
	}

	/** Verifica se o conjunto de tesouros entregue satisfaz a exigência do conselheiro. */
	boolean aceita(List<TipoTesouro> entrega) {
		if (tipo == Tipo.TRIO)
			return entrega.size() == 3;
		return entrega.size() == exigidos.size() && contar(entrega).equals(contar(exigidos));
	}

	private static HashMap<TipoTesouro, Integer> contar(List<TipoTesouro> lista) {
		HashMap<TipoTesouro, Integer> contagem = new HashMap<>();
		for (TipoTesouro t : lista)
			contagem.merge(t, 1, Integer::sum);
		return contagem;
	}

	/** Retira a peça de bônus, se ainda estiver disponível. */
	boolean recolherBonus() {
		if (!bonusDisponivel)
			return false;
		bonusDisponivel = false;
		return true;
	}

	boolean isBonusDisponivel() {
		return bonusDisponivel;
	}
}
