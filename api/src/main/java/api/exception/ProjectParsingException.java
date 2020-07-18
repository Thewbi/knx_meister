package api.exception;

public class ProjectParsingException extends Exception {

	private static final long serialVersionUID = 1L;

	public ProjectParsingException(final String message) {
		super(message);
	}

	public ProjectParsingException(final Throwable throwable) {
		super(throwable);
	}

}
