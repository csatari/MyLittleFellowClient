package hu.jex.mylittlefellow.model;

import hu.jex.mylittlefellow.R;
import hu.jex.mylittlefellow.storage.BuildingDatabaseAdapter;
import hu.jex.mylittlefellow.storage.Storage;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView.LayoutParams;
import android.widget.ImageButton;


/**
 * Egy ter�let jobb oldali gombjai�rt felel�s
 * @author Albert
 *
 */
public class PlaceAction {
	public enum PlaceActionType {
		BUILD,STORAGE,TOWEROFKNOWLEDGE,TOOLSTATION,TAX
	}
	private static boolean isOwner;
	
	static OnPlaceActionEventListener mListener;
	public interface OnPlaceActionEventListener {
		/**
		 * R�nyomott az �p�tkez�s gombra
		 */
		public void onBuild();
		/**
		 * R�nyomott a rakt�r gombra
		 */
		public void onStorageEnter();
		/**
		 * R�nyomott a Tud�sTornya gombra
		 */
		public void onTower();
		/**
		 * R�nyomott az eszk�zk�sz�t� gombra
		 */
		public void onToolStation();
		/**
		 * R�nyomott az ad�be�ll�t�s gombra
		 */
		public void onTax();
	}
	/**
	 * Be�ll�tja az esem�nykezel�t
	 * @param eventListener
	 */
	public static void setCustomEventListener(OnPlaceActionEventListener eventListener) {
		mListener=eventListener;
	}
	
	public PlaceAction() {
		isOwner = false;
	}
	
	
	public static boolean isOwner() {
		return isOwner;
	}

	public static void setOwner(boolean isOwner) {
		PlaceAction.isOwner = isOwner;
	}

