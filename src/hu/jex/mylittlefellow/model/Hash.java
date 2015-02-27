package hu.jex.mylittlefellow.model;

import java.security.MessageDigest;
/**
 * A jelszó hash-eléséért felelõs osztály
 * @author Albert
 *
 */
public class Hash {
	/**
	 * Hasheli a megadott stringet sha256-os kódolással
	 * @param base A hashelendõ string
	 * @return A hashelt string
	 */
	public static String sha256(String base) {
	    try{
	    	MessageDigest md = MessageDigest.getInstance("SHA-256");
	    	md.reset();
	        md.update(base.getBytes("UTF-8"));
	        
	        byte byteData[] = md.digest();
	        
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < byteData.length; i++) {
	        	sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
	        }
	        md.reset();
	        return sb.toString();
	        
	    } catch(Exception ex){
	       throw new RuntimeException(ex);
	    }
	}
	
}
