package chess;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;

public abstract class Piece {
	public static enum Team {
		ONE, TWO, THREE
	}

	public static enum Type {
		PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING
	}

	public Team team;
	public Type type;
	public static final int numDirections = 12;
	
	public abstract ArrayList<Location> getMoveLocations(Location l,
			int direction);

	public ArrayList<Location> getMoves(Location l) {
		return getMoveLocations(l, getTeamDirection());
	}

	public int getTeamDirection() {
		switch (team) {
		case ONE:
			return ChessBoard.DN;
		case TWO:
			return ChessBoard.DSE;
		case THREE:
			return ChessBoard.DSW;
		default:
			return 0;
		}
	}

	public int getRightAdjacentDirection(int d) {
		return getRightDirection(getRightDirection(d));
	}

	public int getRightDiagonalDirection(int d) {
		return getRightDirection(getRightDirection(d));
	}

	public int getLeftAdjacentDirection(int d) {
		return getLeftDirection(getLeftDirection(d));
	}

	public int getLeftDiagonalDirection(int d) {
		return getLeftDirection(getLeftDirection(d));
	}

	public int getRightDirection(int d) {
		return (d + 1) % numDirections;
	}

	public int getLeftDirection(int d) {
		return (d - 1) % numDirections;
	}

	public int getAbsFromRel(int facing, int relative) {
		return (facing + relative) % numDirections;
	}
}
