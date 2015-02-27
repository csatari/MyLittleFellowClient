package hu.jex.mylittlefellow.model;

import hu.jex.mylittlefellow.communicator.CommunicatorTile;
import hu.jex.mylittlefellow.communicator.CommunicatorUserDetails;
import hu.jex.mylittlefellow.model.Tile.OnTileDownloaded;
import hu.jex.mylittlefellow.storage.Storage;
import android.app.Activity;
import android.os.AsyncTask;

/**
 * Egy ter�let modellje
 * @author Albert
 *
 */
public class Place {

	private static Activity context;
	
	private static Tile tile;
	
	static OnPlaceEvent mListener;
	public interface OnPlaceEvent {
		/**
		 * Hiba t�rt�nt
		 */
		public void onError();
		/**
		 * Sikersen let�lt�tte a ter�let adatait
		 * @param tile
		 */
		public void onGetTaxFinished(Tile tile);
		/**
		 * Lek�rdezte, hogy f�ldes�r-e a karakter
		 */
		public void onAmIOwnerFinished();
	}
	/**
	 * Be�ll�tja az esem�nykezel�t
	 * @param eventListener
	 */
	public void setCustomEventListener(OnPlaceEvent eventListener) {
		mListener=eventListener;
	}
	
	public Place(Activity context,Tile tile) {
		Place.context = context;
		Place.tile = tile;
	}
	/**
	 * Lek�rdezi a be�ll�tott ad�t
	 */
	public void getTax() {
		Async a = new Async();
		a.execute(Async.GET_TAX);
	}
	/**
	 * Lek�rdezi, hogy f�ldes�r-e
	 */
	public void amIOwner() {
		Async a = new Async();
		a.execute(Async.IS_OWNER);
	}
	/**
	 * Befizeti az ad�t
	 */
	public void payTax() {
		Async a = new Async();
		a.execute(Async.PAY_TAX);
	}
	
	/**
	 * A m�sik sz�lon val� dolgok�rt felel�s
	 * @author Albert
	 *
	 */
	private class Async extends AsyncTask<Integer, Void, Void> {
		public final static int IS_OWNER = 1;
		public final static int PAY_TAX = 3;
		public final static int GET_TAX = 2;
		@Override
		protected Void doInBackground(Integer... params) {
			if(params[0] == IS_OWNER) {
				if(tile.getId() == Storage.getUserTileId(context)) {
					CommunicatorTile comm = new CommunicatorTile(context);
					boolean amiowner = comm.amIOwner(tile.getId());
					PlaceAction.setOwner(amiowner);
					context.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if(mListener!=null)mListener.onAmIOwnerFinished();
						}
					});
				}
			}
			else if(params[0] == GET_TAX) {
				Tile.setOnTileDownloadedListener(new OnTileDownloaded() {
					@Override
					public void onFinished(Tile t) {
						tile = t;
						if(mListener!=null)mListener.onGetTaxFinished(tile);
					}
				});
				try {
					Tile.refreshTileWithDownload(context, tile);
				}
				catch(NullPointerException e) { //elfelejtette a tile-t
					if(mListener!=null)mListener.onError();
				}
				
			}
			else if(params[0] == PAY_TAX) {
				CommunicatorUserDetails comm = new CommunicatorUserDetails(context);
				comm.payTax(tile.getId());
				
			}
			return null;
		}
	}
}
