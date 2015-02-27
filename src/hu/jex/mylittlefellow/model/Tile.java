package hu.jex.mylittlefellow.model;

import hu.jex.mylittlefellow.R;
import hu.jex.mylittlefellow.communicator.CommunicatorTile;
import hu.jex.mylittlefellow.storage.TileDatabaseAdapter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

/**
 * Egy terület modellje
 * @author Albert
 *
 */
public class Tile extends SaveableObject {
	
	public enum TileTypes {
		FOREST, PRAIRIE, SHRUBBY, SAND, STONE_QUARRY, CAVE, LAKE, SETTLEMENT, RIVER
	}
	public final static int FOREST = 0;
	public final static int PRAIRIE = 1;
	public final static int SHRUBBY = 2;
	public final static int SAND = 3;
	public final static int STONE_QUARRY = 4;
	public final static int CAVE = 5;
	public final static int LAKE = 6;
	public final static int SETTLEMENT = 7;
	public final static int RIVER = 8;
	
	public final static int NORTHWEST = 1;
	public final static int NORTH = 2;
	public final static int NORTHEAST = 3;
	public final static int WEST = 4;
	public final static int MIDDLE = 5;
	public final static int EAST = 6;
	public final static int SOUTHWEST = 7;
	public final static int SOUTH = 8;
	public final static int SOUTHEAST = 9;
	
	private static LatLng center = new LatLng(0.0, 0.0);
	public static double tileSize = 0.005;
	private double tileCenterLatitude;
	private double tileCenterLongitude;
	
	private int type; //terület típusa
	private int resource1; //elsõ nyersanyag száma
	private int resource2; //2. nyersanyag száma
	private int resource3; //3. nyersanyag száma
	private int id; //terület azonosítója
	private boolean examined; //meg van-e vizsgálva
	private String owner; //A földesúr neve
	private ResourceStorage tax; //A beállított adó
	private int population = 0; //A lakosok száma

	//events
	static OnTileDownloaded mListener;
	public interface OnTileDownloaded {
		/**
		 * Sikeresen letöltõdött a terület
		 * @param t
		 */
		public void onFinished(Tile t);
	}
	/**
	 * Az eseménykezelõ beállítása
	 * @param eventListener
	 */
	public static void setOnTileDownloadedListener(OnTileDownloaded eventListener) {
		mListener=eventListener;
	}

