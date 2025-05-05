package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Vector;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private ChessGame.TeamColor color;
    private PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.type = type;
        color = pieceColor;
    }


    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return switch (this.type) {
            case KING -> getKingMoves(board, myPosition);
            case QUEEN -> getQueenMoves(board, myPosition);
            case BISHOP -> getBishopMoves(board, myPosition);
            case KNIGHT -> getKnightMoves(board, myPosition);
            case ROOK -> getRookMoves(board, myPosition);
            case PAWN -> getPawnMoves(board, myPosition);
        };

    }

    private Collection<ChessMove> getPawnMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> pawnMoves = new ArrayList<>();
        int myRow = myPosition.getRow();
        int myCol = myPosition.getColumn();
        ChessGame.TeamColor myColor = getTeamColor();

        Vector<ChessPosition> possibleMoves = new Vector<>();

        //White version of possibleMoves vector
        if (myColor == ChessGame.TeamColor.WHITE) {
            if (myPosition.getRow() == 2 && isClear(board, new ChessPosition(myRow + 2, myCol)) &&
            isClear(board, new ChessPosition(myRow+1, myCol))) {
                possibleMoves.add(new ChessPosition(myRow + 2, myCol));
            }
            ChessPosition forward = new ChessPosition(myRow + 1, myCol);
            if (isOnBoard(forward) && isClear(board, forward)) {
                possibleMoves.add(new ChessPosition(myRow + 1, myCol));
            }
            ChessPosition leftCapture = new ChessPosition(myRow + 1, myCol - 1);
            ChessPosition rightCapture = new ChessPosition(myRow + 1, myCol + 1);
            if (isOnBoard(leftCapture) && !isClear(board, leftCapture) &&
                    board.getPiece(leftCapture).getTeamColor() != myColor) {
                possibleMoves.add(leftCapture);
            }
            if (isOnBoard(rightCapture) && !isClear(board, rightCapture) &&
                    board.getPiece(rightCapture).getTeamColor() != myColor) {
                possibleMoves.add(rightCapture);
            }
        }
        //When myColor == Black
        else{
            if (myPosition.getRow() == 7 && isClear(board, new ChessPosition(myRow - 2, myCol)) &&
                    isClear(board,new ChessPosition(myRow-1,myCol))) {
                possibleMoves.add(new ChessPosition(myRow - 2, myCol));
            }
            ChessPosition forward = new ChessPosition(myRow - 1, myCol);
            if (isOnBoard(forward) && isClear(board, forward)) {
                possibleMoves.add(new ChessPosition(myRow - 1, myCol));
            }
            ChessPosition leftCapture = new ChessPosition(myRow - 1, myCol - 1);
            ChessPosition rightCapture = new ChessPosition(myRow - 1, myCol + 1);
            if (isOnBoard(leftCapture) && !isClear(board, leftCapture) &&
                    board.getPiece(leftCapture).getTeamColor() != myColor) {
                possibleMoves.add(leftCapture);
            }
            if (isOnBoard(rightCapture) && !isClear(board, rightCapture) &&
                    board.getPiece(rightCapture).getTeamColor() != myColor) {
                possibleMoves.add(rightCapture);
            }
        }



        //PromotionMoves First

        while (!possibleMoves.isEmpty()) {
            ChessPosition pos = possibleMoves.firstElement();
            if (!isOnBoard(pos)) {
                possibleMoves.remove(0);
            }

            if (myColor == ChessGame.TeamColor.WHITE && pos.getRow() == 8 ||
                    myColor == ChessGame.TeamColor.BLACK && pos.getRow() == 1) {
                ChessMove rookPromo = new ChessMove(myPosition, pos, PieceType.ROOK);
                ChessMove knightPromo = new ChessMove(myPosition, pos, PieceType.KNIGHT);
                ChessMove bishopPromo = new ChessMove(myPosition, pos, PieceType.BISHOP);
                ChessMove queenPromo = new ChessMove(myPosition, pos, PieceType.QUEEN);
                pawnMoves.add(rookPromo);
                pawnMoves.add(knightPromo);
                pawnMoves.add(bishopPromo);
                pawnMoves.add(queenPromo);
                possibleMoves.remove(0);
            }

            // White nonPromotion moves
            else {
                pawnMoves.add(new ChessMove(myPosition, pos, null));
                possibleMoves.remove(0);
            }
        }


        return pawnMoves;
    }

    private Collection<ChessMove> getRookMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> rookMoves = new ArrayList<>();
        int myRow = myPosition.getRow();
        int myCol = myPosition.getColumn();
        ChessGame.TeamColor myColor = getTeamColor();

        //checks forward
        ChessPosition pos = new ChessPosition(myRow + 1, myCol);
        while (isOnBoard(pos)) {
            ChessMove posMove = new ChessMove(myPosition, pos, null);
            if (isClear(board, pos)) {
                rookMoves.add(posMove);
            } else if (board.getPiece(pos).getTeamColor() != myColor) {
                rookMoves.add(posMove);
                break;
            } else {
                break;
            }
            pos = new ChessPosition(pos.getRow() + 1, pos.getColumn());
        }

        //checks backward
        pos = new ChessPosition(myRow - 1, myCol);
        while (isOnBoard(pos)) {
            ChessMove posMove = new ChessMove(myPosition, pos, null);
            if (isClear(board, pos)) {
                rookMoves.add(posMove);
            } else if (board.getPiece(pos).getTeamColor() != myColor) {
                rookMoves.add(posMove);
                break;
            } else {
                break;
            }
            pos = new ChessPosition(pos.getRow() - 1, pos.getColumn());
        }

        //checks left
        pos = new ChessPosition(myRow, myCol - 1);
        while (isOnBoard(pos)) {
            ChessMove posMove = new ChessMove(myPosition, pos, null);
            if (isClear(board, pos)) {
                rookMoves.add(posMove);
            } else if (board.getPiece(pos).getTeamColor() != myColor) {
                rookMoves.add(posMove);
                break;
            } else {
                break;
            }
            pos = new ChessPosition(pos.getRow(), pos.getColumn() - 1);
        }

        //checks right
        pos = new ChessPosition(myRow, myCol + 1);
        while (isOnBoard(pos)) {
            ChessMove posMove = new ChessMove(myPosition, pos, null);
            if (isClear(board, pos)) {
                rookMoves.add(posMove);
            } else if (board.getPiece(pos).getTeamColor() != myColor) {
                rookMoves.add(posMove);
                break;
            } else {
                break;
            }
            pos = new ChessPosition(pos.getRow(), pos.getColumn() + 1);
        }


        return rookMoves;
    }

    private Collection<ChessMove> getKnightMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> knightMoves = new ArrayList<>();
        int myRow = myPosition.getRow();
        int myCol = myPosition.getColumn();
        ChessGame.TeamColor myColor = getTeamColor();

        //Adds possible moves to possibleMoves
        Vector<ChessPosition> possibleMoves = new Vector<>();
        possibleMoves.add(new ChessPosition(myRow + 1, myCol + 2));
        possibleMoves.add(new ChessPosition(myRow + 1, myCol - 2));
        possibleMoves.add(new ChessPosition(myRow + 2, myCol - 1));
        possibleMoves.add(new ChessPosition(myRow + 2, myCol + 1));
        possibleMoves.add(new ChessPosition(myRow - 1, myCol + 2));
        possibleMoves.add(new ChessPosition(myRow - 1, myCol - 2));
        possibleMoves.add(new ChessPosition(myRow - 2, myCol + 1));
        possibleMoves.add(new ChessPosition(myRow - 2, myCol - 1));

        //Determines if possibleMoves belong in knightMoves
        while (!possibleMoves.isEmpty()) {
            ChessPosition pos = possibleMoves.firstElement();

            //safety check to ensure pos is on the board
            if (!isOnBoard(pos)) {
                possibleMoves.remove(0);
            }

            //if pos is clear or populated by the enemy, adds it to knightMoves
            else if (isClear(board, pos)) {
                knightMoves.add(new ChessMove(myPosition, pos, null));
                possibleMoves.remove(0);
            } else if (board.getPiece(pos).getTeamColor() != myColor) {
                knightMoves.add(new ChessMove(myPosition, pos, null));
                possibleMoves.remove(0);
            } else {
                possibleMoves.remove(0);
            }
        }
        return knightMoves;
    }

    private Collection<ChessMove> getBishopMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> bishopMoves = new ArrayList<>();
        int myRow = myPosition.getRow();
        int myCol = myPosition.getColumn();
        ChessGame.TeamColor myColor = getTeamColor();


        //checks NW
        ChessPosition pos = new ChessPosition(myRow + 1, myCol + 1);
        while (isOnBoard(pos)) {
            ChessMove posMove = new ChessMove(myPosition, pos, null);
            if (isClear(board, pos)) {
                bishopMoves.add(posMove);
            } else if (board.getPiece(pos).getTeamColor() != myColor) {
                bishopMoves.add(posMove);
                break;
            } else {
                break;
            }
            pos = new ChessPosition(pos.getRow() + 1, pos.getColumn() + 1);
        }

        //checks NE
        pos = new ChessPosition(myRow + 1, myCol - 1);
        while (isOnBoard(pos)) {
            ChessMove posMove = new ChessMove(myPosition, pos, null);
            if (isClear(board, pos)) {
                bishopMoves.add(posMove);
            } else if (board.getPiece(pos).getTeamColor() != myColor) {
                bishopMoves.add(posMove);
                break;
            } else {
                break;
            }
            pos = new ChessPosition(pos.getRow() + 1, pos.getColumn() - 1);
        }

        //checks SW
        pos = new ChessPosition(myRow - 1, myCol - 1);
        while (isOnBoard(pos)) {
            ChessMove posMove = new ChessMove(myPosition, pos, null);
            if (isClear(board, pos)) {
                bishopMoves.add(posMove);
            } else if (board.getPiece(pos).getTeamColor() != myColor) {
                bishopMoves.add(posMove);
                break;
            } else {
                break;
            }
            pos = new ChessPosition(pos.getRow() - 1, pos.getColumn() - 1);
        }

        //checks SE
        pos = new ChessPosition(myRow - 1, myCol + 1);
        while (isOnBoard(pos)) {
            ChessMove posMove = new ChessMove(myPosition, pos, null);
            if (isClear(board, pos)) {
                bishopMoves.add(posMove);
            } else if (board.getPiece(pos).getTeamColor() != myColor) {
                bishopMoves.add(posMove);
                break;
            } else {
                break;
            }
            pos = new ChessPosition(pos.getRow() - 1, pos.getColumn() + 1);
        }

        return bishopMoves;
    }

    private Collection<ChessMove> getQueenMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> queenMoves = new ArrayList<>();
        queenMoves.addAll(getRookMoves(board, myPosition));
        queenMoves.addAll(getBishopMoves(board, myPosition));
        return queenMoves;
    }

    private Collection<ChessMove> getKingMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> kingMoves = new ArrayList<>();
        int myRow = myPosition.getRow();
        int myCol = myPosition.getColumn();
        ChessGame.TeamColor myColor = getTeamColor();

        //populates possibleMoves with every square around and including myPosition
        Vector<ChessPosition> possibleMoves = new Vector<>();
        for (int r = myRow - 1; r <= myRow + 1; r++) {
            for (int c = myCol - 1; c <= myCol + 1; c++) {
                possibleMoves.add(new ChessPosition(r, c));
            }
        }

        //Determines if possibleMoves belong in kingMoves
        while (!possibleMoves.isEmpty()) {
            ChessPosition pos = possibleMoves.firstElement();
            //safety check to remove current position from possibleMoves
            if (pos == myPosition) {
                possibleMoves.remove(0);
            }

            //safety check to ensure pos is on the board
            if (!isOnBoard(pos)) {
                possibleMoves.remove(0);
            }

            //if pos is clear or populated by the enemy, adds it to kingMoves
            else if (isClear(board, pos)) {
                kingMoves.add(new ChessMove(myPosition, pos, null));
                possibleMoves.remove(0);
            } else if (board.getPiece(pos).getTeamColor() != myColor) {
                kingMoves.add(new ChessMove(myPosition, pos, null));
                possibleMoves.remove(0);
            } else {
                possibleMoves.remove(0);
            }
        }

        return kingMoves;
    }

    private boolean isClear(ChessBoard board, ChessPosition position) {
        return (board.getPiece(position) == null);
    }

    //returns true if row and column of position are between 1 and 8
    private boolean isOnBoard(ChessPosition position) {
        return 1 <= position.getRow() && position.getRow() <= 8 &&
                1 <= position.getColumn() && position.getColumn() <= 8;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return color == that.color && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, type);
    }

    @Override
    public String toString() {
        return "ChessPiece{" +
                "color=" + color +
                ", type=" + type +
                '}';
    }
}
