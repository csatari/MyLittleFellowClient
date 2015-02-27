package hu.jex.mylittlefellow.model;

import hu.jex.mylittlefellow.communicator.CommunicatorTile;
import hu.jex.mylittlefellow.communicator.CommunicatorUserDetails;
import hu.jex.mylittlefellow.model.Tile.OnTileDownloaded;
import hu.jex.mylittlefellow.storage.Storage;
import android.app.Activity;
import android.os.AsyncTask;

/**
 * Egy terület modellje
 * @author Albert
 *
 */
public class Place {

	private static Activity context;
	
	private static Tile tile;
	
	static OnPlaceEvent mListener;
	public interface OnPlaceEvent {
		/**
		 * Hiba történt
		 */
		public void onError();
		/**
		 * Sikersen letöltötte a terület adatait
		 * @param tile
		 */
		public void onGetTaxFinished(Tile tile);
		/**
		 * Lekérdezte, hogy földesúr-e a karakter
		 */
		public void onAmIOwnerFinished();
	}
	/**
	 * Beállítja az eseménykezelõt
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
	 * Lekérdezi a beállított adót
	 */
	public void getTax() {
		Async a = new Async();
		a.execute(Async.GET_TAX);
	}
	/**
	 * Lekérdezi, hogy földesúr-e
	 */
	public void amIOwner() {
		Async a = new Async();
		a.execute(Async.IS_OWNER);
	}
	/**
	 * Befizeti az adót
	 */
	public void payTax() {
		Async a = new Async();
		a.execute(Async.PAY_TAX);
	}
	
	/**
	 * A másik szálon való dolgokért felelõs
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
