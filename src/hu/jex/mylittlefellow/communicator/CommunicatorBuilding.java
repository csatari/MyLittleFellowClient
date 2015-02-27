package hu.jex.mylittlefellow.communicator;

import hu.jex.mylittlefellow.model.Building;
import hu.jex.mylittlefellow.model.BuildingReceipt;
import hu.jex.mylittlefellow.model.Logger;
import hu.jex.mylittlefellow.model.ResourceStorage;
import hu.jex.mylittlefellow.storage.Storage;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.NetworkErrorException;
import android.app.Activity;

/**
 * Az építésekkel kapcsolatos kommunikációt tartalmazza.
 * @author Albert
 *
 */
public class CommunicatorBuilding extends CommunicatorBase {

	private static String URL = URL_HOST+"building.php";
	
	private static String PARAM_TILEID = "tileid";
	private static String PARAM_SLICEID = "tileslice";
	private static String PARAM_BUILDINGTYPE = "buildingtype";
	private static String PARAM_BUILDINGLEVEL = "buildinglevel";
	
	private static String table_id = "id";
	private static String table_tileid = "tileid";
	private static String table_sliceid = "tilesliceid";
	private static String table_type = "buildingtype";
	private static String table_typeid = "buildingid";
	private static String table_level = "buildinglevel";
	private static String table_level2 = "level";
	private static String table_finished = "finished";
	private static String table_resources = "resources";
	private static String table_name = "name";
	private static String table_ipo = "ipo";
	
	private static String other_time = "time";
	private static String other_oldtime = "oldtime";
	
	public CommunicatorBuilding(Activity context, String sessionid) {
		super(context, URL, sessionid);
	}
	public CommunicatorBuilding(Activity context) {
		super(context, URL, Storage.getUserSessionId(context));
	}
	/**
	 * Lekéri az összes karakter által épített épületet
	 * @return Az épületek tömbje
	 */
	public ArrayList<Building> getAllBuildings() {
		addPair(PARAM_OPERATION, "2");
		
		ArrayList<Building> allBuildings = new ArrayList<Building>();
		JSONArray jArray = null;
		try {
			jArray = getMultiResponse();
			if(jArray == null) return null;
			for(int i=0; i<jArray.length();i++) {
				//Logger.writeToLog(jArray.getJSONObject(i).toString());
				Building building = new Building();
				building.setId(jArray.getJSONObject(i).getInt(table_id));
				building.setTileId(jArray.getJSONObject(i).getInt(table_tileid));
				building.setSliceId(jArray.getJSONObject(i).getInt(table_sliceid));
				building.setType(jArray.getJSONObject(i).getInt(table_type));
				building.setLevel(jArray.getJSONObject(i).getInt(table_level));
				building.setFinished(jArray.getJSONObject(i).getInt(table_finished) == 1);
				allBuildings.add(building);
			}
			return allBuildings;
		}
		catch(NetworkErrorException e) { handleNetworkErrorException(e); }
		catch(JSONException e) { handleJSONException(e); }
		catch(CommunicatorException e) { handleCommunicatorException(e); }
		catch(Exception e) { handleException(e); }
		return null;
	}
	/**
	 * Lekéri az összes építhetõ épületet a megadott területen
	 * @param tileId a terület azonosítója
	 * @param sliceId a területrészlet: 0-9-ig lehet, 0 a bal felsõ, 9 a jobb alsó
	 * @return Az épületreceptek tömbje
	 */
	public ArrayList<BuildingReceipt> getAllBuildables(int tileId, int sliceId) {
		addPair(PARAM_OPERATION, "1");
		addPair(PARAM_TILEID, tileId+"");
		addPair(PARAM_SLICEID, sliceId+"");
		ArrayList<BuildingReceipt> allReceipts = new ArrayList<BuildingReceipt>();
		JSONArray jArray = null;
		try {
			jArray = getMultiResponse();
			if(jArray == null) return null;
			for(int i=0; i<jArray.length();i++) {
				//Logger.writeToLog(jArray.getJSONObject(i).toString());
				BuildingReceipt receipt = new BuildingReceipt();
				receipt.setType(jArray.getJSONObject(i).getInt(table_type));
				receipt.setLevel(jArray.getJSONObject(i).getInt(table_level));
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
	 * Elkezd építeni egy épületet a megadott paramétereken
	 * @param tile a terület azonosítója
	 * @param slice a területrészlet azonosítója: 0-9-ig lehet, 0 a bal felsõ, 9 a jobb alsó
	 * @param buildingType az épület típusa
	 * @return A szükséges idõ
	 */
	public long build(int tile, int slice, int buildingType) {
		addPair(PARAM_OPERATION, "0");
		addPair(PARAM_TILEID, tile+"");
		addPair(PARAM_SLICEID, slice+"");
		addPair(PARAM_BUILDINGTYPE, buildingType+"");
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
	/**
	 * Elkezdi kifejleszteni a megadott épületet
	 * @param buildingType az épület típusa
	 * @param buildingLevel az épület szintje
	 * @return A szükséges idõ
	 */
	public long develop(int buildingType, int buildingLevel) {
		addPair(PARAM_OPERATION, "4");
		addPair(PARAM_BUILDINGTYPE, buildingType+"");
		addPair(PARAM_BUILDINGLEVEL, buildingLevel+"");
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
	/**
	 * Lekéri az összes kifejleszthetõ épület listáját
	 * @return Az épületreceptek tömbje
	 */
	public ArrayList<BuildingReceipt> getAllDevelopableBuildings() {
		addPair(PARAM_OPERATION, "3");
		ArrayList<BuildingReceipt> allReceipts = new ArrayList<BuildingReceipt>();
		JSONArray jArray = null;
		try {
			jArray = getMultiResponse();
			if(jArray == null) return null;
			for(int i=0; i<jArray.length();i++) {
				//Logger.writeToLog(jArray.getJSONObject(i).toString());
				BuildingReceipt receipt = new BuildingReceipt();
				receipt.setType(jArray.getJSONObject(i).getInt(table_typeid));
				receipt.setLevel(jArray.getJSONObject(i).getInt(table_level2));
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
}
