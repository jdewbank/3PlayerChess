package chess;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JOptionPane;

import chess.Connection.Receiver;
import chess.Piece.Team;
import chess.Piece.Type;

public class Controller {
	ArrayList<Client> clients;
	BoardState boardState;
	int boardSize;

	Team next = Team.ONE;

	ServerSocketChannel socket;

	public Controller(int size, boolean local) throws IOException {
		clients = new ArrayList<>();
		boardState = new BoardState(size);

		boardSize = size;

		if (!local) {
			socket = ServerSocketChannel.open();
			socket.bind(new InetSocketAddress(12345));
			socket.configureBlocking(false);
		}
	}

	public void addLocal(LocalConnection lcl) {
		Client c = new Client();
		LocalConnection conn = new LocalConnection(c, lcl);
		c.connection = conn;

		clients.add(c);
	}

	public void update() {
		SocketChannel chn = null;
		try {
			if (socket != null)
				chn = socket.accept();
			if (chn != null) {
				Client c = new Client();
				RemoteConnection remote = new RemoteConnection(c, chn);
				c.connection = remote;
				clients.add(c);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (Client c : clients) {
			try {
				c.connection.update();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void sendAll(String s) {
		for (Client c : clients) {
			c.connection.send(s);
		}
	}

	public class Client implements Receiver {
		Connection connection;
		ArrayList<Team> teams;

		@Override
		public void receive(String s) {
			System.out.println("Controller: " + s);
			String[] command = s.split(" ");
			if (command[0].equals("move")) {
				boardState.move(command[1], command[2], command[3]);
				sendAll(s);
				boardState.incTurn();

				for (Client c : clients) {
					c.connection.send("turn " + boardState.turn + " "
							+ c.teams.contains(boardState.turn));
				}
				connection.send("mate?");
			} else if (command[0].equals("hello")) {
				if (teams == null) {
					teams = new ArrayList<>();
					for (int i = 0; i < Integer.parseInt(command[1]); i++) {
						if (next != null) {
							teams.add(next);
							try {
								next = Team.values()[next.ordinal() + 1];
							} catch (Exception e) {
								next = null;
							}
						}
					}

					String teamString = "";
					for (Team t : teams) {
						teamString += t + ",";
					}
					connection.send("size " + boardSize + " " + teamString
							+ " " + command[2]);
					connection.send("turn " + boardState.turn + " "
							+ teams.contains(boardState.turn));
				}
			} else if (command[0].equals("undo")) {
				boardState = boardState.undo;
				sendAll(s);
			} else if (command[0].equals("mate")) {
				if (command[1].equals("true")) {
					sendAll("checkmate");
					boardState.deadPeople.add(boardState.turn);
					if (boardState.deadPeople.size() == Team.values().length - 1) {
						for (Team t : Team.values())
							if (!boardState.deadPeople.contains(t))
								sendAll("gameover " + t);
					} else
						boardState.incTurn();
				}
			}

		}

	}
}