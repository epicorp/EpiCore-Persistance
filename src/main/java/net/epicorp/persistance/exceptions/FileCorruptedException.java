package net.epicorp.persistance.exceptions;

/**
 * this is thrown when a chunk file has been tampered with incorrectly
 */
public class FileCorruptedException extends RuntimeException {
	public FileCorruptedException() {
	}

	public FileCorruptedException(String message) {
		super(message);
	}

	public FileCorruptedException(String message, Throwable cause) {
		super(message, cause);
	}

	public FileCorruptedException(Throwable cause) {
		super(cause);
	}

	public FileCorruptedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
