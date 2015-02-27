package hu.jex.mylittlefellow.model;

import hu.jex.mylittlefellow.communicator.CommunicatorBuilding;
import hu.jex.mylittlefellow.storage.BuildingDatabaseAdapter;
import hu.jex.mylittlefellow.storage.TileDatabaseAdapter;

import java.util.ArrayList;

import android.app.Activity;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

/**
 * Az építkezõ oldal modellje
 * @author Albert
 *
 */
public class TileSliceChoose {
	private static Activity context;
	
	private static Tile tile;
	private static int slice;
	private static ArrayList<BuildingReceipt> buildingReceipts;
	
	static OnTileSliceChooseEvent mListener;
	public interface OnTileSliceChooseEvent {
		/**
		 * Ki kell rajzolni egy épületet
		 * @param building Az épület
		 * @param center A koordináta, ahova rajzolni kell 
		 * @param distance A szélessége méterben
		 */
		public void onDrawOneBuilding(Building building, LatLng center, float distance);
		/**
		 * Ki kell rajzolni egy ablakot az megadott receptekkel
		 * @param buildingReceipts A receptek
		 */
		public void onShowDialog(ArrayList<BuildingReceipt> buildingReceipts);
		/**
		 * Elkezdett egy épületet építeni
		 * @param tileid A terület azonosítója
		 * @param buildingReceiptType A recept típusa
		 * @param slice A terület részlete
		 */
		public void onStartBuilding(int tileid, int buildingReceiptType,int slice);
	}
	/**
	 * Az eseménykezelõ beállítása
	 * @param eventListener
	 */
	public void setCustomEventListener(OnTileSliceChooseEvent eventListener) {
		mListener=eventListener;
	}
	
	public TileSliceChoose(Activity context) {
		TileSliceChoose.context = context;
	}
	/**
	 * Kikeresi a területet id szerint
	 * @param tileid
	 */
	public void setTileById(int tileid) {
		TileDatabaseAdapter db = new TileDatabaseAdapter(context);
		db.open();
		tile = db.getById(tileid);
		db.close();
	}
	
	public Tile getTile() {
		return tile;
	}
	/**
	 * Lekérdezi a kamera pozícióját
	 * @return
	 */
	public CameraPosition getCameraPosition() {
		return new CameraPosition.Builder()
		.target(Tile.getTileFromAllTilesById(VisibleTileAdapter.getAllTiles(), tile.getId()).getCenteredCoordinate())
		.bearing(0.0f).tilt(0.0f).zoom(16f).build();
	}
	/**
	 * Betölti a megjelenítendõ területeket adatbázisból
	 * @param visibleTileAdapter
	 */
	public void loadTilesFromDatabase(VisibleTileAdapter visibleTileAdapter) {
		TileDatabaseAdapter db = new TileDatabaseAdapter(context);
		db.open();
		try {
			visibleTileAdapter.setAllTiles(db.getAll());
		}
		catch(Exception e) {
			Logger.writeException(e);
		}
		finally {
			db.close();
		}
	}
	/**
	 * Lekérdezi a kirajzolandó épületeket adatbázisból
	 */
	public void getAllBuildingsForDraw() {
		BuildingDatabaseAdapter buildingDb = new BuildingDatabaseAdapter(context);
		buildingDb.open();
		try {
			for(Building building : buildingDb.getBuildingsOnTile(tile.getId())) {
				float distance = Building.getBuildingDistance(tile, building.getSliceId());
				LatLng buildingCenter = Building.getBuildingCenter(tile, building.getSliceId());
				//Logger.writeToLog("Az épület kirajzolása: distance: "+distance+" center: "+buildingCenter.toString());
				if(mListener!=null)mListener.onDrawOneBuilding(building, buildingCenter, distance);
			}
		}
		catch(Exception e) {
			Logger.writeException(e);
		}
		finally {
			buildingDb.close();
		}
	}
	/**
	 * Lekérdezi az épületreceptek számát
	 * @return A szám
	 */
	public int getBuildingReceiptSize() {
		if(buildingReceipts == null) {
			return 0;
		}
		return buildingReceipts.size();
	}
	
	public BuildingReceipt getBuildingReceiptOnPosition(int position) {
		return buildingReceipts.get(position);
	}
	/**
	 * Elkezdi letölteni az összes építhetõ épületlistát a megadott koordináta alapján
	 * @param coordinate A koordináta, amibõl kiszámítja, hogy melyik területrészletre kattintott
	 */
	public void downloadAllBuildables(LatLng coordinate) {
		//Logger.writeToLog("coordinate: "+coordinate);
		slice = tile.getCoordinateTileSlice(coordinate);
		//Logger.writeToLog("Slice is: "+slice);
		if(slice > 0) {
			Communicator c = new Communicator();
			c.execute(0);
		}
	}
	/**
	 * Elkezdi az épület építését
	 * @param buildingReceiptType A recept azonosítója
	 */
	public void startBuilding(int buildingReceiptType) {
		Communicator c = new Communicator();
		c.execute(1,buildingReceiptType);
	}
	
	/**
	 * A szerverrel való kommunikáció másik szálon
	 * @author Albert
	 *
	 */
	private class Communicator extends AsyncTask<Integer, Void, Void> {
		@Override
		protected Void doInBackground(Integer... params) {
			CommunicatorBuilding comm = new CommunicatorBuilding(context);
			if(params[0] == 0) {
				buildingReceipts = comm.getAllBuildables(tile.getId(), slice);
				context.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(mListener!=null)mListener.onShowDialog(buildingReceipts);
					}
				});
			}
			else if(params[0] == 1) {
				if(mListener!=null)mListener.onStartBuilding(tile.getId(), params[1], slice);
			}
			return null;
		}
	}
}
