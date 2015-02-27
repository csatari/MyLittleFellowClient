package hu.jex.mylittlefellow.communicator;

import java.util.Iterator;

import hu.jex.mylittlefellow.model.Logger;
import hu.jex.mylittlefellow.model.ResourceStorage;
import hu.jex.mylittlefellow.model.Tile;
import hu.jex.mylittlefellow.storage.Storage;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.NetworkErrorException;
import android.app.Activity;

/**
 * A területekkel kapcsolatos kommunikációt tartalmazza
 * @author Albert
 *
 */
public class CommunicatorTile extends CommunicatorBase {
	//private static String table_title = "tile";
	private static String table_id = "id";
	private static String table_lat = "latitude";
	private static String table_long = "longitude";
	private static String table_type = "type";
	private static String table_resource1 = "resource1";
	private static String table_resource2 = "resource2";
	private static String table_resource3 = "resource3";
	private static String table_examined = "examined";
	private static String table_owner = "owner";
	private static String table_tax = "tax";
	private static String table_population = "population";
	
	
	private static String PARAM_ID = "id";
	private static String PARAM_OPERATION = "operation";
	//private static String PARAM_LAT = "latitude";
	//private static String PARAM_LONG = "longitude";
	private static String PARAM_RES1 = "resource1";
	private static String PARAM_RES2 = "resource2";
	private static String PARAM_RES3 = "resource3";
	
	private static String URL = URL_HOST+"tile.php";
	//private static String URL = "http://178.48.133.195:7776/mylittlefellow/tile.php";
	public CommunicatorTile(String sessionid) {
		super(URL,sessionid);
	}
	public CommunicatorTile(Activity context, String sessionid) {
		super(context,URL,sessionid);
	}
	public CommunicatorTile(Activity context) {
		super(context,URL,Storage.getUserSessionId(context));
	}
	/**
	 * Lekérdezi egy tile adatait megadott id szerint
	 * @param id
	 * @return
	 * @throws NetworkErrorException
	 * @throws JSONException
	 * @throws Exception
	 */
	public Tile getTileById(int id) {
		addPair(PARAM_OPERATION, "1");
		addPair(PARAM_ID ,""+id);
		try {
			JSONObject jObj = getSingleResponse();
			Tile tile = new Tile();
			tile.setId(jObj.getInt(table_id));
			tile.setTileCenterLatitude(Double.parseDouble(jObj.getString(table_lat)));
			tile.setTileCenterLongitude(Double.parseDouble(jObj.getString(table_long)));
			tile.setType(jObj.getInt(table_type));
			tile.setResource1(jObj.getInt(table_resource1));
			tile.setResource2(jObj.getInt(table_resource2));
			tile.setResource3(jObj.getInt(table_resource3));
			tile.setExamined(jObj.getInt(table_examined) == 1);
			tile.setOwner(jObj.getString(table_owner));
			//Logger.writeToLog("set population: "+jObj.getInt(table_population));
			tile.setPopulation(jObj.getInt(table_population));
			Logger.writeToLog("population is: "+tile.getPopulation());
			String res = jObj.getString(table_tax);
			if(!res.equals("")) {
				JSONObject resourceArray = new JSONObject(res);
				ResourceStorage resources = new ResourceStorage();
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
			    tile.setTax(resources);
			}
			
			return tile;
		}
		catch(NetworkErrorException e) { handleNetworkErrorException(e); }
		catch(JSONException e) { handleJSONException(e); }
		catch(CommunicatorException e) { handleCommunicatorException(e); }
		catch(Exception e) { handleException(e); }
		return null;
	}
	/**
	 * Beállítja a terület nyersanyagait
	 * @param tile a frissítendõ terület
	 */
	public void setTileResources(Tile tile) {
		addPair(PARAM_OPERATION, "0");
		addPair(PARAM_ID ,""+tile.getId());
		addPair(PARAM_RES1 ,""+tile.getResource1());
		addPair(PARAM_RES2 ,""+tile.getResource2());
		addPair(PARAM_RES3 ,""+tile.getResource3());
		try {
			getSingleResponse();
		}
		catch(NetworkErrorException e) { handleNetworkErrorException(e); }
		catch(JSONException e) { handleJSONException(e); }
		catch(CommunicatorException e) { handleCommunicatorException(e); }
		catch(Exception e) { handleException(e); }
	}
	/**
	 * Lekérdezi a területrõl, hogy a karakter a földesura-e
	 * @param tileid A terület azonosítója
	 * @return Igaz, ha õ a földesúr, egyébként hamis
	 */
	public boolean amIOwner(int tileid) {
		addPair(PARAM_OPERATION, "3");
		addPair(PARAM_ID ,""+tileid);
		try {
			JSONObject jObj = getSingleResponse();
			return jObj.getInt(table_owner) == 1;
		}
		catch(NetworkErrorException e) { handleNetworkErrorException(e); }
		catch(JSONException e) { handleJSONException(e); }
		catch(CommunicatorException e) { handleCommunicatorException(e); }
		catch(Exception e) { handleException(e); }
		return false;
	}
	
	
	/*public ArrayList<Tile> getAllTiles() throws NetworkErrorException,JSONException,Exception {
		ArrayList<Tile> allTiles = new ArrayList<Tile>();
		addPair(PARAM_OPERATION, "1");
		JSONArray jArray = getMultiResponse();
		for(int i=0; i<jArray.length();i++) {
			//Logger.writeToLog(jArray.getJSONObject(i).toString());
			Tile tile = new Tile();
			tile.setId(jArray.getJSONObject(i).getInt(table_id));
			tile.setTileCenterLatitude(Double.parseDouble(jArray.getJSONObject(i).getString(table_lat)));
			tile.setTileCenterLongitude(Double.parseDouble(jArray.getJSONObject(i).getString(table_long)));
			tile.setType(jArray.getJSONObject(i).getInt(table_type));
			tile.setResources(Integer.parseInt(jArray.getJSONObject(i).getString(table_resources)));
			allTiles.add(tile);
		}
		return allTiles;
	}*/
}
