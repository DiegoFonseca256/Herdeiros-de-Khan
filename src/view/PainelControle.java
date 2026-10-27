package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import model.ModelFacade;

/** Painel lateral: jogador da vez, movimentos restantes, botões das colunas e encerrar turno. */
class PainelControle extends JPanel {
	private static final int LARGURA = 260;
	private static final HashMap<String, Color> CORES_JOGADORES = new HashMap<>();
	private static final HashMap<String, Color> CORES_COLUNAS = new HashMap<>();

	static {
		CORES_JOGADORES.put("VERMELHO", new Color(200, 30, 30));
		CORES_JOGADORES.put("AZUL", new Color(40, 70, 190));
		CORES_JOGADORES.put("VERDE", new Color(40, 140, 60));
		CORES_JOGADORES.put("AMARELO", new Color(230, 180, 20));
		CORES_JOGADORES.put("PRETO", new Color(60, 60, 60));
		CORES_COLUNAS.put("VERMELHA", new Color(220, 90, 80));
		CORES_COLUNAS.put("VERDE", new Color(110, 180, 100));
		CORES_COLUNAS.put("AMARELA", new Color(240, 210, 90));
		CORES_COLUNAS.put("BRANCA", Color.WHITE);
	}

	private final JanelaJogo janela;
	private final ModelFacade modelo = ModelFacade.getInstancia();
	private final JLabel vez = new JLabel("", SwingConstants.CENTER);
	private final JLabel movimentos = new JLabel("", SwingConstants.CENTER);
	private final LinkedHashMap<String, JButton> botoesColunas = new LinkedHashMap<>();
	private final JButton encerrar = new JButton("Encerrar turno");

	PainelControle(JanelaJogo janela) {
		this.janela = janela;
		setPreferredSize(new Dimension(LARGURA, 0));
		setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
		setLayout(new BorderLayout());

		JPanel conteudo = new JPanel(new GridLayout(0, 1, 0, 8));
		vez.setOpaque(true);
		vez.setFont(vez.getFont().deriveFont(Font.BOLD, 18f));
		conteudo.add(vez);
		conteudo.add(movimentos);
		conteudo.add(new JLabel("Ativar coluna:"));
		for (String coluna : modelo.getColunas()) {
			JButton botao = new JButton(coluna);
			botao.setBackground(CORES_COLUNAS.get(coluna));
			botao.addActionListener(new AtivarColuna(coluna));
			botoesColunas.put(coluna, botao);
			conteudo.add(botao);
		}
		encerrar.addActionListener(new EncerrarTurno());
		conteudo.add(encerrar);
		conteudo.add(new JLabel("Cartelas:"));
		for (String cor : modelo.getCores()) {
			JButton botao = new JButton(cor);
			botao.setBackground(CORES_JOGADORES.get(cor));
			botao.setForeground(corDoTexto(cor));
			botao.addActionListener(new AlternarCartela(cor));
			conteudo.add(botao);
		}
		add(conteudo, BorderLayout.NORTH);
		atualizar();
	}

	/** Mostra a cor do jogador da vez e habilita só as ações que o Model permite agora. */
	void atualizar() {
		String cor = modelo.getCorDaVez();
		vez.setText("Vez: " + cor);
		vez.setBackground(CORES_JOGADORES.get(cor));
		vez.setForeground(corDoTexto(cor));
		movimentos.setText("Movimentos: " + modelo.getMovimentos());
		for (String coluna : botoesColunas.keySet())
			botoesColunas.get(coluna).setEnabled(modelo.podeAtivarColuna(coluna));
		encerrar.setEnabled(modelo.podeEncerrarTurno());
	}

	private static Color corDoTexto(String cor) {
		return cor.equals("AMARELO") ? Color.BLACK : Color.WHITE;
	}

	private class AtivarColuna implements ActionListener {
		private final String coluna;

		AtivarColuna(String coluna) {
			this.coluna = coluna;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (modelo.ativarColuna(coluna))
				janela.atualizar();
		}
	}

	private class EncerrarTurno implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (modelo.encerrarTurno())
				janela.atualizar();
		}
	}

	private class AlternarCartela implements ActionListener {
		private final String cor;

		AlternarCartela(String cor) {
			this.cor = cor;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			janela.alternarCartela(cor);
		}
	}
}
