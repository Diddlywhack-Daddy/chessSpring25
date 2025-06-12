package chess;

import java.util.Arrays;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private ChessPiece[][] board;
    public ChessBoard() {
        board = new ChessPiece[8][8];
    }
    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[position.getRow()-1][position.getColumn()-1]= piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getRow()-1][position.getColumn()-1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        resetFrontRanks();
        resetBackRanks();
    }

    public ChessPiece[][] getPieces() {
        return board;
    }
    public ChessBoard cloneBoard(){
        ChessBoard newBoard = new ChessBoard();
        for(int r=1;r<=8;r++){
            for (int c=1;c<=8;c++){
                ChessPosition pos = new ChessPosition(r,c);
                ChessPiece piece = getPiece(pos);
                if(piece!=null){
                    newBoard.addPiece(pos,new ChessPiece(piece.getTeamColor(),piece.getPieceType()));
                }
                else {
                    newBoard.addPiece(pos, null);
                }
            }
        }
        return newBoard;
    }
    private void resetFrontRanks(){
        for (int c=1;c<=8;c++){
            addPiece(new ChessPosition(2,c),new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            addPiece(new ChessPosition(7,c),new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
        }
    }

    private void resetBackRanks(){
        resetBackRank(ChessGame.TeamColor.WHITE);
        resetBackRank(ChessGame.TeamColor.BLACK);
    }
    private void resetBackRank(ChessGame.TeamColor color){
        int row;
        if(color == ChessGame.TeamColor.WHITE){row = 1;}
        else{row = 8;}
        addPiece(new ChessPosition(row,1),new ChessPiece(color, ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(row,2),new ChessPiece(color, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(row,3),new ChessPiece(color, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(row,4),new ChessPiece(color, ChessPiece.PieceType.QUEEN));
        addPiece(new ChessPosition(row,5),new ChessPiece(color, ChessPiece.PieceType.KING));
        addPiece(new ChessPosition(row,6),new ChessPiece(color, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(row,7),new ChessPiece(color, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(row,8),new ChessPiece(color, ChessPiece.PieceType.ROOK));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Arrays.deepEquals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

    @Override
    public String toString() {
        return "ChessBoard{" +
                "board=" + Arrays.toString(board) +
                '}';
    }
}
