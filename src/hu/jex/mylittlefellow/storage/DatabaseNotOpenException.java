package hu.jex.mylittlefellow.storage;

public class DatabaseNotOpenException extends Exception {

	/**
	 * Az adatb�zis nincs nyitva - kiv�tel
	 */
	private static final long serialVersionUID = 1L;
	
	public DatabaseNotOpenException() {}
	
	public DatabaseNotOpenException(String message) {
		super(message);
	}

}
