package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** Trilha do Kurultai: a ficha neutra dispara o fim ao atingir o limite. */
class Kurultai {
	private final int limite;
	private int fichaNeutra;

	Kurultai(int numeroJogadores) {
		this.limite = limitePara(numeroJogadores);
	}

	/** Marca de fim de jogo: 10 / 14 / 16 / 17 votos para 2 / 3 / 4 / 5 jogadores. */
	static int limitePara(int numeroJogadores) {
		return switch (numeroJogadores) {
			case 2 -> 10;
			case 3 -> 14;
			case 4 -> 16;
			case 5 -> 17;
			default -> throw new IllegalArgumentException("O jogo é para 2 a 5 jogadores");
		};
	}

	void registrarVotos(int votos) {
		fichaNeutra += votos;
	}

	int getFichaNeutra() {
		return fichaNeutra;
	}

	int getLimite() {
		return limite;
	}

	boolean isCompleto() {
		return fichaNeutra >= limite;
	}

	/** Mais votos vence. Desempate: mais tesouros, depois mais perto de Karakorum; se persistir, todos vencem. */
	static List<Jogador> vencedores(List<Jogador> jogadores, Tabuleiro tabuleiro) {
		List<Jogador> candidatos = new ArrayList<>(jogadores);
		int maxVotos = 0;
		for (Jogador j : candidatos)
			maxVotos = Math.max(maxVotos, j.getVotos());
		final int votos = maxVotos;
		candidatos.removeIf(j -> j.getVotos() < votos);

		int maxTesouros = 0;
		for (Jogador j : candidatos)
			maxTesouros = Math.max(maxTesouros, j.getTotalTesouros());
		final int tesouros = maxTesouros;
		candidatos.removeIf(j -> j.getTotalTesouros() < tesouros);

		HashMap<Jogador, Integer> distancias = new HashMap<>();
		int menorDistancia = Integer.MAX_VALUE;
		for (Jogador j : candidatos) {
			int d = tabuleiro.distancia(j.getPosicao(), tabuleiro.getKarakorum());
			distancias.put(j, d);
			menorDistancia = Math.min(menorDistancia, d);
		}
		final int distancia = menorDistancia;
		candidatos.removeIf(j -> distancias.get(j) > distancia);
		return candidatos;
	}
}
