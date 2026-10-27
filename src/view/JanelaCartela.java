package view;

import javax.swing.JFrame;

/** Janela de status de um jogador. Fechar só a esconde, para o botão do painel poder reabri-la. */
class JanelaCartela extends JFrame {
	JanelaCartela(String cor, JFrame principal) {
		super("Cartela - " + cor);
		add(new PainelCartela(cor));
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setResizable(false);
		pack();
		setLocationRelativeTo(principal);
	}
}
