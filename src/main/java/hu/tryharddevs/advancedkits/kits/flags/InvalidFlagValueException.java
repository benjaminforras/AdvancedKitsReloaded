package hu.tryharddevs.advancedkits.kits.flags;

public class InvalidFlagValueException extends Exception {
	private static final long serialVersionUID = 1997753363232807009L;

	private String[] messages;

	public InvalidFlagValueException() {}

	public InvalidFlagValueException(String message) {
		super(message);

		this.messages = new String[1];
		this.messages[0] = message;
	}

	public InvalidFlagValueException(String... messages) {
		this.messages = messages;
	}

	public InvalidFlagValueException(Throwable cause) {
		super(cause);
	}

	public InvalidFlagValueException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidFlagValueException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public String[] getMessages() {
		return this.messages;
	}
}
