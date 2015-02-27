package hu.jex.mylittlefellow.model;


import hu.jex.mylittlefellow.communicator.CommunicatorIpo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * A t�rk�pen val� dinamikus megjelen�t�st szolg�lja
 * @author Albert
 *
 */
public class VisibleTileAdapter {
	private static ArrayList<Tile> allTiles = new ArrayList<Tile>(); //az �sszes ter�let
	private static Map<Integer,GroundOverlay> visiblePolygon = new TreeMap<Integer,GroundOverlay>(); //a l�that� ter�let
	private static ArrayList<Point> regionCoordinates = new ArrayList<Point>(); //a l�that� ter�let
	private Activity context;
	private GoogleMap map;
	//IPOs
	private static ArrayList<Ipo> ipoSeenByCamera = new ArrayList<Ipo>(); //a kamera �ltal l�tott l�tv�nyoss�gok
	private static ArrayList<Ipo> ipoSeenByCharacter = new ArrayList<Ipo>(); //a felhaszn�l� �ltal l�tott l�tv�nyoss�gok
	private static Map<Integer,Marker> visibleIpoMarker = new TreeMap<Integer,Marker>(); //a l�that� l�tv�nyoss�gok grafikai elemei
	private static Map<Integer,Circle> visibleIpoCircle = new TreeMap<Integer, Circle>(); //a l�that� k�r�k grafikai elelmei
	private final static int NODE_DISTANCE_UNTIL_RECOGNIZE = 15; //h�ny m�teren bel�l ismerje fel az ipo-t
	
	
	static int mero = 0;
	public VisibleTileAdapter(Activity context, GoogleMap map) {
		this.context = context;
		this.map = map;
		//allTiles = new ArrayList<Tile>();
		//regionCoordinates = new ArrayList<Point>();
		//visiblePolygon = new TreeMap<Integer,GroundOverlay>();
        /*ipoSeenByCamera = new ArrayList<Ipo>();
        ipoSeenByCharacter = new ArrayList<Ipo>();
        visibleIpoMarker = new TreeMap<Integer,Marker>();
        visibleIpoCircle = new TreeMap<Integer, Circle>();*/
	}
	
	public void setAllTiles(ArrayList<Tile> allTiles) {
		VisibleTileAdapter.allTiles = allTiles;
	}
	public static ArrayList<Tile> getAllTiles() {
		return allTiles;
	}
	public static void removeAllTiles() {
		for(Map.Entry<Integer, GroundOverlay> entry : visiblePolygon.entrySet()) {
			entry.getValue().remove();
	    }
		visiblePolygon.clear();
	}
	
	public static ArrayList<Ipo> getIpoSeenByCamera() {
		return ipoSeenByCamera;
	}

	public static void setIpoSeenByCamera(ArrayList<Ipo> ipoSeenByCamera) {
		VisibleTileAdapter.ipoSeenByCamera = ipoSeenByCamera;
	}

	public static ArrayList<Ipo> getIpoSeenByCharacter() {
		return ipoSeenByCharacter;
	}

	public static void setIpoSeenByCharacter(ArrayList<Ipo> ipoSeenByCharacter) {
		VisibleTileAdapter.ipoSeenByCharacter = ipoSeenByCharacter;
	}

