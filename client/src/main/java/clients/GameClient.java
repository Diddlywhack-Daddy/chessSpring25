package clients;

import backend.ServerFacade;
import chess.*;
import server.exceptions.BadRequestException;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import chess.ChessGame.*;
import chess.ChessPiece.PieceType;


import java.util.*;
import java.util.stream.Collectors;

public class GameClient extends Client {
    private final ServerFacade server;
    private final String serverUrl;
    protected ChessGame game;
    protected ChessGame.TeamColor userColor;

    private static final int BOARD_SIZE_IN_SQUARES = 10;
    private static final int SQUARE_SIZE_IN_PADDED_CHARS = 1;

    public GameClient(String serverUrl) {
        super(serverUrl);
        this.serverUrl = serverUrl;
        server = new ServerFacade(serverUrl);
    }

    public void setGameAndColor(ChessGame game, ChessGame.TeamColor color) {
        this.game = game;
        this.userColor = color;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "highlight" -> highlight(params);
                case "redraw" -> redraw();
                case "move" -> move(params);
                case "leave" -> leave();
                case "resign" -> resign();
                case "start" -> enterGameMode();
                default -> help();
            };
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }


    private String enterGameMode() {
        return help();
    }


    public String move(String... params) throws BadRequestException {
        if (params.length >= 2 && params.length <= 3) {
            ChessPosition start = new ChessPosition(Integer.parseInt(String.valueOf(params[0].charAt(1))), this.convertHeaderToInt(params[0]));
            ChessPosition end = new ChessPosition(Integer.parseInt(String.valueOf(params[1].charAt(1))), this.convertHeaderToInt(params[1]));
            ChessPiece.PieceType promotionPiece;
            if (params.length == 3) {
                promotionPiece = this.convertStringToPiece(params[2]);
            } else {
                promotionPiece = null;
            }

            ChessMove move = new ChessMove(start, end, promotionPiece);
            if (this.game.validMoves(start).contains(move)) {
                // Make the move
                return "";
            } else {
                throw new BadRequestException("Invalid move.");
            }
        } else {
            throw new BadRequestException("Expected: <source> <destination> <optional: promotion>.");
        }

    }



    private String leave() {
        game = null;
        return "leave";
    }

    private String resign() {
        game = null;
        return "resign";
    }

    public String help() {
        return """
                Please choose one of the following options:
                - highlight <position> (ex. G7)
                - move <source> <destination> <optional: promotion> (ex. f5 e4 queen)
                - redraw (redraws the board)
                - resign (rage quit, game over you lose, returns to previous menu)
                - leave (returns to previous menu)
                - help (shows current options)
                """;
    }


    public void printBoard(ChessGame.TeamColor color, ChessGame game) {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        this.drawHeader(out, color);
        Collection<ChessMove> moves = new ArrayList();
        this.drawBoard(out, game, color, moves);
        this.drawHeader(out, color);
    }

    private void drawHeader(PrintStream out, ChessGame.TeamColor color) {
        setBorderColor(out);
        List<String> headers = Arrays.asList(" a ", "  b ", "  c ", " d ", "  e ", "  f ", " g ", "  h ");
        if (color.equals(TeamColor.BLACK)) {
            Collections.reverse(headers);
        }

        out.print("   ");
        Iterator var4 = headers.iterator();

        while (var4.hasNext()) {
            String c = (String) var4.next();
            out.print(c);
        }

        out.print("   ");
        resetColors(out);
        out.println();
    }

    private void drawBoard(PrintStream out, ChessGame game, ChessGame.TeamColor color, Collection<ChessMove> moves) {
        out.print("\u001b[38;5;15m");
        List<String> rowNums = Arrays.asList(" 8 ", " 7 ", " 6 ", " 5 ", " 4 ", " 3 ", " 2 ", " 1 ");
        ChessPiece[][] pieces = game.getBoard().getPieces();
        Collection<ChessPosition> highlights = new ArrayList();
        ChessPosition start = new ChessPosition(-1, -1);
        Iterator var9 = moves.iterator();

        while (var9.hasNext()) {
            ChessMove move = (ChessMove) var9.next();
            highlights.add(move.getEndPosition());
            if (color == TeamColor.BLACK) {
                start = new ChessPosition(move.getStartPosition().getRow() - 1, 8 - move.getStartPosition().getColumn());
            } else {
                start = new ChessPosition(8 - move.getStartPosition().getRow(), move.getStartPosition().getColumn() - 1);
            }
        }

        int row;
        ChessPiece[][] blackPieces;
        if (color.equals(TeamColor.BLACK)) {
            Collections.reverse(rowNums);
            ChessPiece[][] blackPiecesTemp = new ChessPiece[8][8];

            int rowIdx = 0;
            for (ChessPiece[] pieceRow : pieces) {
                ChessPiece[] reversedRow = new ChessPiece[8];
                for (int i = 0; i < 8; i++) {
                    reversedRow[i] = pieceRow[7 - i];
                }
                blackPiecesTemp[7 - rowIdx] = reversedRow;
                rowIdx++;
            }

            pieces = blackPiecesTemp;

            Collection<ChessPosition> newPositions = new ArrayList<>();
            for (ChessPosition highlight : highlights) {
                newPositions.add(new ChessPosition(highlight.getRow() - 1, 8 - highlight.getColumn()));
            }
            highlights = newPositions;
        } else {
            Collection<ChessPosition> newPositions = new ArrayList();
            Iterator var23 = highlights.iterator();

            while (var23.hasNext()) {
                ChessPosition highlight = (ChessPosition) var23.next();
                newPositions.add(new ChessPosition(8 - highlight.getRow(), highlight.getColumn() - 1));
            }

            highlights = newPositions;
        }

        row = 0;
        blackPieces = pieces;
        int var26 = pieces.length;

        for (int var28 = 0; var28 < var26; ++var28) {
            ChessPiece[] pieceRow = blackPieces[var28];
            setBorderColor(out);
            out.print((String) rowNums.get(row));
            int index = 0;
            ChessPiece[] var15 = pieceRow;
            int var16 = pieceRow.length;

            for (int var17 = 0; var17 < var16; ++var17) {
                ChessPiece piece = var15[var17];
                ChessPosition position = new ChessPosition(row, index);
                if (highlights.contains(position)) {
                    this.highlightPiece(out, index, row);
                } else if (position.equals(start)) {
                    out.print("\u001b[48;2;220;220;220m");
                } else {
                    this.drawPiece(out, index, row);
                }

                if (piece == null) {
                    out.print("   ");
                } else {
                    out.print(this.getPiece(piece));
                }

                ++index;
            }

            setBorderColor(out);
            out.print((String) rowNums.get(row));
            resetColors(out);
            out.println();
            ++row;
        }

    }

    private void drawPiece(PrintStream out, int index, int row) {
        if (row % 2 == 0) {
            if (index % 2 == 0) {
                out.print("\u001b[48;2;193;154;107m");
            } else {
                out.print("\u001b[48;2;84;42;24m");
            }
        } else if (index % 2 == 0) {
            out.print("\u001b[48;2;84;42;24m");
        } else {
            out.print("\u001b[48;2;193;154;107m");
        }

    }

    private void highlightPiece(PrintStream out, int index, int row) {
        if (row % 2 == 0) {
            if (index % 2 == 0) {
                out.print("\u001b[48;5;242m");
            } else {
                out.print("\u001b[48;5;238m");
            }
        } else if (index % 2 == 0) {
            out.print("\u001b[48;5;238m");
        } else {
            out.print("\u001b[48;5;242m");
        }

    }

    private String getPiece(ChessPiece piece) {
        String var10000;
        if (piece.getTeamColor().equals(TeamColor.WHITE)) {
            switch (piece.getPieceType()) {
                case KING -> var10000 = " ♔ ";
                case QUEEN -> var10000 = " ♕ ";
                case BISHOP -> var10000 = " ♗ ";
                case KNIGHT -> var10000 = " ♘ ";
                case ROOK -> var10000 = " ♖ ";
                case PAWN -> var10000 = " ♙ ";
                default -> {
                    var10000 = " ? ";
                }
            }

            return var10000;
        } else {
            var10000 = switch (piece.getPieceType()) {
                case KING -> " ♚ ";
                case QUEEN -> " ♛ ";
                case BISHOP -> " ♝ ";
                case KNIGHT -> " ♞ ";
                case ROOK -> " ♜ ";
                case PAWN -> "\u0020\u265f\u202f";
            };

            return var10000;
        }
    }

    private static void setBorderColor(PrintStream out) {
        out.print("\u001b[48;2;120;62;32m");
        out.print("\u001b[38;5;0m");
    }

    private static void resetColors(PrintStream out) {
        out.print("\u001b[49m");
        out.print("\u001b[39m");
    }

    public void highlight(ChessGame.TeamColor color, ChessGame game, Collection<ChessMove> validMoves) {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        this.drawHeader(out, color);
        this.drawBoard(out, game, color, validMoves);
        this.drawHeader(out, color);
    }

    public String highlight(String... params) throws BadRequestException {
        try {
            if (params.length != 1) {
                throw new BadRequestException("Expected <position>");
            } else {
                String col = String.valueOf(params[0].charAt(1));
                ChessPosition position = new ChessPosition(Integer.parseInt(col), this.convertHeaderToInt(params[0]));
                Collection<ChessMove> moves = game.validMoves(position);
                if (moves.isEmpty()) {
                    throw new BadRequestException("This piece cannot move.");
                } else {
                    highlight(userColor, game, moves);
                    return "";
                }
            }
        } catch (NullPointerException var5) {
            throw new BadRequestException("There is no piece at this location.");
        }
    }

    private int convertHeaderToInt(String position) throws BadRequestException {
        byte var10000;
        switch (position.toLowerCase().charAt(0)) {
            case 'a':
                var10000 = 1;
                break;
            case 'b':
                var10000 = 2;
                break;
            case 'c':
                var10000 = 3;
                break;
            case 'd':
                var10000 = 4;
                break;
            case 'e':
                var10000 = 5;
                break;
            case 'f':
                var10000 = 6;
                break;
            case 'g':
                var10000 = 7;
                break;
            case 'h':
                var10000 = 8;
                break;
            default:
                throw new BadRequestException("Expected: <source> <destination> <optional: promotion>");
        }

        return var10000;
    }

    private ChessPiece.PieceType convertStringToPiece(String piece) throws BadRequestException {
        ChessPiece.PieceType var10000;
        switch (piece.toLowerCase()) {
            case "queen":
                var10000 = PieceType.QUEEN;
                break;
            case "rook":
                var10000 = PieceType.ROOK;
                break;
            case "knight":
                var10000 = PieceType.KNIGHT;
                break;
            case "bishop":
                var10000 = PieceType.BISHOP;
                break;
            default:
                throw new BadRequestException("Expected: <source> <destination> <optional: promotion>");
        }

        return var10000;
    }

    public String redraw() throws BadRequestException {
        if (userColor == ChessGame.TeamColor.BLACK) {
            printBoard(TeamColor.BLACK, game);
        } else {
            printBoard(TeamColor.WHITE, game);
        }

        return "";
    }

}


