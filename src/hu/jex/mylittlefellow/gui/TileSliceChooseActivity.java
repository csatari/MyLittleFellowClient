package hu.jex.mylittlefellow.gui;

import hu.jex.mylittlefellow.R;
import hu.jex.mylittlefellow.model.Building;
import hu.jex.mylittlefellow.model.BuildingReceipt;
import hu.jex.mylittlefellow.model.Logger;
import hu.jex.mylittlefellow.model.Tile;
import hu.jex.mylittlefellow.model.TileSliceChoose;
import hu.jex.mylittlefellow.model.TileSliceChoose.OnTileSliceChooseEvent;
import hu.jex.mylittlefellow.model.TimedAction;
import hu.jex.mylittlefellow.model.VisibleTileAdapter;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.VisibleRegion;

/**
 * Az építkezés oldala
 * @author Albert
 *
 */
public class TileSliceChooseActivity extends BaseActivity implements OnMapClickListener,OnCameraChangeListener,OnTileSliceChooseEvent {

	private static Activity context;
	private static VisibleTileAdapter visibleTileAdapter;
	private static GoogleMap mMap;

	
	private static GridView buildingReceiptGridview;
	private static TextView countdownTextView;
	private static BuildingReceiptsAdapter buildingReceiptsAdapter;
	private static Dialog dialog;

