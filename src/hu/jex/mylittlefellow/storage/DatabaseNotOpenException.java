package hu.jex.mylittlefellow.storage;

public class DatabaseNotOpenException extends Exception {

	/**
	 * Az adatbázis nincs nyitva - kivétel
	 */
	private static final long serialVersionUID = 1L;
	
	public DatabaseNotOpenException() {}
	
	public DatabaseNotOpenException(String message) {
		super(message);
	}

}
