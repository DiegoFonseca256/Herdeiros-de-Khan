package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** Votos finais de influência: 1º e 2º em yurts por região, conforme as melhorias da coluna. */
final class PontuacaoInfluencia {
	// índice = número de melhorias na coluna (0 a 4)
	private static final int[] VOTOS_PRIMEIRO = { 1, 1, 2, 2, 3 };
	private static final int[] VOTOS_SEGUNDO = { 0, 1, 1, 2, 2 };

	private PontuacaoInfluencia() {
	}

	static HashMap<Jogador, Integer> calcular(Tabuleiro tabuleiro, List<Jogador> jogadores) {
		HashMap<Jogador, Integer> votos = new HashMap<>();
		for (Jogador j : jogadores)
			votos.put(j, 0);

		for (Regiao regiao : Regiao.values()) {
			HashMap<Jogador, Integer> yurts = new HashMap<>();
			int maior = 0;
			for (Jogador j : jogadores) {
				int n = tabuleiro.contarYurts(j, regiao);
				yurts.put(j, n);
				maior = Math.max(maior, n);
			}
			if (maior == 0)
				continue;

			List<Jogador> primeiros = comQuantidade(yurts, maior);
			Coluna coluna = Coluna.daRegiao(regiao);
			for (Jogador j : primeiros)
				votos.merge(j, VOTOS_PRIMEIRO[j.getNumeroMelhorias(coluna)], Integer::sum);

			// empate no 1º lugar: ninguém fica em 2º
			if (primeiros.size() > 1)
				continue;

			int segundoMaior = 0;
			for (int n : yurts.values())
				if (n < maior)
					segundoMaior = Math.max(segundoMaior, n);
			if (segundoMaior == 0)
				continue;
			for (Jogador j : comQuantidade(yurts, segundoMaior))
				votos.merge(j, VOTOS_SEGUNDO[j.getNumeroMelhorias(coluna)], Integer::sum);
		}
		return votos;
	}

	private static List<Jogador> comQuantidade(HashMap<Jogador, Integer> yurts, int quantidade) {
		List<Jogador> lista = new ArrayList<>();
		for (Jogador j : yurts.keySet())
			if (yurts.get(j) == quantidade)
				lista.add(j);
		return lista;
	}
}
