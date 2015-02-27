package hu.jex.mylittlefellow.communicator;

import hu.jex.mylittlefellow.model.Ipo;
import hu.jex.mylittlefellow.storage.Storage;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.accounts.NetworkErrorException;
import android.app.Activity;

import com.google.android.gms.maps.model.LatLng;

/**
 * A látványosságokkal kapcsolatos kommunikációt tartalmazza.
 * @author Albert
 *
 */
public class CommunicatorIpo extends CommunicatorBase {

	private static String URL = URL_HOST+"ipo.php";
	
	private static String PARAM_LATFROM = "latfrom";
	private static String PARAM_LATTO = "latto";
	private static String PARAM_LONFROM = "lonfrom";
	private static String PARAM_LONTO = "lonto";
	private static String PARAM_IPOID = "ipoid";
	
	private static String table_id = "id";
	private static String table_lat = "lat";
	private static String table_lon = "lon";
	private static String table_farlat = "farlat";
	private static String table_farlon = "farlon";
	private static String table_name = "name";
	private static String table_known = "known";
	private static String table_url = "url";
	
	
	
	public CommunicatorIpo(Activity context) {
		super(context, URL, Storage.getUserSessionId(context));
	}
	/**
	 * Lekérdezi az összes paraméterbe foglalt látványosságot
	 * @param from A délnyugati pont, amitõl lekérdez
	 * @param to Az északkeleti pont, ameddig lekérdez
	 * @return Látványosságok listája
	 */
	public ArrayList<Ipo> getIpos(LatLng from, LatLng to) {
		addPair(PARAM_OPERATION, "1");
		addPair(PARAM_LATFROM, from.latitude+"");
		addPair(PARAM_LATTO, to.latitude+"");
		addPair(PARAM_LONFROM, from.longitude+"");
		addPair(PARAM_LONTO, to.longitude+"");
		
		ArrayList<Ipo> allIpos = new ArrayList<Ipo>();
		JSONArray jArray = null;
		try {
			jArray = getMultiResponse();
			if(jArray == null) return null;
			for(int i=0; i<jArray.length();i++) {
				//Logger.writeToLog(jArray.getJSONObject(i).toString());
				Ipo ipo = new Ipo();
				ipo.setId(jArray.getJSONObject(i).getInt(table_id));
				ipo.setLatitude(jArray.getJSONObject(i).getDouble(table_lat));
				ipo.setLongitude(jArray.getJSONObject(i).getDouble(table_lon));
				ipo.setName(jArray.getJSONObject(i).getString(table_name));
				ipo.setUrl(jArray.getJSONObject(i).getString(table_url));
				ipo.setKnown(jArray.getJSONObject(i).getInt(table_known) == 1);
				if(!jArray.getJSONObject(i).isNull(table_farlat)) {
					ipo.setRadiusFromCoordinate(new LatLng(
							jArray.getJSONObject(i).getDouble(table_farlat),
							jArray.getJSONObject(i).getDouble(table_farlon)
					));
				}
				else {
					ipo.setRadius(0);
				}
				
				allIpos.add(ipo);
			}
			return allIpos;
		}
		catch(NetworkErrorException e) { handleNetworkErrorException(e); }
		catch(JSONException e) { handleJSONException(e); }
		catch(CommunicatorException e) { handleCommunicatorException(e); }
		catch(Exception e) { handleException(e); }
		return null;
	}
	/**
	 * Felfedez egy látványosságot
	 * @param ipo A látványosság
	 */
	public void discoverIpo(Ipo ipo) {
		addPair(PARAM_OPERATION, "2");
		addPair(PARAM_IPOID, ipo.getId()+"");
		try {
			getSingleResponse();
		}
		catch(NetworkErrorException e) { handleNetworkErrorException(e); }
		catch(JSONException e) { handleJSONException(e); }
		catch(CommunicatorException e) { handleCommunicatorException(e); }
		catch(Exception e) { handleException(e); }
	}

}