	public void setTileCenterLatitude(double latitude) {
		this.tileCenterLatitude = latitude;
	}
	public void setTileCenterLongitude(double longitude) {
		this.tileCenterLongitude = longitude;
	}
	public double getTileCenterLatitude() {
		return tileCenterLatitude;
	}
	public double getTileCenterLongitude() {
		return tileCenterLongitude;
	}
	public LatLng getCenteredCoordinate() {
		return new LatLng(tileCenterLatitude,tileCenterLongitude);
	}
	public LatLng getNorthWest() {
		return new LatLng(tileCenterLatitude+(tileSize/2),tileCenterLongitude-(tileSize/2));
	}
	public LatLng getNorthEast() {
		return new LatLng(tileCenterLatitude+(tileSize/2),tileCenterLongitude+(tileSize/2));
	}
	public LatLng getSouthWest() {
		return new LatLng(tileCenterLatitude-(tileSize/2),tileCenterLongitude-(tileSize/2));
	}
	public LatLng getSouthEast() {
		return new LatLng(tileCenterLatitude-(tileSize/2),tileCenterLongitude+(tileSize/2));
	}
	public LatLng getSouth() {
		return new LatLng(tileCenterLatitude-(tileSize/2),tileCenterLongitude);
	}
	public LatLng getNorth() {
		return new LatLng(tileCenterLatitude+(tileSize/2),tileCenterLongitude);
	}
	public LatLng getEast() {
		return new LatLng(tileCenterLatitude,tileCenterLongitude+(tileSize/2));
	}
	public LatLng getWest() {
		return new LatLng(tileCenterLatitude,tileCenterLongitude-(tileSize/2));
	}
	//Középsõ épület
	public LatLng getCenterBuildingWest() {
		return new LatLng(tileCenterLatitude, tileCenterLongitude-(tileSize/6));
	}
	public LatLng getCenterBuildingEast() {
		return new LatLng(tileCenterLatitude, tileCenterLongitude+(tileSize/6));
	}
	//Északnyugati épület
	public LatLng getNorthWestBuildingWest() {
		return new LatLng(tileCenterLatitude+(tileSize/3), tileCenterLongitude-((tileSize)/2));
	}
	public LatLng getNorthWestBuildingEast() {
		return new LatLng(tileCenterLatitude+(tileSize/3), tileCenterLongitude-((tileSize)/6));
	}
	public LatLng getNorthWestBuildingCenter() {
		return new LatLng(tileCenterLatitude+(tileSize/3), tileCenterLongitude-((tileSize)/3));
	}
	//Északi épület
	public LatLng getNorthBuildingWest() {
		return new LatLng(tileCenterLatitude+(tileSize/3), tileCenterLongitude-(tileSize/6));
	}
	public LatLng getNorthBuildingEast() {
		return new LatLng(tileCenterLatitude+(tileSize/3), tileCenterLongitude+(tileSize/6));
	}
	public LatLng getNorthBuildingCenter() {
		return new LatLng(tileCenterLatitude+(tileSize/3), tileCenterLongitude);
	}
	//Északkeleti épület
	public LatLng getNorthEastBuildingWest() {
		return new LatLng(tileCenterLatitude+(tileSize/3), tileCenterLongitude+(tileSize/6));
	}
	public LatLng getNorthEastBuildingEast() {
		return new LatLng(tileCenterLatitude+(tileSize/3), tileCenterLongitude+(tileSize/2));
	}
	public LatLng getNorthEastBuildingCenter() {
		return new LatLng(tileCenterLatitude+(tileSize/3), tileCenterLongitude+(tileSize/3));
	}
	//Nyugati épület
	public LatLng getWestBuildingWest() {
		return new LatLng(tileCenterLatitude, tileCenterLongitude-((tileSize)/2));
	}
	public LatLng getWestBuildingEast() {
		return new LatLng(tileCenterLatitude, tileCenterLongitude-((tileSize)/6));
	}
	public LatLng getWestBuildingCenter() {
		return new LatLng(tileCenterLatitude, tileCenterLongitude-((tileSize)/3));
	}
	//Keleti épület
	public LatLng getEastBuildingWest() {
		return new LatLng(tileCenterLatitude, tileCenterLongitude+(tileSize/6));
	}
	public LatLng getEastBuildingEast() {
		return new LatLng(tileCenterLatitude, tileCenterLongitude+(tileSize/2));
	}
	public LatLng getEastBuildingCenter() {
		return new LatLng(tileCenterLatitude, tileCenterLongitude+(tileSize/3));
	}
	//Délnyugati épület
	public LatLng getSouthWestBuildingWest() {
		return new LatLng(tileCenterLatitude-(tileSize/3), tileCenterLongitude-((tileSize)/2));
	}
	public LatLng getSouthWestBuildingEast() {
		return new LatLng(tileCenterLatitude-(tileSize/3), tileCenterLongitude-((tileSize)/6));
	}
	public LatLng getSouthWestBuildingCenter() {
		return new LatLng(tileCenterLatitude-(tileSize/3), tileCenterLongitude-((tileSize)/3));
	}
	//Déli épület
	public LatLng getSouthBuildingWest() {
		return new LatLng(tileCenterLatitude-(tileSize/3), tileCenterLongitude-(tileSize/6));
	}
	public LatLng getSouthBuildingEast() {
		return new LatLng(tileCenterLatitude-(tileSize/3), tileCenterLongitude+(tileSize/6));
	}
	public LatLng getSouthBuildingCenter() {
		return new LatLng(tileCenterLatitude-(tileSize/3), tileCenterLongitude);
	}
	//Délkeleti épület
	public LatLng getSouthEastBuildingWest() {
		return new LatLng(tileCenterLatitude-(tileSize/3), tileCenterLongitude+(tileSize/6));
	}
	public LatLng getSouthEastBuildingEast() {
		return new LatLng(tileCenterLatitude-(tileSize/3), tileCenterLongitude+(tileSize/2));
	}
	public LatLng getSouthEastBuildingCenter() {
		return new LatLng(tileCenterLatitude-(tileSize/3), tileCenterLongitude+(tileSize/3));
	}
	
