package hu.jex.mylittlefellow.gui;

import hu.jex.mylittlefellow.R;
import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;
/**
 * Custom megjelenítés a Marker InfoWindow-jához. Mûködik a sortörés
 * @author Albert
 *
 */
public class CustomInfoWindowAdapter implements InfoWindowAdapter {
	private final View mymarkerview;

	CustomInfoWindowAdapter(Activity context) {
        mymarkerview = context.getLayoutInflater().inflate(R.layout.infowindow_custom, null); 
    }
	@Override
	public View getInfoContents(Marker marker) {
		TextView tv = (TextView)mymarkerview.findViewById(R.id.textview);
		tv.setText(marker.getTitle());
		return mymarkerview;
	}

	@Override
	public View getInfoWindow(Marker marker) {
		return null;
	}
}
