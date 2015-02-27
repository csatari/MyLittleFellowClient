package hu.jex.mylittlefellow.communicator;

import hu.jex.mylittlefellow.model.Tile;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.NetworkErrorException;
import android.app.Activity;

/**
 * A megismert területekkel kapcsolatos kommunikációt tartalmazza.
 * @author Albert
 *
 */
public class CommunicatorKnownTiles extends CommunicatorBase {

	private static String table_examined = "examined";
	private static String other_time = "time";
	private static String other_oldtime = "oldtime";
	//private static String table_tileid = "tileid";
	
	private static String PARAM_OPERATION = "operation";
	private static String PARAM_TILEID = "tileid";
	
	private static String URL = URL_HOST+"knownTiles.php";
	
	public CommunicatorKnownTiles(String sessionid) {
		super(URL, sessionid);
	}
	public CommunicatorKnownTiles(Activity context, String sessionid) {
		super(context,URL, sessionid);
	}
	
	/**
	 * Lekérdezi, hogy az adott tile meg volt-e már vizsgálva
	 * @param tile
	 * @return
	 * @throws NetworkErrorException
	 * @throws JSONException
	 * @throws Exception
	 */
	public boolean isTileExamined(Tile tile) throws NetworkErrorException, JSONException, CommunicatorException, Exception  {
		addPair(PARAM_OPERATION, "1");
		addPair(PARAM_TILEID, tile.getId()+"");
		JSONObject jObj = getSingleResponse();
		return jObj.getInt(table_examined) == 1;
	}
	
	/**
	 * Beállítja, hogy a megadott tile meg lett vizsgálva
	 * @param tile
	 * @throws NetworkErrorException
	 * @throws JSONException
	 * @throws Exception
	 */
	public void examineTile(Tile tile) throws NetworkErrorException, JSONException, CommunicatorException, Exception  {
		addPair(PARAM_OPERATION, "0");
		addPair(PARAM_TILEID, tile.getId()+"");
		getSingleResponse();
	}
	/**
	 * Megvizsgál egy területet
	 * @param tileid A terület azonosítója
	 * @return A szükséges idõ
	 */
	public long examineTileId(int tileid) {
		addPair(PARAM_OPERATION, "0");
		addPair(PARAM_TILEID, tileid+"");
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

}
