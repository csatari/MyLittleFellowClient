package hu.jex.mylittlefellow.communicator;

import hu.jex.mylittlefellow.model.TimedAction;
import hu.jex.mylittlefellow.storage.Storage;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.NetworkErrorException;
import android.app.Activity;

/**
 * Az idõigényes interakcióval kapcsolatos kommunikációt tartalmazza
 * @author Albert
 *
 */
public class CommunicatorTimedAction extends CommunicatorBase {

	private static String other_result = "result";
	private static String other_goal = "goal";
	private static String table_oldtime = "oldtime";
	private static String table_newtime = "newtime";
	private static String table_actionid = "actionid";
	
	private static String PARAM_OPERATION = "operation";
	
	private static String URL = URL_HOST+"timedAction.php";
	
	public CommunicatorTimedAction(String sessionid) {
		super(URL, sessionid);
	}
	
	public CommunicatorTimedAction(Activity context, String sessionid) {
		super(context,URL, sessionid);
	}
	public CommunicatorTimedAction(Activity context) {
		super(context,URL, Storage.getUserSessionId(context));
	}
	/**
	 * Akkor kell meghívni, amikor a kliensen letelt az idõ
	 * @return Igaz, ha tényleg letelt az idõ
	 */
	public boolean refreshData() {
		addPair(PARAM_OPERATION, "1");
		try {
			JSONObject jObj = getSingleResponse();
			int res = jObj.getInt(other_result);
			return res == 1;
		}
		catch(NetworkErrorException e) { handleNetworkErrorException(e); }
		catch(JSONException e) { handleJSONException(e); }
		catch(CommunicatorException e) { handleCommunicatorException(e); }
		catch(Exception e) { handleException(e); }
		return false;
	}
	/**
	 * Lekérdezi a legutolsó interakció adatait
	 * @return Igaz, ha sikerült lekérdezni
	 */
	public boolean getData() {
		addPair(PARAM_OPERATION, "0");
		try {
			JSONObject jObj = getSingleResponse();
			long oldtime = jObj.getLong(table_oldtime)*1000;
			long newtime = jObj.getLong(table_newtime)*1000;
			int actionid = jObj.getInt(table_actionid);
			int goal = jObj.getInt(other_goal);
			//long timedif = (newtime-oldtime)*1000;
			TimedAction timedAction = new TimedAction(context, actionid, goal);
			timedAction.setStartTime(oldtime);
			timedAction.setEndTime(newtime);
			timedAction.save(context);
			return true;
		}
		catch(NetworkErrorException e) { handleNetworkErrorException(e); }
		catch(JSONException e) { handleJSONException(e); }
		catch(CommunicatorException e) { handleCommunicatorException(e); }
		catch(Exception e) { handleException(e); }
		return false;
	}
	/**
	 * Leállítja az interakciót.
	 */
	public void stopAction() {
		addPair(PARAM_OPERATION, "2");
		try {
			getSingleResponse();
			return;
		}
		catch(NetworkErrorException e) { handleNetworkErrorException(e); }
		catch(JSONException e) { handleJSONException(e); }
		catch(CommunicatorException e) { handleCommunicatorException(e); }
		catch(Exception e) { handleException(e); }
		return;
	}
}
