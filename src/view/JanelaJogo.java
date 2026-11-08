package view;

import java.awt.BorderLayout;
import java.util.LinkedHashMap;

import javax.swing.JFrame;

import controller.Controller;
import controller.ObservadoIF;
import controller.ObservadorIF;
import model.ModelFacade;

/** Janela principal do jogo: tabuleiro à esquerda e painel de controle à direita. */
public class JanelaJogo extends JFrame implements ObservadorIF {
	private static final int LARGURA_MAXIMA = 1400;
	private static final int ALTURA_MAXIMA = 1050;

	private final PainelTabuleiro tabuleiro;
	private final PainelControle controle;
	private final LinkedHashMap<String, JanelaCartela> cartelas = new LinkedHashMap<>();

	/** Abre o tabuleiro da partida já iniciada pelo Controller (na JanelaInicial). */
	public JanelaJogo() {
		super("Herdeiros de Khan");
		tabuleiro = new PainelTabuleiro();
		controle = new PainelControle();
		add(tabuleiro, BorderLayout.CENTER);
		add(controle, BorderLayout.EAST);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		pack();
		setSize(Math.min(getWidth(), LARGURA_MAXIMA), Math.min(getHeight(), ALTURA_MAXIMA));
		setLocationRelativeTo(null);
		Controller.getInstancia().registra(this);
	}

	/** Diz se o evento mudou o estado da partida (e não só abriu ou fechou uma janela). */
	static boolean mudouPartida(int evento) {
		return evento != Controller.CARTELA_ALTERNADA && evento != Controller.CARTAS_ALTERNADA;
	}

	/** Mostra ou esconde a cartela de um jogador quando o Controller avisa. */
	@Override
	public void notify(ObservadoIF o) {
		if (o.get(Controller.EVENTO) != Controller.CARTELA_ALTERNADA)
			return;
		String cor = ModelFacade.getInstancia().getCores().get(o.get(Controller.JOGADOR));
		mostrarCartela(cor, o.get(Controller.VISIVEL) == 1);
	}

	/** A cartela é criada no 1º uso. */
	private void mostrarCartela(String cor, boolean visivel) {
		JanelaCartela cartela = cartelas.get(cor);
		if (cartela == null) {
			cartela = new JanelaCartela(cor, this);
			cartelas.put(cor, cartela);
		}
		cartela.setVisible(visivel);
	}

	public static void main(String[] args) {
		new JanelaInicial().setVisible(true);
	}
}
