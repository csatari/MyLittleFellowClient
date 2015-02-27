package hu.jex.mylittlefellow.gui;

import hu.jex.mylittlefellow.R;
import hu.jex.mylittlefellow.model.Place;
import hu.jex.mylittlefellow.model.Place.OnPlaceEvent;
import hu.jex.mylittlefellow.model.Logger;
import hu.jex.mylittlefellow.model.PlaceAction;
import hu.jex.mylittlefellow.model.Resource;
import hu.jex.mylittlefellow.model.ResourceAction;
import hu.jex.mylittlefellow.model.ResourceAction.ActionButtonType;
import hu.jex.mylittlefellow.model.ResourceAction.OnActionEventListener;
import hu.jex.mylittlefellow.model.Tile;
import hu.jex.mylittlefellow.model.Tile.OnTileDownloaded;
import hu.jex.mylittlefellow.model.TimedAction;
import hu.jex.mylittlefellow.storage.Storage;
import hu.jex.mylittlefellow.storage.TileDatabaseAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Egy terület oldala
 * @author Albert
 *
 */
public class PlaceActivity extends BaseActivity implements OnItemClickListener, OnPlaceEvent {
	
	private static Activity context;
	private static GridView actionGridview;
	private static GridView resourceGridview;
	private static GridView placeActionGridview;
	private static TextView countdownTextview;
	
	private static Tile tile;
	private static ActionAdapter actionAdapter;
	private static ResourceAdapter resourceAdapter;
	private static PlaceActionAdapter placeActionAdapter;
	
