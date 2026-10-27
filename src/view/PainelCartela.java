package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import model.ModelFacade;

/** Desenha a cartela do jogador (Tolui) com suas melhorias e, abaixo, tributos, tesouros, votos e yurts. */
class PainelCartela extends JPanel {
	private static final int LARGURA = 900;
	private static final int ALTURA_STATUS = 120;
	private static final int TAMANHO_FICHA = 40;
	private static final int ESPACO_ENTRE_FICHAS = 150;

	// posições na imagem original da cartela (5926 x 3235 px); colunas na ordem de ModelFacade.getColunas()
	private static final int[] CENTRO_X_COLUNA = { 2005, 3055, 4102, 5258 };
	private static final int[] CENTRO_Y_LINHA = { 2142, 1608, 1075, 540 }; // da 1ª melhoria (embaixo) à 4ª
	private static final int LARGURA_ESPACO = 880;
	private static final int ALTURA_ESPACO = 465;
	private static final int TOPO_BASE = 2450; // ações impressas na base de cada coluna
	private static final int ALTURA_BASE = 480;

	// arquivo de cada melhoria: prefixo pelo tipo (ou tesouro) e sufixo pela cor
	private static final HashMap<String, String> PREFIXOS = new HashMap<>();
	private static final HashMap<String, String> SUFIXOS = new HashMap<>();

	static {
		PREFIXOS.put("MOEDA", "MOE");
		PREFIXOS.put("ESPADA", "ESP");
		PREFIXOS.put("YURT", "CAS");
		PREFIXOS.put("PUNHO", "MAO");
		PREFIXOS.put("CAVALOS", "CAV");
		PREFIXOS.put("GADO", "BOI");
		PREFIXOS.put("ESPECIARIA", "CUI");
		PREFIXOS.put("FERRO", "MOB");
		PREFIXOS.put("PELE", "MOP");
		PREFIXOS.put("SEDA", "PER");
		SUFIXOS.put("VERMELHA", "R");
		SUFIXOS.put("VERDE", "V");
		SUFIXOS.put("AMARELA", "A");
	}

	// imagens compartilhadas pelas cartelas de todos os jogadores (carregadas no 1º uso)
	private static BufferedImage cartela;
	private static double escala;
	private static final HashMap<String, Image> imagensMelhorias = new HashMap<>();
	private static final HashMap<String, Image> imagensFichas = new HashMap<>();

	private final String cor;
	private final ModelFacade modelo = ModelFacade.getInstancia();

	PainelCartela(String cor) {
		this.cor = cor;
		if (cartela == null) {
			BufferedImage original = Imagens.carregar("cartela-tolui.jpg");
			escala = (double) LARGURA / original.getWidth();
			cartela = Imagens.reduzir(original, LARGURA, (int) Math.round(original.getHeight() * escala));
		}
		setPreferredSize(new Dimension(LARGURA, cartela.getHeight() + ALTURA_STATUS));
	}

	/** Imagem da melhoria pelo identificador do Model (ex.: "CAVALOS-AMARELA" -> melhorias/CAV-A.jpg). */
	private static Image imagemDaMelhoria(String id) {
		Image imagem = imagensMelhorias.get(id);
		if (imagem == null) {
			int separador = id.lastIndexOf('-');
			String arquivo = PREFIXOS.get(id.substring(0, separador)) + "-" + SUFIXOS.get(id.substring(separador + 1));
			imagem = Imagens.carregar("melhorias/" + arquivo + ".jpg");
			imagensMelhorias.put(id, imagem);
		}
		return imagem;
	}

	/** Ficha de tributo ou tesouro (ex.: "tributo-moeda.jpg"). */
	private static Image imagemDaFicha(String arquivo) {
		Image imagem = imagensFichas.get(arquivo);
		if (imagem == null) {
			imagem = Imagens.carregar(arquivo);
			imagensFichas.put(arquivo, imagem);
		}
		return imagem;
	}

	private static int escalar(int valor) {
		return (int) Math.round(valor * escala);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.drawImage(cartela, 0, 0, null);

		List<String> colunas = modelo.getColunas();
		for (int i = 0; i < colunas.size(); i++) {
			List<String> melhorias = modelo.getMelhorias(cor, colunas.get(i));
			for (int linha = 0; linha < melhorias.size(); linha++) {
				int x = escalar(CENTRO_X_COLUNA[i] - LARGURA_ESPACO / 2);
				int y = escalar(CENTRO_Y_LINHA[linha] - ALTURA_ESPACO / 2);
				g2.drawImage(imagemDaMelhoria(melhorias.get(linha)), x, y,
						escalar(LARGURA_ESPACO), escalar(ALTURA_ESPACO), null);
			}
			if (modelo.isColunaUsada(cor, colunas.get(i)))
				escurecerBase(g2, CENTRO_X_COLUNA[i]);
		}
		desenharStatus(g2, cartela.getHeight());
	}

	/** Coluna já usada: escurece as ações da base, como a peça de ativação no jogo físico. */
	private void escurecerBase(Graphics2D g2, int centroX) {
		int x = escalar(centroX - LARGURA_ESPACO / 2);
		int y = escalar(TOPO_BASE);
		int largura = escalar(LARGURA_ESPACO);
		int altura = escalar(ALTURA_BASE);
		g2.setColor(new Color(0, 0, 0, 150));
		g2.fillRect(x, y, largura, altura);
		g2.setColor(Color.WHITE);
		g2.setFont(getFont().deriveFont(Font.BOLD, 16f));
		FontMetrics fm = g2.getFontMetrics();
		g2.drawString("USADA", x + (largura - fm.stringWidth("USADA")) / 2, y + (altura + fm.getAscent()) / 2);
	}

	private void desenharStatus(Graphics2D g2, int topo) {
		g2.setFont(getFont().deriveFont(Font.BOLD, 16f));
		int linha1 = topo + 15;
		int linha2 = topo + 65;
		desenharFichas(g2, "Tributos:", "tributo-", modelo.getTributos(cor), linha1);
		desenharFichas(g2, "Tesouros:", "tesouro-", modelo.getTesouros(cor), linha2);

		int meio = linha1 + TAMANHO_FICHA / 2 + 6;
		g2.setColor(Color.BLACK);
		g2.drawString("Votos: " + modelo.getVotos(cor), 590, meio);
		g2.drawString("Yurts restantes: " + modelo.getYurtsDisponiveis(cor), 720, meio);
	}

	/** Uma linha de fichas, com a quantidade ao lado de cada uma. */
	private void desenharFichas(Graphics2D g2, String titulo, String prefixoArquivo, Map<String, Integer> quantidades, int y) {
		int meio = y + TAMANHO_FICHA / 2 + 6;
		g2.setColor(Color.BLACK);
		g2.drawString(titulo, 16, meio);
		int x = 110;
		for (String tipo : quantidades.keySet()) {
			Imagens.desenharFicha(g2, imagemDaFicha(prefixoArquivo + tipo.toLowerCase() + ".jpg"), x, y, TAMANHO_FICHA);
			g2.setColor(Color.BLACK);
			g2.drawString("x " + quantidades.get(tipo), x + TAMANHO_FICHA + 8, meio);
			x += ESPACO_ENTRE_FICHAS;
		}
	}
}
