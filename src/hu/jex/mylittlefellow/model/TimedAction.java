package hu.jex.mylittlefellow.model;

import hu.jex.mylittlefellow.communicator.CommunicatorBuilding;
import hu.jex.mylittlefellow.communicator.CommunicatorKnownTiles;
import hu.jex.mylittlefellow.communicator.CommunicatorTimedAction;
import hu.jex.mylittlefellow.communicator.CommunicatorTool;
import hu.jex.mylittlefellow.communicator.CommunicatorUserDetails;
import hu.jex.mylittlefellow.gui.InformationDialog;
import hu.jex.mylittlefellow.storage.BuildingDatabaseAdapter;
import hu.jex.mylittlefellow.storage.Storage;
import hu.jex.mylittlefellow.storage.StorageDatabaseAdapter;
import hu.jex.mylittlefellow.storage.TileDatabaseAdapter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.text.format.Time;
import android.widget.Toast;

/**
 * Az idõigényes interakciókért felel
 * @author Albert
 *
 */
public class TimedAction {
	public final static int NONE = 0;
	public final static int TRAVEL = 1;
	public final static int EXAMINE = 2;
	public final static int SETLLEDOWN = 3;
	public final static int RESOURCE1 = 4;
	public final static int RESOURCE2 = 5;
	public final static int RESOURCE3 = 6;
	public final static int BUILD = 7;
	public final static int DEVELOP_BUILDING = 8;
	public final static int DEVELOP_TOOL = 9;
	
	private static int errorCount = 0;
	
	private final static int TIMER_TICK = 200;
	
	private long firstStartTime; //a legelsõ idõpont
	private long startTime; //A kezdeti idõ
	private long endTime; //A befejezési idõ
	private int type; //az interakció típusa
	private int goal; //az egyik segédváltozó
	private int goal_2; //a másik segédváltozó
	public int helperint; //a harmadik segédváltozó
	public int helperint2; //a 4. segédváltozó
	
	
	public static boolean RUNNING = false; //fut-e interakció
	
	private Activity context;
	
	private ActionCountdown actionCountdown;
	
	//events
	static OnTimedActionEventListener mListener;
	public interface OnTimedActionEventListener {
		/**
		 * Befejezõdött az interakció
		 */
		public void onFinish();
		/**
		 * Fut az interakció
		 * @param millisUntilFinished ennyi milliszekundum van hátra
		 */
		public void onTick(long millisUntilFinished);
		/**
		 * Elkezdõdött az interakció
		 */
		public void onStart();
		/**
		 * Hiba történt az interakciónál
		 */
		public void onError();
		/**
		 * Elkezd kommunikálni a szerverrel
		 */
		public void onFinishUpload();
	}
	/**
	 * Beállítja az eseménykezelõt
	 * @param eventListener
	 */
	public void setCustomEventListener(OnTimedActionEventListener eventListener) {
		mListener=eventListener;
	}
	
