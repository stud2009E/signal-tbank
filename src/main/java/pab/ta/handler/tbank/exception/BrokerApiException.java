package pab.ta.handler.tbank.exception;

public class BrokerApiException extends AppRootException{
    public BrokerApiException() {
    }

    public BrokerApiException(String message) {
        super(message);
    }

    public BrokerApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public BrokerApiException(Throwable cause) {
        super(cause);
    }

    public BrokerApiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
