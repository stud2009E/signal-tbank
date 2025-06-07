package pab.ta.handler.tbank.exception;

public class AppRootException extends RuntimeException {

    public AppRootException() {
    }

    public AppRootException(String message) {
        super(message);
    }

    public AppRootException(String message, Throwable cause) {
        super(message, cause);
    }

    public AppRootException(Throwable cause) {
        super(cause);
    }

    public AppRootException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
