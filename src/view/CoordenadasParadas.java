package view;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;

import model.ModelFacade;

/** Posição de cada parada na imagem original do tabuleiro (2905 x 2919 px) e área de clique. */
class CoordenadasParadas {
	private static final int RAIO_SIMPLES = 60;
	private static final int RAIO_DUPLA = 90;
	/** Brasão de Karakorum: o clique vale em qualquer ponto dele. */
	private static final Rectangle AREA_KARAKORUM = new Rectangle(1200, 1044, 494, 495);

	private final HashMap<String, Point> pontos = new HashMap<>();
	private final HashMap<String, Integer> raios = new HashMap<>();

	CoordenadasParadas() {
		// Rússia
		adicionar("R1", 125, 540, RAIO_SIMPLES);
		adicionar("R2", 487, 256, RAIO_SIMPLES);
		adicionar("R3", 470, 537, RAIO_SIMPLES);
		adicionar("R4", 892, 210, RAIO_DUPLA);
		adicionar("R5", 855, 550, RAIO_SIMPLES);
		adicionar("R6", 1272, 492, RAIO_SIMPLES);
		adicionar("R7", 1078, 842, RAIO_SIMPLES);
		adicionar("R8", 1558, 672, RAIO_SIMPLES);
		adicionar("R9", 1756, 225, RAIO_SIMPLES);
		adicionar("R10", 1894, 588, RAIO_SIMPLES);
		adicionar("R11", 2237, 497, RAIO_DUPLA);
		// Pérsia
		adicionar("P1", 460, 895, RAIO_DUPLA);
		adicionar("P2", 840, 1190, RAIO_SIMPLES);
		adicionar("P3", 575, 1415, RAIO_SIMPLES);
		adicionar("P4", 958, 1550, RAIO_SIMPLES);
		adicionar("P5", 215, 1494, RAIO_SIMPLES);
		adicionar("P6", 456, 1644, RAIO_SIMPLES);
		adicionar("P7", 181, 1912, RAIO_SIMPLES);
		adicionar("P8", 725, 1881, RAIO_DUPLA);
		adicionar("P9", 1273, 1820, RAIO_SIMPLES);
		adicionar("P10", 1212, 2237, RAIO_SIMPLES);
		adicionar("P11", 1465, 2060, RAIO_SIMPLES);
		// China
		adicionar("C1", 2494, 650, RAIO_SIMPLES);
		adicionar("C2", 2769, 875, RAIO_SIMPLES);
		adicionar("C3", 2519, 1062, RAIO_SIMPLES);
		adicionar("C4", 2142, 1248, RAIO_SIMPLES);
		adicionar("C5", 1893, 1604, RAIO_SIMPLES);
		adicionar("C6", 2171, 1675, RAIO_SIMPLES);
		adicionar("C7", 2556, 1550, RAIO_DUPLA);
		adicionar("C8", 2525, 2031, RAIO_SIMPLES);
		adicionar("C9", 1837, 2222, RAIO_DUPLA);
		adicionar("C10", 2233, 2230, RAIO_SIMPLES);
		adicionar("C11", 2585, 2410, RAIO_SIMPLES);
		// o clique em Karakorum usa AREA_KARAKORUM; o ponto é onde os peões são desenhados
		adicionar(ModelFacade.KARAKORUM, 1447, 1290, 0);
	}

	private void adicionar(String nome, int x, int y, int raio) {
		pontos.put(nome, new Point(x, y));
		raios.put(nome, raio);
	}

	Point getPonto(String nome) {
		return pontos.get(nome);
	}

	/** Parada sob o ponto (em coordenadas da imagem original), ou null se não houver. */
	String paradaEm(int x, int y) {
		if (AREA_KARAKORUM.contains(x, y))
			return ModelFacade.KARAKORUM;
		for (String nome : pontos.keySet())
			if (pontos.get(nome).distance(x, y) <= raios.get(nome))
				return nome;
		return null;
	}
}
