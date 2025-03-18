package app.exception;

public class NotificationServiceFeignCallException extends RuntimeException {

    public NotificationServiceFeignCallException(String message) {
        super(message);
    }
}
