package chess;

import java.util.ArrayList;

import chess.Piece.Team;
import chess.Piece.Type;

public class Rook extends Piece {

	public Rook(Team t) {
		team = t;
		type = Type.ROOK;
	}

	@Override
	public ArrayList<Location> getMoveLocations(Location l, int direction) {
		ArrayList<Location> output = new ArrayList<Location>();
		Location loc = l;

		int i = getRightDirection(direction);
		do {
			loc = l.getNextLocation(i);

			while (loc != null && loc.getPiece() == null) {
				output.add(loc);
				loc = loc.getNextLocation(i);
			}
			if (loc != null && loc.getPiece().team != this.team)
				output.add(loc);

			i = getRightAdjacentDirection(i);
		} while (i != getRightDirection(direction));
		return output;
	}
}
