package hu.jex.mylittlefellow.communicator;

import hu.jex.mylittlefellow.gui.InformationDialog;
import hu.jex.mylittlefellow.gui.LoginActivity;
import hu.jex.mylittlefellow.model.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.NetworkErrorException;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;

/**
 * A kommunik�l� oszt�lyok �se
 * @author Albert
 *
 */
public abstract class CommunicatorBase {
	protected ArrayList<NameValuePair> nameValueList;
	protected static String table_hibaSzoveg = "hiba";
	protected static String table_hibaId = "hibaid";
	
	protected static String PARAM_OPERATION = "operation";
	
	private static boolean hibaDialog = false;
	
	
	protected Activity context;
	protected boolean problem = false;
	
	@SuppressWarnings("unused")
	private static boolean busy;
	
	protected static String URL_HOST = "http://csatari64.web.elte.hu/mlf/";
	//protected static String URL_HOST = "http://178.48.133.195:7776/mlf/";
	public CommunicatorBase(String url,String sessionid) {
		nameValueList = new ArrayList<NameValuePair>();
		busy = false;
		nameValueList.add(new BasicNameValuePair("url", url));
		nameValueList.add(new BasicNameValuePair("sessionid", sessionid));
	}
	public CommunicatorBase(Activity context, String url,String sessionid) {
		this.context = context;
		nameValueList = new ArrayList<NameValuePair>();
		busy = false;
		nameValueList.add(new BasicNameValuePair("url", url));
		nameValueList.add(new BasicNameValuePair("sessionid", sessionid));
	}
	/**
	 * Megh�vja a szervert a megadott param�terekkel, majd vissza is t�r vel�k �s adja tov�bb az eredm�nyt JSON nyers stringk�nt
	 * @param nameValueList
	 * @return
	 * @throws NetworkErrorException
	 * @throws RuntimeException
	 * @throws JSONException
	 * @throws CommunicatorException
	 * @throws Exception
	 */
	private String getResponse(ArrayList<NameValuePair> nameValueList) throws NetworkErrorException,RuntimeException,JSONException,CommunicatorException,Exception {
		String result = "";
		InputStream is = null;
		//while(busy) { 
			//addig v�r, am�g az el�z� szerverh�v�s be nem fejez�d�tt
		//}
		try{
			busy = true;
			HttpParams httpParams = new BasicHttpParams();
			HttpProtocolParams.setContentCharset(httpParams, "utf-8");
			HttpConnectionParams.setConnectionTimeout(httpParams, 10000);

			HttpConnectionParams.setSoTimeout(httpParams, 10000);
			
            HttpClient httpclient = new DefaultHttpClient(httpParams);
            String url = "";
            for(NameValuePair nvp : nameValueList) {
            	if(nvp.getName().compareTo("url") == 0) {
            		url = nvp.getValue();
            	}
            }
            
            HttpPost httppost = new HttpPost(url);
            Logger.writeToLog("Lek�rdez�s a szervert�l: " + nameValueList.toString());
            httppost.setEntity(new UrlEncodedFormEntity(nameValueList));
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
            busy = false;
		} catch ( UnknownHostException e) {
			busy = false;
			throw new CommunicatorException("1000", "Server is unreachable!");
		} catch(HttpHostConnectException e) {
			busy = false;
			throw new CommunicatorException("1000", "Server is unreachable!");
		} catch(SocketTimeoutException e) {
			busy = false;
			throw new CommunicatorException("1000", "Server is unreachable!");
        }catch(Exception e){
        	busy = false;
        	e.printStackTrace();
        	throw new NetworkErrorException();
        }
		
		try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"),8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            result=sb.toString();
        }catch(Exception e){
        	throw new RuntimeException(e.toString());
        }
		Logger.writeToLog("V�lasz a szervert�l: '"+result.trim()+"' "+result.toString());
        if(result.trim().compareTo("null") == 0) {
        	//Logger.writeToLog("!result: '"+result.trim()+"'");
        	return null;
        }
        return result;
	}
	/**
	 * Hozz�ad egy param�tert a h�v�shoz
	 * @param name a param�ter neve
	 * @param value a param�ter �rt�ke
	 */
	protected void addPair(String name, String value) {
		boolean exists = false;
		int index = 0;
		if(name.equals(PARAM_OPERATION)) {
			for(NameValuePair nvp : nameValueList) {
				if(nvp.getName().equals(PARAM_OPERATION)) {
					exists = true;
					break;
				}
				index++;
			}
		}
		if(exists) {
			nameValueList.remove(index);
		}
		nameValueList.add(new BasicNameValuePair(name, value));
	}
	/**
	 * Lekezeli a szerver �ltal dobott hib�t, �s ha nincs, akkor tov�bbadja a kapott kulcs-�rt�k p�rokat (JSONObject), amit kapott a szervert�l.
	 * Csak kulcs-�rt�k p�rokat tud kezelni, a parse-ol�st is � v�gzi.
	 * @return
	 * @throws JSONException
	 * @throws NetworkErrorException
	 * @throws CommunicatorException
	 * @throws Exception
	 */
	protected JSONObject getSingleResponse() throws JSONException,NetworkErrorException,CommunicatorException,Exception {
		String response = getResponse(nameValueList);
		JSONObject jObject = null;
		try {
			jObject = new JSONObject(response);
		}
		catch(JSONException e) {
			return null;
		}
    	try {
    		String hiba = jObject.getString("hiba");
    		String hibaid = jObject.getString("hibaid");
    		if(hibaid.compareTo("0") == 0) {
    			return jObject;
    		}
    		throw new CommunicatorException(hibaid,hiba);
    	}
    	catch(JSONException e) {
    		return jObject;
    	}
	}
	/**
	 * Lekezeli a szerver �ltal dobott hib�t, �s ha nincs, akkor tov�bbadja a t�mb�t, amit kapott a szervert�l.
	 * Csak t�mb�ket tud kezelni, a parse-ol�st is � v�gzi.
	 * @return
	 * @throws JSONException
	 * @throws NetworkErrorException
	 * @throws CommunicatorException
	 * @throws Exception
	 */
	protected JSONArray getMultiResponse() throws JSONException,NetworkErrorException,CommunicatorException,Exception {
		String response = getResponse(nameValueList);
		if(response == null) return null;
		JSONArray jArray = new JSONArray(response);
		
    	try {
    		String hiba = jArray.getJSONObject(0).getString("hiba");
    		String hibaid = jArray.getJSONObject(0).getString("hibaid");
    		if(hibaid.compareTo("0") == 0) {
    			return jArray;
    		}
    		throw new CommunicatorException(hibaid,hiba);
    	}
    	catch(JSONException e) {
    		return jArray;
    	}
	}
	/**
	 * H�l�zati probl�ma eset�n a Logba �rja a hib�t
	 * @param e
	 */
	protected void handleNetworkErrorException(NetworkErrorException e) {
		Logger.writeToLog("Hiba: "+e.getMessage());
		problem = true;
	}
	/**
	 * Parse-ol�s probl�ma eset�n a Logba �rja a hib�t
	 * @param e
	 */
	protected void handleJSONException(JSONException e) {
		problem = true;
		Logger.writeException(e);
	}
	/**
	 * Szerver �ltal dobott hib�t kezeli.
	 * @param e
	 */
	protected void handleCommunicatorException(CommunicatorException e) {
		if(e.hibaId == 2) {
			//final String hibaszoveg = e.hibaSzoveg;
			InformationDialog.errorDialogUIThread(context, e.hibaSzoveg, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					try {
						Intent it = new Intent(context,LoginActivity.class);
						context.startActivity(it);
					}
					catch(Exception e) {
						Logger.writeToLog("Hiba t�rt�nt a hiba ki�r�sakor :), mert: ");
						Logger.writeException(e);
					}
				}
			});
		}
		else {
			if(!hibaDialog) {
				InformationDialog.errorDialogUIThread(context, e.hibaSzoveg,new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						hibaDialog = false;
					}
				});
				hibaDialog = true;
			}
		}
		problem = true;
	}
	/**
	 * Ismeretlen probl�ma eset�n a Logba �rja a hib�t
	 * @param e
	 */
	protected void handleException(Exception e) {
		Logger.writeException(e);
		problem = true;
	}
	
	/**
	 * Visszaadja, hogy volt-e valamilyen kiv�tel
	 * @return
	 */
	public boolean isProblem() {
		return problem;
	}
}