	public static void updateOneTileByIndex(Tile tile, int index) {
		allTiles.remove(index);
		addTile(tile);
	}
	public static void updateOneTile(Tile tile) {
		int index = 0;
		for(Tile t : allTiles) {
			if(t.getId() == tile.getId()) {
				break;
			}
			index++;
		}
		updateOneTileByIndex(tile, index);
	}
	/**
	 * Hozz�ad egy ter�letet az �sszes ter�lethez
	 * @param tile
	 */
	public static void addTile(Tile tile) {
		allTiles.add(tile);
		Logger.writeToLog("A tile hozz�adva az adatb�zishoz...");
	}
	/**
	 * Be�ll�tja a kamera �ltal l�tott ter�letet
	 * @param nearLeft
	 * @param nearRight
	 * @param farLeft
	 * @param farRight
	 */
	public static void setVisibleRegion(LatLng nearLeft, LatLng nearRight, LatLng farLeft, LatLng farRight) {
		//Logger.writeToLog("regionCoordinates be�ll�t�s: "+nearLeft.toString());
		if(regionCoordinates != null) {
			regionCoordinates.clear();
			regionCoordinates.add(new Point(nearLeft.longitude,nearLeft.latitude));
			regionCoordinates.add(new Point(farLeft.longitude,farLeft.latitude));
			regionCoordinates.add(new Point(farRight.longitude,farRight.latitude));
			regionCoordinates.add(new Point(nearRight.longitude,nearRight.latitude));
		}
	}
	/**
	 * Let�r�l egy ter�letet a t�rk�pr�l
	 * @param tile
	 */
	public static void removeTileFromMap(Tile tile) {
		visiblePolygon.get(tile.getId()).remove();
		visiblePolygon.remove(tile.getId());
	}
	/**
	 * Kirajzolja az �sszes l�that� ter�letet
	 */
	public void getAllPolygons() {
		long now = System.currentTimeMillis();
		mero = 0;
		Map<Integer,Tile> avt = getAllVisibleTiles();
		long again = System.currentTimeMillis();
		
		Logger.writeToLog("getAllVisibleTiles gyorsas�g: "+(again-now)+" lefut�s: "+mero);
		LinkedList<Integer> torlendo = new LinkedList<Integer>();
		
		for(Map.Entry<Integer, GroundOverlay> entry : visiblePolygon.entrySet()) {
			if(!avt.containsKey(entry.getKey())) {
				entry.getValue().remove();
				torlendo.add(entry.getKey());
			}
			else {
				avt.remove(entry.getKey());
			}
	    }
		//Logger.writeToLog("torlendo: "+torlendo.size());
		for(Integer i : torlendo) {
			visiblePolygon.remove(i);
		}
		//Logger.writeToLog("getAllVisibleTiles2: "+avt.size());
		for(Map.Entry<Integer, Tile> entry : avt.entrySet()) {
			//Logger.writeToLog("rajzol�s: "+entry.getValue().getId());
			GroundOverlay polygon = drawTile(entry.getValue(),map);
			visiblePolygon.put(entry.getValue().getId(), polygon);
		}
	}
	/*private Polygon drawTile(Tile tile, GoogleMap map) {
		PolygonOptions polygonOptions = new PolygonOptions();
		polygonOptions.add(tile.getNorthWest(),tile.getNorthEast(),tile.getSouthEast(),tile.getSouthWest(), tile.getNorthWest());
		polygonOptions.strokeColor(Color.BLACK);
        polygonOptions.strokeWidth(1);
        polygonOptions.fillColor(tile.getColor());
        Polygon polygon = map.addPolygon(polygonOptions);
        
        return polygon;
        
	}*/
	/**
	 * Kirajzol egy ter�letet
	 * @param tile a ter�let
	 * @param map a t�rk�
	 * @return a kirajzol t�glalap
	 */
	private GroundOverlay drawTile(Tile tile, GoogleMap map) {
        int[] colors = { tile.getColor() };
        Bitmap terkep = Bitmap.createBitmap(colors, 1, 1, Bitmap.Config.RGB_565);
        
        GroundOverlayOptions groundOverlayOptions = new GroundOverlayOptions();
        groundOverlayOptions.image(BitmapDescriptorFactory.fromBitmap(terkep));
        LatLngBounds bounds = new LatLngBounds(tile.getSouthWest(), tile.getNorthEast());
        groundOverlayOptions.positionFromBounds(bounds);
        groundOverlayOptions.visible(true);
        groundOverlayOptions.transparency(0.2f);
        GroundOverlay go = map.addGroundOverlay(groundOverlayOptions);
        
        return go;
        
	}
	