	//vonalak behúzásához
	public LatLng getNorthOneThird() {
		return new LatLng(tileCenterLatitude+(tileSize/2), tileCenterLongitude-(tileSize/6));
	}
	public LatLng getSouthOneThird() {
		return new LatLng(tileCenterLatitude-(tileSize/2), tileCenterLongitude-(tileSize/6));
	}
	public LatLng getNorthTwoThird() {
		return new LatLng(tileCenterLatitude+(tileSize/2), tileCenterLongitude+(tileSize/6));
	}
	public LatLng getSouthTwoThird() {
		return new LatLng(tileCenterLatitude-(tileSize/2), tileCenterLongitude+(tileSize/6));
	}
	public LatLng getWestOneThird() {
		return new LatLng(tileCenterLatitude+(tileSize/6), tileCenterLongitude-(tileSize/2));
	}
	public LatLng getEastOneThird() {
		return new LatLng(tileCenterLatitude+(tileSize/6), tileCenterLongitude+(tileSize/2));
	}
	public LatLng getWestTwoThird() {
		return new LatLng(tileCenterLatitude-(tileSize/6), tileCenterLongitude-(tileSize/2));
	}
	public LatLng getEastTwoThird() {
		return new LatLng(tileCenterLatitude-(tileSize/6), tileCenterLongitude+(tileSize/2));
	}
	
