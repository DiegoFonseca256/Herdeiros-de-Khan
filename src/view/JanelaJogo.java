package view;

import java.awt.BorderLayout;
import java.util.LinkedHashMap;

import javax.swing.JFrame;

import model.ModelFacade;

/** Janela principal do jogo: tabuleiro à esquerda e painel de controle à direita. */
public class JanelaJogo extends JFrame {
	private static final int LARGURA_MAXIMA = 1400;
	private static final int ALTURA_MAXIMA = 1050;
	private static final int NUMERO_JOGADORES = 3; // fixo até existir a janela de escolha dos jogadores

	private final PainelTabuleiro tabuleiro;
	private final PainelControle controle;
	private final LinkedHashMap<String, JanelaCartela> cartelas = new LinkedHashMap<>();

	public JanelaJogo() {
		super("Herdeiros de Khan");
		ModelFacade.getInstancia().novaPartida(NUMERO_JOGADORES);
		tabuleiro = new PainelTabuleiro(this);
		controle = new PainelControle(this);
		add(tabuleiro, BorderLayout.CENTER);
		add(controle, BorderLayout.EAST);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		pack();
		setSize(Math.min(getWidth(), LARGURA_MAXIMA), Math.min(getHeight(), ALTURA_MAXIMA));
		setLocationRelativeTo(null);
	}

	/** Chamado depois de cada ação aceita pelo Model. */
	void atualizar() {
		controle.atualizar();
		tabuleiro.repaint();
		for (JanelaCartela cartela : cartelas.values())
			if (cartela.isVisible())
				cartela.repaint();
	}

	/** Abre a cartela do jogador se estiver fechada e fecha se estiver aberta. É criada no 1º uso. */
	void alternarCartela(String cor) {
		JanelaCartela cartela = cartelas.get(cor);
		if (cartela == null) {
			cartela = new JanelaCartela(cor, this);
			cartelas.put(cor, cartela);
		}
		cartela.setVisible(!cartela.isVisible());
	}

	public static void main(String[] args) {
		new JanelaJogo().setVisible(true);
	}
}