	private static Place place;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.activity_place);
		Intent intent = getIntent();
		int tileid = intent.getIntExtra("tileid", 0);
		TileDatabaseAdapter db = new TileDatabaseAdapter(context);
		db.open();
		try {
			tile = db.getById(tileid);
		}
		catch(Exception e) {
			Logger.writeException(e);
			goBackToRootActivity();
			return;
		}
		finally {
			db.close();
		}
		place = new Place(context,tile);
		place.setCustomEventListener(this);
		
		//Logger.writeToLog("Place, amire kattintott: " + tile.toString());
		actionGridview = (GridView) findViewById(R.id.gridViewLeft);
		actionAdapter = new ActionAdapter();
		actionGridview.setAdapter(actionAdapter);
		actionGridview.setOnItemClickListener(this);
		actionGridview.setPadding(25, 25, 25, 25);
		actionGridview.setHorizontalSpacing(25);
		
		resourceGridview = (GridView) findViewById(R.id.gridViewRight);
		resourceAdapter = new ResourceAdapter();
		resourceGridview.setAdapter(resourceAdapter);
		resourceGridview.setHorizontalSpacing(25);
		resourceGridview.setGravity(Gravity.RIGHT);
		
		placeActionGridview = (GridView) findViewById(R.id.gridViewRightDown);
		placeActionAdapter = new PlaceActionAdapter();
		placeActionGridview.setAdapter(placeActionAdapter);
		placeActionGridview.setHorizontalSpacing(25);
		placeActionGridview.setGravity(Gravity.RIGHT);
		
		countdownTextview = (TextView) findViewById(R.id.textViewRightTop);
		
		setBackground(tile.getType());
	}
	@Override
	public void onPause() {
		super.onPause();
	}
	@Override
	public void onResume() {
		super.onResume();
		Logger.writeToLog("Tile újra betöltve, a tileid: "+tile.getId());
		if(TimedAction.RUNNING) {
			countdownTextview.setVisibility(TextView.VISIBLE);
		}
		else {
			countdownTextview.setVisibility(TextView.GONE);
		}
		actionAdapter.notifyDataSetChanged();
		resourceAdapter.notifyDataSetChanged();
		if(placeActionAdapter != null) {
			placeActionAdapter.notifyDataSetChanged();
		}
		place.getTax();
	}
	/**
	 * Beállítja az activity megfelelõ háttérképét
	 * @param type
	 */
	private void setBackground(int type) {
		findViewById(R.id.background).setBackgroundResource(Tile.getTileBackground(tile.getType()));
	}
	
	/**
	 * Elindítja az adófizetés mechanizmusát
	 */
	public void doTaxAction() {
		place.amIOwner();
		
		if(tile.getTax() != null) {
			place.payTax();
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
			AlertDialog alertDialog = null;
			alertDialogBuilder.setTitle("Pay taxes");
			alertDialogBuilder
			.setMessage(tile.getTax().getResourcesInText())
			.setCancelable(false)
			.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
				}
			});

			alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		}
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		//Logger.writeToLog("megnyomva"+view.getId()+" "+R.id.gridViewLeft+" "+position+" "+id);
		if(view.getId() == R.id.gridViewLeft) {
			
		}
	}
	/**
	 * A bal oldali gombokat jeleníti meg és kezeli
	 * @author Albert
	 *
	 */
	private class ActionAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return ResourceAction.getActionButtonCount(Storage.getHomeId(context),Storage.getUserTileId(context),tile);
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageButton button = ResourceAction.getActionButton(context,position, tile);
			ResourceAction.setCustomEventListener(new OnActionEventListener() {
				@Override
				public void onExamineEvent() {
					Communicator comm = new Communicator(Communicator.EXAMINE);
					comm.execute(tile);
				}
				@Override
				public void onSettleDownEvent() {
					Communicator comm = new Communicator(Communicator.SETTLEDOWN);
					comm.execute(tile);
				}
				@Override
				public void onActionEvent(ActionButtonType type) {
					Communicator comm = new Communicator(Communicator.OTHER_ACTION);
					comm.setActionType(type);
					comm.execute(tile);
				}
				@Override
				public void onTravelEvent() {
					Communicator comm = new Communicator(Communicator.TRAVEL);
					comm.execute(tile);
				}
			});
			return button;
		}
	}
	
	/**
	 * A nyersanyagokat jeleníti meg.
	 * @author Albert
	 *
	 */
	private class ResourceAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return Resource.getPlaceResourceNumber(tile);
		}
		@Override
		public Object getItem(int arg0) {
			return null;
		}
		@Override
		public long getItemId(int arg0) {
			return 0;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView textView = new TextView(context);
			textView.setText(Resource.getPlaceResourceByPosition(position, tile));
			textView.setTextColor(Color.MAGENTA);
			textView.setGravity(Gravity.RIGHT);
			textView.setPadding(0, 0, 20, 0);
			return textView;
		}
	}
	/**
	 * A jobb oldali gombokat jeleníti meg
	 * @author Albert
	 *
	 */
	private class PlaceActionAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			//Logger.writeToLog("A placeactionök száma: "+PlaceAction.getPlaceActionCount(context, tile, Storage.getUserTileId(context))+" usertileid: "+Storage.getUserTileId(context));
			return PlaceAction.getPlaceActionCount(context, tile, Storage.getUserTileId(context));
		}
		@Override
		public Object getItem(int arg0) {
			return null;
		}
		@Override
		public long getItemId(int arg0) {
			return 0;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//Logger.writeToLog("placeaction getView....position: "+position);
			ImageButton button = PlaceAction.getPlaceActionButton(context, position, tile);
			PlaceAction.setCustomEventListener(new PlaceAction.OnPlaceActionEventListener() {
				@Override
				public void onStorageEnter() {
					//Logger.writeToLog("onStorageEnter");
					Intent it = new Intent(PlaceActivity.this,StorageActivity.class);
					startActivity(it);
				}
				@Override
				public void onBuild() {
					//Logger.writeToLog("onBuild");
					Intent it = new Intent(PlaceActivity.this,TileSliceChooseActivity.class);
					it.putExtra("tileid", tile.getId());
					startActivity(it);
				}
				@Override
				public void onTower() {
					//Logger.writeToLog("onTower");
					Intent it = new Intent(PlaceActivity.this,TowerActivity.class);
					startActivity(it);
				}
				@Override
				public void onToolStation() {
					//Logger.writeToLog("onToolStation");
					Intent it = new Intent(PlaceActivity.this,ToolstationActivity.class);
					startActivity(it);
				}
				@Override
				public void onTax() {
					//Logger.writeToLog("onTax");
					Intent it = new Intent(PlaceActivity.this,TaxActivity.class);
					startActivity(it);
				}
			});
			return button;
		}
	}
	
	/**
	 * A nem a UI threaden végzendõ dolgokat kezeli
	 * @author Albert
	 *
	 */
	private class Communicator extends AsyncTask<Tile, Void, Boolean> {
		public static final int EXAMINE = 0;
		public static final int TRAVEL = 1;
		public static final int SETTLEDOWN = 2;
		public static final int OTHER_ACTION = 3;
		
		private int type;
		private ActionButtonType actionType;
		public Communicator(int type) {
			this.type = type;
		}
		public void setActionType(ActionButtonType actionButtonType) {
			actionType = actionButtonType;
		}
		@Override
		protected Boolean doInBackground(Tile... params) {
			if(type == EXAMINE) {
				startTimedAction(TimedAction.EXAMINE, params[0].getId());
			}
			else if(type == TRAVEL) {
				startTimedAction(TimedAction.TRAVEL, params[0].getId());
			}
			else if(type == SETTLEDOWN) {
				startTimedAction(TimedAction.SETLLEDOWN, params[0].getId());
			}
			else if(type == OTHER_ACTION) {
				if(actionType == ActionButtonType.COLLECTTWIG) {
					if(params[0].getResource2() == 0) {
						InformationDialog.errorDialogUIThread(context, "No more resources...", null);
						return false;
					}
				}
				else {
					if(params[0].getResource1() == 0) {
						InformationDialog.errorDialogUIThread(context, "No more resources...", null);
						return false;
					}
				}
				int type = 0;
				switch(actionType) {
					case WOODCUT: type = Resource.WOOD;  break;
					case COLLECTBERRIES: type = Resource.BERRY;  break;
					case COLLECTTWIG: type = Resource.TWIG;  break;
					case FISH: type = Resource.FISH;  break;
					case HUNT: type = Resource.MEAT;  break;
					case MINE: type = Resource.STONE;  break;
					default: break;
				}
				if(actionType == ActionButtonType.COLLECTTWIG) {
					startTimedAction(TimedAction.RESOURCE2, type,tile.getId());
				}
				else {
					startTimedAction(TimedAction.RESOURCE1, type,tile.getId());
				}
				return true;
			}
			context.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					actionAdapter.notifyDataSetChanged();
					resourceAdapter.notifyDataSetChanged();
				}
			});
			return false;
		}
	}
	
	

	@Override
	protected ActivityType getActivityType() {
		return ActivityType.PLACE;
	}
	@Override
	protected void onTimedActionFinish(TimedAction timedAction) {
		//Logger.writeToLog("onTimedActionFinishPlace");
		Tile.setOnTileDownloadedListener(new OnTileDownloaded() {
			@Override
			public void onFinished(Tile t) {
				if(tile.getId() == t.getId()) {
					tile = t;
				}
				//Logger.writeToLog("tile downloading finished..."+tile.getId());
				resourceAdapter.notifyDataSetChanged();
				if(placeActionAdapter != null) {
					//Logger.writeToLog("placeactionadapter != null");
					
					placeActionAdapter.notifyDataSetChanged();
				}
				else {
					//Logger.writeToLog("placeactionadapter == null");
					placeActionGridview = (GridView) findViewById(R.id.gridViewRightDown);
					placeActionAdapter = new PlaceActionAdapter();
					placeActionGridview.setAdapter(placeActionAdapter);
					placeActionGridview.setHorizontalSpacing(25);
					placeActionGridview.setGravity(Gravity.RIGHT);
					placeActionAdapter.notifyDataSetChanged();
				}
				actionAdapter.notifyDataSetChanged();
				resourceAdapter.notifyDataSetChanged();
			}
		});
		try {			
			Tile.refreshTileWithDownload(context, tile);
		}
		catch(NullPointerException e) { //elfelejtette a tile-t
			goBackToRootActivity();
		}
		countdownTextview.setVisibility(TextView.GONE);
		/*if(timedAction.getType() == TimedAction.EXAMINE) {
			Tile examinedTile = Tile.getTileFromAllTilesById(VisibleTileAdapter.getAllTiles(), timedAction.getGoal());
			tile = examinedTile;
		}*/
		
		doTaxAction();
	}
	@Override
	protected void onTimedActionTick(TimedAction timedAction,long secondsUntilFinished) {
		//Logger.writeToLog("tick..."+secondsUntilFinished);
		countdownTextview.setText(timedAction.getTimedActionType()+" "+(int)Math.ceil((long)secondsUntilFinished/1000)+"");
	}
	@Override
	protected void onTimedActionStart(TimedAction timedAction) {
		//Logger.writeToLog("start...");
		countdownTextview.setVisibility(TextView.VISIBLE);
	}
	@Override
	protected void onTimedActionError(TimedAction timedAction) {
		countdownTextview.setVisibility(TextView.GONE);
	}
	@Override
	protected void onTimedActionFinishUpload(TimedAction timedAction) {
		countdownTextview.setText("Uploading...");
	}
	@Override
	public void onError() {
		goBackToRootActivity();
	}
	@Override
	public void onGetTaxFinished(Tile tile) {
		PlaceActivity.tile = tile;
		doTaxAction();
	}
	@Override
	public void onAmIOwnerFinished() {
		if(placeActionAdapter != null) {
			placeActionAdapter.notifyDataSetChanged();
		}
	}
	

}
