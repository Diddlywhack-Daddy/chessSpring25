package dataaccess;

import model.ErrorCode;

public class DataAccessException extends Exception {
    private final ErrorCode errorCode;

    public DataAccessException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
