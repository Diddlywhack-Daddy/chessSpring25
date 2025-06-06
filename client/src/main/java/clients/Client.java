package clients;

import backend.ServerFacade;
import model.AuthData;
import model.UserData;
import chess.*;
import server.exceptions.BadRequestException;

public abstract class Client {

    protected final ServerFacade server;
    protected final String serverURL;

    protected UserData user;
    protected AuthData auth;
    protected ChessGame game;
    protected ChessGame.TeamColor userColor;
    protected int gameID;
    protected GameClient gameClient;


    public Client(String serverURL) {

        server = new ServerFacade(serverURL);
        this.serverURL = serverURL;
    }

    protected String clear(String... params) throws BadRequestException {
        try {
            if (params.length != 1) {
                throw new BadRequestException("Unauthorized.");
            }
            if (params[0].equals("biscuit")) {
                server.clear();
                return "";
            }
            throw new BadRequestException("Unauthorized.");
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    protected int convertHeaderToInt(String position) throws BadRequestException {
        return switch (position.toLowerCase().charAt(0)) {
            case 'a' -> 1;
            case 'b' -> 2;
            case 'c' -> 3;
            case 'd' -> 4;
            case 'e' -> 5;
            case 'f' -> 6;
            case 'g' -> 7;
            case 'h' -> 8;
            default -> throw new BadRequestException("Expected: <source> <destination> <optional: promotion>");
        };
    }

    protected ChessPiece.PieceType convertStringToPiece(String piece) throws BadRequestException {
        return switch (piece.toLowerCase()) {
            case "queen" -> ChessPiece.PieceType.QUEEN;
            case "rook" -> ChessPiece.PieceType.ROOK;
            case "knight" -> ChessPiece.PieceType.KNIGHT;
            case "bishop" -> ChessPiece.PieceType.BISHOP;
            default -> throw new BadRequestException("Expected: <source> <destination> <optional: promotion>");
        };
    }



    protected void assertNotEmpty(String... params) throws BadRequestException {
        for (String param : params) {
            if (param.isEmpty() || param.equals(" ")) {
                throw new BadRequestException("Unexpected input.");
            }
        }
    }
}
