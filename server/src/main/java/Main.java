import chess.ChessGame;
import chess.ChessPiece;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import dataaccess.SqlDataAccess;
import server.Server;
import service.ClearService;
import service.GameService;
import service.UserService;


public class Main {
    public static void main(String[] args) {
        int port = 8080;
        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }

        boolean useSql = true;
        DataAccess dataAccess;

        if (useSql) {
            try {
                dataAccess = new SqlDataAccess();
            } catch (DataAccessException e) {
                System.err.println("SQL startup failed: " + e.getMessage());
                return;
            }
        } else {
            dataAccess = new MemoryDataAccess();
        }

        var userService = new UserService(dataAccess);
        var gameService = new GameService(dataAccess);
        var clearService = new ClearService(dataAccess);
        var server = new Server(userService, gameService, clearService);

        server.run(port);

        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);
    }
}
