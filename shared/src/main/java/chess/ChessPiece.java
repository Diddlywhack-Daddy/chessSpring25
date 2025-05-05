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
        return pawnMoves;
    }

    private Collection<ChessMove> getRookMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> rookMoves = new ArrayList<>();
        return rookMoves;
    }

    private Collection<ChessMove> getKnightMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> knightMoves = new ArrayList<>();
        return knightMoves;
    }

    private Collection<ChessMove> getBishopMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> bishopMoves = new ArrayList<>();
        return bishopMoves;
    }

    private Collection<ChessMove> getQueenMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> queenMoves = new ArrayList<>();
        queenMoves.addAll(getRookMoves(board, myPosition));
        queenMoves.addAll(getBishopMoves(board,myPosition));
        return queenMoves;
    }

    private Collection<ChessMove> getKingMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> kingMoves = new ArrayList<>();
        int myRow = myPosition.getRow();
        int myCol = myPosition.getColumn();
        ChessGame.TeamColor myColor = getTeamColor();

        //populates possibleMoves with every square around and including myPosition
        Vector<ChessPosition> possibleMoves = new Vector<>();
        for(int r=myRow-1; r<=myRow+1; r++){
            for(int c=myCol-1; c<=myCol+1; c++){
                possibleMoves.add(new ChessPosition(r,c));
            }
        }

        //Determines if possibleMoves belong in kingMoves
        while(!possibleMoves.isEmpty()){
            ChessPosition pos = possibleMoves.firstElement();
            //safety check to remove current position from possibleMoves
            if(pos == myPosition){possibleMoves.remove(0);}

            //safety check to ensure pos is on the board
            if (!isOnBoard(pos)) {possibleMoves.remove(0);}

            //if pos is clear or populated by the enemy, adds it to kingMoves
            else if (isClear(board, pos)) {
                kingMoves.add(new ChessMove(myPosition,pos,null));
                possibleMoves.remove(0);
            } else if (board.getPiece(pos).getTeamColor()!=myColor) {
                kingMoves.add(new ChessMove(myPosition,pos,null));
                possibleMoves.remove(0);
            }
            else{
                possibleMoves.remove(0);
            }
        }
        
        return kingMoves;
    }

    private boolean isClear(ChessBoard board, ChessPosition position){
        return (board.getPiece(position)==null);
    }
    //returns true if row and column of position are between 1 and 8
    private boolean isOnBoard(ChessPosition position){
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
