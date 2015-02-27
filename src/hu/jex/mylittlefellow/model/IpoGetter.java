package hu.jex.mylittlefellow.model;

import hu.jex.mylittlefellow.communicator.CommunicatorIpo;

import java.util.ArrayList;

import android.app.Activity;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

/**
 * A l�tv�nyoss�g lek�r�s��rt felel�s oszt�ly
 * @author Albert
 *
 */
public class IpoGetter {
	private final static double RENEW_DISTANCE = 200; //m�terben
	private final static double RENEW_DISTANCE_COORDINATE = 0.05; //m�terben
	private double characterLat = -1000;
	private double characterLon = -1000;
	
	private ArrayList<Ipo> seenByCharacter;
	private ArrayList<Ipo> seenByCamera;
	
	private double cameraFromLat;
	private double cameraFromLon;
	private double cameraToLat;
	private double cameraToLon;
	
	private double characterFromLat;
	private double characterFromLon;
	private double characterToLat;
	private double characterToLon;
	
	private Activity context;
	
	private OnIpoGetListener mListener;
	public interface OnIpoGetListener {
		/**
		 * Akkor fut le, amikor v�ltozott a kameran�zet �s le lett k�rve az �sszes l�tv�nyoss�g
		 * @param seenByCamera A l�that� l�tv�nyoss�gok t�mbje
		 */
		public void onCameraIpoChange(ArrayList<Ipo> seenByCamera);
		/**
		 * Akkor fut le, amikor v�ltozott a karakter n�zete �s le lett k�rve az �ltala l�that� l�tv�nyoss�g
		 * @param seenByCharacter A karakter �ltal l�that� l�tv�nyoss�gok t�mbje
		 */
		public void onCharacterIpoChange(ArrayList<Ipo> seenByCharacter);
	}
	/**
	 * Be�ll�tja az esem�nykezel�t
	 * @param eventListener
	 */
	public void setCustomEventListener(OnIpoGetListener eventListener) {
		mListener=eventListener;
	}
	
	public IpoGetter(Activity context) {
		this.context = context;
		seenByCharacter = new ArrayList<Ipo>();
		seenByCamera = new ArrayList<Ipo>();
	}
	/**
	 * Lek�ri a legutolj�ra be�ll�tott koordin�t�t
	 * @return
	 */
	private LatLng getCharacterLastCoordinate() {
		return new LatLng(characterLat, characterLon);
	}
	/**
	 * Be�ll�tja a karakter koordin�t�j�t �s let�lti a l�that� l�tv�nyoss�gokat, ha sz�ks�ges
	 * @param coordinate A karakter koordin�t�ja
	 */
	public void setCharacterPlace(LatLng coordinate) {
		
		if(Ipo.getDistance(coordinate,getCharacterLastCoordinate()) > RENEW_DISTANCE) {
			if(seenByCharacter != null) {
				seenByCharacter.clear();
			}
			characterFromLat = coordinate.latitude - (RENEW_DISTANCE_COORDINATE);
			characterFromLon = coordinate.longitude - (RENEW_DISTANCE_COORDINATE);
			characterToLat = coordinate.latitude + (RENEW_DISTANCE_COORDINATE);
			characterToLon = coordinate.longitude + (RENEW_DISTANCE_COORDINATE);
			characterLat = coordinate.latitude;
			characterLon = coordinate.longitude;
			downloadIposForCharacter();
		}
	}
	/**
	 * Be�ll�tja a kamera koordin�t�it �s let�lti az �j l�tv�nyoss�gokat
	 * @param nearLeft
	 * @param nearRight
	 * @param farLeft
	 * @param farRight
	 */
	public void setCameraPosition(LatLng nearLeft, LatLng nearRight, LatLng farLeft, LatLng farRight) {
		if(seenByCamera != null) seenByCamera.clear();
		ArrayList<Double> latCoordinates = new ArrayList<Double>();
		latCoordinates.add(nearLeft.latitude);
		latCoordinates.add(nearRight.latitude);
		latCoordinates.add(farLeft.latitude);
		latCoordinates.add(farRight.latitude);
		
		ArrayList<Double> lonCoordinates = new ArrayList<Double>();
		lonCoordinates.add(nearLeft.longitude);
		lonCoordinates.add(nearRight.longitude);
		lonCoordinates.add(farLeft.longitude);
		lonCoordinates.add(farRight.longitude);
		
		cameraFromLat = latCoordinates.get(0);
		cameraToLat = latCoordinates.get(0);
		for(double coord : latCoordinates) {
			if(cameraFromLat > coord) {
				cameraFromLat = coord;
			}
			if(cameraToLat < coord) {
				cameraToLat = coord;
			}
		}
		
		cameraFromLon = lonCoordinates.get(0);
		cameraToLon = lonCoordinates.get(0);
		for(double coord : lonCoordinates) {
			if(cameraFromLon > coord) {
				cameraFromLon = coord;
			}
			if(cameraToLon < coord) {
				cameraToLon = coord;
			}
		}
		downloadIposForCamera();
	}
	
	/**
	 * Elkezdi let�lteni a karakter �ltal l�tott l�tv�nyoss�gokat
	 */
	private void downloadIposForCharacter() {
		IpoGet i = new IpoGet();
		i.execute(IpoGet.FOR_CHARACTER);
	}
	/**
	 * Elkezdi let�lteni a kamera �ltal l�tott l�tv�nyoss�gokat
	 */
	private void downloadIposForCamera() {
		IpoGet i = new IpoGet();
		i.execute(IpoGet.FOR_CAMERA);
	}
	
	
	/**
	 * A l�tv�nyoss�gok let�lt�s�t kezel� oszt�ly
	 * @author Albert
	 *
	 */
	private class IpoGet extends AsyncTask<Integer, Void, Void> {
		public final static int FOR_CHARACTER = 1;
		public final static int FOR_CAMERA = 2;
		@Override
		protected Void doInBackground(Integer... params) {
			if(params[0] == FOR_CHARACTER) {
				CommunicatorIpo comm = new CommunicatorIpo(context);
				seenByCharacter = comm.getIpos(new LatLng(characterFromLat, characterFromLon), new LatLng(characterToLat, characterToLon));
				if(mListener != null) mListener.onCharacterIpoChange(seenByCharacter);
			}
			else if(params[0] == FOR_CAMERA) {
				CommunicatorIpo comm = new CommunicatorIpo(context);
				seenByCamera = comm.getIpos(new LatLng(cameraFromLat, cameraFromLon), new LatLng(cameraToLat, cameraToLon));
				if(mListener != null) mListener.onCameraIpoChange(seenByCamera);
			}
			return null;
		}
	}
}