	/**
	 * Lek�rdezi, hogy h�ny gomb szerepel a megadott ter�leten.
	 * @param context
	 * @param tile
	 * @param usertileid
	 * @return
	 */
	public static int getPlaceActionCount(Context context, Tile tile, int usertileid) {
		if(tile.getId() != usertileid) {
			return 0;
		}
		BuildingDatabaseAdapter buildingDb = new BuildingDatabaseAdapter(context);
		buildingDb.open();
		ArrayList<Building> epuletLista = new ArrayList<Building>();
		try {
			epuletLista = buildingDb.getBuildingsOnTile(tile.getId());
		}
		catch(Exception e) {
			Logger.writeException(e);
		}
		finally {
			buildingDb.close();
		}
		
		int count = 1;
		for(Building build : epuletLista) {
			if(build.getType() == Building.TOWNCENTER) {
				count++;
			}
			if(build.getType() == Building.TOWEROFKNOWLEDGE) {
				count++;
			}
			if(build.getType() == Building.TOOLSTATION) {
				count++;
			}
		}
		if(PlaceAction.isOwner()) {
			count++;
		}
		return count;
	}
	/**
	 * Lek�rdezi megadott poz�ci� alapj�n, hogy melyik �p�lethez vonatkoz� action jelenik meg
	 * @param context
	 * @param buildingList
	 * @param position
	 * @param tile
	 * @param usertileid
	 * @return
	 */
	private static PlaceActionType getPlaceActionButtonType(Context context, ArrayList<Building> buildingList, int position, Tile tile, int usertileid) {
		//Logger.writeToLog("getPlaceActionButtonType: pos: "+position+" tile: "+tile.toString()+" usertileid: "+usertileid);
		PlaceActionType buttonType = null;
		if(position == 0) {
			buttonType = PlaceActionType.STORAGE;
			return buttonType;
		}
		ArrayList<Building> usefulBuildingList = new ArrayList<Building>();
		for(Building b : buildingList) {
			if(b.getType() == Building.TOWNCENTER) { //mag�hoz a rakt�r�p�lethez nem j�r placeaction
				usefulBuildingList.add(b);
			}
			if(b.getType() == Building.TOWEROFKNOWLEDGE) { 
				usefulBuildingList.add(b);
			}
			if(b.getType() == Building.TOOLSTATION) { 
				usefulBuildingList.add(b);
			}
		}
		if(PlaceAction.isOwner()) {
			Building b = new Building(0,0,Building.TAX_BUILDING,0);
			usefulBuildingList.add(b);
		}
		//Logger.writeToLog("useful buildings size: "+usefulBuildingList.size());
		if(usertileid != tile.getId() && usertileid != Storage.getHomeId(context)) {
			return buttonType;
		}
		int buildingType = usefulBuildingList.get(position-1).getType();
		if(buildingType == Building.TOWNCENTER) {
			buttonType = PlaceActionType.BUILD;
		}
		else if(buildingType == Building.TOWEROFKNOWLEDGE) {
			buttonType = PlaceActionType.TOWEROFKNOWLEDGE;
		}
		else if(buildingType == Building.TOOLSTATION) {
			buttonType = PlaceActionType.TOOLSTATION;
		}
		else if(buildingType == Building.TAX_BUILDING) {
			buttonType = PlaceActionType.TAX;
		}
		return buttonType;
	}
	/**
	 * Visszaadja a k�rt poz�ci�n a gombot
	 * @param context
	 * @param position A poz�ci�
	 * @param tile A ter�let
	 * @return Maga a gomb
	 */
	public static ImageButton getPlaceActionButton(Context context, int position, Tile tile) {
		ImageButton button = new ImageButton(context);
		BuildingDatabaseAdapter buildingDb = new BuildingDatabaseAdapter(context);
		buildingDb.open();
		ArrayList<Building> buildingList = new ArrayList<Building>();
		try {
			buildingList = buildingDb.getBuildingsOnTile(tile.getId());
		}
		catch(Exception e) {
			Logger.writeException(e);
		}
		finally {
			buildingDb.close();
		}
		
		PlaceActionType type = getPlaceActionButtonType(context,buildingList,position, tile, Storage.getUserTileId(context));
		if(type != null) {
			//Logger.writeToLog("getActionButtonType: "+position+" "+tile.getType()+" "+Storage.getHomeId(context)+" "+type.toString());
		}
		button.setId(position);
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		button.setPadding(25, 25, 25, 25);
		button.setLayoutParams(params);
		PlaceActionButtonClickListener l = new PlaceActionButtonClickListener(type);
		switch(type) {
			case BUILD:
				button.setImageResource(R.drawable.build_icon_32);
				break;
			case STORAGE:
				button.setImageResource(R.drawable.storage_icon_32);
				break;
			case TOWEROFKNOWLEDGE:
				button.setImageResource(R.drawable.tower_icon_32);
				break;
			case TOOLSTATION:
				button.setImageResource(R.drawable.drill_icon_32);
				break;
			case TAX:
				button.setImageResource(R.drawable.tax_icon_32);
				break;
			default:
				button.setImageResource(R.drawable.cancel);
				break;
		}
		button.setOnClickListener(l);
		return button;
	}
	
	/**
	 * A gombok megnyom�s�t vizsg�lja
	 * @author Albert
	 *
	 */
	static class PlaceActionButtonClickListener implements OnClickListener {
		private PlaceActionType type;
		public PlaceActionButtonClickListener(PlaceActionType type) {
			this.type = type;
		}
		@Override
		public void onClick(View v) {
			switch(type) {
			case BUILD:
				//Logger.writeToLog("build?");
				if(mListener!=null) mListener.onBuild();
				break;
			case STORAGE:
				//Logger.writeToLog("storage");
				if(mListener!=null) mListener.onStorageEnter();
				break;
			case TOWEROFKNOWLEDGE:
				//Logger.writeToLog("tower");
				if(mListener!=null) mListener.onTower();
				break;
			case TOOLSTATION:
				//Logger.writeToLog("toolstation");
				if(mListener!=null) mListener.onToolStation();
				break;
			case TAX:
				//Logger.writeToLog("tax");
				if(mListener!=null) mListener.onTax();
				break;
			default:
				//Logger.writeToLog("wat");
				break;
			}
		}
		
	}
}
