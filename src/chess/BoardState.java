package chess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.JOptionPane;

import chess.ChessBoard.Move;
import chess.Controller.Client;
import chess.Piece.Team;
import chess.Piece.Type;

public class BoardState {
	Location[][] places;
	Team turn;
	ArrayList<Team> deadPeople;
	Location lastMoveTo;
	Location lastMoveFrom;
	Location doubleLastMoveTo;
	Location doubleLastMoveFrom;
	BoardState undo;

	public BoardState(Location[][] places, Team turn, BoardState undo,
			ArrayList<Team> dead) {
		this.places = copyOf(places);
		this.turn = turn;
		this.undo = undo;
		deadPeople = new ArrayList<>();
		Collections.copy(this.deadPeople, dead);
	}

	public BoardState(int boardSize) {
		turn = Team.values()[0];
		initializePlaces(boardSize);
		deadPeople = new ArrayList<>();
	}

	public Location[][] copyOf(Location[][] original) {
		Location[][] output = new Location[original.length][];
		for (int i = 0; i < original.length; i++) {
			output[i] = new Location[original[i].length];
			for (int j = 0; j < original[i].length; j++)
				output[i][j] = new Location(original[i][j]);
		}
		return output;
	}

	/**
	 * initialize the board with locations
	 */
	private void initializePlaces(int boardSize) {
		// NOTE! row-major
		places = new Location[boardSize * 2 - 1][];
		for (int y = 0; y < boardSize * 2 - 1; y++) {
			places[y] = new Location[2 * boardSize - 1
					- Math.abs(y - (boardSize - 1))];
			for (int x = 0; x < places[y].length; x++) {
				places[y][x] = new Location(y, x, boardSize);
			}
		}
	}

	public void move(String from, String to, String promotion) {

		int fromx = Integer.parseInt(from.substring(0, from.indexOf(',')));
		int fromy = Integer.parseInt(from.substring(from.indexOf(',') + 1));

		int tox = Integer.parseInt(to.substring(0, to.indexOf(',')));
		int toy = Integer.parseInt(to.substring(to.indexOf(',') + 1));

		undo = new BoardState(places, turn, undo, deadPeople);

		places[toy][tox].piece = places[fromy][fromx].piece;
		places[fromy][fromx].piece = null;

		promotion = promotion.toLowerCase().trim();
		switch (promotion) {
		case "queen":
			places[toy][tox].piece = new Queen(places[toy][tox].piece.team);
			break;
		case "rook":
			places[toy][tox].piece = new Rook(places[toy][tox].piece.team);
			break;
		case "bishop":
			places[toy][tox].piece = new Bishop(places[toy][tox].piece.team);
			break;
		case "knight":
			places[toy][tox].piece = new Knight(places[toy][tox].piece.team);
			break;
		}

		doubleLastMoveFrom = lastMoveFrom;
		doubleLastMoveTo = lastMoveTo;

		lastMoveTo = places[toy][tox];
		lastMoveFrom = places[fromy][fromx];
	}

	/**
	 * Change boardState.turn to the next player
	 */
	public void incTurn() {
		int i = 0;
		while (Team.values()[i++] != turn)
			;
		do {
			turn = Team.values()[(i++) % Team.values().length];
		} while (deadPeople.contains(turn));
	}

	public void undo() {
		if (undo == null) {
			System.out.println("can't undo.");
			return;
		}
		places = undo.places;
		turn = undo.turn;
		undo = undo.undo;
	}

	public void moveHelp(Move move) {
		Location from = move.from;
		Location to = move.to;
		String promotion = " none";

		if (from.piece instanceof Pawn) {
			if (((Pawn) from.piece).firstTurn)
				((Pawn) from.piece).firstTurn = false;
			if (isOnLastRank(to.x, to.y)) {
				// TODO - change this;
				Type type = Type.QUEEN;

				promotion = " " + type.toString();
				switch (type) {
				case ROOK:
					from.piece = new Rook(from.piece.team);
					break;
				case KNIGHT:
					from.piece = new Knight(from.piece.team);
					break;
				case QUEEN:
					from.piece = new Queen(from.piece.team);
					break;
				case BISHOP:
					from.piece = new Bishop(from.piece.team);
					break;
				}
			}
		}
		int tox = to.x;
		int toy = to.y;
		int fromx = from.x;
		int fromy = from.y;

		undo = new BoardState(places, turn, undo, deadPeople);

		places[toy][tox].piece = places[fromy][fromx].piece;
		places[fromy][fromx].piece = null;

		promotion = promotion.toLowerCase().trim();
		switch (promotion) {
		case "queen":
			places[toy][tox].piece = new Queen(places[toy][tox].piece.team);
			break;
		case "rook":
			places[toy][tox].piece = new Rook(places[toy][tox].piece.team);
			break;
		case "bishop":
			places[toy][tox].piece = new Bishop(places[toy][tox].piece.team);
			break;
		case "knight":
			places[toy][tox].piece = new Knight(places[toy][tox].piece.team);
			break;
		}

		incTurn();
	}

	public boolean isOnLastRank(int x, int y) {
		int size = ChessBoard.boardSize;
		int boardHeight = ChessBoard.boardHeight;
		y = boardHeight - y - 1;
		if (y < 2)
			// team 1
			return y == 0;
		else if (x <= 1 && y >= size - 2 && !(x == 1 && y == size - 2))
			// team 2
			return !(x == 1 || (x == 0 && y == size - 2));
		else if (x >= boardHeight - Math.abs(y - (size - 1)) - 2
				&& y >= size - 2
				&& !(x == boardHeight - Math.abs(y - (size - 1)) - 2 && y == size - 2))
			// team 3
			return !(x == boardHeight - Math.abs(y - (size - 1)) - 2 || (x == boardHeight
					- Math.abs(y - (size - 1)) - 1 && y == size - 2));
		else
			return false;

	}

}
