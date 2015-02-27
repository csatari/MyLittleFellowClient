package hu.jex.mylittlefellow.model;

import hu.jex.mylittlefellow.communicator.CommunicatorBuilding;
import hu.jex.mylittlefellow.communicator.CommunicatorUserDetails;
import hu.jex.mylittlefellow.gui.InformationDialog;
import hu.jex.mylittlefellow.gui.MapActivity;
import hu.jex.mylittlefellow.model.IpoGetter.OnIpoGetListener;
import hu.jex.mylittlefellow.model.Tile.OnTileDownloaded;
import hu.jex.mylittlefellow.storage.BuildingDatabaseAdapter;
import hu.jex.mylittlefellow.storage.Storage;
import hu.jex.mylittlefellow.storage.TileDatabaseAdapter;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.VisibleRegion;

/**
 * A térkép modellje
 * @author Albert
 *
 */
public class Map {
	private static Activity context;
	private static VisibleTileAdapter visibleTileAdapter;

	private static GoogleMap map;
	//IPO
	private static IpoGetter ipoGetter;
	
	private static LocationManager locationManager;
	private static LocationListener locationListener;
	private static boolean discoverModeOn = false;
	
	private static Tile clickedTile;
	
	private static boolean firstLocationChange;
	
	private static ArrayList<LatLng> pendingCheckin = new ArrayList<LatLng>();

	static OnMapEvent mListener;
	public interface OnMapEvent {
		/**
		 * Sikersen betöltõdött a megnyitható infowindow
		 * @param tile
		 */
		public void onTileWindowShow(Tile tile);
		/**
		 * Meg lett nyomva az infowindow
		 * @param tile a terület 
		 * @param marker a jelölõpont
		 */
		public void onInfoWindowClicked(Tile tile,Marker marker);
		/**
		 * Hiba történt
		 */
		public void onError();
		/**
		 * Letöltõdött a terület
		 * @param tile a terület adatai
		 */
		public void onTileDownloaded(Tile tile);
		/**
		 * El lett mozdítva a kamera
		 * @param coordinate A 
		 */
		public void onMoveCamera(LatLng coordinate);
		/**
		 * Be kell kapcsolni a GPS-t
		 */
		public void turnOnGPS();
		/**
		 * Ki kell rajzolni a karakter
		 * @param coordinate A koordináta, ahova ki kell rajzolni
		 */
		public void onDrawCharacter(LatLng coordinate);
		/**
		 * Ki kell rajzolni az épületet
		 * @param building az épület
		 */
		public void onDrawBuilding(Building building);
		/**
		 * Ki kell rajzolni az otthont
		 * @param coordinate A koordinátára kell kirajzolni
		 * @param distance Az otthon szélessége méterben
		 */
		public void onDrawHome(LatLng coordinate, float distance);
		/**
		 * A megadott url-re kell menni
		 * @param url
		 */
		public void onGoToURL(String url);
	}
	/**
	 * Beállítja az eseménykezelõt
	 * @param eventListener
	 */
	public static void setCustomEventListener(OnMapEvent eventListener) {
		mListener=eventListener;
	}
	
