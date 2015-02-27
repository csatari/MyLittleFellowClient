package hu.jex.mylittlefellow.model;

import hu.jex.mylittlefellow.R;
import hu.jex.mylittlefellow.communicator.CommunicatorBuilding;
import hu.jex.mylittlefellow.communicator.CommunicatorException;
import hu.jex.mylittlefellow.communicator.CommunicatorTimedAction;
import hu.jex.mylittlefellow.communicator.CommunicatorUser;
import hu.jex.mylittlefellow.communicator.CommunicatorUserDetails;
import hu.jex.mylittlefellow.gui.InformationDialog;
import hu.jex.mylittlefellow.storage.BuildingDatabaseAdapter;
import hu.jex.mylittlefellow.storage.Storage;
import hu.jex.mylittlefellow.storage.StorageDatabaseAdapter;
import hu.jex.mylittlefellow.storage.TileDatabaseAdapter;

import java.util.ArrayList;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.EditText;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;

/**
 * A bejelentkezéshez szükséges modell osztály
 * @author Albert
 *
 */
public class Login {
	private static Activity context;
	
	private static String accountName;
	
	public static final String PREFS_NAME = "Authpref";
	
	public Login(Activity context) {
		Login.context = context;
	}
	
	static OnLoginEvent mListener;
	public interface OnLoginEvent {
		/**
		 * Amikor minden sikeres, beléphet a felhasználó
		 */
		public void onLogin();
		/**
		 * Amikor elindul a folyamat és megjelenhet a betöltõablak
		 */
		public void onStartProgress();
		/**
		 * Le kell cserélni a betöltõablak szövegét.
		 * @param message Amire cserélni kell a szöveget
		 */
		public void setProgressMessage(String message);
		/**
		 * A regisztrációs ablakot kell megmutatni
		 * @param account A fiók neve
		 */
		public void showRegistrationDialog(String account);
	}
	/**
	 * Beállítja az eseményt
	 * @param eventListener
	 */
	public void setCustomEventListener(OnLoginEvent eventListener) {
		mListener=eventListener;
	}
	
	/**
	 * Elkezdi a bejelentkeztetést
	 */
	public void startLogin() {
		Loginner loginner = new Loginner();
		loginner.execute(1);
		if(mListener!=null)mListener.onStartProgress();
	}
	/**
	 * Lekéri az összes fiókot
	 * @return A fiókok tömbje
	 */
	private String[] getAccountNames() {
	    AccountManager mAccountManager = AccountManager.get(context);
	    Account[] accounts = mAccountManager.getAccountsByType(
	            GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
	    String[] names = new String[accounts.length];
	    for (int i = 0; i < names.length; i++) {
	        names[i] = accounts[i].name;
	    }
	    return names;
	}
	/**
	 * A megadott fiókot kiválasztotta
	 * @param accountName A fiók neve
	 */
	public void accountPicked(String accountName) {
		Login.accountName = accountName;
		Logger.writeToLog("accountName: "+accountName);
		//Toast.makeText(context, context.getResources().getString(R.string.new_account) + accountName, Toast.LENGTH_LONG).show();
		Logger.writeToLog("bejelentkezve1");
		Loginner loginner = new Loginner();
		loginner.execute(2);
	}
	/**
	 * Elkezdi a regisztrálást a megadott adatokkal
	 * @param username Felhasználónév
	 * @param password Jelszó 
	 * @param rePassword Jelszó még egyszer
	 */
	public void register(String username, String password, String rePassword) {
		try {
			username = username.trim();
			String hash = Hash.sha256(password);
			Logger.writeToLog("saving: "+accountName+" "+username+" "+hash+
					" "+password+" "+rePassword);
			Storage.saveRegisteredAccount(context,accountName,username,hash);
			Loginner loginner = new Loginner();
			loginner.execute(3);
		}
		catch(Exception e) {
			Logger.writeException(e);
		}
	}
	/**
	 * Ellenõrzi a beírt adatok helyességét
	 * @param username
	 * @param password
	 * @param rePassword
	 * @return Hamis, ha valami nem stimmel
	 */
	public boolean checkRegistrationData(EditText username, EditText password, EditText rePassword) {
		username.setError(null);
		password.setError(null);
		rePassword.setError(null);
		if(username.getText().toString().length() == 0) {
			username.setError(context.getResources().getString(R.string.register_username_empty));
			username.requestFocus();
			return false;
		}
		else if(isAccented(username.getText().toString())) {
			username.setError(context.getResources().getString(R.string.register_username_accented));
			username.requestFocus();
			return false;
		}
		else if(password.getText().toString().length() == 0) {
			password.setError(context.getResources().getString(R.string.register_password_empty));
			password.requestFocus();
			return false;
		}
		else if(rePassword.getText().toString().length() == 0) {
			rePassword.setError(context.getResources().getString(R.string.register_password_empty));
			rePassword.requestFocus();
			return false;
		}
		String passw = password.getText().toString();
		String rePassw = rePassword.getText().toString();
		if(passw.compareTo(rePassw) != 0) {
			password.setError(context.getResources().getString(R.string.register_password_notequal));
			password.requestFocus();
			return false;
		}
		return true;
	}
	/**
	 * A bejelentkezést különbözõ állapotait végzi el
	 * @param status
	 */
	private void login_(int status) {
		//elmentett adatok ellenõrzése
		if(status == 0) {
			context.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(mListener!=null)mListener.setProgressMessage("Checking registered data");
				}
			});
			
