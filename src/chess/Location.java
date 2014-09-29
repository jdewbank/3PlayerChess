package chess;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;

import chess.ChessBoard.State;
import chess.Piece.Team;

/**
 * BECAUSE EVERYONE LOVES NESTED CLASSES
 * 
 * @author Joey
 * 
 */
public class Location {
	public int x;
	public int y;

	public Piece piece;
	private Polygon p;

	private double pixelX;
	private double pixelY;

	public State state;
	private Color color;
	
	private int size;

	private int getOffset(int y) {
		return Math.max(0, y - (size - 1));
	}

	public Location(int y, int x, int boardSize) {
		this.y = y;
		this.x = x;
		size = boardSize;
		switch ((x + y + getOffset(y)) % 3) {
		case 0:
			color = Color.gray;
			break;
		case 1:
			color = Color.white;
			break;
		case 2:
			color = Color.lightGray;
			break;
		}
		state = State.plain;
		piece = startingPosition(x, y, boardSize);
		makePolygon();
	}

	public Location(Location l) {
		x = l.x;
		y = l.y;
		color = l.color;
		state = l.state;
		piece = l.piece;
		p = l.p;
		size =l.size;
		pixelX = l.pixelX;
		pixelY = l.pixelY;
	}

	/**
	 * make dat polygon
	 */
	public void makePolygon() {
		int newX = this.x;
		int newY = this.y;
		int i = 0;
		// This is possibly the most boss algorithm ever.
		if (ChessBoard.chessBoard == null
				|| ChessBoard.chessBoard.boardState == null
				|| ChessBoard.myTeams == null)
			return;

		if (ChessBoard.myTeams.contains(ChessBoard.chessBoard.boardState.turn))
			i = ChessBoard.chessBoard.boardState.turn.ordinal();
		else
			try {
				i = ChessBoard.myTeams.get(0).ordinal();
			} catch (NullPointerException e) {
				i = 0;
			}
		i += ChessBoard.rotateOffset % 3;
		if (i != 0) {
			int[] xyz = convertToXYZ(newX, newY);
			for (int x = 0; x < i; x++)
				rotate120(xyz);
			newY = newY(xyz[2]);
			newX = newX(xyz[1], newY);
		}
		// the boss algorithm ends here

		int offset = Math.abs((size - 1) - newY);

		pixelX = (Math.sqrt(3) * ChessBoard.hexSize + 2) * newX / 2.0
				+ ChessBoard.buffer + ChessBoard.hexSize * offset
				* Math.sqrt(3) / 4.0;
		pixelY = newY * ChessBoard.hexSize + ChessBoard.buffer - newY
				* ChessBoard.hexSize / 4;

		p = new Polygon();
		p.addPoint((int) (pixelX), (int) (pixelY + ChessBoard.hexSize / 4));
		p.addPoint((int) (pixelX), (int) (pixelY + ChessBoard.hexSize * 3 / 4));

		p.addPoint((int) (pixelX + Math.sqrt(3) * ChessBoard.hexSize / 4.0),
				(int) (pixelY + ChessBoard.hexSize));

		p.addPoint((int) (pixelX + Math.sqrt(3) / 2 * ChessBoard.hexSize),
				(int) (pixelY + ChessBoard.hexSize * 3 / 4));
		p.addPoint((int) (pixelX + Math.sqrt(3) / 2 * ChessBoard.hexSize),
				(int) (pixelY + ChessBoard.hexSize / 4));

		p.addPoint((int) (pixelX + Math.sqrt(3) * ChessBoard.hexSize / 4.0),
				(int) (pixelY));
	}

	public int[] convertToXYZ(int x, int y) {
		int[] output = new int[3];
		output[1] = -(x + getOffset(y) - (size - 1));
		output[2] = y - (size - 1);
		output[0] = -(output[1] + output[2]);
		return output;
	}

	public void rotate120(int[] xyz) {
		int x = xyz[0];
		xyz[0] = xyz[2];
		xyz[2] = xyz[1];
		xyz[1] = x;
	}

	public int newY(int z) {
		return z + (size - 1);
	}

	public int newX(int y1, int y2) {
		return -y1 - getOffset(y2) + (size - 1);
	}

	/**
	 * actually draws the stuff
	 * 
	 * @param g
	 */
	public void draw(Graphics g) {
		// fill color
		switch (state) {
		case selected:
			g.setColor(Color.green);
			break;
		case highlighted:
			g.setColor(Color.red);
			break;
		case plain:
			g.setColor(color);
			break;
		case otherSelected:
			g.setColor(Color.CYAN);
			break;
		case otherHighlighted:
			g.setColor(Color.yellow);
			break;
		case lastMove:
			g.setColor(Color.pink);
			break;
		}

		makePolygon();
		g.fillPolygon(p);

		g.setColor(Color.BLACK);
		g.drawPolygon(p);

		drawString(g);
	}

