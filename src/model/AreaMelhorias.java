package model;

import java.util.ArrayList;
import java.util.List;

/** Área de melhorias: 5 espaços revelados e a pilha. */
class AreaMelhorias {
	static final int ESPACOS = 5;
	static final int ESPACO_MOEDA = 1;  // espaço com o ícone de moeda
	static final int ESPACO_ESPADA = 3; // espaço com o ícone de espada
	static final int TOPO_DA_PILHA = -1;

	private final List<Melhoria> espacos = new ArrayList<>();
	private final List<Melhoria> pilha;

	AreaMelhorias(List<Melhoria> pilha) {
		this.pilha = new ArrayList<>(pilha);
		for (int i = 0; i < ESPACOS; i++)
			espacos.add(null);
		reabastecer();
	}

	/** Melhoria no espaço indicado, ou no topo da pilha se indice == TOPO_DA_PILHA. */
	Melhoria ver(int indice) {
		if (indice == TOPO_DA_PILHA)
			return pilha.isEmpty() ? null : pilha.get(0);
		if (indice < 0 || indice >= ESPACOS)
			return null;
		return espacos.get(indice);
	}

	/** Retira a melhoria e desloca as restantes para a direita (sem revelar novas). */
	Melhoria retirar(int indice) {
		if (indice == TOPO_DA_PILHA)
			return pilha.isEmpty() ? null : pilha.remove(0);
		Melhoria m = ver(indice);
		if (m == null)
			return null;
		for (int i = indice; i > 0; i--)
			espacos.set(i, espacos.get(i - 1));
		espacos.set(0, null);
		return m;
	}

	boolean isEspacoVazio(int indice) {
		return espacos.get(indice) == null;
	}

	/** Revela melhorias da pilha nos espaços vazios. */
	void reabastecer() {
		for (int i = 0; i < ESPACOS; i++)
			if (espacos.get(i) == null && !pilha.isEmpty())
				espacos.set(i, pilha.remove(0));
	}
}
