package hu.jex.mylittlefellow.communicator;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import hu.jex.mylittlefellow.model.Logger;
import hu.jex.mylittlefellow.model.ResourceStorage;
import hu.jex.mylittlefellow.model.ToolReceipt;
import hu.jex.mylittlefellow.storage.Storage;
import android.accounts.NetworkErrorException;
import android.app.Activity;

/**
 * Az eszközökkel kapcsolatos kommunikációt tartalmazza.
 * @author Albert
 *
 */
public class CommunicatorTool extends CommunicatorBase {

	private static String URL = URL_HOST+"tool.php";
	
	private static String PARAM_TOOLTYPE = "tooltype";
	
	private static String table_id = "toolid";
	private static String table_name = "name";
	private static String table_resources = "resources";
	private static String table_ipo = "ipo";
	
	private static String other_time = "time";
	private static String other_oldtime = "oldtime";
	
	public CommunicatorTool(Activity context) {
		super(context, URL, Storage.getUserSessionId(context));
	}
	/**
	 * Lekérdezi az összes kifejleszthetõ eszközt
	 * @return Az eszközreceptek tömbje
	 */
	public ArrayList<ToolReceipt> getAllDevelopableTools() {
		addPair(PARAM_OPERATION, "0");
		ArrayList<ToolReceipt> allReceipts = new ArrayList<ToolReceipt>();
		JSONArray jArray = null;
		try {
			jArray = getMultiResponse();
			if(jArray == null) return null;
			for(int i=0; i<jArray.length();i++) {
				//Logger.writeToLog(jArray.getJSONObject(i).toString());
				ToolReceipt receipt = new ToolReceipt();
				receipt.setId(jArray.getJSONObject(i).getInt(table_id));
				receipt.setName(jArray.getJSONObject(i).getString(table_name));
				receipt.setIpo(jArray.getJSONObject(i).getInt(table_ipo));
				ResourceStorage resources = new ResourceStorage();
				String res = jArray.getJSONObject(i).getString(table_resources);
				JSONObject resourceArray = new JSONObject(res);
				
				@SuppressWarnings("unchecked")
				Iterator<String> iter = resourceArray.keys();
			    while (iter.hasNext()) {
			        String key = iter.next();
			        try {
			            String value = resourceArray.get(key)+"";
			            resources.add(Integer.parseInt(key), Integer.parseInt(value));
			        } catch (JSONException e) {
			        	Logger.writeException(e);
			        }
			    }
				receipt.setResources(resources);
				allReceipts.add(receipt);
			}
			return allReceipts;
		}
		catch(NetworkErrorException e) { handleNetworkErrorException(e); }
		catch(JSONException e) { handleJSONException(e); }
		catch(CommunicatorException e) { handleCommunicatorException(e); }
		catch(Exception e) { handleException(e); }
		return null;
	}
	/**
	 * Elkezdi kifejleszteni a megadott eszközt
	 * @param toolType Az eszköz típusa
	 * @return A szükséges idõ
	 */
	public long develop(int toolType) {
		addPair(PARAM_OPERATION, "1");
		addPair(PARAM_TOOLTYPE, toolType+"");
		JSONObject jObj = null;
		try {
			jObj = getSingleResponse();
			if(jObj == null) return -1;
			long time = jObj.getLong(other_time);
			long oldtime = jObj.getLong(other_oldtime);
			return (time-oldtime)*1000;
		}
		catch(NetworkErrorException e) { handleNetworkErrorException(e); }
		catch(JSONException e) { handleJSONException(e); }
		catch(CommunicatorException e) { handleCommunicatorException(e); }
		catch(Exception e) { handleException(e); }
		return -1;
	}
	
}
