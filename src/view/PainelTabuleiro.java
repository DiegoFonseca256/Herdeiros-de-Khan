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
import javax.swing.SwingUtilities;

import controller.Controller;
import controller.ObservadoIF;
import controller.ObservadorIF;
import model.ModelFacade;

/** Desenha o tabuleiro, as peças de tributo, o Khan e os peões (só com drawImage) e trata os cliques. */
class PainelTabuleiro extends JPanel implements ObservadorIF {
	private static final int LARGURA_MAXIMA = 980;
	private static final int MARGEM_JANELA = 45; // barra de título e bordas da janela
	// em pixels da imagem original, como as coordenadas das paradas
	private static final int TAMANHO_PEAO = 105;
	private static final int ESPACO_ENTRE_PEOES = 88;
	private static final int TAMANHO_TRIBUTO = 95;
	private static final int ESPACO_ENTRE_TRIBUTOS = 72;
	/** Fração de cada lado da ficha de tributo que é borda branca (cortada no tabuleiro). */
	private static final double BORDA_FICHA_TRIBUTO = 0.15;
	private static final int TAMANHO_YURT = 80;
	private static final int ESPACO_ENTRE_YURTS = 85;
	private static final int ALTURA_YURT = 85; // acima do centro da parada, para o peão não esconder
	private static final int TAMANHO_KHAN = 170;
	/** Rosto da área de melhorias, recortado do tabuleiro para servir de peça do Khan. */
	private static final Rectangle RECORTE_KHAN = new Rectangle(32, 2732, 140, 140);
	private static final Color COR_ANEL_KHAN = new Color(240, 190, 30);

	private final ModelFacade modelo = ModelFacade.getInstancia();
	private final Controller controller = Controller.getInstancia();
	private final CoordenadasParadas coordenadas = new CoordenadasParadas();
	private final CoordenadasMapa mapa = new CoordenadasMapa();
	private final HashMap<String, Image> imagensPeoes = new HashMap<>();
	private final HashMap<String, Image> imagensTributos = new HashMap<>();
	private final HashMap<String, Image> imagensTesouros = new HashMap<>(); // carregadas no 1º uso
	private final BufferedImage tabuleiro;
	private final BufferedImage khan;
	private final double escala;

	PainelTabuleiro() {
		BufferedImage original = Imagens.carregar("tabuleiro.jpg");
		int largura = larguraQueCabeNaTela(original);
		escala = (double) largura / original.getWidth();
		tabuleiro = Imagens.reduzir(original, largura, (int) Math.round(original.getHeight() * escala));
		khan = original.getSubimage(RECORTE_KHAN.x, RECORTE_KHAN.y, RECORTE_KHAN.width, RECORTE_KHAN.height);
		for (String cor : modelo.getCores())
			imagensPeoes.put(cor, Imagens.carregar("peao-" + cor.toLowerCase() + ".jpg"));
		for (String provincia : modelo.getProvincias()) {
			String tributo = modelo.getTributoProvincia(provincia);
			if (!imagensTributos.containsKey(tributo))
				imagensTributos.put(tributo, semBorda(Imagens.carregar("tributo-" + tributo.toLowerCase() + ".jpg")));
		}
		setPreferredSize(new Dimension(tabuleiro.getWidth(), tabuleiro.getHeight()));
		addMouseListener(new CliqueTabuleiro());
		controller.registra(this);
	}

	/** Redesenha a cada mudança da partida (peões, tributos, Khan, jogador da vez). */
	@Override
	public void notify(ObservadoIF o) {
		if (JanelaJogo.mudouPartida(o.get(Controller.EVENTO)))
			repaint();
	}

	/** Parte central da ficha, sem a borda branca larga, para ficar legível em tamanho pequeno. */
	private static BufferedImage semBorda(BufferedImage ficha) {
		int mx = (int) Math.round(ficha.getWidth() * BORDA_FICHA_TRIBUTO);
		int my = (int) Math.round(ficha.getHeight() * BORDA_FICHA_TRIBUTO);
		return ficha.getSubimage(mx, my, ficha.getWidth() - 2 * mx, ficha.getHeight() - 2 * my);
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
		desenharTributos(g2);
		desenharCidades(g2);
		desenharYurts(g2);
		desenharKhan(g2);
		desenharPeoes(g2);
	}

	// ---- províncias e Khan ----

	/** As peças de cada província ficam lado a lado sobre o ícone impresso. */
	private void desenharTributos(Graphics2D g2) {
		int tamanho = (int) Math.round(TAMANHO_TRIBUTO * escala);
		for (String provincia : modelo.getProvincias()) {
			int pecas = modelo.getPecasProvincia(provincia);
			Image imagem = imagensTributos.get(modelo.getTributoProvincia(provincia));
			Point p = mapa.getPontoProvincia(provincia);
			for (int i = 0; i < pecas; i++) {
				double deslocamento = (i - (pecas - 1) / 2.0) * ESPACO_ENTRE_TRIBUTOS;
				int cx = (int) Math.round((p.x + deslocamento) * escala);
				int cy = (int) Math.round(p.y * escala);
				Imagens.desenharFicha(g2, imagem, cx - tamanho / 2, cy - tamanho / 2, tamanho);
				g2.setColor(Color.BLACK);
				g2.setStroke(new BasicStroke(1));
				g2.drawOval(cx - tamanho / 2, cy - tamanho / 2, tamanho, tamanho);
			}
		}
	}

	// ---- cidades ----

