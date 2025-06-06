package backend;

import com.google.gson.Gson;
import model.request.LoginRequest;
import model.request.RegisterRequest;
import model.result.LoginResult;
import server.exceptions.BadRequestException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
        // Implementation omitted
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

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
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
}
