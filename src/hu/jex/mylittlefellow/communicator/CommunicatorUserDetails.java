package hu.jex.mylittlefellow.communicator;

import hu.jex.mylittlefellow.model.Logger;
import hu.jex.mylittlefellow.model.Resource;
import hu.jex.mylittlefellow.model.ResourceStorage;
import hu.jex.mylittlefellow.model.Tile;
import hu.jex.mylittlefellow.storage.Storage;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.NetworkErrorException;
import android.app.Activity;

import com.google.android.gms.maps.model.LatLng;

/**
 * A karakterrel kapcsolatos kommunikációt tartalmazza
 * @author Albert
 *
 */
public class CommunicatorUserDetails extends CommunicatorBase {

	private static String table_id = "id";
	private static String table_lat = "latitude";
	private static String table_long = "longitude";
	private static String table_type = "type";
	private static String table_resource1 = "resource1";
	private static String table_resource2 = "resource2";
	private static String table_resource3 = "resource3";
	private static String table_owner = "owner";
	private static String table_placeid = "placeid";
	private static String table_examined = "examined";
	private static String table_homeid = "homeid";
	private static String table_storagelimit = "storagelimit";
	private static String table_taxresources = "taxresources";
	private static String table_ipo = "ipo";
	private static String other_time = "time";
	private static String other_oldtime = "oldtime";
	
	private static String PARAM_OPERATION = "operation";
	private static String PARAM_LATITUDE = "latitude";
	private static String PARAM_LONGITUDE = "longitude";
	private static String PARAM_PLACEID = "placeid";
	private static String PARAM_HOMEID = "homeid";
	private static String PARAM_TYPE = "type";
	private static String PARAM_AMOUNT = "amount";
	//private static String PARAM_STORAGELIMIT = "storagelimit";
	private static String PARAM_TILEID = "tileid";
	
	private static String URL = URL_HOST+"userDetails.php";
	
	public CommunicatorUserDetails(String sessionid) {
		super(URL, sessionid);
	}
	
	public CommunicatorUserDetails(Activity context, String sessionid) {
		super(context,URL, sessionid);
	}
	public CommunicatorUserDetails(Activity context) {
		super(context,URL, Storage.getUserSessionId(context));
	}
	
	/**
	 * Lekérdezi a felhasználó összes felfedezett tile-ját
	 * @return
	 * @throws NetworkErrorException
	 * @throws JSONException
	 * @throws Exception
	 */
	public ArrayList<Tile> getAllTiles() throws NetworkErrorException, JSONException, CommunicatorException, Exception {
		addPair(PARAM_OPERATION, "1");
		
		ArrayList<Tile> allTiles = new ArrayList<Tile>();
		JSONArray jArray = getMultiResponse();
		if(jArray == null) return null;
		for(int i=0; i<jArray.length();i++) {
			//Logger.writeToLog(jArray.getJSONObject(i).toString());
			Tile tile = new Tile();
			tile.setId(jArray.getJSONObject(i).getInt(table_id));
			tile.setTileCenterLatitude(Double.parseDouble(jArray.getJSONObject(i).getString(table_lat)));
			tile.setTileCenterLongitude(Double.parseDouble(jArray.getJSONObject(i).getString(table_long)));
			tile.setType(jArray.getJSONObject(i).getInt(table_type));
			tile.setResource1(jArray.getJSONObject(i).getInt(table_resource1));
			tile.setResource2(jArray.getJSONObject(i).getInt(table_resource2));
			tile.setResource3(jArray.getJSONObject(i).getInt(table_resource3));
			tile.setExamined(jArray.getJSONObject(i).getInt(table_examined) == 1);
			tile.setOwner(jArray.getJSONObject(i).getString(table_owner));
			allTiles.add(tile);
		}
		return allTiles;
	}
	/**
	 * Felfedez egy Tile-t, a szervertõl lekérdezi, majd vissza is adja az információkat róla
	 * @param coordinate
	 * @return A felfedezett Tile
	 * @throws NetworkErrorException
	 * @throws JSONException
	 * @throws Exception
	 */
	public Tile discoverTile(LatLng coordinate) {
		Logger.writeToLog("discoverTile("+coordinate.toString()+")");
		addPair(PARAM_OPERATION, "0");
		addPair(PARAM_LATITUDE, ""+coordinate.latitude);
		addPair(PARAM_LONGITUDE, ""+coordinate.longitude);
		JSONObject jObj = null;
		try {
			jObj = getSingleResponse();
			Tile tile = new Tile();
			tile.setId(jObj.getInt(table_id));
			tile.setTileCenterLatitude(Double.parseDouble(jObj.getString(table_lat)));
			tile.setTileCenterLongitude(Double.parseDouble(jObj.getString(table_long)));
			tile.setType(jObj.getInt(table_type));
			tile.setResource1(jObj.getInt(table_resource1));
			tile.setResource2(jObj.getInt(table_resource2));
			tile.setResource3(jObj.getInt(table_resource3));
			return tile;
		}
		catch(NetworkErrorException e) { handleNetworkErrorException(e); }
		catch(JSONException e) { handleJSONException(e); }
		catch(CommunicatorException e) { handleCommunicatorException(e); }
		catch(Exception e) { handleException(e); }
		return null;
		
	}
	/**
	 * Lekérdezi annak a tile-nak az id-jét, amelyiken van épp a karakter
	 * @return
	 * @throws NetworkErrorException
	 * @throws JSONException
	 * @throws Exception
	 */
	public int getPlaceId() {
		addPair(PARAM_OPERATION, "2");
		try {
			JSONObject jObj = getSingleResponse();
			return jObj.getInt(table_placeid);
		}
		catch(NetworkErrorException e) { handleNetworkErrorException(e); }
		catch(JSONException e) { handleJSONException(e); }
		catch(CommunicatorException e) { handleCommunicatorException(e); }
		catch(Exception e) { handleException(e); }
		return 0;
	}
	