	/**
	 * lek�ri az �sszes olyan ter�letet, ami l�that�
	 * @return
	 */
	public Map<Integer,Tile> getAllVisibleTiles() {
		Map<Integer,Tile> visibleTiles = new TreeMap<Integer,Tile>();
		for(Tile tile : allTiles) {
			if(isPointInPolygon(new Point(tile.getTileCenterLongitude(),tile.getTileCenterLatitude()))) {
				visibleTiles.put(tile.getId(), tile);
			}
			else {
				if(isPointInPolygon(new Point(tile.getNorthEast().longitude,tile.getNorthEast().latitude))) {
					visibleTiles.put(tile.getId(), tile);
				}
				else if(isPointInPolygon(new Point(tile.getNorthWest().longitude,tile.getNorthWest().latitude))) {
					visibleTiles.put(tile.getId(), tile);
				}
				else if(isPointInPolygon(new Point(tile.getSouthEast().longitude,tile.getSouthEast().latitude))) {
					visibleTiles.put(tile.getId(), tile);
				}
				else if(isPointInPolygon(new Point(tile.getSouthWest().longitude,tile.getSouthWest().latitude))) {
					visibleTiles.put(tile.getId(), tile);
				}
				else if(isPointInPolygon(new Point(tile.getSouth().longitude,tile.getSouth().latitude))) {
					visibleTiles.put(tile.getId(), tile);
				}
				else if(isPointInPolygon(new Point(tile.getWest().longitude,tile.getWest().latitude))) {
					visibleTiles.put(tile.getId(), tile);
				}
				else if(isPointInPolygon(new Point(tile.getNorth().longitude,tile.getNorth().latitude))) {
					visibleTiles.put(tile.getId(), tile);
				}
				else if(isPointInPolygon(new Point(tile.getEast().longitude,tile.getEast().latitude))) {
					visibleTiles.put(tile.getId(), tile);
				}
			}
		}
		return visibleTiles;
	}
	
	/**
	 * Visszaadja, hogy egy koordin�ta a kamera �ltal l�that�-e
	 * @param coordinate A koordin�ta
	 * @return Igaz, ha l�that�
	 */
	private boolean isPointInPolygon(Point coordinate) {
		mero++;
		if(regionCoordinates == null) return false;
		if(regionCoordinates.size() == 0) return false;
		if(coordinate == null) return false;
		int i=0;
		int j = 0;
		boolean c = false;
		
		for(i=0,j=regionCoordinates.size()-1; i < regionCoordinates.size(); j = i++) {
			if ( ((regionCoordinates.get(i).y > coordinate.y) != (regionCoordinates.get(j).y > coordinate.y)) && 
					(coordinate.x < (regionCoordinates.get(j).x-regionCoordinates.get(i).x) *
							(coordinate.y - regionCoordinates.get(i).y) / (regionCoordinates.get(j).y-regionCoordinates.get(i).y) + regionCoordinates.get(i).x) ) {
				c = !c;
			}
		}
		return c;
	}
	
