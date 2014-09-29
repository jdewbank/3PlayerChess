package chess;

import java.util.ArrayList;

import chess.Piece.Team;

public class Pawn extends Piece {
	boolean firstTurn;

	public Pawn(Team t) {
		team = t;
		type = Type.PAWN;
		firstTurn = true;
	}

	@Override
	public ArrayList<Location> getMoveLocations(Location l, int direction) {
		ArrayList<Location> output = new ArrayList<Location>();
		Location upLeft = l.getNextLocation(getAbsFromRel(direction,
				ChessBoard.ANW));
		Location upRight = l.getNextLocation(getAbsFromRel(direction,
				ChessBoard.ANE));

		Location dLeft = l.getNextLocation(getAbsFromRel(direction,
				ChessBoard.DNW));
		Location dRight = l.getNextLocation(getAbsFromRel(direction,
				ChessBoard.DNE));

		if (upLeft != null && upLeft.getPiece() == null) {
			output.add(upLeft);
		}
		if (upRight != null && upRight.getPiece() == null) {
			output.add(upRight);
		}
		if (dLeft != null && dLeft.getPiece() != null
				&& dLeft.getPiece().team != this.team) {
			output.add(dLeft);
		}
		if (dRight != null && dRight.getPiece() != null
				&& dRight.getPiece().team != this.team) {
			output.add(dRight);
		}

		if (firstTurn) {
			Location fRight;
			try {
				fRight = l.getNextLocation(
						getAbsFromRel(direction, ChessBoard.ANE))
						.getNextLocation(
								getAbsFromRel(direction, ChessBoard.ANE));
			} catch (NullPointerException e) {
				fRight = null;
			}
			
			Location fLeft;
			try {
				fLeft = l.getNextLocation(
						getAbsFromRel(direction, ChessBoard.ANW))
						.getNextLocation(
								getAbsFromRel(direction, ChessBoard.ANW));
			} catch (NullPointerException e) {
				fLeft = null;
			}


			Location fCenter = l.getNextLocation(getAbsFromRel(direction,
					ChessBoard.DN));

			if (fRight != null && fRight.getPiece() == null
					&& output.contains(upRight)) {
				output.add(fRight);
			}
			if (fLeft != null && fLeft.getPiece() == null
					&& output.contains(upLeft)) {
				output.add(fLeft);
			}
			if (fCenter != null && fCenter.getPiece() == null
					&& (output.contains(upRight) || output.contains(upLeft))) {
				output.add(fCenter);
			}
		}
		return output;
	}
}
