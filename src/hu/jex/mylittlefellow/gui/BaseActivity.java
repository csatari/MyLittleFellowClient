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
 * Az Activity-k �soszt�lya, minden olyan Activity-t ebb�l kell sz�rmaztatni, ami a bejelentkez�s ut�n van
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
	 * Az Activity l�trehoz�sa
	 */
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	/**
	 * Az Activity ideiglenes meg�ll�t�sa
	 */
	@Override
	protected void onPause() {
		super.onPause();
		if(timedAction != null) {
			timedAction.pause();
		}
	}
	/**
	 * Az Activity-be val� visszal�p�s
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
	 * Elind�tja az online ellen�rz�st
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
	 * Men�pont kiv�laszt�sa
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
	 * Elind�tja a megadott id�ig�nyes interakci�t
	 * @param type Az interakci� t�pusa
	 * @param goal_1 Az egyik seg�dv�ltoz�
	 * @param goal_2 A m�sik seg�dv�ltoz�
	 * @param helper Harmadik seg�dv�ltoz�
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
	 * Elind�tja a megadott id�ig�nyes interakci�t
	 * @param type Az interakci� t�pusa
	 * @param goal_1 Az egyik seg�dv�ltoz�
	 * @param goal_2 A m�sik seg�dv�ltoz�
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
	 * Elind�tja a megadott id�ig�nyes interakci�t
	 * @param type Az interakci� t�pusa
	 * @param goal_1 Az egyik seg�dv�ltoz�
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
	 * Meg�ll�tja az interakci�t utaz�s eset�n
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
	 * Visszak�ldi a programot a bejelentkez�shez
	 */
	protected void goBackToRootActivity() {
		Logger.writeToLog("Go back to root activity....");
		Intent it = new Intent(this,LoginActivity.class);
		it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		this.finish();
		startActivity(it);
	}
	/**
	 * Visszaadja az oldal t�pus�t
	 * @return Az oldal t�pusa
	 */
	protected abstract ActivityType getActivityType();
	
	/**
	 * Befejez�d�tt az id�ig�nyes interakci�
	 * @param timedAction
	 */
	protected abstract void onTimedActionFinish(TimedAction timedAction);
	/**
	 * Tart az id�ig�nyes interakci�.
	 * @param timedAction 
	 * @param secondsUntilFinished Ennyi m�sodperc van m�g h�tra a befejez�sig
	 */
	protected abstract void onTimedActionTick(TimedAction timedAction, long secondsUntilFinished);
	/**
	 * Elkezd�d�tt az id�ig�nyes interakci�
	 * @param timedAction
	 */
	protected abstract void onTimedActionStart(TimedAction timedAction);
	/**
	 * Hib�ra l�pett az interakci�
	 * @param timedAction
	 */
	protected abstract void onTimedActionError(TimedAction timedAction);
	/**
	 * Elkezd�dik az interakci� felt�lt�se
	 * @param timedAction
	 */
	protected abstract void onTimedActionFinishUpload(TimedAction timedAction);
	/**
	 * A visszasz�ml�l� oszt�ly
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
	 * Az online ellen�rz�s oszt�lya
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
