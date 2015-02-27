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
 * Az �p�tkez� oldal modellje
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
		 * Ki kell rajzolni egy �p�letet
		 * @param building Az �p�let
		 * @param center A koordin�ta, ahova rajzolni kell 
		 * @param distance A sz�less�ge m�terben
		 */
		public void onDrawOneBuilding(Building building, LatLng center, float distance);
		/**
		 * Ki kell rajzolni egy ablakot az megadott receptekkel
		 * @param buildingReceipts A receptek
		 */
		public void onShowDialog(ArrayList<BuildingReceipt> buildingReceipts);
		/**
		 * Elkezdett egy �p�letet �p�teni
		 * @param tileid A ter�let azonos�t�ja
		 * @param buildingReceiptType A recept t�pusa
		 * @param slice A ter�let r�szlete
		 */
		public void onStartBuilding(int tileid, int buildingReceiptType,int slice);
	}
	/**
	 * Az esem�nykezel� be�ll�t�sa
	 * @param eventListener
	 */
	public void setCustomEventListener(OnTileSliceChooseEvent eventListener) {
		mListener=eventListener;
	}
	
	public TileSliceChoose(Activity context) {
		TileSliceChoose.context = context;
	}
	/**
	 * Kikeresi a ter�letet id szerint
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
	 * Lek�rdezi a kamera poz�ci�j�t
	 * @return
	 */
	public CameraPosition getCameraPosition() {
		return new CameraPosition.Builder()
		.target(Tile.getTileFromAllTilesById(VisibleTileAdapter.getAllTiles(), tile.getId()).getCenteredCoordinate())
		.bearing(0.0f).tilt(0.0f).zoom(16f).build();
	}
	/**
	 * Bet�lti a megjelen�tend� ter�leteket adatb�zisb�l
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
	 * Lek�rdezi a kirajzoland� �p�leteket adatb�zisb�l
	 */
	public void getAllBuildingsForDraw() {
		BuildingDatabaseAdapter buildingDb = new BuildingDatabaseAdapter(context);
		buildingDb.open();
		try {
			for(Building building : buildingDb.getBuildingsOnTile(tile.getId())) {
				float distance = Building.getBuildingDistance(tile, building.getSliceId());
				LatLng buildingCenter = Building.getBuildingCenter(tile, building.getSliceId());
				//Logger.writeToLog("Az �p�let kirajzol�sa: distance: "+distance+" center: "+buildingCenter.toString());
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
	 * Lek�rdezi az �p�letreceptek sz�m�t
	 * @return A sz�m
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
	 * Elkezdi let�lteni az �sszes �p�thet� �p�letlist�t a megadott koordin�ta alapj�n
	 * @param coordinate A koordin�ta, amib�l kisz�m�tja, hogy melyik ter�letr�szletre kattintott
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
	 * Elkezdi az �p�let �p�t�s�t
	 * @param buildingReceiptType A recept azonos�t�ja
	 */
	public void startBuilding(int buildingReceiptType) {
		Communicator c = new Communicator();
		c.execute(1,buildingReceiptType);
	}
	
	/**
	 * A szerverrel val� kommunik�ci� m�sik sz�lon
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
