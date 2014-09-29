package chess;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.*;

import chess.Connection.Receiver;
import chess.Controller.Client;
import chess.Piece.Team;
import chess.Piece.Type;

public class ChessBoard extends JPanel implements MouseListener, KeyListener,
		Receiver {
	private static final long serialVersionUID = 1L;

	public enum State {
		selected, highlighted, plain, otherSelected, otherHighlighted, lastMove
	}

	public static final int DN = 0;
	public static final int DS = 6;
	public static final int DNW = 10;
	public static final int DNE = 2;
	public static final int DSW = 8;
	public static final int DSE = 4;
	public static final int AE = 3;
	public static final int AW = 9;
	public static final int ANW = 11;
	public static final int ASW = 7;
	public static final int ANE = 1;
	public static final int ASE = 5;

	public static final int buffer = 10;

	public static ChessBoard chessBoard;

	public static int boardSize;
	public static int boardHeight;
	public static double hexSize;

	public BoardState boardState;

	private ArrayList<Team> deadPeople;

	private Connection connection;

	public static int rotateOffset = 0;

	private Location selection;
	private ArrayList<Location> moveLocs;
	private ArrayList<Location> otherSelection;
	private ArrayList<Location> otherMoveLocs;
	public static boolean myTurn;
	public Controller controller;
	private String message = "";
	public static ArrayList<Team> myTeams;
	public ArrayList<Team> ai;

	/**
	 * Make a new Chess board with a particular size
	 * 
	 * @param size
	 */
	public ChessBoard() {
		// find hexagon height
		chessBoard = this;

		deadPeople = new ArrayList<Team>();
		setBackground(Color.white);
		setLayout(null);
		addMouseListener(this);
	}

	public void connect(Connection c, int numAI) {
		connection = c;
		connection.send("hello " + c.numPlayers() + " " + numAI);
	}

	private void initBoard(int size) {
		boardSize = size;
		boardHeight = 2 * boardSize - 1;
		double screenHeight = Toolkit.getDefaultToolkit().getScreenSize()
				.getHeight();
		double totHexSize = (screenHeight - 30 - (6 * buffer));
		totHexSize -= (totHexSize / boardHeight) / 4;
		hexSize = totHexSize / boardHeight;
		hexSize = hexSize * (4.0 / 3.0);

		setPreferredSize(new Dimension((int) screenHeight + 50,
				(int) screenHeight));
		boardState = new BoardState(boardSize);
	}

	/**
	 * draw all the locations (they will draw their hexagons
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (boardState != null) {
			for (Location[] locs : boardState.places)
				for (Location l : locs)
					l.draw(g);
		}
	}

	/**
	 * Get the next location from a location and a direction. Return null if out
	 * of bounds
	 * 
	 * @param loc
	 *            location
	 * @param direction
	 *            direction
	 * @return location in direction direction from loc
	 */
	public Location getNextLocation(Location loc, int direction) {
		int x = loc.x;
		int y = loc.y;
		int size = boardSize;
		try {
			switch (direction) {
			case DN:
				if (y < size)
					return boardState.places[y - 2][x - 1];
				else if (y == size)
					return boardState.places[y - 2][x];
				else
					return boardState.places[y - 2][x + 1];
			case DS:
				if (y == size - 2)
					return boardState.places[y + 2][x];
				else if (y > size - 2)
					return boardState.places[y + 2][x - 1];
				else
					return boardState.places[y + 2][x + 1];
			case DNW:
				if (y < size)
					return boardState.places[y - 1][x - 2];
				else
					return boardState.places[y - 1][x - 1];
			case DNE:
				if (y < size)
					return boardState.places[y - 1][x + 1];
				else
					return boardState.places[y - 1][x + 2];
			case DSW:
				if (y < size - 1)
					return boardState.places[y + 1][x - 1];
				else
					return boardState.places[y + 1][x - 2];
			case DSE:
				if (y < size - 1)
					return boardState.places[y + 1][x + 2];
				else
					return boardState.places[y + 1][x + 1];
			case AE:
				return boardState.places[y][x + 1];
			case AW:
				return boardState.places[y][x - 1];
			case ANE:
				if (y < size)
					return boardState.places[y - 1][x];
				else
					return boardState.places[y - 1][x + 1];
			case ANW:
				if (y < size)
					return boardState.places[y - 1][x - 1];
				else
					return boardState.places[y - 1][x];
			case ASE:
				if (y < size - 1)
					return boardState.places[y + 1][x + 1];
				else
					return boardState.places[y + 1][x];
			case ASW:
				if (y < size - 1)
					return boardState.places[y + 1][x];
				else
					return boardState.places[y + 1][x - 1];
			default:
				throw new IllegalArgumentException(direction
						+ " is not a valid direction.");
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}

	}

	public Piece getPieceAt(Location l) {
		return l.getPiece();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		Point p = e.getPoint();
		if (boardState == null)
			return;
		for (Location[] locs : boardState.places)
			for (Location l : locs)
				if (l.wasClicked(p)) {

					// //////// ---- This is where stuff happens, yo ----------
					switch (l.state) {
					case selected:
						selection = null;
						moveLocs = new ArrayList<Location>();
						break;
					case highlighted:
						move(selection, l);
						// checkForCheckMate(boardState.turn);
						break;
					case otherSelected:
						otherSelection = new ArrayList<Location>();
						otherMoveLocs = new ArrayList<Location>();
						break;
					default: // plain or otherHighlighted
						if (l.getPiece() != null) {
							if (l.getPiece().team == boardState.turn && myTurn) {

								moveLocs = new ArrayList<Location>();
								selection = l;
								for (Location x : getMoves(l))
									moveLocs.add(x);
							} else {
								otherMoveLocs = new ArrayList<Location>();
								otherSelection = new ArrayList<Location>();
								otherSelection.add(l);
								for (Location x : getMoves(l))
									otherMoveLocs.add(x);
							}
						}
						break;
					}
					// //////// ---- This is where stuff happens, yo ----------

				}

		repaint();
	}

	public boolean isOnLastRank(int x, int y) {
		int size = boardSize;
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

	/**
	 * calculates all valid moves for a piece (includes check)
	 * 
	 * @param l
	 *            location of the piece whose moves you want to get
	 * @return
	 */
	public ArrayList<Location> getMoves(Location l) {
		ArrayList<Location> moves = l.getPiece().getMoves(l);
		ArrayList<Location> checks = new ArrayList<Location>();
		for (Location loc : moves) {
			if (causesCheck(l, loc)) {
				checks.add(loc);
			}
		}
		moves.removeAll(checks);
		return moves;
	}

	/**
	 * Returns true if moving from location "from" to location "to" causes the
	 * moving piece's King to be in check.
	 * 
	 * @param from
	 * @param to
	 * @param team
	 *            - team which the piece at from is on
	 * @return
	 */
	public boolean causesCheck(Location from, Location to) {
		Team team = from.getPiece().team;
		Piece p = moveCheck(from, to);

		for (Location[] locs : boardState.places) {
			for (Location l : locs) {
				if (l.piece != null) {
					if (l.piece.team != team
							&& !deadPeople.contains(l.piece.team)) {
						for (Location moveLoc : l.piece.getMoves(l)) {
							if (moveLoc.piece != null
									&& moveLoc.piece.type == Type.KING
									&& moveLoc.piece.team == team) {
								moveCheck(to, from);
								boardState.places[to.y][to.x].piece = p;
								return true;
							}
						}
					}
				}
			}
		}

		moveCheck(to, from);
		boardState.places[to.y][to.x].piece = p;
		return false;
	}

	/**
	 * moves a piece from a location to another
	 * 
	 * @param from
	 * @param to
	 * @return the Piece at location to
	 */
	public Piece moveCheck(Location from, Location to) {
		Piece p = boardState.places[to.y][to.x].piece;

		boardState.places[to.y][to.x].piece = boardState.places[from.y][from.x].piece;
		boardState.places[from.y][from.x].piece = null;
		return p;
	}

	public void move(Location from, Location to) {
		if (!myTurn)
			return;

		String promotion = " none";

		if (from.piece instanceof Pawn) {
			if (((Pawn) from.piece).firstTurn)
				((Pawn) from.piece).firstTurn = false;
			if (isOnLastRank(to.x, to.y)) {
				Type[] options = Arrays.copyOfRange(Type.values(), 1,
						Type.values().length - 1);
				Type type = options[JOptionPane
						.showOptionDialog(this,
								"What would you like to promote your pawn to?",
								"PROMOTION!", JOptionPane.DEFAULT_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options,
								options[0])];

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

		move(from, to, promotion);
		rotateOffset = 0;

		selection = null;
		moveLocs = new ArrayList<Location>();
		otherSelection = new ArrayList<Location>();
		otherMoveLocs = new ArrayList<Location>();
	}

	public void move(Location from, Location to, String promotion) {
		connection.send("move " + from + " " + to + promotion);
	}

	public void update() {

		try {
			if (controller != null)
				controller.update();
			if (connection != null)
				connection.update();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (message != null && message != "") {
			JOptionPane.showMessageDialog(null, message);
			message = "";
		}

	}

	/**
	 * overrides the repaint method to set each location to a certain color
	 * first
	 */
	@Override
	public void repaint() {
		update();
		if (myTurn)
			setBackground(Color.green);
		else
			setBackground(Color.red);
		if (boardState != null && boardState.places != null) {
			for (Location[] locs : boardState.places)
				for (Location l : locs) {
					if (moveLocs != null && moveLocs.contains(l))
						l.highlight();
					else if (l.equals(selection))
						l.select();
					else {
						if (otherSelection != null
								&& otherSelection.contains(l))
							l.selectOther();
						else if (otherMoveLocs != null
								&& otherMoveLocs.contains(l))
							l.highlightOther();
						else {
							if (l.equals(boardState.lastMoveFrom)
									|| l.equals(boardState.lastMoveTo)
									|| l.equals(boardState.doubleLastMoveTo)
									|| l.equals(boardState.doubleLastMoveFrom))
								l.lastMove();
							else
								l.plain();
						}
					}
				}
		}
		super.repaint();
	}

	// Just for mouseListener
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void keyPressed(KeyEvent ke) {
		if (ke.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
			if ((myTurn) && boardState.undo != null) {
				connection.send("undo");
				selection = null;
				otherMoveLocs = new ArrayList<>();
				moveLocs = new ArrayList<>();
				otherSelection = new ArrayList<>();
			}
		} else if (ke.getKeyCode() == KeyEvent.VK_RIGHT) {
			rotateOffset += Team.values().length - 1;
			rotateOffset %= Team.values().length;
			repaint();
		} else if (ke.getKeyCode() == KeyEvent.VK_LEFT) {
			rotateOffset++;
			rotateOffset %= Team.values().length;
			repaint();
		} else if (ke.getKeyCode() == KeyEvent.VK_SPACE) {
			if (otherSelection == null)
				otherSelection = new ArrayList<Location>();
			if (otherMoveLocs == null)
				otherMoveLocs = new ArrayList<>();
			else if (!otherSelection.isEmpty() || !otherMoveLocs.isEmpty()) {
				otherSelection = new ArrayList<Location>();
				otherMoveLocs = new ArrayList<Location>();
				return;
			}
			for (Location[] ls : boardState.places)
				for (Location l : ls)
					if (l.piece != null && l.piece.team == boardState.turn) {
						if (!getMoves(l).isEmpty())
							otherSelection.add(l);
						for (Location loc : getMoves(l))
							otherMoveLocs.add(loc);
					}
		}
	}

	public HashMap<Location, ArrayList<Location>> getAllMoves() {
		HashMap<Location, ArrayList<Location>> output = new HashMap<>();
		for (Location[] locs : boardState.places)
			for (Location l : locs)
				if (l.piece != null && l.piece.team == boardState.turn) {
					if (getMoves(l).isEmpty())
						continue;
					ArrayList<Location> moves = new ArrayList<Location>();
					for (Location loc : getMoves(l))
						moves.add(loc);
					output.put(l, moves);
				}
		return output;
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void receive(String s) {
		System.out.println("Board: " + s);
		String[] commands = s.split(" ");
		if (commands[0].equals("move")) {

			boardState.move(commands[1], commands[2], commands[3]);

		} else if (commands[0].equals("turn")) {

			boardState.turn = Team.valueOf(commands[1]);

		} else if (commands[0].equals("size")) {

			myTeams = new ArrayList<Team>();
			ai = new ArrayList<>();
			initBoard(Integer.parseInt(commands[1]));
			String[] teams = commands[2].split(",");
			for (String str : teams)
				myTeams.add(Team.valueOf(str.trim()));
			for (int i = 1; i <= Integer.parseInt(commands[3]); i++) {
				ai.add(myTeams.get(myTeams.size() - i));
			}
			System.out.println("My Teams: " + myTeams);
			System.out.println("AI: " + ai);

		} else if (commands[0].equals("undo")) {

			boardState = boardState.undo;

		} else if (commands[0].equals("mate?")) {

			connection.send("mate " + getAllMoves().isEmpty());

		} else if (commands[0].equals("checkmate")) {

			deadPeople.add(boardState.turn);
			message = "Team " + boardState.turn + " is in checkmate!";

		} else if (commands[0].equals("gameover")) {

			message = "Team " + commands[1] + " is the winner! Congrats.";

		}
		myTurn = myTeams.contains(boardState.turn);
		if (commands[0].equals("turn") && ai.contains(boardState.turn)){
			makeMove(getAIMove());
		}
	}

	public void makeMove(Move move) {
		if(move == null)
			return;
		move(move.from, move.to);
	}

	public ArrayList<Move> allMoves() {
		HashMap<Location, ArrayList<Location>> allMoveLocations = getAllMoves();
		ArrayList<Move> allMoves = new ArrayList<>();
		for (Location l : allMoveLocations.keySet())
			for (Location loc : allMoveLocations.get(l))
				allMoves.add(new Move(l, loc));
		return allMoves;
	}

	private Team currentTurn;

	public Move getAIMove() {
		Move best = null;
		double bestScore = -Double.MAX_VALUE;
		currentTurn = boardState.turn;
		for (Move move : allMoves()) {
			boardState.moveHelp(move);
			double score = alphaBetaMax(-Double.MAX_VALUE, Double.MAX_VALUE, 2);
			System.out.println(move + " yields score of " + score);
			if (score > bestScore) {
				bestScore = score;
				best = move;
			}
			boardState.undo();
		}
		return best;

	}

	public double chooseAlphaBeta(double alpha, double beta, int depth) {
		return boardState.turn != currentTurn ? alphaBetaMin(alpha, beta, depth)
				: alphaBetaMax(alpha, beta, depth);
	}

	public double alphaBetaMax(double alpha, double beta, int depth) {
		if (depth == 0)
			return evaluate();
		for (Move m : allMoves()) {
			boardState.moveHelp(m);
			double score = alphaBetaMin(alpha, beta, depth - 1);
			boardState.undo();
			if (score >= beta)
				return beta; // fail hard beta-cutoff
			if (score > alpha)
				alpha = score; // alpha acts like max in MiniMax
		}
		return alpha;
	}

	double alphaBetaMin(double alpha, double beta, int depth) {
		if (depth == 0)
			return -evaluate();
		for (Move m : allMoves()) {
			boardState.moveHelp(m);
			double score = alphaBetaMax(alpha, beta, depth - 1);
			boardState.undo();
			if (score <= alpha)
				return alpha; // fail hard alpha-cutoff
			if (score < beta)
				beta = score; // beta acts like min in MiniMax
		}
		return beta;
	}

	public double evaluate() {
		double output = 0.0;
		for (Location[] locs : boardState.places)
			for (Location loc : locs)
				if (loc.piece != null)
					switch (loc.piece.type) {
					case KING:
						output += (loc.piece.team == currentTurn ? 1 : -1) * 200;
						break;
					case QUEEN:
						output += (loc.piece.team == currentTurn ? 1 : -1) * 12;
						break;
					case ROOK:
						output += (loc.piece.team == currentTurn ? 1 : -1) * 5;
						break;
					case BISHOP:
						output += (loc.piece.team == currentTurn ? 1 : -1) * 3;
						break;
					case KNIGHT:
						output += (loc.piece.team == currentTurn ? 1 : -1) * 3;
						break;
					case PAWN:
						output += (loc.piece.team == currentTurn ? 1 : -1) * 1;
						break;
					}

		return output;
	}

	public class Move {
		Location to;
		Location from;

		Move(Location from, Location to) {
			this.from = from;
			this.to = to;
		}

		public String toString() {
			return "Move " + from + " to " + to + ". ";
		}
	}
}
