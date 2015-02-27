package hu.jex.mylittlefellow.gui;

import hu.jex.mylittlefellow.R;
import hu.jex.mylittlefellow.model.Building;
import hu.jex.mylittlefellow.model.Logger;
import hu.jex.mylittlefellow.model.Map;
import hu.jex.mylittlefellow.model.Map.OnMapEvent;
import hu.jex.mylittlefellow.model.Tile;
import hu.jex.mylittlefellow.model.TimedAction;
import hu.jex.mylittlefellow.model.VisibleTileAdapter;
import hu.jex.mylittlefellow.storage.Storage;

import java.util.ConcurrentModificationException;
import java.util.LinkedList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.VisibleRegion;

public class MapActivity extends BaseActivity implements OnMapEvent, OnClickListener,OnLongClickListener, OnMapClickListener, OnInfoWindowClickListener,OnCameraChangeListener, OnMarkerClickListener {
	private static GoogleMap mMap;

	private static ImageButton togglMapButton;
	private static ImageButton discoverModeButton;
	private static ImageButton locationButton;
	private static boolean mapShow;
	private static Activity context;
	
	private static Marker clickedMarker;
	//private static GroundOverlay homeGroundOverlay;
	
	private static VisibleRegion visibleregion;
	private static CameraPosition cameraPosition;
	
	private static int homeId = 0;
	
	public static int MARKER_NONE = 0;
	public static int MARKER_TILE = 1;
	//private static int MARKER_SETTLEMENT = 3;
	private static int shownMarker;
	
	private static Polyline travelLine;
	private boolean beenError = false;
	
	private static Map map;
	
	//private static GroundOverlay characterDrawing;
	private static Marker characterMarker;
	private static LinkedList<GroundOverlay> buildingDrawing;
	

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        context = this;
        
        buildingDrawing = new LinkedList<GroundOverlay>();
        
        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        map = new Map(context,mMap);
        Map.setCustomEventListener(this);
        
        mapShow = false;
        
        mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        mMap.setOnMapClickListener(this);
        
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(context));
        mMap.setOnMarkerClickListener(this);
        
        
        togglMapButton = (ImageButton)findViewById(R.id.togglButton);
        togglMapButton.setOnClickListener(this);
        
        discoverModeButton = (ImageButton)findViewById(R.id.discoverModeButton);
        discoverModeButton.setOnClickListener(this);
        discoverModeButton.setImageResource(R.drawable.discovery_mode_off_icon32);
        
        locationButton = (ImageButton)findViewById(R.id.locationButton);
        locationButton.setOnClickListener(this);
        locationButton.setOnLongClickListener(this);
        
        loadAllTiles();
        
        shownMarker = MARKER_NONE;
        //Ha még csak most regisztrált és nincs userTileId-ja, akkor bekapcsolom a discovery mode-ot
        mMap.setOnInfoWindowClickListener(this);
	    mMap.setOnCameraChangeListener(this);
        
    }
	@Override
	public void onResume() {
		super.onResume();
		
		VisibleTileAdapter.removeAllTiles();
		loadAllTiles();
		try {
			map.goCameraToCoordinate(MapActivity.cameraPosition.target);
		}
		catch(Exception e) {
			map.goToLastPlace();
		}
		if(Map.isDiscoverModeOn()) {
			Logger.writeToLog("A discovery mód elindítása");
			//map.startDiscoverMode();
			//discoverModeButton.setImageResource(R.drawable.discovery_mode_on_icon32);
			map.drawIpos();
		}
		
		//Logger.writeToLog("Shown marker ID = "+shownMarker);
		
		map.drawCharacter(shownMarker,clickedMarker);
		
		
		homeId = Storage.getHomeId(context);
		
		//Az épületek letörlése és újrarajzolása
		for(GroundOverlay go : buildingDrawing) {
			go.remove();
		}
		buildingDrawing.clear();
        map.drawBuildings();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if(travelLine != null) {
			travelLine.remove();
		}
	}
	
	@Override
	public void onBackPressed() {
		if(Map.isDiscoverModeOn()) {
			map.stopDiscoverMode();
			discoverModeButton.setImageResource(R.drawable.discovery_mode_off_icon32);
		}
		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(startMain);
	}
	
	/**
	 * Betölti a térképre az összes területet
	 */
	public void loadAllTiles() {
		/*context.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mMap.clear();
			}
		});*/
		map.loadAllTiles();
	}
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.togglButton:
			if(mapShow) {
				mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
				mapShow = false;
			}
			else {
				mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				mapShow = true;
			}
			break;
		case R.id.discoverModeButton:
			if(!Map.isDiscoverModeOn()) { //on
				stopTimedActionIfTravel();
				map.startDiscoverMode();
				discoverModeButton.setImageResource(R.drawable.discovery_mode_on_icon32);
			}
			else { //off
				Map.setDiscoverModeOn(false);
				map.stopDiscoverMode();
				discoverModeButton.setImageResource(R.drawable.discovery_mode_off_icon32);
			}
			break;
		case R.id.locationButton:
			if(characterMarker != null) {
				map.goCameraToCoordinate(characterMarker.getPosition());
			}
			break;
		}
	}
	@Override
	public boolean onLongClick(View v) {
		switch(v.getId()) {
		case R.id.locationButton:
			try {
				Tile homeTile = Tile.getTileFromAllTilesById(VisibleTileAdapter.getAllTiles(), homeId);
				map.goCameraToCoordinate(homeTile.getCenteredCoordinate());
				return true;
			}
			catch(Exception e) {
				Logger.writeToLog("Hiba történt a kamera otthonra rakásánál...");
				Logger.writeException(e);
			}
			break;
		}
		return false;
	}
	@Override
	public void onMapClick(LatLng clickedCoordinate) {
		LatLng centeredClickedCoordinate = Tile.getCenteredCoordinate(clickedCoordinate);
		
		map.setClickedTile(centeredClickedCoordinate);

		map.refreshMarkers(clickedMarker);
		
	}
	/**
	 * Megmutatja a terület infowindow-ját
	 * @param tile A terület
	 */
	private void showTileInfoWindow(Tile tile) {
		map.downloadTile(tile);
	}
	
	@Override
	public void onInfoWindowClick(Marker marker) {
		map.infoWindowClicked(marker);
	}
	
	@Override
	public void onCameraChange(CameraPosition cameraPosition) {
		visibleregion = mMap.getProjection().getVisibleRegion();
		MapActivity.cameraPosition = cameraPosition;
		mapMoved();
	}
	/**
	 * Akkor fut le, amikor a térkép elmozdult.
	 */
	private void mapMoved() {
		VisibleTileAdapter.setVisibleRegion(visibleregion.nearLeft, visibleregion.nearRight, visibleregion.farLeft, visibleregion.farRight);
		try {
			if(cameraPosition.zoom > 10) {
				map.loadAllVisibleTiles();
			}
		}
		catch(ConcurrentModificationException e) { //elfelejtette a tile-t
			Logger.writeToLog("ConcurrentModificationException hiba történt...");
			Logger.writeException(e);
			mapMoved();//goBackToRootActivity();
			return;
		}
		if(Map.isDiscoverModeOn()) {
			if(cameraPosition.zoom > 10) {
				map.setIpoRegion(visibleregion);
			}
		}
	}
	/**
	 * Kirajzolja a karakter az adott pontra
	 * @param latlng Az adott koordináta
	 */
	public static void drawCharacterOnMap(final LatLng latlng) {
		Logger.writeToLog("A karakter rajzolása: "+latlng.toString());
		context.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				BitmapDescriptor bmpd = BitmapDescriptorFactory.fromResource(R.drawable.character_icon_50_center);
				/*GroundOverlayOptions groundOverlayOptions = new GroundOverlayOptions();
				groundOverlayOptions.image(bmpd);
				groundOverlayOptions.anchor(0.5f, 1f);
		        groundOverlayOptions.position(latlng, DiscoverMode.distance);
		        groundOverlayOptions.visible(true);
		        groundOverlayOptions.zIndex(3.0f);
				if(characterDrawing != null) {
					characterDrawing.remove();
				}
				characterDrawing = mMap.addGroundOverlay(groundOverlayOptions);*/
				
				if(characterMarker != null) {
					characterMarker.remove();
				}
				characterMarker = mMap.addMarker(new MarkerOptions()
		        .position(latlng)
		        .draggable(false)
		        .infoWindowAnchor(0.5f, 1f)
		        .icon(bmpd));
				
			}
		});
	}
	/**
	 * Letörli a karaktert a térképrõl
	 */
	public static void removeCharacterFromMap() {
		context.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(characterMarker != null) {
					characterMarker.remove();
				}
			}
		});
		
	}
	
	@Override
	protected ActivityType getActivityType() {
		return ActivityType.MAP;
	}
	@Override
	protected void onTimedActionFinish(TimedAction timedAction) {
		if(timedAction.getType() == TimedAction.TRAVEL) {
			Storage.setUserTileId(context, timedAction.getGoal());
			Tile userTile = map.getUserTile();
			Logger.writeToLog("A karakter rajzolása timedActionFinish miatt...");
			drawCharacterOnMap(userTile.getCenteredCoordinate());
			if(travelLine != null) {
				travelLine.remove();
			}
		}
		/*else if(timedAction.getType() == TimedAction.EXAMINE) {
			Tile examinedTile = map.getTimedActionGoalTile(timedAction.getGoal());
			map.refreshTileInAllTiles(examinedTile);
			
		}*/
		else if(timedAction.getType() == TimedAction.SETLLEDOWN) {
			homeId = Storage.getHomeId(context);
			if(homeId != 0) {
				Tile homeTile = Tile.getTileFromAllTilesById(VisibleTileAdapter.getAllTiles(), homeId);
				map.drawHome(homeTile);
			}
			for(GroundOverlay go : buildingDrawing) {
				go.remove();
			}
			buildingDrawing.clear();
	        map.drawBuildings();
		}
		else {
			try {
				characterMarker.hideInfoWindow();
				Tile userTile = map.getUserTile();
				showTileInfoWindow(userTile);
			}
			catch(Exception e) {
				Logger.writeToLog("Hiba történt...:");
				Logger.writeException(e);
			}
		}
	}
	@Override
	protected void onTimedActionTick(TimedAction timedAction, long secondsUntilFinished) {
		if(timedAction.getType() == TimedAction.TRAVEL && !beenError) {
			
			
			double szazalek = map.getTimedActionTravelPercent(timedAction);
			
			//Logger.writeToLog("szazalek: "+szazalek+" timeRangeMost: "+timeRangeMost+" timeRange: "+timeRange);
			if(szazalek >= 0) {
				//Logger.writeToLog("tick... usertileid:"+Storage.getUserTileId(context)+" "+secondsUntilFinished+" %: "+szazalek+", "+timedAction.getEndTime()+" "+timedAction.getStartTime()+" "+timedAction.getFirstStartTime()+" "+timeRange);
				Tile userTile = map.getUserTile();
				Tile goalTile = map.getTimedActionGoalTile(timedAction.getGoal());
				
				double m1 = 1-szazalek;
				double m2 = szazalek;
				
				double latitude = m2*userTile.getTileCenterLatitude() + m1*goalTile.getTileCenterLatitude();
				double longitude = m2*userTile.getTileCenterLongitude() + m1*goalTile.getTileCenterLongitude();
				Logger.writeToLog("A karakter kirajzolása ontTimedActionTick miatt...");
				drawCharacterOnMap(new LatLng(latitude,longitude));
			}
		}
	}
	@Override
	protected void onTimedActionStart(TimedAction timedAction) {
		beenError = false;
		Logger.writeToLog("TIMED ACTION START");
		if(timedAction.getType() == TimedAction.TRAVEL) {
			Map.setDiscoverModeOn(false);
			map.stopDiscoverMode();
			discoverModeButton.setImageResource(R.drawable.discovery_mode_off_icon32);
			Logger.writeToLog("EZ MOST ELINDUL map timed action start...");
			Tile userTile = map.getUserTile();
			Tile goalTile = map.getTimedActionGoalTile(timedAction.getGoal());			
			PolylineOptions po = new PolylineOptions()
		     .add(userTile.getCenteredCoordinate(), goalTile.getCenteredCoordinate())
		     .width(5)
		     .color(Color.BLUE);
		    travelLine = mMap.addPolyline(po);
		}
	}
	@Override
	protected void onTimedActionError(TimedAction timedAction) {
		beenError = true;
		context.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				map.setCharacterPlace(Storage.getUserTileId(context));
				if(travelLine != null) {
					travelLine.remove();
				}
			}
		});
	}
	@Override
	protected void onTimedActionFinishUpload(TimedAction timedAction) { }
	
	
	@Override
	public void onTileWindowShow(Tile tile) {
		showTileInfoWindow(tile);
	}

	@Override
	public void onInfoWindowClicked(Tile tile,Marker marker) {
		if(tile != null) {
			map.saveLastCoordinates(marker.getPosition());
			
			Intent it = new Intent(MapActivity.this,PlaceActivity.class);
			it.putExtra("tileid", tile.getId());
			startActivity(it);
		}
		if(marker.equals(marker)) {
			shownMarker = MARKER_TILE;
		}
	}

	@Override
	public void onError() {
		Logger.writeToLog("OnError............");
		goBackToRootActivity();
	}

	@Override
	public void onTileDownloaded(Tile tile) {
		BitmapDescriptor bmpd = BitmapDescriptorFactory.fromResource(R.drawable.blank_marker);
		MarkerOptions markerOptions = new MarkerOptions()
		.position(tile.getCenteredCoordinate())
		.anchor(0.5f, 0)
		.icon(bmpd)
		.alpha(0.0f)
		.title(Map.getTileInfoWindowText(tile));
		
		clickedMarker = mMap.addMarker(markerOptions);
		clickedMarker.showInfoWindow();
		mapMoved();
	}
	@Override
	public void onMoveCamera(LatLng coordinate) {
		CameraPosition currentPlace = new CameraPosition.Builder()
		.target(coordinate)
		.bearing(0.0f).tilt(0.0f).zoom(14f).build();
		mMap.moveCamera(CameraUpdateFactory.newCameraPosition(currentPlace));
	}
	@Override
	public void turnOnGPS() {
		InformationDialog.errorToast(context, "You have to turn on the GPS", Toast.LENGTH_LONG);
		Intent i = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(i);
	}
	@Override
	public void onDrawCharacter(LatLng coordinate) {
		Logger.writeToLog("A karakter rajzolása a térkép következõ koordinátájára: "+coordinate.toString());
		removeCharacterFromMap();
		drawCharacterOnMap(coordinate);
	}
	@Override
	public void onDrawBuilding(Building building) {
		BitmapDescriptor bmpd = null;
		if(building.getType() == Building.TOWNCENTER) {
			bmpd = BitmapDescriptorFactory.fromResource(R.drawable.settledown_icon_high);
		}
		else if(building.getType() == Building.STORAGE) {
			bmpd = BitmapDescriptorFactory.fromResource(R.drawable.storage_icon_high);
		}
		else if(building.getType() == Building.TOWEROFKNOWLEDGE) {
			bmpd = BitmapDescriptorFactory.fromResource(R.drawable.tower_icon_high);
		}
		else if(building.getType() == Building.TOOLSTATION) {
			bmpd = BitmapDescriptorFactory.fromResource(R.drawable.drill_icon_high);
		}
		else {
			bmpd = BitmapDescriptorFactory.fromResource(R.drawable.cancel);
		}
		GroundOverlayOptions groundOverlayOptions = new GroundOverlayOptions();
		//Logger.writeToLog("slice: "+building.getSliceId());
		Tile tile = Tile.getTileFromAllTilesById(VisibleTileAdapter.getAllTiles(), building.getTileId());
		if(tile != null) {
			float distance = Building.getBuildingDistance(tile, building.getSliceId());
			LatLng buildingCenter = Building.getBuildingCenter(tile, building.getSliceId());
	        //Logger.writeToLog("distance: "+distance);
	        groundOverlayOptions.image(bmpd);
	        groundOverlayOptions.position(buildingCenter, distance);
	        groundOverlayOptions.visible(true);
	        groundOverlayOptions.zIndex(2.0f);
	        GroundOverlay buildingDraw = mMap.addGroundOverlay(groundOverlayOptions);
	        buildingDrawing.add(buildingDraw);
		}
	}
	@Override
	public void onDrawHome(LatLng coordinate, float distance) {
		BitmapDescriptor bmpd = BitmapDescriptorFactory.fromResource(R.drawable.settledown_icon32);
		GroundOverlayOptions groundOverlayOptions = new GroundOverlayOptions();
        groundOverlayOptions.image(bmpd);
        groundOverlayOptions.position(coordinate, distance);
        groundOverlayOptions.visible(true);
        groundOverlayOptions.zIndex(2.0f);
        mMap.addGroundOverlay(groundOverlayOptions);
	}
	@Override
	public void onGoToURL(String url) {
		try {
			Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse( url ) );
		    context.startActivity( browse );
		}
		catch(Exception e) {
			InformationDialog.errorToast(context, "The link can't be parsed", Toast.LENGTH_SHORT);
		}
	}
	@Override
	public boolean onMarkerClick(Marker marker) {
		if(marker.equals(characterMarker)) { //Ha karakter marker, akkor csak úgy tesz, mintha a térképre kattintott volna
			Logger.writeToLog("characterMarker");
			onMapClick(marker.getPosition());
			return true;
		}
		return false;
	}
}
