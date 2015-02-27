package hu.jex.mylittlefellow.communicator;

/**
 * Szerver �ltal dobott hiba�zenet kiv�teloszt�lya
 * @author Albert
 *
 */
public class CommunicatorException extends Exception {
	public int hibaId;
	public String hibaSzoveg;
	/**
	 * Egy kiv�tel a megadott param�terekkel
	 * @param hibaId A hiba azonos�t�ja
	 * @param hibaSzoveg A hiba le�r�sa
	 */
	public CommunicatorException(String hibaId,String hibaSzoveg)
    {
		super(hibaId+": "+hibaSzoveg);
		this.hibaId = Integer.parseInt(hibaId);
		this.hibaSzoveg = hibaSzoveg;
    }
	private static final long serialVersionUID = 3353270863462138251L;
	
}
