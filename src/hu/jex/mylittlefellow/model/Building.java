package hu.jex.mylittlefellow.model;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Egy épület logikai osztálya
 * @author Albert
 *
 */
public class Building extends SaveableObject {

	private int id; //azonosító
	private int tileId; //melyik területen található
	private int sliceId; //a terület melyik részletén található
	private int type; //az épület típusa
	private int level; //az épület szintje
	private boolean finished; //be van-e fejezve az épület
	
	public final static int TOWNCENTER = 1;
	public final static int STORAGE = 2;
	public final static int TOWEROFKNOWLEDGE = 3;
	public final static int TOOLSTATION = 4;
	public final static int TAX_BUILDING = 100;
	
	public Building() {}

	public Building(int tileId, int sliceId, int type, int level) {
		super();
		this.tileId = tileId;
		this.sliceId = sliceId;
		this.type = type;
		this.level = level;
	}

	public int getTileId() {
		return tileId;
	}

	public void setTileId(int tileId) {
		this.tileId = tileId;
	}

	public int getSliceId() {
		return sliceId;
	}

	public void setSliceId(int sliceId) {
		this.sliceId = sliceId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}
	
	/**
	 * Lekéri egy épület nagyságát
	 * @param tile A terület, amire ki kell majd rajzolni
	 * @param slice A terület részlete, amire rajzolni kell majd
	 * @return Méterben megadva, hogy milyen széles legyen az épület
	 */
	public static float getBuildingDistance(Tile tile, int slice) {
		if(tile == null) {
			return 0;
		}
		Location location1 = new Location("first");
		Location location2 = new Location("second");
		//Logger.writeToLog("slice: "+slice);
		switch(slice) {
			case 1:
				location1.setLatitude(tile.getNorthWestBuildingEast().latitude);
		        location1.setLongitude(tile.getNorthWestBuildingEast().longitude);
		        location2.setLatitude(tile.getNorthWestBuildingWest().latitude);
		        location2.setLongitude(tile.getNorthWestBuildingWest().longitude);
				break;
			case 2:
				location1.setLatitude(tile.getNorthBuildingEast().latitude);
		        location1.setLongitude(tile.getNorthBuildingEast().longitude);
		        location2.setLatitude(tile.getNorthBuildingWest().latitude);
		        location2.setLongitude(tile.getNorthBuildingWest().longitude);
				break;
			case 3:
				location1.setLatitude(tile.getNorthEastBuildingEast().latitude);
		        location1.setLongitude(tile.getNorthEastBuildingEast().longitude);
		        location2.setLatitude(tile.getNorthEastBuildingWest().latitude);
		        location2.setLongitude(tile.getNorthEastBuildingWest().longitude);
				break;
			case 4:
				location1.setLatitude(tile.getWestBuildingEast().latitude);
		        location1.setLongitude(tile.getWestBuildingEast().longitude);
		        location2.setLatitude(tile.getWestBuildingWest().latitude);
		        location2.setLongitude(tile.getWestBuildingWest().longitude);
				break;
			case 5:
				location1.setLatitude(tile.getCenterBuildingEast().latitude);
		        location1.setLongitude(tile.getCenterBuildingEast().longitude);
		        location2.setLatitude(tile.getCenterBuildingWest().latitude);
		        location2.setLongitude(tile.getCenterBuildingWest().longitude);
		        break;
			case 6:
				location1.setLatitude(tile.getEastBuildingEast().latitude);
		        location1.setLongitude(tile.getEastBuildingEast().longitude);
		        location2.setLatitude(tile.getEastBuildingWest().latitude);
		        location2.setLongitude(tile.getEastBuildingWest().longitude);
				break;
			case 7:
				location1.setLatitude(tile.getSouthWestBuildingEast().latitude);
		        location1.setLongitude(tile.getSouthWestBuildingEast().longitude);
		        location2.setLatitude(tile.getSouthWestBuildingWest().latitude);
		        location2.setLongitude(tile.getSouthWestBuildingWest().longitude);
				break;
			case 8:
				location1.setLatitude(tile.getSouthBuildingEast().latitude);
		        location1.setLongitude(tile.getSouthBuildingEast().longitude);
		        location2.setLatitude(tile.getSouthBuildingWest().latitude);
		        location2.setLongitude(tile.getSouthBuildingWest().longitude);
				break;
			case 9:
				location1.setLatitude(tile.getSouthEastBuildingEast().latitude);
		        location1.setLongitude(tile.getSouthEastBuildingEast().longitude);
		        location2.setLatitude(tile.getSouthEastBuildingWest().latitude);
		        location2.setLongitude(tile.getSouthEastBuildingWest().longitude);
				break;
	        default:
	        	location1.setLatitude(tile.getCenterBuildingEast().latitude);
		        location1.setLongitude(tile.getCenterBuildingEast().longitude);
		        location2.setLatitude(tile.getCenterBuildingWest().latitude);
		        location2.setLongitude(tile.getCenterBuildingWest().longitude);
		        break;
		}
        float distance = location1.distanceTo(location2);
		
		return distance;
	}
	/**
	 * Lekéri egy épület középpontjának koordinátáját
	 * @param tile A terület, amin van az épület
	 * @param slice A területrészlet
	 * @return Koordináta
	 */
	public static LatLng getBuildingCenter(Tile tile, int slice) {
		switch (slice) {
		case 1: return tile.getNorthWestBuildingCenter();
		case 2: return tile.getNorthBuildingCenter();
		case 3: return tile.getNorthEastBuildingCenter();
		case 4: return tile.getWestBuildingCenter();
		case 5: return tile.getCenteredCoordinate();
		case 6: return tile.getEastBuildingCenter();
		case 7: return tile.getSouthWestBuildingCenter();
		case 8: return tile.getSouthBuildingCenter();
		case 9: return tile.getSouthEastBuildingCenter();
		default:break;
		}
		return null;
	}
}