	/**
	 * Beállítja, hogy hol melyik tile-on van a karakter
	 * @param placeId A tile id-je
	 * @return
	 * @throws NetworkErrorException
	 * @throws JSONException
	 * @throws Exception
	 */
	public long setPlaceId(int placeId) {
		addPair(PARAM_OPERATION, "7");
		addPair(PARAM_PLACEID, placeId+"");
		try {
			JSONObject jObj = getSingleResponse();
			long time = jObj.getLong(other_time);
			long oldtime = jObj.getLong(other_oldtime);
			//Storage.setTimedActionTime(context, (time-oldtime)*1000);
			return (time-oldtime)*1000;
		}
		catch(NetworkErrorException e) { handleNetworkErrorException(e); }
		catch(JSONException e) { handleJSONException(e); }
		catch(CommunicatorException e) { handleCommunicatorException(e); }
		catch(Exception e) { handleException(e); }
		return 0;
	}
	
	
	/**
	 * Lekérdezi a karakter lakhelyét 
	 * @return
	 * @throws NetworkErrorException
	 * @throws JSONException
	 * @throws CommunicatorException
	 * @throws Exception
	 */
	public int getHomeId() throws NetworkErrorException, JSONException, CommunicatorException, Exception  {
		addPair(PARAM_OPERATION, "3");
		JSONObject jObj = getSingleResponse();
		return jObj.getInt(table_homeid);
	}
	
	/**
	 * Beállítja a karakter lakhelyét
	 * @param placeId
	 * @throws NetworkErrorException
	 * @throws JSONException
	 * @throws CommunicatorException
	 * @throws Exception
	 */
	public long setHomeId(int placeId) {
		/*addPair(PARAM_OPERATION, "6");
		addPair(PARAM_HOMEID, placeId+"");
		Logger.writeToLog("setHomeId: "+placeId);
		getSingleResponse();*/
		addPair(PARAM_OPERATION, "8");
		addPair(PARAM_HOMEID, placeId+"");
		try {
			JSONObject jObj = getSingleResponse();
			long time = jObj.getLong(other_time);
			long oldtime = jObj.getLong(other_oldtime);
			return (time-oldtime)*1000;
		}
		catch(NetworkErrorException e) { handleNetworkErrorException(e); }
		catch(JSONException e) { handleJSONException(e); }
		catch(CommunicatorException e) { handleCommunicatorException(e); }
		catch(Exception e) { handleException(e); }
		return 0;
	}
	