			SharedPreferences settings2 = context.getSharedPreferences(PREFS_NAME, 0);
			String account = settings2.getString("email", null);
			if(account == null) {
				login_(2);
				return;
			}
			else {
				boolean stimmel = false;
				for(String name : getAccountNames()) {
					if(name.equalsIgnoreCase(account)) {
						stimmel = true;
						break;
					}
				}
				if(stimmel) {
					accountName = account;
					login_(3);
				}
				else {
					login_(2);
					return;
				}
			}
		}
		//regisztráló ablak mutatása
		else if(status == 1) {
			//adatbázisból a tile-ok törlése
			TileDatabaseAdapter db = new TileDatabaseAdapter(context);
			db.open();
			try {
				db.deleteAll();
			}
			catch(Exception e) {
				Logger.writeException(e);
			}
			finally {
				db.close();
			}
			
			//regisztráció
			context.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(mListener!=null)mListener.setProgressMessage("Checking registered data");
					if(mListener!=null)mListener.showRegistrationDialog(accountName);
				}
			});
		}
		//e-mail választás
		else if(status == 2) {
			context.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(mListener!=null)mListener.setProgressMessage("Picking account");
				}
			});
			
			Intent intent2 = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"},
					false, null, null, null, null);
			context.startActivityForResult(intent2, 15);
		}
		//sessionid lekérése
		else if(status == 3) {
			context.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(mListener!=null)mListener.setProgressMessage("Getting data from server");
				}
			});
			
			CommunicatorUser communicator = new CommunicatorUser(context);
			try {
				SharedPreferences settings2 = context.getSharedPreferences(PREFS_NAME, 0);
				String email = settings2.getString("email", null);
				String username = settings2.getString("username", null);
				String password = settings2.getString("password", null);
				
				String sessionid = communicator.getSessionId(email,username,password);
				SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("sessionid", sessionid);
				editor.commit();
				//Logger.writeToLog("kész: "+sessionid);
				
				CommunicatorUserDetails commUserDetails = new CommunicatorUserDetails(sessionid);
				
				TileDatabaseAdapter db = new TileDatabaseAdapter(context);
				db.open();
				try {
					if(db.isEmpty()) { //üres, le kell tölteni
						Logger.writeToLog("üres tile adatbázis...");
						ArrayList<Tile> allTiles = commUserDetails.getAllTiles();
						if(allTiles != null) {
							for(Tile tile : allTiles) {
								Logger.writeToLog("Hozzáadás:"+tile.toString());
								db.addRow(tile);
							}
						}
						int placeid = commUserDetails.getPlaceId();
						Storage.setUserTileId(context, placeid);
						int homeid = commUserDetails.getHomeId();
						Storage.setHomeId(context, homeid);
					}
				}
				catch(Exception e) {
					Logger.writeException(e);
				}
				finally {
					db.close();
				}
				
				StorageDatabaseAdapter storageDb = new StorageDatabaseAdapter(context);
				storageDb.open();
				try {
					if(storageDb.isEmpty()) {
						Logger.writeToLog("üres storage adatbázis...");
						ResourceStorage allResources = commUserDetails.getAllStorage();
						if(allResources != null) {
							for(Resource res : allResources.getStorage()) {
								storageDb.addRow(res);
							}
						}
					}
				}
				catch(Exception e) {
					Logger.writeException(e);
				}
				finally {
					storageDb.close();
				}
				
				BuildingDatabaseAdapter buildingDb = new BuildingDatabaseAdapter(context);
				buildingDb.open();
				try {
					//buildingDb.deleteAll();
					if(buildingDb.isEmpty()) {
						Logger.writeToLog("üres building adatbázis...");
						CommunicatorBuilding commBuilding = new CommunicatorBuilding(context, sessionid);
						ArrayList<Building> allBuildings = commBuilding.getAllBuildings();
						if(allBuildings != null) {
							for(Building bui : allBuildings) {
								buildingDb.addRow(bui);
							}
						}
					}
				}
				catch(Exception e) {
					Logger.writeException(e);
				}
				finally {
					buildingDb.close();
				}
				
				CommunicatorTimedAction commTimedAction = new CommunicatorTimedAction(context, sessionid);
				commTimedAction.getData();
				
				if(Storage.getUserTileId(context) == 0) {
					Logger.writeToLog("User tile id lekérése szerverrõl...");
					Storage.setUserTileId(context, commUserDetails.getPlaceId());
				}
				login_(4);
			}
			catch(final CommunicatorException e) {
				context.runOnUiThread(new Runnable(){
	    		    public void run(){
	    		    	InformationDialog.errorDialog(context, e.hibaSzoveg, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								startLogin();
							}
						});
	    		    }
	    		});
				if(e.hibaId == 2) {
					Storage.emptyRegisteredAccount(context);
				}
			}
			catch(Exception e) {
				Logger.writeException(e);
			}
			return;
		}
		//beléptetés és sessionid elmentése
		else if(status == 4) {
			if(mListener!=null) {
				mListener.onLogin();
			}
		}
	}
	/**
	 * Garantálja, hogy a bejelentkeztetés nem a UI Threaden történik
	 * @author Albert
	 *
	 */
	private class Loginner extends AsyncTask<Integer, Void, Void> {
		@Override
		protected Void doInBackground(Integer... params) {
			if(params[0] == 1) {
				login_(0);
			}
			else if(params[0] == 2) {
				login_(1);
			}
			else if(params[0] == 3) {
				login_(3);
			}
			else if(params[0] == 10) {
				//test();
			}
			return null;
		}
		
	}
	/**
	 * Ellenõrzi, hogy van-e a string-ben ékezetes betû
	 * @param str
	 * @return Igaz, ha van ékezet
	 */
	private boolean isAccented(String str) {
		if(str.contains("ö") || str.contains("ü") || str.contains("ó") || str.contains("é") || str.contains("á") || 
				str.contains("û") || str.contains("í") || str.contains("õ") || str.contains("ú") || 
				str.contains("Ö") || str.contains("Ü") || str.contains("Ó") || str.contains("É") || 
				str.contains("Á") || str.contains("Û") || str.contains("Í") || str.contains("Õ") || str.contains("Ú") ) {
			return true;
		}
		return false;
	}
}
