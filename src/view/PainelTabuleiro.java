package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JPanel;

import model.ModelFacade;

/** Desenha o tabuleiro e os peões (só com drawImage) e trata o clique para mover o peão da vez. */
class PainelTabuleiro extends JPanel {
	private static final int LARGURA_MAXIMA = 980;
	private static final int MARGEM_JANELA = 45; // barra de título e bordas da janela
	// em pixels da imagem original, como as coordenadas das paradas
	private static final int TAMANHO_PEAO = 105;
	private static final int ESPACO_ENTRE_PEOES = 88;

	private final JanelaJogo janela;
	private final ModelFacade modelo = ModelFacade.getInstancia();
	private final CoordenadasParadas coordenadas = new CoordenadasParadas();
	private final HashMap<String, Image> imagensPeoes = new HashMap<>();
	private final BufferedImage tabuleiro;
	private final double escala;

	PainelTabuleiro(JanelaJogo janela) {
		this.janela = janela;
		BufferedImage original = Imagens.carregar("tabuleiro.jpg");
		int largura = larguraQueCabeNaTela(original);
		escala = (double) largura / original.getWidth();
		tabuleiro = Imagens.reduzir(original, largura, (int) Math.round(original.getHeight() * escala));
		for (String cor : modelo.getCores())
			imagensPeoes.put(cor, Imagens.carregar("peao-" + cor.toLowerCase() + ".jpg"));
		setPreferredSize(new Dimension(tabuleiro.getWidth(), tabuleiro.getHeight()));
		addMouseListener(new CliqueTabuleiro());
	}

	/** Largura do tabuleiro para a janela caber na área livre da tela (sem a barra de tarefas), até LARGURA_MAXIMA. */
	private static int larguraQueCabeNaTela(BufferedImage original) {
		Rectangle areaLivre = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		int alturaDisponivel = areaLivre.height - MARGEM_JANELA;
		int largura = alturaDisponivel * original.getWidth() / original.getHeight();
		return Math.min(LARGURA_MAXIMA, largura);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.drawImage(tabuleiro, 0, 0, null);
		desenharPeoes(g2);
	}

	/** Peões que dividem a mesma parada são desenhados lado a lado. */
	private void desenharPeoes(Graphics2D g2) {
		LinkedHashMap<String, List<String>> porParada = new LinkedHashMap<>();
		for (String cor : modelo.getCores()) {
			String parada = modelo.getPosicao(cor);
			if (!porParada.containsKey(parada))
				porParada.put(parada, new ArrayList<>());
			porParada.get(parada).add(cor);
		}
		for (String parada : porParada.keySet()) {
			List<String> cores = porParada.get(parada);
			Point p = coordenadas.getPonto(parada);
			int cx = (int) Math.round(p.x * escala);
			int cy = (int) Math.round(p.y * escala);
			for (int i = 0; i < cores.size(); i++) {
				int deslocamento = (int) Math.round((i - (cores.size() - 1) / 2.0) * ESPACO_ENTRE_PEOES * escala);
				desenharPeao(g2, cores.get(i), cx + deslocamento, cy);
			}
		}
	}

	/** Desenha a ficha do peão e destaca o peão da vez. */
	private void desenharPeao(Graphics2D g2, String cor, int cx, int cy) {
		int tamanho = (int) Math.round(TAMANHO_PEAO * escala);
		int x = cx - tamanho / 2;
		int y = cy - tamanho / 2;
		Imagens.desenharFicha(g2, imagensPeoes.get(cor), x, y, tamanho);

		boolean daVez = cor.equals(modelo.getCorDaVez());
		g2.setColor(daVez ? Color.WHITE : Color.BLACK);
		g2.setStroke(new BasicStroke(daVez ? 3 : 1));
		g2.drawOval(x, y, tamanho, tamanho);
	}

	/** Clique no tabuleiro: move o peão da vez se o Model permitir; senão, nada acontece. */
	private class CliqueTabuleiro extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			int x = (int) Math.round(e.getX() / escala);
			int y = (int) Math.round(e.getY() / escala);
			String parada = coordenadas.paradaEm(x, y);
			if (parada != null && modelo.mover(parada))
				janela.atualizar();
		}
	}
}