	public TimedAction(Activity context) {
		this.context = context;
	}
	public TimedAction(Activity context, int type, int goal) {
		super();
		this.type = type;
		this.goal = goal;
		this.context = context;
	}
	
	
	public TimedAction(Activity context, int type, int goal, int goal_2) {
		super();
		this.type = type;
		this.goal = goal;
		this.goal_2 = goal_2;
		this.context = context;
	}
	/**
	 * Lekérdezi az interakció típusát szövegként
	 * @return
	 */
	public String getTimedActionType() {
		switch(type) {
		case TRAVEL:
			return "Travel:";
		case BUILD:
			return "Building:";
		case DEVELOP_BUILDING:
			return "Developing:";
		case DEVELOP_TOOL:
			return "Developing:";
		case EXAMINE:
			return "Examining:";
		case RESOURCE1:
			return "Getting resource:";
		case RESOURCE2:
			return "Getting resource:";
		case RESOURCE3:
			return "Getting resource:";
		case SETLLEDOWN:
			return "Settling down:";
		default:
			return "";
		}
	}
	/**
	 * Elkezdi az interakciót
	 * @return a szükséges idõ
	 */
	private long startTimedAction() {
		long time_ = 0;
		if(type == TRAVEL) {
			CommunicatorUserDetails comm = new CommunicatorUserDetails(context,Storage.getUserSessionId(context));
			time_ = comm.setPlaceId(goal);
			if(comm.isProblem()) {
				RUNNING = false;
				return -1;
			}
		}
		else if(type == EXAMINE) {
			CommunicatorKnownTiles comm = new CommunicatorKnownTiles(context, Storage.getUserSessionId(context));
			time_ = comm.examineTileId(goal);
			if(comm.isProblem()) {
				RUNNING = false;
				return -1;
			}
		}
		else if(type == SETLLEDOWN) {
			CommunicatorUserDetails comm = new CommunicatorUserDetails(context,Storage.getUserSessionId(context));
			time_ = comm.setHomeId(goal);
			if(comm.isProblem()) {
				RUNNING = false;
				return -1;
			}
		}
		else if(type == RESOURCE1) {
			CommunicatorUserDetails comUserDetails = new CommunicatorUserDetails(context,Storage.getUserSessionId(context));
			boolean isFull = comUserDetails.isStorageFull();
			if(comUserDetails.isProblem()) {
				killMyself();
				return -1;
			}
			if(isFull) {
				Logger.writeToLog("RES1ERROR");
				InformationDialog.errorDialogUIThread(context, "The storage is full...", null);
				killMyself();
				context.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(mListener!=null) mListener.onError();
					}
				});
				return -1;
			}
			time_ = comUserDetails.setMineResource1(goal, goal_2);
		}
		else if(type == RESOURCE2) {
			CommunicatorUserDetails comUserDetails = new CommunicatorUserDetails(context,Storage.getUserSessionId(context));
			boolean isFull = comUserDetails.isStorageFull();
			if(comUserDetails.isProblem()) {
				killMyself();
				return -1;
			}
			if(isFull) {
				InformationDialog.errorDialogUIThread(context, "The storage is full...", null);
				context.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(mListener!=null) mListener.onError();
					}
				});
				killMyself();
				return -1;
			}
			time_ = comUserDetails.setMineResource2(goal, goal_2);
		}
		else if(type == RESOURCE3) {
			CommunicatorUserDetails comUserDetails = new CommunicatorUserDetails(context,Storage.getUserSessionId(context));
			boolean isFull = comUserDetails.isStorageFull();
			if(comUserDetails.isProblem()) {
				RUNNING = false;
				return -1;
			}
			if(isFull) {
				InformationDialog.errorDialogUIThread(context, "The storage is full...", null);
				context.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(mListener!=null) mListener.onError();
					}
				});
				RUNNING = false;
				return -1;
			}
			time_ = comUserDetails.setMineResource3(goal, goal_2);
		}
		else if(type == BUILD) {
			CommunicatorBuilding comm = new CommunicatorBuilding(context);
			time_ = comm.build(goal, helperint, goal_2);
			if(comm.isProblem()) {
				RUNNING = false;
				return -1;
			}
		}
		else if(type == DEVELOP_BUILDING) {
			CommunicatorBuilding comm = new CommunicatorBuilding(context);
			time_ = comm.develop(goal, goal_2);
			if(comm.isProblem()) {
				RUNNING = false;
				return -1;
			}
		}
		else if(type == DEVELOP_TOOL) {
			CommunicatorTool comm = new CommunicatorTool(context);
			time_ = comm.develop(goal);
			if(comm.isProblem()) {
				RUNNING = false;
				return -1;
			}
		}
		return time_;
	}
	/**
	 * Befejezi az interakciót
	 */
	public void endTimedAction() {
		if(type == TimedAction.TRAVEL) {
			Storage.setUserTileId(context, getGoal());
			InformationDialog.errorToast(context, "Travelled!", Toast.LENGTH_SHORT);
		}
		else if(type == TimedAction.EXAMINE) {
			Tile examinedTile = Tile.getTileFromAllTilesById(VisibleTileAdapter.getAllTiles(), getGoal());
			examinedTile.setExamined(true);
			TileDatabaseAdapter db = new TileDatabaseAdapter(context);
			db.open();
			try {
				db.update(examinedTile);
			}
			catch(Exception e) {
				Logger.writeException(e);
			}
			finally {
				db.close();
			}

			VisibleTileAdapter.updateOneTile(examinedTile);
			InformationDialog.errorToast(context, "Examined!", Toast.LENGTH_SHORT);
		}
		else if(type == TimedAction.SETLLEDOWN) {
			Storage.setHomeId(context, getGoal());
			BuildingDatabaseAdapter buildingDb = new BuildingDatabaseAdapter(context);
			buildingDb.open();
			try {
				buildingDb.deleteAll();
				if(buildingDb.isEmpty()) {
					Logger.writeToLog("üres building adatbázis...");
					CommunicatorBuilding commBuilding = new CommunicatorBuilding(context);
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
			InformationDialog.errorToast(context, "Settled down!", Toast.LENGTH_SHORT);
		}
		else if(type == TimedAction.RESOURCE1 || type == TimedAction.RESOURCE2 || type == TimedAction.RESOURCE3) {
			StorageDatabaseAdapter sdb = new StorageDatabaseAdapter(context);
			TileDatabaseAdapter tdb = new TileDatabaseAdapter(context);
			sdb.open();
			tdb.open();
			try {
				Resource res = null;
				int amount = 0;
				Tile tile = Tile.getTileFromAllTilesById(VisibleTileAdapter.getAllTiles(), getGoal_2());
				Logger.writeToLog("Kattintott tile: "+getGoal_2()+" ");
				try {
					if(type == TimedAction.RESOURCE1) {
						int resource = tile.getResource1();
						tile.setResource1(resource-1);
					}
					else if(type == TimedAction.RESOURCE2) {
						int resource = tile.getResource2();
						tile.setResource2(resource-1);
					}
					else if(type == TimedAction.RESOURCE3) {
						int resource = tile.getResource3();
						tile.setResource3(resource-1);
					}
					res = sdb.getById(getGoal());
					if(res == null) {
						res = new Resource();
						res.setId(getGoal());
						amount = 0;
					}
					else {
						amount = res.getAmount();
					}
				}
				catch(Exception e) {
					Logger.writeException(e);
				}
				
				res.setAmount(amount+1);
				sdb.update(res);
				tdb.update(tile);
			}
			catch(Exception e) {
				Logger.writeException(e);
			}
			finally {
				sdb.close();
				tdb.close();
			}
			InformationDialog.errorToast(context, "Resource mined!", Toast.LENGTH_SHORT);
		}
		else if(type == TimedAction.BUILD) {
			BuildingDatabaseAdapter buildingDb = new BuildingDatabaseAdapter(context);
			buildingDb.open();
			try {
				buildingDb.deleteAll();
				if(buildingDb.isEmpty()) {
					Logger.writeToLog("üres building adatbázis...");
					CommunicatorBuilding commBuilding = new CommunicatorBuilding(context);
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
			InformationDialog.errorToast(context, "Building built!", Toast.LENGTH_SHORT);
		}
		else if(type == TimedAction.DEVELOP_BUILDING) {
			Logger.writeToLog("Vége a buildingfejlesztésnek...");
			InformationDialog.errorToast(context, "Building developed!", Toast.LENGTH_SHORT);
		}
		else if(type == TimedAction.DEVELOP_TOOL) {
			Logger.writeToLog("Vége a toolfejlesztésnek...");
			InformationDialog.errorToast(context, "Tool developed!", Toast.LENGTH_SHORT);
		}
	}
	/**
	 * Elindítja az interakciót.
	 */
	public void start() {
		if(RUNNING) {
			return;
		}
		long time_ = startTimedAction();
		if(time_ == -1) {
			return;
		}
		final long time = time_;
		Time now = new Time();
		now.setToNow();
		startTime = now.toMillis(false);
		firstStartTime = startTime;
		endTime = startTime+(time);
		Logger.writeToLog("time: "+time+" startTime: "+startTime+" "+endTime);
		
		final TimedAction t = this;
		context.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(time < 0) {
					actionCountdown = new ActionCountdown(t,1000,TIMER_TICK);
				}
				else {
					actionCountdown = new ActionCountdown(t,time,TIMER_TICK);
				}
				RUNNING = true;
				save(context);
				actionCountdown.start();
				if(mListener!=null) mListener.onStart();
			}
		});
	}
	/**
	 * Visszatér az elõzõ számoláshoz
	 */
	public void resume() {
		if(type == TimedAction.NONE) {
			return;
		}
		RUNNING = true;
		Logger.writeToLog("resume...");
		Time time = new Time();
		time.setToNow();
		long now = time.toMillis(false);
		if(now > endTime) {
			Logger.writeToLog("ended...");
			actionCountdown = new ActionCountdown(this,0,TIMER_TICK);
			actionCountdown.start();
		}
		else {
			Logger.writeToLog("resuming...");
			if(firstStartTime == 0) {
				firstStartTime = startTime;
			}
			startTime = now;
			long ticks = endTime-startTime;
			actionCountdown = new ActionCountdown(this,ticks,TIMER_TICK);
			actionCountdown.start();
		}
		if(mListener!=null) mListener.onStart();
		Logger.writeToLog("time: "+time+" startTime: "+startTime+" "+endTime);
	}
	/**
	 * Megszakítja az interakciót
	 */
	public void stop() {
		pause();
		type = NONE;
		save(context);
		killMyself();
		Async a = new Async();
		a.execute(Async.STOP_TIMED_ACTION);
		if(mListener!=null) mListener.onError();
	}
	/**
	 * Szünetelteti a visszaszámolást
	 */
	public void pause() {
		if(actionCountdown != null) {
			actionCountdown.cancel();
		}
	}
	/**
	 * Kikapcsolja az interakciót teljesen
	 */
	public void killMyself() {
		RUNNING = false;
		type = NONE;
	}
	/**
	 * Ezt indítja el, ha elõzõleg hibára futott
	 */
	public void startWhenError() {
		final TimedAction t = this;
		context.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				actionCountdown = new ActionCountdown(t,5000,TIMER_TICK);
				actionCountdown.start();
			}
		});
	}
	/**
	 * Elmenti az interakciót
	 * @param context
	 */
	public void save(Context context) {
		if(!RUNNING) {
			return;
		}
		Storage.setTimedActionStartTime(context, startTime);
		Storage.setTimedActionEndTime(context, endTime);
		Storage.setTimedActionType(context, type);
		Storage.setTimedActionGoal(context, goal);
		Storage.setTimedActionGoal2(context, goal_2);
		
	}
	/**
	 * Betölti az elõzõ interakciót
	 * @param context
	 * @return
	 */
	public static TimedAction load(Activity context) {
		TimedAction timedAction = new TimedAction(context,
				Storage.getTimedActionType(context),
				Storage.getTimedActionGoal(context),
				Storage.getTimedActionGoal2(context));
		timedAction.setEndTime(Storage.getTimedActionEndTime(context));
		timedAction.setStartTime(Storage.getTimedActionStartTime(context));
		return timedAction;
	}
	
	/**
	 * A visszaszámolásért felelõs osztály
	 * @author Albert
	 *
	 */
	private class ActionCountdown extends CountDownTimer {
		private TimedAction timedAction;
		
		public ActionCountdown(TimedAction timedAction, long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			this.timedAction = timedAction;
		}
		@Override
		public void onFinish() {
			Logger.writeToLog("onFinish");
			Storage.setTimedActionType(context, TimedAction.NONE);
			context.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(mListener!=null) mListener.onFinishUpload();
				}
			});
			CommunicatorEnd communicator = new CommunicatorEnd();
			communicator.execute(timedAction);
		}
		@Override
		public void onTick(long millisUntilFinished) {
			if(mListener!=null) mListener.onTick(millisUntilFinished);
			//TODO if(mListener!=null) mListener.onTick((long)Math.ceil(millisUntilFinished/1000));
		}
	}
	
	/**
	 * Befejezésnél a szerverrel való kommunikációért felelõs
	 * @author Albert
	 *
	 */
	private class CommunicatorEnd extends AsyncTask<TimedAction, Void, Boolean> {
		@Override
		protected Boolean doInBackground(TimedAction... params) {
			CommunicatorTimedAction comm = new CommunicatorTimedAction(context, Storage.getUserSessionId(context));
			boolean vege = comm.refreshData();
			if(!vege && errorCount <= 5) {
				Logger.writeToLog("ERROR "+errorCount);
				errorCount++;
				params[0].startWhenError();
			}
			else if(errorCount >= 5) {
				Logger.writeToLog("ERROR - "+errorCount);
				RUNNING = false;
				context.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(mListener!=null) mListener.onError();
						
					}
				});
				errorCount=0;
			}
			else {
				errorCount = 0;
				params[0].endTimedAction();
				
				
				context.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(mListener!=null) mListener.onFinish();
					}
				});
				RUNNING = false;
			}
			return vege;
		}
	}
	/**
	 * Megszakításért felelõs
	 * @author Albert
	 *
	 */
	private class Async extends AsyncTask<Integer, Void, Void> {
		public final static int STOP_TIMED_ACTION = 1;
		@Override
		protected Void doInBackground(Integer... params) {
			if(params[0] == STOP_TIMED_ACTION) {
				CommunicatorTimedAction comm = new CommunicatorTimedAction(context);
				comm.stopAction();
			}
			return null;
		}
		
	}

	
	public long getStartTime() {
		return startTime;
	}
	public long getFirstStartTime() {
		return firstStartTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getGoal() {
		return goal;
	}

	public void setGoal(int goal) {
		this.goal = goal;
	}
	
	public int getGoal_2() {
		return goal_2;
	}

	public void setGoal_2(int goal_2) {
		this.goal_2 = goal_2;
	}

	@Override
	public String toString() {
		return "TimedAction [firstStartTime=" + firstStartTime + ", startTime="
				+ startTime + ", endTime=" + endTime + ", type=" + type
				+ ", goal=" + goal + ", goal_2=" + goal_2 + ", helperint="
				+ helperint + ", helperint2=" + helperint2 + "]";
	}

	



	
}
