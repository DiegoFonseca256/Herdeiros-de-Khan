package view;

import java.awt.Point;
import java.util.LinkedHashMap;

import model.ModelFacade;

/** Posição das províncias e dos destinos do Khan na imagem original do tabuleiro (2905 x 2919 px). */
class CoordenadasMapa {
	private static final int RAIO_PROVINCIA = 80;
	private static final int RAIO_KHAN = 75;
	/** Distância entre os centros dos tesouros na grade 2x2 sobre a cidade. */
	static final int ESPACO_TESOUROS = 105;
	static final int TAMANHO_TESOURO = 100;

	private final LinkedHashMap<String, Point> provincias = new LinkedHashMap<>();
	private final LinkedHashMap<String, Point> destinosKhan = new LinkedHashMap<>();
	private final LinkedHashMap<String, Point> cidades = new LinkedHashMap<>();

	CoordenadasMapa() {
		// ícone impresso de cada província (onde ficam as peças de tributo)
		provincias.put("YURT_KIEV", new Point(632, 418));
		provincias.put("ESPADA_MOSCOU", new Point(857, 359));
		provincias.put("MOEDA_MOSCOU", new Point(798, 938));
		provincias.put("YURT_MOSCOU", new Point(1248, 927));
		provincias.put("MOEDA_SARAI", new Point(1964, 758));
		provincias.put("ESPADA_KAESONG", new Point(2158, 1037));
		provincias.put("ESPADA_SAMARCANDA", new Point(525, 1200));
		provincias.put("MOEDA_BAGDA", new Point(605, 1661));
		provincias.put("YURT_PEQUIM", new Point(1518, 1624));
		provincias.put("ESPADA_CABUL", new Point(1685, 1914));
		provincias.put("YURT_CANTAO", new Point(2067, 1828));
		provincias.put("MOEDA_PEQUIM", new Point(2394, 1560));

		// rosto de cada província do Khan e o da área de melhorias
		destinosKhan.put("YURT_KIEV", new Point(632, 600));
		destinosKhan.put("MOEDA_SARAI", new Point(1744, 908));
		destinosKhan.put("MOEDA_BAGDA", new Point(809, 1725));
		destinosKhan.put("YURT_CANTAO", new Point(2131, 2038));
		destinosKhan.put(ModelFacade.AREA_MELHORIAS, new Point(102, 2802));

		// centro do desenho de cada cidade (onde ficam os tesouros ou o yurt do conquistador)
		cidades.put("KIEV", new Point(311, 429));
		cidades.put("MOSCOU", new Point(1071, 643));
		cidades.put("SARAI", new Point(1723, 430));
		cidades.put("KAESONG", new Point(2550, 860));
		cidades.put("PEQUIM", new Point(2088, 1490));
		cidades.put("CANTAO", new Point(2443, 2242));
		cidades.put("SAMARCANDA", new Point(804, 1382));
		cidades.put("BAGDA", new Point(268, 1693));
		cidades.put("CABUL", new Point(1264, 2036));
	}

	Point getPontoCidade(String cidade) {
		return cidades.get(cidade);
	}

	/** Centro do i-ésimo tesouro da cidade, numa grade 2x2 (esquerda/direita, cima/baixo). */
	Point getPontoTesouro(String cidade, int indice) {
		Point c = cidades.get(cidade);
		int dx = (indice % 2 == 0 ? -1 : 1) * ESPACO_TESOUROS / 2;
		int dy = (indice < 2 ? -1 : 1) * ESPACO_TESOUROS / 2;
		return new Point(c.x + dx, c.y + dy);
	}

	Point getPontoProvincia(String nome) {
		return provincias.get(nome);
	}

	Point getPontoKhan(String destino) {
		return destinosKhan.get(destino);
	}

	/** Província sob o ponto (em coordenadas da imagem original), ou null se não houver. */
	String provinciaEm(int x, int y) {
		return dentroDoRaio(provincias, x, y, RAIO_PROVINCIA);
	}

	/** Destino do Khan (rosto) sob o ponto, ou null se não houver. */
	String destinoKhanEm(int x, int y) {
		return dentroDoRaio(destinosKhan, x, y, RAIO_KHAN);
	}

	private static String dentroDoRaio(LinkedHashMap<String, Point> pontos, int x, int y, int raio) {
		for (String nome : pontos.keySet())
			if (pontos.get(nome).distance(x, y) <= raio)
				return nome;
		return null;
	}
}
