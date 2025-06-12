package backend;

import chess.ChessMove;
import com.google.gson.Gson;
import model.request.*;
import model.result.*;
import server.exceptions.*;

import java.io.*;
import java.net.*;
import java.util.Objects;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public RegisterResult register(RegisterRequest request)
            throws BadRequestException {
        var path = "/user";
        return this.makeRequest("POST", path, request, RegisterResult.class, null);
    }


    public LoginResult login(LoginRequest request) throws BadRequestException {
        var path = "/session";
        return this.makeRequest("POST", path, request, LoginResult.class, null);
    }

    public void logout(LogoutRequest request) throws BadRequestException {
        var path = "/session";
        this.makeRequest("DELETE", path, request, LogoutResult.class, request.authToken());
    }

    public ListGamesResult listGames(ListGamesRequest request) throws BadRequestException {
        var path = "/game";
        return this.makeRequest("GET", path, request, ListGamesResult.class, request.authToken());
    }

    public CreateGameResult createGame(CreateGameRequest request) throws BadRequestException {
        var path = "/game";
        return this.makeRequest("POST", path, request, CreateGameResult.class, request.authToken());
    }

    public JoinGameResult joinGame(JoinGameRequest request) throws BadRequestException {
        var path = "/game";
        return this.makeRequest("PUT", path, request, JoinGameResult.class, request.authToken());
    }

    public void clear() throws BadRequestException {
        var path = "/db";
        this.makeRequest("DELETE", path, null, null, null);
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String auth) throws BadRequestException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            if (request != null && request.getClass() != LoginRequest.class && request.getClass() != RegisterRequest.class) {
                http.addRequestProperty("authorization", auth);
            }
            if (Objects.equals(method, "POST") || Objects.equals(method, "PUT")) {
                http.setDoOutput(true);
                writeBody(request, http);
            }
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception ex) {
            throw new BadRequestException(ex.getMessage());
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, BadRequestException {
        int status = http.getResponseCode();
        if (status / 100 == 2) {
            try (InputStream err = http.getErrorStream()) {
                if (err != null) {
                    throw BadRequestException.fromJson(err);
                }
            }
        } else {
            if (status == 403) {
                throw new BadRequestException("Already taken.");
            }
            if (status == 401) {
                throw new BadRequestException("Wrong username or password.");
            }
            throw new BadRequestException("Other failure: " + status);
        }
    }


}
