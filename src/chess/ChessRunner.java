package chess;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import chess.Piece.Team;

public class ChessRunner implements ActionListener {

	ChessBoard b;

	public static void main(String[] args) {
		new ChessRunner();
	}

	public ChessRunner() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		b = new ChessBoard();
		boolean local = false;
		int numPlayers = 0;

		String[] options = { "Local", "Host", "Join" };
		switch (JOptionPane.showOptionDialog(null,
				"Would you like to host or join?", "Host?",
				JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
				options, options[0])) {
		case 0:
			numPlayers = Team.values().length;
		case 1:
			int size = Integer.parseInt(JOptionPane
					.showInputDialog("What size board?"));
			if (numPlayers == 0)
				numPlayers = Integer.parseInt(JOptionPane
						.showInputDialog("How many players on this computer?"));
			int numAI = Integer.parseInt(JOptionPane
					.showInputDialog("How many AI players?"));
			Controller controller;
			try {
				controller = new Controller(size, local);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				break;
			}

			LocalConnection conn = new LocalConnection(b, numPlayers);
			controller.addLocal(conn);

			b.connect(conn, numAI);
			b.controller = controller;
			break;
		case 2:
			String s = JOptionPane
					.showInputDialog("What address would you like to connect to?");

			numPlayers = Integer.parseInt(JOptionPane
					.showInputDialog("How many players on this computer?"));

			RemoteConnection remote;
			try {
				System.out.println("Making a remote connection with "
						+ numPlayers + " players.");
				remote = new RemoteConnection(b, s, 12345, numPlayers);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
			b.connect(remote, 0);
			break;
		}

		frame.add(b);
		frame.addKeyListener(b);
		frame.setSize(b.getPreferredSize());
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

		frame.setVisible(true);

		b.repaint();
		b.update();

		new Timer(100, this).start();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (b != null) {
			b.repaint();
		}
	}
}
