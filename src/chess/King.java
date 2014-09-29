package chess;

import java.util.ArrayList;

import chess.Piece.Type;

public class King extends Piece {
	public King(Team t) {
		team = t;
		type = Type.KING;
	}

	@Override
	public ArrayList<Location> getMoveLocations(Location l, int direction) {
		ArrayList<Location> output = new ArrayList<Location>();
		Location loc = l;

		int i = direction;
		do {
			loc = l.getNextLocation(i);

			if (loc != null
					&& (loc.getPiece() == null || loc.getPiece().team != this.team))
				output.add(loc);

			i = getRightDirection(i);
		} while (i != direction);
		return output;
	}
}
