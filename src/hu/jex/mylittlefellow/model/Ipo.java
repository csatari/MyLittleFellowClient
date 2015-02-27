package hu.jex.mylittlefellow.model;


import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Egy látványosság logikai osztálya
 * @author Albert
 *
 */
public class Ipo {
	private double latitude; //A szélességi fok
	private double longitude; //Hosszúsági fok
	
	private String name; //A látványosság neve
	private String url; //A látványosság weboldala
	private double radius; //A látványosság sugara
	
	private int id; //A látványosság azonosítója
	private boolean known; //Felfedezte-e már a karakter

	public boolean isKnown() {
		return known;
	}

	public void setKnown(boolean known) {
		this.known = known;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public LatLng getCoordinate() {
		return new LatLng(latitude,longitude);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}
	/**
	 * Beállítja a rádiuszát a látványosságnak, ha van megadva távolabbi pont
	 * @param far
	 */
	public void setRadiusFromCoordinate(LatLng far) {
		//Logger.writeToLog("setRadiusFromCoordinate: ");
		this.radius = Ipo.getDistance(getCoordinate(), far);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return "Ipo [latitude=" + latitude + ", longitude=" + longitude
				+ ", name=" + name + ", url=" + url + ", radius=" + radius
				+ ", id=" + id + ", known=" + known + "]";
	}
	/**
	 * Kiszámítja a kör sugarát a megadott középpontból és a legtávolabbi pontból
	 * @param coordinate1 A középpont
	 * @param coordinate2 A legtávolabbi pont
	 * @return A sugár méterben
	 */
	public static double getDistance(LatLng coordinate1, LatLng coordinate2) {
		//double distance = Math.sqrt(Math.pow((coordinate1.latitude - coordinate2.latitude),2)+Math.pow((coordinate1.longitude - coordinate2.longitude),2));
		Location location1 = new Location("first");
		Location location2 = new Location("second");
		location1.setLatitude(coordinate1.latitude);
		location1.setLongitude(coordinate1.longitude);
		location2.setLatitude(coordinate2.latitude);
		location2.setLongitude(coordinate2.longitude);
		double distance = location1.distanceTo(location2);
		distance *= 1.25;
		//Logger.writeToLog("The distance is: "+distance);
		return distance;
	}
}
