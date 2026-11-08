package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import controller.Controller;

/** Primeira janela: escolha do número de jogadores (2 a 5) antes de abrir o tabuleiro. */
class JanelaInicial extends JFrame {
	private static final String[] OPCOES = { "2 jogadores", "3 jogadores", "4 jogadores", "5 jogadores" };
	private static final int MINIMO_JOGADORES = 2;

	private final JComboBox<String> numeroJogadores = new JComboBox<>(OPCOES);

	JanelaInicial() {
		super("Herdeiros de Khan");
		JLabel titulo = new JLabel("Herdeiros de Khan", SwingConstants.CENTER);
		titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 24f));

		numeroJogadores.setSelectedIndex(1); // 3 jogadores
		JPanel escolha = new JPanel(new FlowLayout());
		escolha.add(new JLabel("Número de jogadores:"));
		escolha.add(numeroJogadores);

		JButton iniciar = new JButton("Iniciar");
		iniciar.addActionListener(new Iniciar());
		JButton sair = new JButton("Sair");
		sair.addActionListener(new Sair());
		JPanel botoes = new JPanel(new FlowLayout());
		botoes.add(iniciar);
		botoes.add(sair);

		JPanel conteudo = new JPanel(new BorderLayout(0, 12));
		conteudo.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
		conteudo.add(titulo, BorderLayout.NORTH);
		conteudo.add(escolha, BorderLayout.CENTER);
		conteudo.add(botoes, BorderLayout.SOUTH);
		add(conteudo);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		pack();
		setLocationRelativeTo(null);
		getRootPane().setDefaultButton(iniciar); // Enter inicia
	}

	/** Inicia a partida pelo Controller, abre o tabuleiro e fecha esta janela. */
	private class Iniciar implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			Controller.getInstancia().novaPartida(numeroJogadores.getSelectedIndex() + MINIMO_JOGADORES);
			new JanelaJogo().setVisible(true);
			dispose();
		}
	}

	private class Sair implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}
}
