package api.exception;

public class SequenceCounterException extends CommunicationException {
    private static final long serialVersionUID = 1L;

    public SequenceCounterException(final String message) {
        super(message);
    }

    public SequenceCounterException(final Throwable throwable) {
        super(throwable);
    }

}
