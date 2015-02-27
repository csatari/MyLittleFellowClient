package hu.jex.mylittlefellow.gui;

import hu.jex.mylittlefellow.R;
import hu.jex.mylittlefellow.communicator.CommunicatorUser;
import hu.jex.mylittlefellow.model.Logger;
import hu.jex.mylittlefellow.model.TimedAction;
import hu.jex.mylittlefellow.model.TimedAction.OnTimedActionEventListener;
import hu.jex.mylittlefellow.model.VisibleTileAdapter;
import hu.jex.mylittlefellow.storage.Storage;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Az Activity-k õsosztálya, minden olyan Activity-t ebbõl kell származtatni, ami a bejelentkezés után van
 * @author Albert
 *
 */
public abstract class BaseActivity extends FragmentActivity {
	
	public static Activity context;
	
	private static TimedAction timedAction;
	private static TimedActionCountdown actionCountDown;
	
	protected enum ActivityType {
		MAP,PLACE,STORAGE,TAX,TILESLICECHOOSE,TOOLSTATION,TOWER
	}
	
	/**
	 * Az Activity létrehozása
	 */
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	/**
	 * Az Activity ideiglenes megállítása
	 */
	@Override
	protected void onPause() {
		super.onPause();
		if(timedAction != null) {
			timedAction.pause();
		}
	}
	/**
	 * Az Activity-be való visszalépés
	 */
	@Override
	protected void onResume() {
		super.onResume();
		context = this;
		checkIfOnline();
		try {
			if(VisibleTileAdapter.getAllTiles() == null) { //ha elfelejtette volna
				goBackToRootActivity();
			}
			actionCountDown = new TimedActionCountdown();
			timedAction = TimedAction.load(context);
			if(timedAction != null) {
				if(timedAction.getType() != TimedAction.NONE) {
					timedAction.setCustomEventListener(actionCountDown);
					timedAction.resume();
				}
			}
		}
		catch(Exception e) {
			goBackToRootActivity();
		}
	}
	/**
	 * Elindítja az online ellenõrzést
	 */
	private void checkIfOnline() {
		Logger.writeToLog("Checking if is online..........................");
		CheckOnline co = new CheckOnline();
		co.execute((Void) null);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.help, menu);
		return true;
	}
	/**
	 * Menüpont kiválasztása
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_help:
			InformationDialog.showHelpDialog(context, getActivityType());
			//InformationDialog.errorToast(context, "Help menu..."+getActivityType().toString(), Toast.LENGTH_SHORT);
			return true;
		/*case R.id.menu_crash:
			goBackToRootActivity();
			return true;*/
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	/**
	 * Elindítja a megadott idõigényes interakciót
	 * @param type Az interakció típusa
	 * @param goal_1 Az egyik segédváltozó
	 * @param goal_2 A másik segédváltozó
	 * @param helper Harmadik segédváltozó
	 */
	protected void startTimedAction(int type, int goal_1, int goal_2, int helper) {
		if(!TimedAction.RUNNING) {
			timedAction = new TimedAction(context, type, goal_1,goal_2);
			timedAction.setCustomEventListener(actionCountDown);
			timedAction.helperint = helper;
			timedAction.start();
		}
		else {
			InformationDialog.errorToast(context, context.getResources().getString(R.string.baseactivity_alreadydoing), Toast.LENGTH_SHORT);
		}
	}
	/**
	 * Elindítja a megadott idõigényes interakciót
	 * @param type Az interakció típusa
	 * @param goal_1 Az egyik segédváltozó
	 * @param goal_2 A másik segédváltozó
	 */
	protected void startTimedAction(int type, int goal_1, int goal_2) {
		if(!TimedAction.RUNNING) {
			timedAction = new TimedAction(context, type, goal_1,goal_2);
			timedAction.setCustomEventListener(actionCountDown);
			timedAction.start();
		}
		else {
			InformationDialog.errorToast(context, context.getResources().getString(R.string.baseactivity_alreadydoing), Toast.LENGTH_SHORT);
		}
	}
	/**
	 * Elindítja a megadott idõigényes interakciót
	 * @param type Az interakció típusa
	 * @param goal_1 Az egyik segédváltozó
	 */
	protected void startTimedAction(int type, int goal_1) {
		if(!TimedAction.RUNNING) {
			timedAction = new TimedAction(context, type, goal_1);
			timedAction.setCustomEventListener(actionCountDown);
			timedAction.start();
		}
		else {
			InformationDialog.errorToast(context, context.getResources().getString(R.string.baseactivity_alreadydoing), Toast.LENGTH_SHORT);
		}
	}
	/**
	 * Megállítja az interakciót utazás esetén
	 */
	protected void stopTimedActionIfTravel() {
		Logger.writeToLog("timedaction volt: "+timedAction);
		if(timedAction != null) {
			if(TimedAction.RUNNING) {
				if(timedAction.getType() == TimedAction.TRAVEL) {
					timedAction.stop();
				}
			}
		}
	}
	/**
	 * Visszaküldi a programot a bejelentkezéshez
	 */
	protected void goBackToRootActivity() {
		Logger.writeToLog("Go back to root activity....");
		Intent it = new Intent(this,LoginActivity.class);
		it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		this.finish();
		startActivity(it);
	}
	/**
	 * Visszaadja az oldal típusát
	 * @return Az oldal típusa
	 */
	protected abstract ActivityType getActivityType();
	
	/**
	 * Befejezõdött az idõigényes interakció
	 * @param timedAction
	 */
	protected abstract void onTimedActionFinish(TimedAction timedAction);
	/**
	 * Tart az idõigényes interakció.
	 * @param timedAction 
	 * @param secondsUntilFinished Ennyi másodperc van még hátra a befejezésig
	 */
	protected abstract void onTimedActionTick(TimedAction timedAction, long secondsUntilFinished);
	/**
	 * Elkezdõdött az idõigényes interakció
	 * @param timedAction
	 */
	protected abstract void onTimedActionStart(TimedAction timedAction);
	/**
	 * Hibára lépett az interakció
	 * @param timedAction
	 */
	protected abstract void onTimedActionError(TimedAction timedAction);
	/**
	 * Elkezdõdik az interakció feltöltése
	 * @param timedAction
	 */
	protected abstract void onTimedActionFinishUpload(TimedAction timedAction);
	/**
	 * A visszaszámláló osztály
	 * @author Albert
	 *
	 */
	private class TimedActionCountdown implements OnTimedActionEventListener {
		@Override
		public void onFinish() {
			if(timedAction.getType() == TimedAction.TRAVEL) {
				Storage.setUserTileId(context, timedAction.getGoal());
			}
			onTimedActionFinish(timedAction);
		}
		@Override
		public void onTick(long millisUntilFinished) {
			onTimedActionTick(timedAction,millisUntilFinished);
		}
		@Override
		public void onStart() {
			onTimedActionStart(timedAction);
		}
		@Override
		public void onError() {
			onTimedActionError(timedAction);
		}
		@Override
		public void onFinishUpload() {
			onTimedActionFinishUpload(timedAction);
		}
	}
	/**
	 * Az online ellenõrzés osztálya
	 * @author Albert
	 *
	 */
	private class CheckOnline extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			CommunicatorUser comm = new CommunicatorUser(context);
			if(!comm.isOnline()) {
				context.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(context, context.getResources().getString(R.string.baseactivity_notloggedin), Toast.LENGTH_SHORT).show();
						goBackToRootActivity();
					}
				});
			}
			else {
				Logger.writeToLog("ONLINE VAN.................................");
			}
			return null;
		}
		
	}
}
