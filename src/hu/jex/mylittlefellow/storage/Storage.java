package hu.jex.mylittlefellow.storage;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Az egyéb dolgokat lementõ osztály
 * @author Albert
 *
 */
public class Storage {
	public static final String PREFS_NAME = "Authpref";
	
	//email
	//username
	//password
	//sessionid
	//userTileId
	//homeId
	/**
	 * Lement egy integer-t
	 * @param context
	 * @param saveKey a kulcs
	 * @param savedInt az integer
	 */
	private static void saveInt(Context context, String saveKey, int savedInt) {
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(saveKey, savedInt);
		editor.commit();
	}
	/**
	 * Betölt egy integert
	 * @param context
	 * @param saveKey a kulcs
	 * @return az integer
	 */
	private static int loadInt(Context context, String saveKey) {
		SharedPreferences settings2 = context.getSharedPreferences(PREFS_NAME, 0);
		int loadedInt = settings2.getInt(saveKey, 0);
		return loadedInt;
	}
	/**
	 * Lement egy double-t
	 * @param context
	 * @param saveKey a kulcs 
	 * @param savedDouble a double
	 */
	private static void saveDouble(Context context, String saveKey, double savedDouble) {
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(saveKey, savedDouble+"");
		editor.commit();
	}
	/**
	 * Betölt egy double-t
	 * @param context
	 * @param saveKey a kulcs
	 * @return a double
	 */
	private static double loadDouble(Context context, String saveKey) {
		SharedPreferences settings2 = context.getSharedPreferences(PREFS_NAME, 0);
		String loadedDoubleStr = settings2.getString(saveKey, "0");
		return Double.parseDouble(loadedDoubleStr);
	}
	/**
	 * Lement egy long-ot
	 * @param context
	 * @param saveKey a kulcs
	 * @param savedLong a long
	 */
	private static void saveLong(Context context, String saveKey, long savedLong) {
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(saveKey, savedLong);
		editor.commit();
	}
	/**
	 * Betölt egy long-ot
	 * @param context
	 * @param saveKey a kulcs
	 * @return a long
	 */
	private static long loadLong(Context context, String saveKey) {
		SharedPreferences settings2 = context.getSharedPreferences(PREFS_NAME, 0);
		long loadedLong = settings2.getLong(saveKey, 0);
		return loadedLong;
	}
	/**
	 * Elmenti egy felhasználó autentikációs adatait
	 * @param context
	 * @param email
	 * @param username
	 * @param password
	 */
	public static  void saveRegisteredAccount(Context context, String email, String username, String password) {
		
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("email", email);
		editor.putString("username", username);
		editor.putString("password", password);
		editor.commit();
	}
	/**
	 * Kitörli a regisztrált adatokat
	 * @param context
	 */
	public static  void emptyRegisteredAccount(Context context) {
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("email", "");
		editor.putString("username", "");
		editor.putString("password", "");
		editor.commit();
	}
	/**
	 * Lekérdezi a karakter sessionid-jét
	 * @param context
	 * @return
	 */
	public static String getUserSessionId(Context context) {
		SharedPreferences settings2 = context.getSharedPreferences(PREFS_NAME, 0);
		String sessionid = settings2.getString("sessionid", null);
		return sessionid;
	}
	
	public static void setUserTileId(Context context, int tileId) {
		saveInt(context, "userTileId", tileId);
	}
	public static int getUserTileId(Context context) {
		return loadInt(context, "userTileId");
	}
	
	public static void setHomeId(Context context, int homeid) {
		saveInt(context, "homeId", homeid);
	}
	public static int getHomeId(Context context) {
		return loadInt(context, "homeId");
	}
	
	public static void setTimedActionStartTime(Context context, long time) {
		saveLong(context, "timedActionStartTime", time);
	}
	public static long getTimedActionStartTime(Context context) {
		return loadLong(context, "timedActionStartTime");
	}
	
	public static void setTimedActionEndTime(Context context, long time) {
		saveLong(context, "timedActionEndTime", time);
	}
	public static long getTimedActionEndTime(Context context) {
		return loadLong(context, "timedActionEndTime");
	}
	
	public static void setTimedActionType(Context context, int type) {
		saveInt(context, "timedActionType", type);
	}
	public static int getTimedActionType(Context context) {
		return loadInt(context, "timedActionType");
	}
	
	public static void setTimedActionGoal(Context context, int type) {
		saveInt(context, "timedActionGoal", type);
	}
	public static int getTimedActionGoal(Context context) {
		return loadInt(context, "timedActionGoal");
	}
	public static void setTimedActionGoal2(Context context, int type) {
		saveInt(context, "timedActionGoal2", type);
	}
	public static int getTimedActionGoal2(Context context) {
		return loadInt(context, "timedActionGoal2");
	}
	public static void setStorageLimit(Context context, int limit) {
		saveInt(context, "storageLimit", limit);
	}
	public static int getStorageLimit(Context context) {
		return loadInt(context, "storageLimit");
	}
	public static void setIntelligencePoints(Context context, int ipo) {
		saveInt(context, "intelligencePoints", ipo);
	}
	public static int getIntelligencePoints(Context context) {
		return loadInt(context, "intelligencePoints");
	}
	
	public static void setLastLatitude(Context context, double latitude) {
		saveDouble(context, "lastLatitude", latitude);
	}
	public static double getLastLatitude(Context context) {
		return loadDouble(context, "lastLatitude");
	}
	public static void setLastLongitude(Context context, double longitude) {
		saveDouble(context, "lastLongitude", longitude);
	}
	public static double getLastLongitude(Context context) {
		return loadDouble(context, "lastLongitude");
	}
	
}