	/**
	 * draws a string representation of a piece
	 * 
	 * @param g
	 */
	public void drawString(Graphics g) {
		if (piece != null) {
			int xc = (int) (pixelX + Math.sqrt(3) * ChessBoard.hexSize / 6);
			int yc = (int) (pixelY + ChessBoard.hexSize * (3.0 / 4));
			String type = "";
			switch (piece.type) {
			case PAWN:
				type = "P";
				break;
			case ROOK:
				type = "R";
				break;
			case KNIGHT:
				type = "N";
				break;
			case BISHOP:
				type = "B";
				break;
			case QUEEN:
				type = "Q";
				break;
			case KING:
				type = "K";
				break;
			}
			switch (piece.team) {
			case ONE:
				g.setColor(Color.black);
				break;
			case TWO:
				g.setColor(Color.magenta);
				break;
			case THREE:
				g.setColor(Color.blue);
				break;
			}
			g.setFont(new Font(Font.SERIF, Font.BOLD,
					(int) (ChessBoard.hexSize / 2)));
			g.drawString(type, xc, yc);
		}
	}

	/**
	 * 
	 * @param l
	 *            other location
	 * @return if two locations have the same x and y coordinates
	 */
	public boolean equals(Location l) {
		return l != null && l.y == this.y && l.x == this.x;
	}

	/**
	 * set state to selected
	 */
	public void select() {
		state = State.selected;
	}

	/**
	 * set state to highlighted
	 */
	public void highlight() {
		state = State.highlighted;
	}

	/**
	 * set state to plain
	 */
	public void plain() {
		state = State.plain;
	}

	public Location getNextLocation(int direction) {
		return ChessBoard.chessBoard.getNextLocation(this, direction);
	}

	public Piece getPiece() {
		return piece;
	}

	public Piece startingPosition(int x, int y, int size) {
		// if (x == size - 1 && y == size - 1)
		// return new Rook(Team.ONE);
		// if (size < 4)
		// throw new IllegalArgumentException(
		// "Cannot play chess on a board smaller than 4... yet");
		int boardHeight = size * 2 - 1;

		y = boardHeight - y - 1;
		if (y < 2) {
			// team 1
			return choosePiece(Team.ONE, x, y, size);
		} else if (x <= 1 && y >= size - 2 && !(x == 1 && y == size - 2)) {
			// team 2
			if (x == 1 || (x == 0 && y == size - 2))
				return choosePiece(Team.TWO, boardHeight - y - 1, 1, size);
			else
				return choosePiece(Team.TWO, boardHeight - y - 1, 0, size);
		} else if (x >= boardHeight - Math.abs(y - (size - 1)) - 2
				&& y >= size - 2
				&& !(x == boardHeight - Math.abs(y - (size - 1)) - 2 && y == size - 2)) {
			// team 3
			if (x == boardHeight - Math.abs(y - (size - 1)) - 2
					|| (x == boardHeight - Math.abs(y - (size - 1)) - 1 && y == size - 2))
				return choosePiece(Team.THREE, boardHeight - y - 1, 1, size);
			else
				return choosePiece(Team.THREE, boardHeight - y - 1, 0, size);
		}
		return null;
	}

	public Piece choosePiece(Team t, int x, int y, int size) {
		if (y != 0) {
			if (Math.ceil(Math.abs((size + y - 1) / 2.0 - x)) <= 1)
				return new Bishop(t);
			else
				return new Pawn(t);
		} else {
			int index = (int) Math.ceil(Math.abs((size - 1) / 2.0 - x));
			int maxIndex = (int) Math.ceil((size - 1) / 2.0) + 2;
			switch (index) {
			case 0:
				return size % 2 == 1 ? new Bishop(t) : new King(t);
			case 1:
				return x == size / 2 - 1 ? new King(t) : new Queen(t);
			default:
				if (index > maxIndex / 2.0)
					return new Rook(t);
				else
					return new Knight(t);
			}
		}
		// } else {
		// return new King(t);
		// }
		// if (y == 1)
		// return new Pawn(t);
		// else {
		// int index = (int) Math.ceil(Math.abs((size - 1) / 2.0 - x));
		// switch (index) {
		// case 0:
		// return new King(t);
		// case 1:
		// if (size % 2 == 0 && x == size / 2)
		// return new King(t);
		// else
		// return new Queen(t);
		// default:
		// index = size / 2 - index;
		// if (index >= (size - 2) / 3)
		// return new Bishop(t);
		// else if (index >= (size - 2) / 6)
		// return new Knight(t);
		// else
		// return new Rook(t);
		// }
		// }
	}

	/**
	 * very helpful helper method for mouseClicked returns if point is in
	 * location
	 * 
	 * @param point
	 * @return
	 */
	public boolean wasClicked(Point point) {
		return p != null && p.contains(point);
	}

	public void selectOther() {
		state = State.otherSelected;
	}

	public void highlightOther() {
		state = State.otherHighlighted;
	}

	public void lastMove() {
		state = State.lastMove;
	}

	public String toString() {
		return x + "," + y;
	}
}
