package server;

public class ErrorMessage {
    private final String message;

    public ErrorMessage(String message) {
        System.out.println("[DEBUG] Creating ErrorMessage: " + message); // ✅ log the message
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