	/**
	 * Beállítja a karakterhez tartozó raktár limitjét
	 * @param limit
	 * @throws NetworkErrorException
	 * @throws JSONException
	 * @throws CommunicatorException
	 * @throws Exception
	 */
	/*public void setStorageLimit(int limit) {
		addPair(PARAM_OPERATION, "8");
		addPair(PARAM_STORAGELIMIT, limit+"");
		try {
			getSingleResponse();
		}
		catch(NetworkErrorException e) { handleNetworkErrorException(e); }
		catch(JSONException e) { handleJSONException(e); }
		catch(CommunicatorException e) { handleCommunicatorException(e); }
		catch(Exception e) { handleException(e); }
	}*/
	/**
	 * Lekérdezi a felhasználó raktárának a méretét.
	 * @return A raktár mérete
	 */
	public int getStorageLimit() {
		addPair(PARAM_OPERATION, "4");
		try {
			JSONObject jObj = getSingleResponse();
			return jObj.getInt(table_storagelimit);
		}
		catch(NetworkErrorException e) { handleNetworkErrorException(e); }
		catch(JSONException e) { handleJSONException(e); }
		catch(CommunicatorException e) { handleCommunicatorException(e); }
		catch(Exception e) { handleException(e); }
		return 0;
	}
	/**
	 * Lekérdezi a raktárban található összes nyersanyagot
	 * @return Az összes nyersanyag
	 */
	public ResourceStorage getAllStorage() {
		addPair(PARAM_OPERATION, "5");
		
		ResourceStorage allResources = new ResourceStorage();
		try {
			JSONObject jObj = getSingleResponse();
			if(jObj == null) return null;
			@SuppressWarnings("unchecked")
			Iterator<String> iter = jObj.keys();
		    while (iter.hasNext()) {
		        String key = iter.next();
	            String value = jObj.get(key)+"";
	            //Logger.writeToLog("key: "+key+" "+value);
	            allResources.add(Integer.parseInt(key),Integer.parseInt(value));
		    }
		}
		catch(NetworkErrorException e) { handleNetworkErrorException(e); }
		catch(JSONException e) { handleJSONException(e); }
		catch(CommunicatorException e) { handleCommunicatorException(e); }
		catch(Exception e) { handleException(e); }
		return allResources;
	}
	
