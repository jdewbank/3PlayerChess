package chess;

import java.util.ArrayList;

import chess.Piece.Team;
import chess.Piece.Type;

public class Knight extends Piece {

	public Knight(Team t) {
		team = t;
		type = Type.KNIGHT;
	}

	@Override
	public ArrayList<Location> getMoveLocations(Location l, int direction) {
		ArrayList<Location> output = new ArrayList<Location>();

		int i = direction;
		do {
			try {
				Location loc1 = l.getNextLocation(
						getAbsFromRel(i, ChessBoard.DN)).getNextLocation(
						getAbsFromRel(i, ChessBoard.ANE));

				Location loc2 = l.getNextLocation(
						getAbsFromRel(i, ChessBoard.DN)).getNextLocation(
						getAbsFromRel(i, ChessBoard.ANW));

				if (loc1 != null
						&& (loc1.getPiece() == null || loc1.getPiece().team != this.team))
					output.add(loc1);
				if (loc2 != null
						&& (loc2.getPiece() == null || loc2.getPiece().team != this.team))
					output.add(loc2);
			} catch (NullPointerException e) {
			}
			i = getRightDiagonalDirection(i);
		} while (i != direction);

		return output;
	}
}
