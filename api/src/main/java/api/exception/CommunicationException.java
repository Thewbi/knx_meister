package api.exception;

public class CommunicationException extends Exception {

    private static final long serialVersionUID = 1L;

    public CommunicationException(final String message) {
        super(message);
    }

    public CommunicationException(final Throwable throwable) {
        super(throwable);
    }

}
