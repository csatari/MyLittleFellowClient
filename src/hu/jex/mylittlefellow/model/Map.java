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
 * A t�rk�p modellje
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
		 * Sikersen bet�lt�d�tt a megnyithat� infowindow
		 * @param tile
		 */
		public void onTileWindowShow(Tile tile);
		/**
		 * Meg lett nyomva az infowindow
		 * @param tile a ter�let 
		 * @param marker a jel�l�pont
		 */
		public void onInfoWindowClicked(Tile tile,Marker marker);
		/**
		 * Hiba t�rt�nt
		 */
		public void onError();
		/**
		 * Let�lt�d�tt a ter�let
		 * @param tile a ter�let adatai
		 */
		public void onTileDownloaded(Tile tile);
		/**
		 * El lett mozd�tva a kamera
		 * @param coordinate A 
		 */
		public void onMoveCamera(LatLng coordinate);
		/**
		 * Be kell kapcsolni a GPS-t
		 */
		public void turnOnGPS();
		/**
		 * Ki kell rajzolni a karakter
		 * @param coordinate A koordin�ta, ahova ki kell rajzolni
		 */
		public void onDrawCharacter(LatLng coordinate);
		/**
		 * Ki kell rajzolni az �p�letet
		 * @param building az �p�let
		 */
		public void onDrawBuilding(Building building);
		/**
		 * Ki kell rajzolni az otthont
		 * @param coordinate A koordin�t�ra kell kirajzolni
		 * @param distance Az otthon sz�less�ge m�terben
		 */
		public void onDrawHome(LatLng coordinate, float distance);
		/**
		 * A megadott url-re kell menni
		 * @param url
		 */
		public void onGoToURL(String url);
	}
	/**
	 * Be�ll�tja az esem�nykezel�t
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
	 * Let�lti az �sszes ter�letet az adatb�zisb�l
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
	 * A karakterhez viszi a kamer�t
	 */
	public void goToCharacter() {
		if(Storage.getUserTileId(context) == 0) { //ha m�g nincs koordin�t�ja, Pestre ir�ny�tom
			goCameraToCoordinate(new LatLng(47.498648,19.04443));
			startDiscoverMode();
		}
		else { //ha m�r van koordin�t�ja, akkor odair�ny�tom
			try {
				setCharacterPlace(Storage.getUserTileId(context));
				goCameraToCoordinate(Tile.getTileFromAllTilesById(VisibleTileAdapter.getAllTiles(), Storage.getUserTileId(context)).getCenteredCoordinate());

			}
			catch(Exception e) {}
		}
	}
	/**
	 * Az utolj�ra megl�togatott ponthoz viszi a kamer�t.
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
	 * A megadott koordin�t�hoz k�ldi a kamer�t.
	 * @param coordinate
	 */
	public void goCameraToCoordinate(LatLng coordinate) {
		if(mListener!=null)mListener.onMoveCamera(coordinate);
	}

	/**
	 * Kirajzolja a l�tv�nyoss�gokat.
	 */
	public void drawIpos() {
		visibleTileAdapter.drawAllVisibleMarkers();
	}

	/**
	 * Elind�tja a felfedez� m�dot
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
			Logger.writeToLog("GPS elind�t�sa...");
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
	 * Le�ll�tja a felfedez� m�dot
	 */
	public void stopDiscoverMode() {
		Logger.writeToLog("discovery mode le�ll�t�sa");
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
						
						Logger.writeToLog("Map MARKER_TILE:A karakter rajzol�sa: "+whereAmI.getCenteredCoordinate().toString());
						if(mListener!=null) mListener.onDrawCharacter(whereAmI.getCenteredCoordinate());
					}
				}
				else if(shownMarker == MapActivity.MARKER_NONE) {
					try {
						Logger.writeToLog("Map: MARKER_NONE, a karakter rajzol�sa: "+whereAmI.getCenteredCoordinate().toString());
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
	 * Friss�ti a megjelen�tett jel�l�pontokat.
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
	 * Friss�ti a ter�letet a cache-ben
	 * @param tile
	 */
	public void refreshTileInAllTiles(Tile tile) {
		VisibleTileAdapter.updateOneTile(tile);
	}
	
	/**
	 * Lek�ri a felhaszn�l� tart�zkod�si ter�let�t
	 * @return
	 */
	public Tile getUserTile() {
		return Tile.getTileFromAllTilesById(VisibleTileAdapter.getAllTiles(), Storage.getUserTileId(context));
	}

	/**
	 * Lek�ri az utaz�sn�l a c�lter�letet
	 * @param tileid
	 * @return
	 */
	public Tile getTimedActionGoalTile(int tileid) {
		return Tile.getTileFromAllTilesById(VisibleTileAdapter.getAllTiles(), tileid);
	}
	
	/**
	 * Lek�ri, hogy utaz�sn�l h�ny sz�zal�k�n�l tart az �tnak
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
	 * Let�lti �s megnyitja a kattintott ter�let adatait
	 * @param centeredClickedCoordinate
	 */
	public void setClickedTile(LatLng centeredClickedCoordinate) {
		Logger.writeToLog("kattintott tile k�z�ppontja: "+centeredClickedCoordinate.toString());
		if(VisibleTileAdapter.getAllTiles() != null) {
			clickedTile = null;
			for(Tile tile : VisibleTileAdapter.getAllTiles()) {
				//Logger.writeToLog("VIZSG�LAT: "+centeredClickedCoordinate.toString()+"\n"+
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
	 * Tov�bb�tja a megfelel� oldalra a megnyitott infowindow kattint�s ut�n
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
	 * Bet�lti az �sszes l�that� ter�letet
	 * @throws ConcurrentModificationException
	 */
	public void loadAllVisibleTiles() throws ConcurrentModificationException {
		visibleTileAdapter.getAllPolygons();
	}

	/**
	 * Elind�tja a l�tv�nyoss�glek�r�st
	 */
	private void setIpoGetter() {
		ipoGetter = new IpoGetter(context);
		ipoGetter.setCustomEventListener(new OnIpoGetListener() {
			@Override
			public void onCameraIpoChange(ArrayList<Ipo> seenByCamera) {
				if(seenByCamera != null) {
					try {
						VisibleTileAdapter.setIpoSeenByCamera(seenByCamera);
						Logger.writeToLog("Sikeresen lej�tt a kamera: ");
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
					Logger.writeToLog("Hiba a lek�rdez�sben!");
				}
			}
			@Override
			public void onCharacterIpoChange(ArrayList<Ipo> seenByCharacter) {
				if(seenByCharacter != null) {

					VisibleTileAdapter.setIpoSeenByCharacter(seenByCharacter);
					Logger.writeToLog("Sikeresen lej�tt a karakter: "+seenByCharacter.size());
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
					Logger.writeToLog("Hiba a lek�rdez�sben!");
				}
			}
		});
	}
	/**
	 * Be�ll�tja a l�tott r�gi�t
	 * @param visibleregion
	 */
	public void setIpoRegion(VisibleRegion visibleregion) {
		ipoGetter.setCameraPosition(visibleregion.nearLeft, visibleregion.nearRight, visibleregion.farLeft, visibleregion.farRight);
	}
	/**
	 * GPS lek�rt egy koordin�t�t, akkor h�v�dik meg
	 * @param location
	 */
	private void LocationChanged(Location location) {
		Logger.writeToLog("Lek�r�s: "+location.getAccuracy()+" "+location.getLatitude()+" "+location.getLongitude());
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
		Logger.writeToLog("Karakter IPO let�lt�s....");
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
	 * Kirajzolja az �sszes �p�letet
	 */
	public void drawBuildings() {
		BuildingDownloader bd = new BuildingDownloader();
		bd.execute((Void) null);
	}
	/**
	 * Let�lti az �sszes �p�letet
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
			Logger.writeToLog("Probl�ma mer�lt fel az �p�letek let�lt�se k�zben, mert: ");
			Logger.writeException(e);
		}
		finally {
			buildingDb.close();
		}
	}
	/**
	 * Kirajzolja az otthon�t.
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
	 * Becsekkol egy ter�letre
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
			Logger.writeToLog("M�r benne van a let�lt�sek k�z�tt: "+tileCoordinate.toString());
		}
		else {
			Logger.writeToLog("Hozz�adom a let�lt�sekhez: "+tileCoordinate.toString());
			pendingCheckin.add(loadingTile.getCenteredCoordinate());
			Checkinner chk = new Checkinner();
			chk.execute(loadingTile);
		}
		//Polygon polygon = drawTile(loadingTile);
		//visiblePolygon.put(loadingTile.getId(), polygon);
		
	}
	/**
	 * Lek�rdezi, hogy elkezdte-e m�r let�lteni a megadott koordin�t�t
	 * @param coordinate
	 * @return Igaz, ha m�r elkezdte let�lteni
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
	 * Kit�rli a v�rakoz� let�lt�sek k�z�l a megadott koordin�t�t
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
	 * Be�ll�tja a karakter tart�zkod�si hely�t
	 * @param tileId
	 */
	public void setCharacterPlace(int tileId) {
		//Logger.writeToLog("placeid be�ll�t�sa: "+tileId);
		Storage.setUserTileId(context, tileId);
		Tile tile = Tile.getTileFromAllTilesById(VisibleTileAdapter.getAllTiles(), Storage.getUserTileId(context));
		if(tile != null) {
			if(!discoverModeOn) {
				Logger.writeToLog("setCharacterPlace... a karakter rajzol�sa: "+tile.getCenteredCoordinate());
				if(mListener!=null) mListener.onDrawCharacter(tile.getCenteredCoordinate());
			}
			Storage.setUserTileId(context, Storage.getUserTileId(context));
		}
	}
	
	/**
	 * Elmenti az utolj�ra l�tott koordin�t�t
	 * @param coordinate
	 */
	public void saveLastCoordinates(LatLng coordinate) {
		Storage.setLastLatitude(context, coordinate.latitude);
		Storage.setLastLongitude(context, coordinate.longitude);
	}
	
	/**
	 * Lek�ri az infowindow sz�veg�t
	 * @param tile
	 * @return
	 */
	public static String getTileInfoWindowText(Tile tile) {
		if(tile == null) {
			return "";
		}
		Logger.writeToLog("Infowindow sz�veg�nek lek�r�se: "+tile.toString());
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
	 * Let�lti egy ter�let adatait
	 * @param tile
	 */
	public void downloadTile(Tile tile) {
		TileInfoLoader til = new TileInfoLoader();
		til.execute(tile);
	}
	
	/**
	 * A ter�let friss�t�s��rt felel�s
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
	 * A bejelentkez�s�rt felel�s
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
					Logger.writeToLog("Bet�lt�s!!!!!!!!!!!!!!");
					loadAllVisibleTiles();
				}
			});		
			return null;
		}
	}
	/**
	 * Az �p�letek let�lt�s��rt felel�s
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
	 * Igaz, ha be van kapcsolva a felfedez� m�d
	 * @return
	 */
	public static boolean isDiscoverModeOn() {
		return discoverModeOn;
	}
	/**
	 * Be�ll�tja, hogy a felfedez� m�d be van kapcsolva
	 * @param discoverModeOn
	 */
	public static void setDiscoverModeOn(boolean discoverModeOn) {
		Map.discoverModeOn = discoverModeOn;
	}
	/**
	 * Lek�rdezi, hogy egy ter�letet felfedezett-e m�r
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
			Logger.writeToLog("Hiba t�rt�nt az isCoordinateDiscovered lek�rdez�sekor...:");
			Logger.writeException(e);
		}
		catch(Exception e) {
			Logger.writeToLog("Hiba t�rt�nt az isCoordinateDiscovered lek�rdez�sekor...:");
			Logger.writeException(e);
		}
		return false;
	}
	
	
}
