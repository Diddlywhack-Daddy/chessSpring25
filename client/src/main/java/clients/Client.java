package clients;

import backend.ServerFacade;
import com.sun.nio.sctp.HandlerResult;
import com.sun.nio.sctp.Notification;
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




    protected void assertNotEmpty(String... params) throws BadRequestException {
        for (String param : params) {
            if (param.isEmpty() || param.equals(" ")) {
                throw new BadRequestException("Unexpected input.");
            }
        }
    }


    public HandlerResult handleNotification(Notification notification, Object o) {
        return null;
    }
}
