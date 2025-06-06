package backend;

import model.request.LoginRequest;
import model.request.RegisterRequest;
import model.result.LoginResult;
import server.exceptions.BadRequestException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Objects;

public class ServerFacade {

    private String serverUrl;

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public void register() {

    }

    public LoginResult login(LoginRequest request) throws BadRequestException {
        var path = "/session";
        return this.makeRequest("POST", path, request, LoginResult.class, null);
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

    private void writeBody(Object request, HttpURLConnection http) {
    }

    private <T> T readBody(HttpURLConnection http, Class<T> responseClass) {
        return null;
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
            throw new BadRequestException("other failure: " + status);
        }
    }
}


