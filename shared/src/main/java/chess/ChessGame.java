package chess;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor teamTurn = TeamColor.WHITE;
    private ChessBoard board;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }


    //function to switch teamTurn after a move
    public void switchTeamTurn() {
        if (getTeamTurn() == TeamColor.WHITE) {
            setTeamTurn(TeamColor.BLACK);
        } else {
            setTeamTurn(TeamColor.WHITE);
        }
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */

    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        if(board.getPiece(startPosition) == null){
            return null;
        }
        Collection<ChessMove> possibleMoves = board.getPiece(startPosition).pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();

        for (ChessMove move : possibleMoves) {
            ChessPiece piece = board.getPiece(startPosition);
            if (!movePutsInCheck(piece.getTeamColor(), move)) {
                validMoves.add(move);
            }
        }
        return validMoves;
    }


    private boolean movePutsInCheck(TeamColor teamColor, ChessMove move) {
        ChessBoard testBoard = testMove(move);
        return (isInCheckTester(testBoard, teamColor));
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition start = move.getStartPosition();
        //must move a piece
        if (board.getPiece(start) != null) {
            Collection<ChessMove> validMoves = validMoves(start);

            //Must be your turn to move
            if (board.getPiece(start).getTeamColor() != teamTurn) {
                throw new InvalidMoveException();
            }
            if (validMoves == null || !validMoves.contains(move)) {
                throw new InvalidMoveException();
            }

            //move must be valid
            if (validMoves.contains(move)) {
                ChessPiece piece = board.getPiece(start);

                //handles promotions
                if (move.getPromotionPiece() != null) {
                    piece = new ChessPiece(getTeamTurn(), move.getPromotionPiece());
                }

                //make the move
                board.addPiece(start, null);
                board.addPiece(move.getEndPosition(), piece);
                switchTeamTurn();
            }
        }
        else{
            throw new InvalidMoveException();
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isInCheckTester(board, teamColor);
    }

    private Boolean isInCheckTester(ChessBoard testboard, TeamColor myColor) {
        ChessGame.TeamColor opponentColor;
        if (myColor == TeamColor.WHITE) {
            opponentColor = TeamColor.BLACK;
        } else {
            opponentColor = TeamColor.WHITE;
        }

        Collection<ChessMove> opponentMoves = possibleMovesHelper(testboard,opponentColor);
        for (ChessMove move : opponentMoves) {
            ChessPosition endPosition = move.getEndPosition();
            if (testboard.getPiece(endPosition) != null && testboard.getPiece(endPosition).getTeamColor() == myColor &&
                    testboard.getPiece(endPosition).getPieceType() == ChessPiece.PieceType.KING) {
                return true;
            }
        }
        return false;
    }

    public String serialize() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static ChessGame deserialize(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, ChessGame.class);
    }


    /**
     * The following function defines a possible move as any move that is possible
     * through the base piece movements in chess. The function iterates each square
     * on the board (boardSquare is the current location checked) and returns
     * the a collection of all possible piecemoves for the specified color.
     **/
    private Collection<ChessMove> possibleMoves(TeamColor color) {
        return possibleMovesHelper(board,color);
    }

    private Collection<ChessMove> possibleMovesHelper(ChessBoard testBoard, TeamColor color){
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                ChessPosition boardSquare = new ChessPosition(r, c);
                if (testBoard.getPiece(boardSquare) != null &&
                        testBoard.getPiece(boardSquare).getTeamColor() == color) {
                    possibleMoves.addAll(testBoard.getPiece(boardSquare).pieceMoves(testBoard, boardSquare));
                }
            }
        }
        return possibleMoves;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        Collection<ChessMove> possibleMoves = possibleMoves(teamColor);
        for (ChessMove move : possibleMoves) {
            ChessBoard test = testMove(move);
            if (!isInCheckTester(test, teamColor)) {
                return false;
            }
        }
        return true;
    }

    private ChessBoard testMove(ChessMove move) {
        ChessBoard testBoard = board.cloneBoard();

        ChessPosition start = move.getStartPosition();
        if (testBoard.getPiece(start) != null) {
            ChessPiece piece = testBoard.getPiece(start);
            TeamColor pieceColor = piece.getTeamColor();
            if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
                if (piece.getTeamColor() == TeamColor.WHITE && move.getEndPosition().getRow() == 8 ||
                        piece.getTeamColor() == TeamColor.BLACK && move.getEndPosition().getRow() == 1) {
                    piece = new ChessPiece(pieceColor, move.getPromotionPiece());
                }
            }
            testBoard.addPiece(move.getEndPosition(), new ChessPiece(pieceColor, piece.getPieceType()));
            testBoard.addPiece(start, null);

        }
        return testBoard;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        Collection<ChessMove> possibleMoves = possibleMoves(teamColor);
        if (isInCheck(teamColor)) {
            return false;
        }
        for (ChessMove move : possibleMoves) {
            if (!movePutsInCheck(teamColor, move)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return teamTurn == chessGame.teamTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, board);
    }

}
