package view;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import controller.Controller;

/** Janela de status de um jogador. Quem abre e fecha é o Controller, para saber quais estão abertas. */
class JanelaCartela extends JFrame {
	private final String cor;

	JanelaCartela(String cor, JFrame principal) {
		super("Cartela - " + cor);
		this.cor = cor;
		add(new PainelCartela(cor));
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new FecharCartela());
		setResizable(false);
		pack();
		setLocationRelativeTo(principal);
	}

	/** Fechar pelo X avisa o Controller, que manda a JanelaJogo esconder a cartela. */
	private class FecharCartela extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e) {
			Controller.getInstancia().alternarCartela(cor);
		}
	}
}