	//IPOs
	/**
	 * Kirajzolja az �sszes l�tv�nyoss�got
	 */
	public void drawAllVisibleMarkers() {
		//Lek�rem az �sszes olyan elem id-j�t, ami l�that� kell, hogy legyen
		LinkedList<Integer> megtartando = new LinkedList<Integer>();
		for(Ipo ipo : ipoSeenByCamera) {
			megtartando.add(ipo.getId());
		}
		for(Ipo ipo : ipoSeenByCharacter) {
			megtartando.add(ipo.getId());
		}
		
		//V�gigmegyek a most l�that� elemeken, ha nincs benne az id-je az �jonnan l�that� elemid-k k�z�tt, akkor t�rl�m
		LinkedList<Integer> torlendo = new LinkedList<Integer>();
		try {
			for(Map.Entry<Integer, Marker> entry : visibleIpoMarker.entrySet()) {
				if(!megtartando.contains(entry.getKey())) {
					torlendo.add(entry.getKey());
				}
		    }
		}
		catch(Exception e) {
			
		}
		for(Integer i : torlendo) {
			visibleIpoMarker.get(i).remove();
			if(visibleIpoCircle.get(i) != null) {
				visibleIpoCircle.get(i).remove();
				visibleIpoCircle.remove(i);
			}
			visibleIpoMarker.remove(i);
		}
		//V�gigmegyek az �sszes �jonnan l�that� elemen, �s ha nincs benne a r�gebben l�that� elemekben, hozz�adom
		for(Ipo ipo : ipoSeenByCamera) {
			if(!visibleIpoMarker.containsKey(ipo.getId())) {
				Marker marker = drawMarker(ipo, map);
				visibleIpoMarker.put(ipo.getId(), marker);
				if(ipo.getRadius() > 0) {
					Circle circle = drawCircle(ipo, map);
					visibleIpoCircle.put(ipo.getId(), circle);
				}
			}
			
		}
		for(Ipo ipo : ipoSeenByCharacter) {
			if(!visibleIpoMarker.containsKey(ipo.getId())) {
				Marker marker = drawMarker(ipo, map);
				visibleIpoMarker.put(ipo.getId(), marker);
				if(ipo.getRadius() > 0) {
					Circle circle = drawCircle(ipo, map);
					visibleIpoCircle.put(ipo.getId(), circle);
				}
			}
		}
	}
	/**
	 * Let�rli az �sszes l�tv�nyoss�got
	 */
	public void deleteAllMarkers() {
		for(Map.Entry<Integer, Marker> entry : visibleIpoMarker.entrySet()) {
			entry.getValue().remove();
	    }
		for(Map.Entry<Integer, Circle> entry : visibleIpoCircle.entrySet()) {
			entry.getValue().remove();
	    }
		visibleIpoMarker.clear();
		visibleIpoCircle.clear();
		ipoSeenByCamera.clear();
		ipoSeenByCharacter.clear();
	}
	/**
	 * Kirajzol egy l�tv�nyoss�got
	 * @param ipo a l�tv�nyoss�g
	 * @param map a t�rk�
	 * @return a jel�l�pont
	 */
	public Marker drawMarker(Ipo ipo, GoogleMap map) {
		MarkerOptions markerOptions = new MarkerOptions()
		.position(ipo.getCoordinate())
		.alpha(1.0f)
		.flat(false)
		.title(ipo.getName());
		if(ipo.isKnown()) {
			markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
		}
		else {
			markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
		}
		
		return map.addMarker(markerOptions);
	}
	/**
	 * Kirajzol egy k�rt a l�tv�nyoss�ghoz
	 * @param ipo a l�tv�nyoss�g
	 * @param map a t�rk�p
	 * @return a k�r
	 */
	public Circle drawCircle(Ipo ipo, GoogleMap map) {
		
		CircleOptions co = new CircleOptions()
	     .center(ipo.getCoordinate())
	     .radius(ipo.getRadius())
	     .strokeColor(Color.BLUE)
	     .strokeWidth(1f);
		if(ipo.isKnown()) {
			co.fillColor(Color.argb(50, 105, 255, 117));
		}
		else {
			co.fillColor(Color.argb(50, 69, 243, 255));
		}
		Circle circle = map.addCircle(co);
		return circle;
	}
	/**
	 * null-lal t�r vissza, ha nem �ll egy IPO-n sem, egy�bk�nt meg az ipo-val, amelyiken van
	 * @return
	 */
	public Ipo getCharacterFurthestIpo(LatLng characterCoordinate) {
		Location ch = new Location("ch");
		Location ipoLoc = new Location("ipo");
		ch.setLatitude(characterCoordinate.latitude);
		ch.setLongitude(characterCoordinate.longitude);
		Ipo closest = null;
		//Logger.writeToLog("A k�t t�rol� m�rete: "+ ipoSeenByCharacter.size() + " "+ipoSeenByCamera.size());
		for(Ipo ipo : ipoSeenByCharacter) {
			if(ipo.isKnown()) continue;
			ipoLoc.setLatitude(ipo.getLatitude());
			ipoLoc.setLongitude(ipo.getLongitude());
			double radius = ipo.getRadius();
			if(radius == 0) radius = NODE_DISTANCE_UNTIL_RECOGNIZE;
			//Logger.writeToLog("IPO t�vols�g: "+ch.distanceTo(ipoLoc));
			if(ch.distanceTo(ipoLoc) <= radius) {
				closest = ipo;
				break;
			}
		}
		if(closest != null) {
			return closest;
		}
		for(Ipo ipo : ipoSeenByCamera) {
			if(ipo.isKnown()) continue;
			ipoLoc.setLatitude(ipo.getLatitude());
			ipoLoc.setLongitude(ipo.getLongitude());
			double radius = ipo.getRadius();
			if(radius == 0) radius = NODE_DISTANCE_UNTIL_RECOGNIZE;
			//Logger.writeToLog("IPO t�vols�g: "+ch.distanceTo(ipoLoc));
			if(ch.distanceTo(ipoLoc) <= radius) {
				closest = ipo;
				break;
			}
		}
		return closest;
	}
	/**
	 * Megpr�b�l felfedezni egy �j l�tv�nyoss�got
	 * @param characterCoordinate a karakter koordin�t�ja
	 */
	public void tryToDiscover(LatLng characterCoordinate) {
		Logger.writeToLog("tryToDiscover coordinate: "+characterCoordinate);
		Ipo ipo = getCharacterFurthestIpo(characterCoordinate);
		if(ipo != null) {
			Logger.writeToLog("ipo felfedez�se..."+ipo.toString());
			DiscoverIpo d = new DiscoverIpo();
			d.execute(ipo);
		}
		else {
			Logger.writeToLog("tryToDiscover Null ipo...");
		}
	}
	/**
	 * Lek�rdezi, hogy a megadott jel�l�pont l�tv�nyoss�g-e
	 * @param marker a jel�l�pont
	 * @return Igaz, ha l�tv�nyoss�g
	 */
	public static boolean isIpoMarker(Marker marker) {
		for(Map.Entry<Integer, Marker> entry : visibleIpoMarker.entrySet()) {
			if(marker.equals(entry.getValue())) {
				return true;
			}
	    }
		return false;
	}
	/**
	 * Lek�rdezi a jel�l�pontb�l a l�tv�ynoss�got
	 * @param marker a jel�l�pont
	 * @return a l�tv�nyoss�g
	 */
	public static Ipo getIpoFromMarker(Marker marker) {
		Ipo closest = null;
		for(Ipo ipo : ipoSeenByCharacter) {
			if(Tile.compareLatLng(ipo.getCoordinate(), marker.getPosition())) {
				closest = ipo;
				break;
			}
		}
		if(closest != null) {
			return closest;
		}
		for(Ipo ipo : ipoSeenByCamera) {
			if(Tile.compareLatLng(ipo.getCoordinate(), marker.getPosition())) {
				closest = ipo;
				break;
			}
		}
		return closest;
	}
	/**
	 * Felfedez egy l�tv�nyoss�got
	 * @author Albert
	 *
	 */
	private class DiscoverIpo extends AsyncTask<Ipo, Void, Void> {
		@Override
		protected Void doInBackground(Ipo... params) {
			CommunicatorIpo comm = new CommunicatorIpo(context);
			if(!params[0].isKnown()) {				
				comm.discoverIpo(params[0]);
			}
			if(!comm.isProblem()) {
				for(Ipo ipo : ipoSeenByCharacter) {
					if(ipo.getId() == params[0].getId()) {
						params[0].setKnown(true);
						ipo.setKnown(true);
						break;
					}
				}
				if(!params[0].isKnown()) {
					for(Ipo ipo : ipoSeenByCamera) {
						if(ipo.getId() == params[0].getId()) {
							params[0].setKnown(true);
							ipo.setKnown(true);
							break;
						}
					}
				}
				try {
					context.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							for(Map.Entry<Integer, Marker> entry : visibleIpoMarker.entrySet()) {
								entry.getValue().remove();
						    }
							for(Map.Entry<Integer, Circle> entry : visibleIpoCircle.entrySet()) {
								entry.getValue().remove();
						    }
							visibleIpoMarker.clear();
							visibleIpoCircle.clear();
							drawAllVisibleMarkers();
						}
					});
				}
				catch(NullPointerException e) {
					Logger.writeToLog("DiscoverIpo NullException");
					Logger.writeException(e);
				}
				
			}
			return null;
		}
	}
}