	/**
	 * Megmondja egy koordinátáról, hogy az adott tile melyik slice-ában van. 0, ha nincs a tile-ban
	 * @param coordinate
	 * @return
	 */
	public int getCoordinateTileSlice(LatLng coordinate) {
		//lekérdezzük, hogy benne van-e
		//melyik slice-ban van benne
		if(isPointInSlice(getNorthWestBuildingCenter(), coordinate)) {
			return NORTHWEST;
		}
		else if(isPointInSlice(getNorthBuildingCenter(), coordinate)) {
			return NORTH;
		}
		else if(isPointInSlice(getNorthEastBuildingCenter(), coordinate)) {
			return NORTHEAST;
		}
		else if(isPointInSlice(getWestBuildingCenter(), coordinate)) {
			return WEST;
		}
		else if(isPointInSlice(getCenteredCoordinate(), coordinate)) {
			return MIDDLE;
		}
		else if(isPointInSlice(getEastBuildingCenter(), coordinate)) {
			return EAST;
		}
		else if(isPointInSlice(getSouthWestBuildingCenter(), coordinate)) {
			return SOUTHWEST;
		}
		else if(isPointInSlice(getSouthBuildingCenter(), coordinate)) {
			return SOUTH;
		}
		else if(isPointInSlice(getSouthEastBuildingCenter(), coordinate)) {
			return SOUTHEAST;
		}
		return 0;
	}
	/**
	 * Megmondja egy koordinátáról, hogy benne van-e az adott területrészletben
	 * @param sliceCenter a területrészlet középpontja
	 * @param coordinate a kérdéses koordináta
	 * @return Igaz, ha benne van
	 */
	private boolean isPointInSlice(LatLng sliceCenter, LatLng coordinate) {
		if(isPointInRectangle(
				new LatLng(
						sliceCenter.latitude+tileSize/6,
						sliceCenter.longitude-tileSize/6),
				new LatLng(
						sliceCenter.latitude-tileSize/6,
						sliceCenter.longitude+tileSize/6),
				coordinate)) {
			return true;
		}
		return false;
	}
	/**
	 * Megmondja egy pontról, hogy benne van-e a téglalapban (nem forgatott téglalap)
	 * @param northwest A téglalap északnyugati koordinátája
	 * @param southeast A téglalap délkeleti koordinátája
	 * @param coordinate A kérdéses pont
	 * @return Igaz, ha benne van
	 */
	private boolean isPointInRectangle(LatLng northwest, LatLng southeast, LatLng coordinate) {
		if(northwest.latitude > coordinate.latitude && southeast.latitude < coordinate.latitude &&
				northwest.longitude < coordinate.longitude && southeast.longitude > coordinate.longitude) {
			return true;
		}
		return false;
	}
	/**
	 * Beállítja a terület közepét egy pontból
	 * @param location Egy pont, nem muszáj középpontnak lennie
	 * @return
	 */
	public Tile setTileCenterFromLocation(LatLng location) {
		tileCenterLatitude = ((Math.round((location.latitude-center.latitude)/(tileSize)))*tileSize)+center.latitude;
		tileCenterLongitude = ((Math.round((location.longitude-center.longitude)/(tileSize)))*tileSize)+center.longitude;
		return this;
	}
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getResource1() {
		return resource1;
	}
	public void setResource1(int resource) {
		this.resource1 = resource;
	}
	public int getResource2() {
		return resource2;
	}
	public void setResource2(int resource) {
		this.resource2 = resource;
	}
	public int getResource3() {
		return resource3;
	}
	public void setResource3(int resource) {
		this.resource3 = resource;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public boolean isExamined() {
		return examined;
	}
	public void setExamined(boolean examined) {
		this.examined = examined;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public ResourceStorage getTax() {
		return tax;
	}
	public void setTax(ResourceStorage tax) {
		this.tax = tax;
	}
	public int getPopulation() {
		return population;
	}
	public void setPopulation(int population) {
		this.population = population;
	}
	/**
	 * Lekérdezi a terület színét típus szerint
	 * @return A szín
	 */
	public int getColor() {
		switch(type) {
		case 0: //erdõ
			return Color.argb(200,7, 135, 0); //dark green
		case 1: //puszta
			return Color.argb(200,167,214,165); //light green
		case 2: //bozótos
			return Color.argb(230,133, 84, 84); //brown
		case 3: //sivatag
			return Color.argb(200,255, 255, 79); //yellow
		case 4: //kõlelõhely
			return Color.argb(200,212, 212, 212); //lightgray
		case 5: //barlang
			return Color.argb(200,115, 115, 115); //darkgray	
		case 6: //tó
			return Color.argb(200,41, 0, 207); //dark blue
		case 7: //város
			return Color.argb(200,242, 126, 221); //magenta
		case 8: //folyó
			return Color.argb(200,126, 190, 242); //light blue
		case 9:
			return Color.RED;
		default: return Color.WHITE;
		}
	}
	/**
	 * Lekérdezi a terület nevét
	 * @return A név
	 */
	public String getTypeString() {
		switch(type) {
		case 0: //erdõ
			return "Forest"; //dark green
		case 1: //puszta
			return "Prairie"; //light green
		case 2: //bozótos
			return "Shrubby"; //brown
		case 3: //sivatag
			return "Desert"; //yellow
		case 4: //kõlelõhely
			return "Stone quarry"; //lightgray
		case 5: //barlang
			return "Cave"; //darkgray	
		case 6: //tó
			return "Lake"; //dark blue
		case 7: //város
			return "Settlement"; //magenta
		case 8: //folyó
			return "River"; //light blue
		case 9: //loading
			return "Loading...";
		default: return "wat";
		}
	}
	@Override
	public String toString() {
		return "Tile [tileCenterLatitude=" + tileCenterLatitude
				+ ", tileCenterLongitude=" + tileCenterLongitude + ", type="
				+ type + ", resource1=" + resource1 + ", resource2="
				+ resource2 + ", resource3=" + resource3 + ", id=" + id
				+ ", examined=" + examined + ", owner=" + owner + "]";
	}
	/**
	 * Visszatér a megadott koordináta központosításával (A négyzetének központjával)
	 * @param coordinate
	 * @return
	 */
	public static LatLng getCenteredCoordinate(LatLng coordinate) {
		//A pontos számítások miatt ilyen bonyolultan kell megoldani a kiszámolást
		double atemp3 = Math.round((coordinate.longitude-center.longitude)/tileSize);
		double atemp4 = atemp3*5;
		atemp4 = atemp4/1000;
		double atemp5 = atemp4+center.longitude;
		
		double otemp3 = Math.round((coordinate.latitude-center.latitude)/tileSize);
		double otemp4 = otemp3*5;
		otemp4 = otemp4/1000;
		double otemp5 = otemp4+center.latitude;
		
		/*LatLng latLng = new LatLng(
				((Math.round((coordinate.latitude-center.latitude)/(tileSize)))*tileSize)+center.latitude,
				((Math.round((coordinate.longitude-center.longitude)/(tileSize)))*tileSize)+center.longitude);*/
		LatLng latLng = new LatLng(
				otemp5,
				atemp5);
		//Logger.writeToLog("latLng: "+latLng.toString());
		return latLng;
	}
	/**
	 * Visszaadja a Tile-t a megadott tömbbõl koordináta alapján, ha benne van, egyébként null-t ad vissza.
	 * @param allTiles
	 * @param coordinate
	 * @return
	 */
	public static Tile getTileFromAllTilesByCoordinate(ArrayList<Tile> allTiles, LatLng coordinate) {
		Tile searchTile = new Tile();
		searchTile.setTileCenterFromLocation(coordinate);
		for(Tile tile : allTiles) {
			//Logger.writeToLog("id: "+tile.getId()+" "+tile.getCenteredCoordinate().toString()+" "+searchTile.getCenteredCoordinate().toString());
			if(compareLatLng(tile.getCenteredCoordinate(), searchTile.getCenteredCoordinate())) {
				
				return tile;
			}
		}
		return null;
	}
	/**
	 * Visszaadja a Tile-t a megadott tömbbõl az id-je alapján, ha benne van, egyébként null-t ad vissza.
	 * @param allTiles Az összes tile-t tartalmazó ArrayList
	 * @param tileid A keresett tile id-je
	 * @return
	 */
	public static Tile getTileFromAllTilesById(ArrayList<Tile> allTiles, int tileid) {
		try {
			for(Tile tile : allTiles) {
				if(tile.getId() == tileid) {
					return tile;
				}
			}
		}
		catch(Exception e) {
			Logger.writeToLog("getTileFromAllTilesById");
			Logger.writeException(e);
		}
		return null;
	}
	/**
	 * Frissíti a területet az összes eltárolt területek között
	 * @param context
	 * @param allTiles az összes területet tároló tömb
	 * @param tile a frissített terület
	 */
	public static void refreshTileInAllTiles(Context context, ArrayList<Tile> allTiles, Tile tile) {
		int i = 0;
		for(Tile onetile : allTiles) {
			if(onetile.getId() == tile.getId()) {
				//Logger.writeToLog("Megvan a tile: "+onetile.getId());
				break;
			}
			i++;
		}
		if(allTiles.size() < i) {
			allTiles.remove(i);
		}
		allTiles.add(tile);
		TileDatabaseAdapter db = new TileDatabaseAdapter(context);
		db.open();
		try {
			db.update(tile);
		}
		catch(Exception e) {
			Logger.writeException(e);
		}
		finally {
			db.close();
		}
		
	}
	
	/**
	 * Úgy frissíti a területet, hogy letölti
	 * @param context
	 * @param tile A terület
	 * @throws NullPointerException
	 */
	public static void refreshTileWithDownload(Activity context, Tile tile) throws NullPointerException {
		if(tile.getId() == 0 || VisibleTileAdapter.getAllTiles() == null) {
			throw new NullPointerException();
		}
		Async a = new Async();
		a.oldtile = tile;
		a.context = context;
		a.execute(Async.DOWNLOAD_TILE);
	}
	
	/**
	 * A terület letöltéséért felelõs másik szálon
	 * @author Albert
	 *
	 */
	private static class Async extends AsyncTask<Integer, Void, Void> {
		public final static int DOWNLOAD_TILE = 1;
		public Tile oldtile;
		public Activity context;
		@Override
		protected Void doInBackground(Integer... params) {
			if(params[0] == DOWNLOAD_TILE) {
				try {
					CommunicatorTile comm = new CommunicatorTile(context);
					int oldType = oldtile.getType();
					oldtile = comm.getTileById(oldtile.getId());
					int newType = oldtile.getType();
				//	Logger.writeToLog("Typeváltozás: "+oldType+" "+newType);
					final boolean typeChanged = oldType != newType;
					refreshTileInAllTiles(context,VisibleTileAdapter.getAllTiles(),oldtile);
					context.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if(typeChanged) {
								VisibleTileAdapter.removeTileFromMap(oldtile);
							}
							if(mListener != null) mListener.onFinished(oldtile);
						}
					});
				}
				catch(Exception e) {
					Logger.writeToLog("Hiba történt a tile letöltésekor, mert: ");
					Logger.writeException(e);
				}
			}
			return null;
		}
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(tileCenterLatitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(tileCenterLongitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tile other = (Tile) obj;
		if (Double.doubleToLongBits(tileCenterLatitude) != Double
				.doubleToLongBits(other.tileCenterLatitude))
			return false;
		if (Double.doubleToLongBits(tileCenterLongitude) != Double
				.doubleToLongBits(other.tileCenterLongitude))
			return false;
		return true;
	}

	/**
	 * Összehasonlít két koordinátát, hogy egyeznek-e. Elfogad kis pontatlanságot
	 * @param latlng1 Egyik koordináta
	 * @param latlng2 Másik koordináta
	 * @return Igaz, ha nagyjából egyeznek
	 */
	public static boolean compareLatLng(LatLng latlng1, LatLng latlng2) {
		if(latlng1.latitude-latlng2.latitude < Double.parseDouble("0.0001") && latlng1.latitude-latlng2.latitude > Double.parseDouble("-0.0001")) {
			if(latlng1.longitude-latlng2.longitude < Double.parseDouble("0.0001") && latlng1.longitude-latlng2.longitude > Double.parseDouble("-0.0001")) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Lekérdezi a terület háttérképét a megadott típusra
	 * @param type a típus
	 * @return A háttérkép resource elérése
	 */
	public static int getTileBackground(int type) {
		switch(type) {
		case Tile.FOREST:
			return R.drawable.forest_bg;
		case Tile.PRAIRIE:
			return R.drawable.prairie_bg;
		case Tile.SHRUBBY:
			return R.drawable.shrubby_bg;
		case Tile.SAND:
			return R.drawable.desert_bg;	
		case Tile.STONE_QUARRY:
			return R.drawable.stonequarry_bg;	
		case Tile.CAVE:
			return R.drawable.stonequarry_bg;	
		case Tile.LAKE:
			return R.drawable.lake_bg;	
		case Tile.RIVER:
			return R.drawable.river_bg;	
		case Tile.SETTLEMENT:
			return R.drawable.town_bg;	
		default:
			return R.drawable.forest_bg;
		}
	}
}