	public Map(Activity context, GoogleMap map) {
		Map.context = context;
		visibleTileAdapter = new VisibleTileAdapter(context,map);
		Map.map = map;
		setIpoGetter();
		firstLocationChange = false;
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		Logger.writeToLog("Map konstruktor lefut");
	}
	/**
	 * Letölti az összes területet az adatbázisból
	 */
	public void loadAllTiles() {
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
	 * A karakterhez viszi a kamerát
	 */
	public void goToCharacter() {
		if(Storage.getUserTileId(context) == 0) { //ha még nincs koordinátája, Pestre irányítom
			goCameraToCoordinate(new LatLng(47.498648,19.04443));
			startDiscoverMode();
		}
		else { //ha már van koordinátája, akkor odairányítom
			try {
				setCharacterPlace(Storage.getUserTileId(context));
				goCameraToCoordinate(Tile.getTileFromAllTilesById(VisibleTileAdapter.getAllTiles(), Storage.getUserTileId(context)).getCenteredCoordinate());

			}
			catch(Exception e) {}
		}
	}
	/**
	 * Az utoljára meglátogatott ponthoz viszi a kamerát.
	 */
	public void goToLastPlace() {
		double lat = Storage.getLastLatitude(context);
		double lon = Storage.getLastLongitude(context);
		if(lat != 0 && lon != 0) {
			goCameraToCoordinate(new LatLng(lat,lon));
		}
		else {
			goToCharacter();
		}
	}
	
	/**
	 * A megadott koordinátához küldi a kamerát.
	 * @param coordinate
	 */
	public void goCameraToCoordinate(LatLng coordinate) {
		if(mListener!=null)mListener.onMoveCamera(coordinate);
	}

	/**
	 * Kirajzolja a látványosságokat.
	 */
	public void drawIpos() {
		visibleTileAdapter.drawAllVisibleMarkers();
	}

	/**
	 * Elindítja a felfedezõ módot
	 */
	public void startDiscoverMode() {

		discoverModeOn = true;
		boolean isGPSTurnedOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if(!isGPSTurnedOn) {
			if(mListener!=null)mListener.turnOnGPS();
		}
		else {
			locationListener = new LocationListener() {
				public void onLocationChanged(Location location) {
					LocationChanged(location);
				}

				public void onStatusChanged(String provider, int status, Bundle extras) {}

				public void onProviderEnabled(String provider) {}

				public void onProviderDisabled(String provider) {}
			};
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			criteria.setAltitudeRequired(false);
			criteria.setBearingRequired(false);
			criteria.setCostAllowed(false);
			criteria.setPowerRequirement(Criteria.POWER_HIGH);
			String provider = locationManager.getBestProvider(criteria, true);
			Logger.writeToLog("GPS elindítása...");
			locationManager.requestLocationUpdates(provider, 0, 0, locationListener);

			if(discoverModeOn) {
				
				VisibleRegion vr = map.getProjection().getVisibleRegion();
				if(vr != null) {
					try {
						ipoGetter.setCameraPosition(vr.nearLeft, vr.nearRight, vr.farLeft, vr.farRight);
					}
					catch(Exception e) {

					}
				}
			}
		}
	}
	/**
	 * Leállítja a felfedezõ módot
	 */
	public void stopDiscoverMode() {
		Logger.writeToLog("discovery mode leállítása");
		if(locationManager != null) {
			try {
				locationManager.removeUpdates(locationListener);
			}
			catch(IllegalArgumentException e) {
				Logger.writeToLog("GPS not turned on...");
			}
		}
		setCharacterPlace(Storage.getUserTileId(context));
		visibleTileAdapter.deleteAllMarkers();
	}
	
	/**
	 * Kirajzolja a karaktert
	 * @param shownMarker
	 * @param clickedMarker
	 */
	public void drawCharacter(int shownMarker, final Marker clickedMarker) {
		Tile whereAmI = Tile.getTileFromAllTilesById(VisibleTileAdapter.getAllTiles(), Storage.getUserTileId(context));
		if(whereAmI != null) {
			Logger.writeToLog("Itt van a karakter: whereAmI: "+whereAmI.toString());
			TileDatabaseAdapter db = new TileDatabaseAdapter(context);
			db.open();
			try {
				if(shownMarker == MapActivity.MARKER_TILE) {
					if(clickedTile != null) {
						clickedTile = db.getById(clickedTile.getId());
						refreshTileInAllTiles(clickedTile);
						context.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if(clickedMarker != null) {
									clickedMarker.remove();
									if(mListener!=null) {
										mListener.onTileWindowShow(clickedTile);
									}
								}
							}
						});
						
						Logger.writeToLog("Map MARKER_TILE:A karakter rajzolása: "+whereAmI.getCenteredCoordinate().toString());
						if(mListener!=null) mListener.onDrawCharacter(whereAmI.getCenteredCoordinate());
					}
				}
				else if(shownMarker == MapActivity.MARKER_NONE) {
					try {
						Logger.writeToLog("Map: MARKER_NONE, a karakter rajzolása: "+whereAmI.getCenteredCoordinate().toString());
						if(mListener!=null) mListener.onDrawCharacter(whereAmI.getCenteredCoordinate());
					}
					catch(Exception e) {
						Logger.writeException(e);
					}
				}
			}
			catch(Exception e) {
				Logger.writeException(e);
			}
			finally {
				db.close();
			}
		}
	}
	
	/**
	 * Frissíti a megjelenített jelölõpontokat.
	 * @param clickedMarker
	 */
	public void refreshMarkers(Marker clickedMarker) {
		if(clickedMarker != null) {
			clickedMarker.remove();
		}
		if(clickedTile == null && clickedMarker != null) {
			clickedMarker.hideInfoWindow();
			clickedMarker.remove();
			//DiscoverMode.hideCharacterInfoWindow();
		}
		if(clickedTile != null) {
			Logger.writeToLog("CLICKED TILE ONMAPCLICK: "+clickedTile.toString());
			if(mListener!=null) {
				mListener.onTileWindowShow(clickedTile);
			}
		}
		//visibleTileAdapter.tryToDiscover(clickedCoordinate);
	}
	
	/**
	 * Frissíti a területet a cache-ben
	 * @param tile
	 */
	public void refreshTileInAllTiles(Tile tile) {
		VisibleTileAdapter.updateOneTile(tile);
	}
	
	/**
	 * Lekéri a felhasználó tartózkodási területét
	 * @return
	 */
	public Tile getUserTile() {
		return Tile.getTileFromAllTilesById(VisibleTileAdapter.getAllTiles(), Storage.getUserTileId(context));
	}

	/**
	 * Lekéri az utazásnál a célterületet
	 * @param tileid
	 * @return
	 */
	public Tile getTimedActionGoalTile(int tileid) {
		return Tile.getTileFromAllTilesById(VisibleTileAdapter.getAllTiles(), tileid);
	}
	
	/**
	 * Lekéri, hogy utazásnál hány százalékánál tart az útnak
	 * @param timedAction
	 * @return
	 */
	public double getTimedActionTravelPercent(TimedAction timedAction) {
		//Logger.writeToLog("map tick..."+secondsUntilFinished);
		//long timeRange = (timedAction.getEndTime()-timedAction.getFirstStartTime())/1000;
		long timeRange = (timedAction.getEndTime()-timedAction.getFirstStartTime());
		/*Time now = new Time();
		now.setToNow();
		long most = now.toMillis(false);*/
		Long tsLong = System.currentTimeMillis();
		//long timeRangeMost = (timedAction.getEndTime()-most)/1000;
		long timeRangeMost = (timedAction.getEndTime()-tsLong);
		
		return (double)timeRangeMost/(double)timeRange;
	}
	
	/**
	 * Letölti és megnyitja a kattintott terület adatait
	 * @param centeredClickedCoordinate
	 */
	public void setClickedTile(LatLng centeredClickedCoordinate) {
		Logger.writeToLog("kattintott tile középpontja: "+centeredClickedCoordinate.toString());
		if(VisibleTileAdapter.getAllTiles() != null) {
			clickedTile = null;
			for(Tile tile : VisibleTileAdapter.getAllTiles()) {
				//Logger.writeToLog("VIZSGÁLAT: "+centeredClickedCoordinate.toString()+"\n"+
				//		tile.getCenteredCoordinate().toString());
				if(tile.getCenteredCoordinate().equals(centeredClickedCoordinate)) {
					clickedTile = tile;
					break;
				}
			}
		}
		TileDatabaseAdapter db = new TileDatabaseAdapter(context);
		db.open();
		try {
			clickedTile = db.getById(clickedTile.getId());
		}
		catch(Exception e) {
			//nem kattint semmire
			//Logger.writeException(e);
		}
		finally {
			db.close();
		}
		/*Location l = new Location("asd");
		l.setLatitude(centeredClickedCoordinate.latitude);
		l.setLongitude(centeredClickedCoordinate.longitude);
		LocationChanged(l);*/
	}
	/**
	 * Továbbítja a megfelelõ oldalra a megnyitott infowindow kattintás után
	 * @param marker
	 */
	public void infoWindowClicked(Marker marker) {
		if(VisibleTileAdapter.isIpoMarker(marker)) {
			Logger.writeToLog("ipo marker!!");
			Ipo ipo = VisibleTileAdapter.getIpoFromMarker(marker);
			if(ipo != null) {
				if(ipo.getUrl() != "") {
					if(!ipo.getUrl().contains("http://")) {
						ipo.setUrl("http://"+ipo.getUrl());
					}
					if(mListener!=null)mListener.onGoToURL(ipo.getUrl());
				}
				else {
					InformationDialog.errorToast(context, "There is no link", Toast.LENGTH_SHORT);
				}
			}
			return;
		}
		Tile t = new Tile();
		Tile ezaz = Tile.getTileFromAllTilesByCoordinate(VisibleTileAdapter.getAllTiles(), marker.getPosition());
		if(ezaz == null) {
			Logger.writeToLog("Nem tile");
		}
		else {
			Logger.writeToLog("Tile");
		}
		t.setTileCenterFromLocation(marker.getPosition());
		Tile clickedTile = Tile.getTileFromAllTilesByCoordinate(VisibleTileAdapter.getAllTiles(), t.getCenteredCoordinate());
		if(mListener!=null) {
			mListener.onInfoWindowClicked(clickedTile,marker);
		}
	}

	/**
	 * Betölti az összes látható területet
	 * @throws ConcurrentModificationException
	 */
	public void loadAllVisibleTiles() throws ConcurrentModificationException {
		visibleTileAdapter.getAllPolygons();
	}

	/**
	 * Elindítja a látványosságlekérést
	 */
	private void setIpoGetter() {
		ipoGetter = new IpoGetter(context);
		ipoGetter.setCustomEventListener(new OnIpoGetListener() {
			@Override
			public void onCameraIpoChange(ArrayList<Ipo> seenByCamera) {
				if(seenByCamera != null) {
					try {
						VisibleTileAdapter.setIpoSeenByCamera(seenByCamera);
						Logger.writeToLog("Sikeresen lejött a kamera: ");
						for(Ipo i : seenByCamera) {
							Logger.writeToLog(i.toString());
						}
					}
					catch(Exception e) {

					}
					context.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							visibleTileAdapter.drawAllVisibleMarkers();
						}
					});
				}
				else {
					Logger.writeToLog("Hiba a lekérdezésben!");
				}
			}
			@Override
			public void onCharacterIpoChange(ArrayList<Ipo> seenByCharacter) {
				if(seenByCharacter != null) {

					VisibleTileAdapter.setIpoSeenByCharacter(seenByCharacter);
					Logger.writeToLog("Sikeresen lejött a karakter: "+seenByCharacter.size());
					for(Ipo i : seenByCharacter) {
						Logger.writeToLog(i.toString());
					}
					context.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							visibleTileAdapter.drawAllVisibleMarkers();
						}
					});
				}
				else {
					Logger.writeToLog("Hiba a lekérdezésben!");
				}
			}
		});
	}
	/**
	 * Beállítja a látott régiót
	 * @param visibleregion
	 */
	public void setIpoRegion(VisibleRegion visibleregion) {
		ipoGetter.setCameraPosition(visibleregion.nearLeft, visibleregion.nearRight, visibleregion.farLeft, visibleregion.farRight);
	}
	/**
	 * GPS lekért egy koordinátát, akkor hívódik meg
	 * @param location
	 */
	private void LocationChanged(Location location) {
		Logger.writeToLog("Lekérés: "+location.getAccuracy()+" "+location.getLatitude()+" "+location.getLongitude());
		LatLng loc = new LatLng(location.getLatitude(),location.getLongitude());
		//LatLng loc = new LatLng(47.6691627,19.3070493);

		if(!firstLocationChange) {
			goCameraToCoordinate(loc);
			firstLocationChange = true;
		}
		if(!isCoordinateDiscovered(VisibleTileAdapter.getAllTiles(), loc)) {
			Logger.writeToLog("checkin: "+loc.toString());
			checkinTile(loc);
		}
		else {
			Tile tile = Tile.getTileFromAllTilesByCoordinate(VisibleTileAdapter.getAllTiles(), loc);
			setCharacterPlace(tile.getId());
			
		}
		/*Tile t = */Tile.getTileFromAllTilesById(VisibleTileAdapter.getAllTiles(), Storage.getUserTileId(context));
		
		if(mListener!=null) mListener.onDrawCharacter( loc);
		Logger.writeToLog("Karakter IPO letöltés....");
		ipoGetter.setCharacterPlace(loc);
		try {
			visibleTileAdapter.tryToDiscover(loc);
		}
		catch(RuntimeException e) {
			Logger.writeToLog("LocationChanged, tryToDiscover - RuntimeException");
			Logger.writeException(e);
		}
	}
	
	/**
	 * Kirajzolja az összes épületet
	 */
	public void drawBuildings() {
		BuildingDownloader bd = new BuildingDownloader();
		bd.execute((Void) null);
	}
	/**
	 * Letölti az összes épületet
	 */
	private void downloadBuildings() {
		BuildingDatabaseAdapter buildingDb = new BuildingDatabaseAdapter(context);
		try {
			buildingDb.open();
			buildingDb.deleteAll();
			CommunicatorBuilding commBuilding = new CommunicatorBuilding(context, Storage.getUserSessionId(context));
			ArrayList<Building> allBuildings = commBuilding.getAllBuildings();
			if(allBuildings != null) {
				for(Building bui : allBuildings) {
					buildingDb.addRow(bui);
				}
			}
		}
		catch(Exception e) {
			Logger.writeToLog("Probléma merült fel az épületek letöltése közben, mert: ");
			Logger.writeException(e);
		}
		finally {
			buildingDb.close();
		}
	}
	/**
	 * Kirajzolja az otthonát.
	 * @param homeTileid
	 */
	public void drawHome(Tile homeTileid) {
		if(homeTileid != null) {
			
	        Location location1 = new Location("first");
	        location1.setLatitude(homeTileid.getCenterBuildingEast().latitude);
	        location1.setLongitude(homeTileid.getCenterBuildingEast().longitude);
	        Location location2 = new Location("second");
	        location2.setLatitude(homeTileid.getCenterBuildingWest().latitude);
	        location2.setLongitude(homeTileid.getCenterBuildingWest().longitude);
	        float distance = location1.distanceTo(location2);
	        Logger.writeToLog("distance: "+distance);
	        if(mListener!=null)mListener.onDrawHome(homeTileid.getCenteredCoordinate(), distance);
		}
	}
	/**
	 * Becsekkol egy területre
	 * @param tileCoordinate
	 */
	private void checkinTile(LatLng tileCoordinate) {
		Logger.writeToLog("Ide most bejelentkezek: "+tileCoordinate.toString());
		Tile loadingTile = new Tile();
		loadingTile = loadingTile.setTileCenterFromLocation(tileCoordinate);
		loadingTile.setResource1(0);
		loadingTile.setResource2(0);
		loadingTile.setResource3(0);
		loadingTile.setType(-1);
		
		if(isPendingCheckin(loadingTile.getCenteredCoordinate())) {
			Logger.writeToLog("Már benne van a letöltések között: "+tileCoordinate.toString());
		}
		else {
			Logger.writeToLog("Hozzáadom a letöltésekhez: "+tileCoordinate.toString());
			pendingCheckin.add(loadingTile.getCenteredCoordinate());
			Checkinner chk = new Checkinner();
			chk.execute(loadingTile);
		}
		//Polygon polygon = drawTile(loadingTile);
		//visiblePolygon.put(loadingTile.getId(), polygon);
		
	}
	/**
	 * Lekérdezi, hogy elkezdte-e már letölteni a megadott koordinátát
	 * @param coordinate
	 * @return Igaz, ha már elkezdte letölteni
	 */
	private boolean isPendingCheckin(LatLng coordinate) {
		for(LatLng c : pendingCheckin) {
			if(c.latitude == coordinate.latitude && c.longitude == coordinate.longitude) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Kitörli a várakozó letöltések közül a megadott koordinátát
	 * @param coordinate
	 */
	private void deletePendingCheckin(LatLng coordinate) {
		LatLng torlendo = null;
		for(LatLng c : pendingCheckin) {
			if(c.latitude == coordinate.latitude && c.longitude == coordinate.longitude) {
				torlendo = c;
			}
		}
		if(torlendo != null) {
			pendingCheckin.remove(torlendo);
		}
	}
	
	/**
	 * Beállítja a karakter tartózkodási helyét
	 * @param tileId
	 */
	public void setCharacterPlace(int tileId) {
		//Logger.writeToLog("placeid beállítása: "+tileId);
		Storage.setUserTileId(context, tileId);
		Tile tile = Tile.getTileFromAllTilesById(VisibleTileAdapter.getAllTiles(), Storage.getUserTileId(context));
		if(tile != null) {
			if(!discoverModeOn) {
				Logger.writeToLog("setCharacterPlace... a karakter rajzolása: "+tile.getCenteredCoordinate());
				if(mListener!=null) mListener.onDrawCharacter(tile.getCenteredCoordinate());
			}
			Storage.setUserTileId(context, Storage.getUserTileId(context));
		}
	}
	
	/**
	 * Elmenti az utoljára látott koordinátát
	 * @param coordinate
	 */
	public void saveLastCoordinates(LatLng coordinate) {
		Storage.setLastLatitude(context, coordinate.latitude);
		Storage.setLastLongitude(context, coordinate.longitude);
	}
	
	/**
	 * Lekéri az infowindow szövegét
	 * @param tile
	 * @return
	 */
	public static String getTileInfoWindowText(Tile tile) {
		if(tile == null) {
			return "";
		}
		Logger.writeToLog("Infowindow szövegének lekérése: "+tile.toString());
		String squire = "";
		if(tile.getOwner() != null) {
			if(tile.getOwner().length() > 0) {
				squire = "Lord: "+tile.getOwner()+"\n";
			}
		}
		if(tile.isExamined()) {
			
			return squire+Resource.getPlaceResourceString(tile)+"Type: "+tile.getTypeString()
					+"\nPopulation: "+tile.getPopulation();
			/*return squire+"Resources: "+tile.getResource1()+" "+tile.getResource2()+" "+tile.getResource3()+"\nType: "+tile.getTypeString()
					+"\nPopulation: "+tile.getPopulation();*/
		}
		else {
			return squire+"Type: "+tile.getTypeString();
		}
	}
	
	/**
	 * Letölti egy terület adatait
	 * @param tile
	 */
	public void downloadTile(Tile tile) {
		TileInfoLoader til = new TileInfoLoader();
		til.execute(tile);
	}
	
	/**
	 * A terület frissítéséért felelõs
	 * @author Albert
	 *
	 */
	private class TileInfoLoader extends AsyncTask<Tile, Void, Void> {

		@Override
		protected Void doInBackground(Tile... params) {
			try {
				final Tile tile = params[0];
				Tile.setOnTileDownloadedListener(new OnTileDownloaded() {
					@Override
					public void onFinished(Tile t) {
						final Tile t_ = t;
						context.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if(mListener!=null) {
									mListener.onTileDownloaded(t_);
								}
							}
						});
					}
				});
				Tile.refreshTileWithDownload(context, tile);
			}
			catch(NullPointerException e) { //elfelejtette a tile-t
				if(mListener!=null) {
					mListener.onError();
				}
			}
			/*CommunicatorTile comm = new CommunicatorTile(context);
			final Tile tile = comm.getTileById(params[0].getId());
			if(tile != null) {
				//Tile.refreshTileInAllTiles(context, VisibleTileAdapter.getAllTiles(), tile);
				
				
				
			}*/
			return null;
		}
		
	}
	/**
	 * A bejelentkezésért felelõs
	 * @author Albert
	 *
	 */
	private class Checkinner extends AsyncTask<Tile, Void, Void> {
		@Override
		protected Void doInBackground(Tile... params) {
			if(params[0] == null) {
				return null;
			}
			CommunicatorUserDetails comm = new CommunicatorUserDetails(Storage.getUserSessionId(context));
			TileDatabaseAdapter db = new TileDatabaseAdapter(context);
			try {
				Tile checkedIn = comm.discoverTile(params[0].getCenteredCoordinate());
				comm.setPlaceId(checkedIn.getId());
				int tileid = comm.getPlaceId();
				
				if(comm.isProblem()) {
					return null;
				}
				setCharacterPlace(tileid);
				deletePendingCheckin(checkedIn.getCenteredCoordinate());
				db.open();
				db.addRow(checkedIn);
				VisibleTileAdapter.addTile(checkedIn);
			}
			catch(Exception e) {
				Logger.writeException(e);
			}
			finally {
				db.close();
			}
			
			context.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Logger.writeToLog("Betöltés!!!!!!!!!!!!!!");
					loadAllVisibleTiles();
				}
			});		
			return null;
		}
	}
	/**
	 * Az épületek letöltéséért felelõs
	 * @author Albert
	 *
	 */
	private class BuildingDownloader extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			downloadBuildings();
			BuildingDatabaseAdapter buildingDb = new BuildingDatabaseAdapter(context);
			buildingDb.open();
			
			try {
				for(final Building building : buildingDb.getAll()) {
					//Logger.writeToLog("Building: "+building.toString());
					context.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if(mListener!=null)mListener.onDrawBuilding(building);
						}
					});
				}
			}
			catch(Exception e) {
				Logger.writeException(e);
			}
			finally {
				buildingDb.close();
			}
			return null;
		}
		
	}
	/**
	 * Igaz, ha be van kapcsolva a felfedezõ mód
	 * @return
	 */
	public static boolean isDiscoverModeOn() {
		return discoverModeOn;
	}
	/**
	 * Beállítja, hogy a felfedezõ mód be van kapcsolva
	 * @param discoverModeOn
	 */
	public static void setDiscoverModeOn(boolean discoverModeOn) {
		Map.discoverModeOn = discoverModeOn;
	}
	/**
	 * Lekérdezi, hogy egy területet felfedezett-e már
	 * @param allTiles
	 * @param coordinate
	 * @return
	 */
	public static boolean isCoordinateDiscovered(ArrayList<Tile> allTiles, LatLng coordinate) {
		try {
			Tile madeTile = new Tile();
			madeTile.setTileCenterFromLocation(coordinate);
			for(Tile oneTile : allTiles) {
				if(madeTile.equals(oneTile)) {
					return true;
				}
			}
		}
		catch( ConcurrentModificationException e) {
			Logger.writeToLog("Hiba történt az isCoordinateDiscovered lekérdezésekor...:");
			Logger.writeException(e);
		}
		catch(Exception e) {
			Logger.writeToLog("Hiba történt az isCoordinateDiscovered lekérdezésekor...:");
			Logger.writeException(e);
		}
		return false;
	}
	
	
}
