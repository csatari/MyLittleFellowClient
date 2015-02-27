package hu.jex.mylittlefellow.communicator;

/**
 * Szerver által dobott hibaüzenet kivételosztálya
 * @author Albert
 *
 */
public class CommunicatorException extends Exception {
	public int hibaId;
	public String hibaSzoveg;
	/**
	 * Egy kivétel a megadott paraméterekkel
	 * @param hibaId A hiba azonosítója
	 * @param hibaSzoveg A hiba leírása
	 */
	public CommunicatorException(String hibaId,String hibaSzoveg)
    {
		super(hibaId+": "+hibaSzoveg);
		this.hibaId = Integer.parseInt(hibaId);
		this.hibaSzoveg = hibaSzoveg;
    }
	private static final long serialVersionUID = 3353270863462138251L;
	
}
