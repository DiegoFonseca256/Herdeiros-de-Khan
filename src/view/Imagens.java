package view;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/** Leitura e desenho das imagens da pasta imagens/ do projeto. */
final class Imagens {
	private static final String PASTA = "imagens";

	private Imagens() {
	}

	static BufferedImage carregar(String arquivo) {
		try {
			return ImageIO.read(new File(PASTA, arquivo));
		} catch (IOException e) {
			throw new IllegalStateException("Não foi possível ler a imagem " + arquivo, e);
		}
	}

	/** Reduz a imagem uma única vez, para não redimensioná-la inteira a cada repaint. */
	static BufferedImage reduzir(Image imagem, int largura, int altura) {
		BufferedImage reduzida = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = reduzida.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2.drawImage(imagem, 0, 0, largura, altura, null);
		g2.dispose();
		return reduzida;
	}

	/** Desenha a imagem recortada em círculo (os JPG das fichas não têm transparência). */
	static void desenharFicha(Graphics2D g2, Image imagem, int x, int y, int tamanho) {
		Shape clipAnterior = g2.getClip();
		g2.clip(new Ellipse2D.Double(x, y, tamanho, tamanho));
		g2.drawImage(imagem, x, y, tamanho, tamanho, null);
		g2.setClip(clipAnterior);
	}
}
