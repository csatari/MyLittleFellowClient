package hu.jex.mylittlefellow.communicator;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.NetworkErrorException;
import android.app.Activity;
import android.content.pm.PackageInfo;

/**
 * Kommunikáló osztály, csak bejelentkeztetésre
 * @author Albert
 *
 */
public class CommunicatorUser extends CommunicatorBase {
	
	private static String table_sessionid = "sessionid";
	
	//private static String PARAM_ID = "id";
	private static String PARAM_OPERATION = "operation";
	private static String PARAM_EMAIL = "email";
	private static String PARAM_USERNAME = "username";
	private static String PARAM_PASSWORD = "password";
	private static String PARAM_VERSION = "version";
	private static String URL = URL_HOST+"authenticate.php";
	/*public CommunicatorUser() {
		super(URL,"");
	}*/
	public CommunicatorUser(Activity context) {
		super(context, URL, "");
	}
	
	/**
	 * Lekérdezi a szervertõl a megadott paraméterekkel a sessionid-t, egyúttal be is jelentkeztet
	 * @param email
	 * @param username
	 * @param password
	 * @return Sessionid
	 * @throws NetworkErrorException
	 * @throws JSONException
	 * @throws Exception
	 */
	public String getSessionId(String email,String username,String password) throws NetworkErrorException,JSONException,CommunicatorException, Exception {
		addPair(PARAM_OPERATION, "0");
		addPair(PARAM_EMAIL ,email);
		addPair(PARAM_USERNAME ,username);
		addPair(PARAM_PASSWORD ,password);
		PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		addPair(PARAM_VERSION ,pInfo.versionCode+"");
		JSONObject jObj = getSingleResponse();
		String resp = "";
		try {
			resp = jObj.getString(table_sessionid);
		}
		catch(JSONException ex) {
			throw(new CommunicatorException(jObj.getString(table_hibaId),jObj.getString(table_hibaSzoveg)));
		}
		return resp;
	}
	/**
	 * Lekérdezi, hogy online van-e a felhasználó
	 * @return Igaz, ha online van
	 */
	public boolean isOnline() {
		addPair(PARAM_OPERATION, "500");

		try {
			getSingleResponse();
		}
		catch(NetworkErrorException e) { return false; }
		catch(JSONException e) { return false;  }
		catch(CommunicatorException e) { return false;  }
		catch(Exception e) { return false;  }
		return true;
	}
	
}