	/** Cidade disponível: tesouros em grade 2x2. Conquistada: yurt do conquistador (se ele tinha). */
	private void desenharCidades(Graphics2D g2) {
		int tamanho = (int) Math.round(CoordenadasMapa.TAMANHO_TESOURO * escala);
		for (String cidade : modelo.getCidades()) {
			String estado = modelo.getEstadoCidade(cidade);
			if (estado.equals("DISPONIVEL")) {
				List<String> tesouros = modelo.getTesourosCidade(cidade);
				for (int i = 0; i < tesouros.size(); i++) {
					Point p = mapa.getPontoTesouro(cidade, i);
					int x = (int) Math.round(p.x * escala) - tamanho / 2;
					int y = (int) Math.round(p.y * escala) - tamanho / 2;
					Imagens.desenharFicha(g2, imagemTesouro(tesouros.get(i)), x, y, tamanho);
					g2.setColor(Color.BLACK);
					g2.setStroke(new BasicStroke(1));
					g2.drawOval(x, y, tamanho, tamanho);
				}
			} else if (estado.equals("CONQUISTADA") && modelo.getYurtCidade(cidade) != null) {
				desenharYurt(g2, modelo.getYurtCidade(cidade), mapa.getPontoCidade(cidade), CoordenadasMapa.TAMANHO_TESOURO);
			}
		}
	}

	private Image imagemTesouro(String tesouro) {
		if (!imagensTesouros.containsKey(tesouro))
			imagensTesouros.put(tesouro, Imagens.carregar("tesouro-" + tesouro.toLowerCase() + ".jpg"));
		return imagensTesouros.get(tesouro);
	}

	/** Índice do tesouro da cidade sob o ponto (coordenadas da imagem original), ou -1. */
	private int tesouroEm(String cidade, int x, int y) {
		if (!modelo.getEstadoCidade(cidade).equals("DISPONIVEL"))
			return -1;
		int quantidade = modelo.getTesourosCidade(cidade).size();
		for (int i = 0; i < quantidade; i++)
			if (mapa.getPontoTesouro(cidade, i).distance(x, y) <= CoordenadasMapa.TAMANHO_TESOURO / 2)
				return i;
		return -1;
	}

	// ---- yurts ----

	/** Yurts acima da parada, lado a lado, com anel na cor do dono. */
	private void desenharYurts(Graphics2D g2) {
		for (String parada : coordenadas.getNomes()) {
			List<String> donos = modelo.getYurts(parada);
			Point p = coordenadas.getPonto(parada);
			for (int i = 0; i < donos.size(); i++) {
				double deslocamento = (i - (donos.size() - 1) / 2.0) * ESPACO_ENTRE_YURTS;
				Point centro = new Point((int) Math.round(p.x + deslocamento), p.y - ALTURA_YURT);
				desenharYurt(g2, donos.get(i), centro, TAMANHO_YURT);
			}
		}
	}

	/** Ficha de yurt com anel na cor do dono; centro e tamanho em pixels da imagem original. */
	private void desenharYurt(Graphics2D g2, String dono, Point centro, int tamanhoOriginal) {
		int tamanho = (int) Math.round(tamanhoOriginal * escala);
		int x = (int) Math.round(centro.x * escala) - tamanho / 2;
		int y = (int) Math.round(centro.y * escala) - tamanho / 2;
		Imagens.desenharFicha(g2, imagensTributos.get("YURT"), x, y, tamanho);
		g2.setColor(PainelControle.corDoJogador(dono));
		g2.setStroke(new BasicStroke(3));
		g2.drawOval(x, y, tamanho, tamanho);
	}

	/** O Khan fica sobre o rosto onde está; antes do primeiro movimento, fora do tabuleiro. */
	private void desenharKhan(Graphics2D g2) {
		String posicao = modelo.getPosicaoKhan();
		if (posicao == null)
			return;
		Point p = mapa.getPontoKhan(posicao);
		int tamanho = (int) Math.round(TAMANHO_KHAN * escala);
		int x = (int) Math.round(p.x * escala) - tamanho / 2;
		int y = (int) Math.round(p.y * escala) - tamanho / 2;
		Imagens.desenharFicha(g2, khan, x, y, tamanho);
		g2.setColor(COR_ANEL_KHAN);
		g2.setStroke(new BasicStroke(3));
		g2.drawOval(x, y, tamanho, tamanho);
	}

	// ---- peões ----

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

	/**
	 * Clique esquerdo: parada (move o peão), província (pega tributo), tesouro (ataca a cidade)
	 * ou rosto (move o Khan).
	 * Clique direito numa parada: constrói yurt. Se o Model recusar, nada acontece.
	 */
	private class CliqueTabuleiro extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			int x = (int) Math.round(e.getX() / escala);
			int y = (int) Math.round(e.getY() / escala);
			String parada = coordenadas.paradaEm(x, y);
			if (SwingUtilities.isRightMouseButton(e)) {
				if (parada != null)
					controller.construirYurt(parada);
				return;
			}
			if (!SwingUtilities.isLeftMouseButton(e))
				return;
			if (parada != null) {
				controller.mover(parada);
				return;
			}
			String provincia = mapa.provinciaEm(x, y);
			if (provincia != null) {
				controller.pegarTributo(provincia);
				return;
			}
			for (String cidade : modelo.getCidades()) {
				int indice = tesouroEm(cidade, x, y);
				if (indice >= 0) {
					controller.atacarCidade(cidade, modelo.getTesourosCidade(cidade).get(indice));
					return;
				}
			}
			String destino = mapa.destinoKhanEm(x, y);
			if (destino != null)
				controller.moverKhan(destino);
		}
	}
}