	/*public Resource getOneResource(int type) throws NetworkErrorException, JSONException, CommunicatorException, Exception {
		addPair(PARAM_OPERATION, "11");
		addPair(PARAM_TYPE, type+"");
		JSONObject jObj = getSingleResponse();
		Resource res = new Resource();
		res.setId(type);
		res.setAmount(jObj.getInt(table_type));
		return res;
	}*/
	/**
	 * Beállítja a raktárban a megadott nyersanyagot
	 * @param resource A frissítendõ nyersanyag
	 */
	public void setResource(Resource resource) {
		addPair(PARAM_OPERATION, "6");
		addPair(PARAM_TYPE, resource.getId()+"");
		addPair(PARAM_AMOUNT, resource.getAmount()+"");
		//Logger.writeToLog("setResource: "+resource.toString());
		try {
			getSingleResponse();
		}
		catch(NetworkErrorException e) { handleNetworkErrorException(e); }
		catch(JSONException e) { handleJSONException(e); }
		catch(CommunicatorException e) { handleCommunicatorException(e); }
		catch(Exception e) { handleException(e); }
	}
	/**
	 * Lekérdezi, hogy tele van-e a raktár
	 * @return Igaz, ha tele van
	 */
	public boolean isStorageFull()   {
		addPair(PARAM_OPERATION, "9");
		try {
			JSONObject jObj = getSingleResponse();
			boolean isFull = jObj.getInt(table_storagelimit) == 1;
			return isFull;
		}
		catch(NetworkErrorException e) { handleNetworkErrorException(e); }
		catch(JSONException e) { handleJSONException(e); }
		catch(CommunicatorException e) { handleCommunicatorException(e); }
		catch(Exception e) { handleException(e); }
		return false;
	}
	/**
	 * Elkezdi kitermelni az elsõ nyersanyagot
	 * @param type A nyersanyag típusa
	 * @param placeId A terület azonosítója
	 * @return A szükséges idõ
	 */
	public long setMineResource1(int type,int placeId) {
		addPair(PARAM_OPERATION, "10");
		addPair(PARAM_PLACEID, placeId+"");
		addPair(PARAM_TYPE, type+"");
		try {
			JSONObject jObj = getSingleResponse();
			long time = jObj.getLong(other_time);
			long oldtime = jObj.getLong(other_oldtime);
			return (time-oldtime)*1000;
		}
		catch(NetworkErrorException e) { handleNetworkErrorException(e); }
		catch(JSONException e) { handleJSONException(e); }
		catch(CommunicatorException e) { handleCommunicatorException(e); }
		catch(Exception e) { handleException(e); }
		return 0;
	}
	/**
	 * Elkezdi kitermelni a második nyersanyagot
	 * @param type A nyersanyag típusa
	 * @param placeId A terület azonosítója
	 * @return A szükséges idõ
	 */
	public long setMineResource2(int type,int placeId) {
		addPair(PARAM_OPERATION, "11");
		addPair(PARAM_PLACEID, placeId+"");
		addPair(PARAM_TYPE, type+"");
		try {
			JSONObject jObj = getSingleResponse();
			long time = jObj.getLong(other_time);
			long oldtime = jObj.getLong(other_oldtime);
			return (time-oldtime)*1000;
		}
		catch(NetworkErrorException e) { handleNetworkErrorException(e); }
		catch(JSONException e) { handleJSONException(e); }
		catch(CommunicatorException e) { handleCommunicatorException(e); }
		catch(Exception e) { handleException(e); }
		return 0;
	}
	/**
	 * Elkezdi kitermelni a harmadik nyersanyagot
	 * @param type A nyersanyag típusa
	 * @param placeId A terület azonosítója
	 * @return A szükséges idõ
	 */
	public long setMineResource3(int type,int placeId) {
		addPair(PARAM_OPERATION, "12");
		addPair(PARAM_PLACEID, placeId+"");
		addPair(PARAM_TYPE, type+"");
		try {
			JSONObject jObj = getSingleResponse();
			long time = jObj.getLong(other_time);
			long oldtime = jObj.getLong(other_oldtime);
			return (time-oldtime)*1000;
		}
		catch(NetworkErrorException e) { handleNetworkErrorException(e); }
		catch(JSONException e) { handleJSONException(e); }
		catch(CommunicatorException e) { handleCommunicatorException(e); }
		catch(Exception e) { handleException(e); }
		return 0;
	}
	/**
	 * Lekérdezi az adóban beállított nyersanyagokat
	 * @return A beállított nyersanyagok
	 */
	public ResourceStorage getTaxResources() {
		addPair(PARAM_OPERATION, "13");
		JSONObject jObj = null;
		try {
			jObj = getSingleResponse();
			if(jObj == null) return null;
			ResourceStorage resources = new ResourceStorage();
			String res = jObj.getString(table_taxresources);
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
			return resources;
		}
		catch(NetworkErrorException e) { handleNetworkErrorException(e); }
		catch(JSONException e) { handleJSONException(e); }
		catch(CommunicatorException e) { handleCommunicatorException(e); }
		catch(Exception e) { handleException(e); }
		return null;
	}
	/**
	 * Beállítja az adóban az adott nyersanyag értékét
	 * @param type A nyersanyag típusa
	 * @param amount A nyersanyag mennyisége
	 */
	public void setTaxResource(int type,int amount) {
		addPair(PARAM_OPERATION, "14");
		addPair(PARAM_TYPE, type+"");
		addPair(PARAM_AMOUNT, amount+"");
		try {
			getSingleResponse();
		}
		catch(NetworkErrorException e) { handleNetworkErrorException(e); }
		catch(JSONException e) { handleJSONException(e); }
		catch(CommunicatorException e) { handleCommunicatorException(e); }
		catch(Exception e) { handleException(e); }
	}
	/**
	 * Befizeti az adót.
	 * @param tileid A terület, amin adót fizet
	 */
	public void payTax(int tileid) {
		addPair(PARAM_OPERATION, "15");
		addPair(PARAM_TILEID ,""+tileid);
		try {
			getSingleResponse();
		}
		catch(NetworkErrorException e) { handleNetworkErrorException(e); }
		catch(JSONException e) { handleJSONException(e); }
		catch(CommunicatorException e) { handleCommunicatorException(e); }
		catch(Exception e) { handleException(e); }
	}
	/**
	 * Lekérdezi az intelligenciapontok számát.
	 * @return Az intelligenciapontok száma
	 */
	public int getIpo() {
		addPair(PARAM_OPERATION, "16");
		try {
			JSONObject jObj = getSingleResponse();
			return jObj.getInt(table_ipo);
		}
		catch(NetworkErrorException e) { handleNetworkErrorException(e); }
		catch(JSONException e) { handleJSONException(e); }
		catch(CommunicatorException e) { handleCommunicatorException(e); }
		catch(Exception e) { handleException(e); }
		return 0;
	}
	
}