	private static TileSliceChoose model;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tileslicechoose);

		context = this;
		
		model = new TileSliceChoose(context);
		model.setCustomEventListener(this);
		
		Intent intent = getIntent();
		int tileid = intent.getIntExtra("tileid", 0);
		
		try {
			
			model.setTileById(tileid);

			mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
			mMap.getUiSettings().setAllGesturesEnabled(false);
			mMap.getUiSettings().setZoomControlsEnabled(false);
			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			mMap.setOnMapClickListener(this);

			countdownTextView = (TextView)findViewById(R.id.countdownText);

			visibleTileAdapter = new VisibleTileAdapter(context,mMap);
			
			VisibleTileAdapter.removeAllTiles();
			//loadAllTiles();
			
			CameraPosition currentPlace = model.getCameraPosition();

			mMap.moveCamera(CameraUpdateFactory.newCameraPosition(currentPlace));
			mMap.setOnCameraChangeListener(this);
		}
		catch(Exception e) {
			Logger.writeException(e);
			goBackToRootActivity();
		}

	}
	@Override
	public void onResume() {
		super.onResume();
		try {
			drawAllBuildings(mMap);
		}
		catch(Exception e) {
			goBackToRootActivity();
		}
	}
	@Override
	public void onPause() {
		super.onPause();
		//VisibleTileAdapter.removeAllTiles();
		//mMap.clear();
	}
	/**
	 * Kirajzolja az összes épületet
	 * @param map a megadott térkép, amire rajzolni kell
	 */
	public void drawAllBuildings(GoogleMap map) {
		
		model.getAllBuildingsForDraw();
		
		Tile tile = model.getTile();

		PolylineOptions first = new PolylineOptions()
		.width(3)
		.add(tile.getNorthOneThird())
		.add(tile.getSouthOneThird());
		map.addPolyline(first);

		PolylineOptions second = new PolylineOptions()
		.width(3)
		.add(tile.getNorthTwoThird())
		.add(tile.getSouthTwoThird());
		map.addPolyline(second);

		PolylineOptions third = new PolylineOptions()
		.width(3)
		.add(tile.getWestOneThird())
		.add(tile.getEastOneThird());
		map.addPolyline(third);

		PolylineOptions fourth = new PolylineOptions()
		.width(3)
		.add(tile.getWestTwoThird())
		.add(tile.getEastTwoThird());
		map.addPolyline(fourth);

		PolylineOptions fifth = new PolylineOptions()
		.width(2)
		.add(tile.getNorthEast())
		.add(tile.getNorthWest())
		.add(tile.getSouthWest())
		.add(tile.getSouthEast())
		.add(tile.getNorthEast());
		map.addPolyline(fifth);
	}

	@Override
	public void onCameraChange(CameraPosition cameraPosition) {
		VisibleRegion vr = mMap.getProjection().getVisibleRegion();
		VisibleTileAdapter.setVisibleRegion(vr.nearLeft, vr.nearRight, vr.farLeft, vr.farRight);
		//Logger.writeToLog("OnCameraChange...");
		loadAllVisibleTiles();
	}
	/**
	 * Kirajzolja az összes látható területet
	 */
	private void loadAllVisibleTiles() {
		visibleTileAdapter.getAllPolygons();
	}

	@Override
	public void onMapClick(LatLng latlng) {
		model.downloadAllBuildables(latlng);
	}

	/**
	 * A receptek kirajzolásáért felelõs osztály
	 * @author Albert
	 *
	 */
	private class BuildingReceiptsAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return model.getBuildingReceiptSize();
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
			Button button = new Button(context);
			final BuildingReceipt br = model.getBuildingReceiptOnPosition(position);
			StringBuilder sb = new StringBuilder();
			sb.append("Type: ");
			sb.append(br.getName());
			sb.append("\n Level: ");
			sb.append(br.getLevel());
			sb.append("\n");
			sb.append(br.getResources().getResourcesInText());
			sb.append("Intelligence points: ");
			sb.append(br.getIpo());
			button.setText(sb.toString());
			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					model.startBuilding(br.getType());
					dialog.cancel();
				}
			});
			return button;
		}
	}

	@Override
	protected ActivityType getActivityType() {
		return ActivityType.TILESLICECHOOSE;
	}
	@Override
	protected void onTimedActionFinish(TimedAction timedAction) {
		drawAllBuildings(mMap);
		countdownTextView.setVisibility(TextView.GONE);
	}
	@Override
	protected void onTimedActionTick(TimedAction timedAction,long secondsUntilFinished) {
		countdownTextView.setText(timedAction.getTimedActionType()+" "+(int)Math.ceil((long)secondsUntilFinished/1000)+"");
	}
	@Override
	protected void onTimedActionStart(TimedAction timedAction) {
		Logger.writeToLog("ONSTART");
		countdownTextView.setVisibility(TextView.VISIBLE);
	}
	@Override
	protected void onTimedActionError(TimedAction timedAction) {
		countdownTextView.setVisibility(TextView.GONE);
	}
	@Override
	protected void onTimedActionFinishUpload(TimedAction timedAction) {
		countdownTextView.setText("Uploading...");
	}
	@Override
	public void onDrawOneBuilding(Building building, LatLng center, float distance) {
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
		//Logger.writeToLog("distance: "+distance);
		groundOverlayOptions.image(bmpd);
		groundOverlayOptions.position(center, distance);
		groundOverlayOptions.visible(true);
		groundOverlayOptions.zIndex(2.0f);
		mMap.addGroundOverlay(groundOverlayOptions);
	}
	@Override
	public void onShowDialog(ArrayList<BuildingReceipt> buildingReceipts) {
		try {
			dialog = new Dialog(context);
			dialog.setContentView(R.layout.dialog_buildingreceipts);
			dialog.setTitle("Buildings");
			if(buildingReceipts != null) {
				if(model.getBuildingReceiptSize() == 0) {
					InformationDialog.errorDialogUIThread(context, "You can't do anything here...", null);
					return;
				}
				buildingReceiptGridview = (GridView)dialog.findViewById(R.id.dialogGridview);
				buildingReceiptsAdapter = new BuildingReceiptsAdapter();
				buildingReceiptGridview.setAdapter(buildingReceiptsAdapter);
				buildingReceiptGridview.setGravity(Gravity.RIGHT);
			}
			dialog.show();
		}
		catch(Exception e) {
			Logger.writeToLog("Nem tudja kirajzolni az épület dialogot, mert: ");
			Logger.writeException(e);
		}
	}
	@Override
	public void onStartBuilding(int tileid, int buildingReceiptType, int slice) {
		startTimedAction(TimedAction.BUILD, tileid, buildingReceiptType, slice);
	}
}
