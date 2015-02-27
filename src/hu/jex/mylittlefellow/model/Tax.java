package hu.jex.mylittlefellow.model;

import hu.jex.mylittlefellow.communicator.CommunicatorUserDetails;
import hu.jex.mylittlefellow.gui.InformationDialog;
import hu.jex.mylittlefellow.storage.Storage;
import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Toast;

/**
 * Az adó beállító osztály modellje
 * @author Albert
 *
 */
public class Tax {
	private static Activity context;
	
	private enum CommunicatorType {
		LOADTAXTYPES, SENDTAX
	}
	
	static OnTaxEvent mListener;
	public interface OnTaxEvent {
		/**
		 * Sikeresen frissült az adó
		 * @param taxes
		 */
		public void onTaxRefreshed(ResourceStorage taxes);
	}
	/**
	 * Az eseménykezelõ beállítása
	 * @param eventListener
	 */
	public void setCustomEventListener(OnTaxEvent eventListener) {
		mListener=eventListener;
	}
	
	public Tax(Activity context) {
		Tax.context = context;
	}
	/**
	 * Beállítja az adott nyersanyagot adónak
	 * @param id nyersanyag azonosítója
	 * @param amount nyersanyag darabszáma
	 */
	public void setTax(int id, int amount) {
		Communicator comm = new Communicator();
		comm.helper1 = id;
		comm.helper2 = amount;
		comm.execute(CommunicatorType.SENDTAX);
	}
	/**
	 * Elkezdi letölteni az adót
	 */
	public void loadTax() {
		Communicator comm = new Communicator();
        comm.execute(CommunicatorType.LOADTAXTYPES);
	}
	/**
	 * Lekérdezi a hátteret
	 * @return A háttér azonosítója
	 */
	public int getBackground() {
		int type = Tile.getTileFromAllTilesById(VisibleTileAdapter.getAllTiles(), Storage.getUserTileId(context)).getType();
		return Tile.getTileBackground(type);
	}
	
	/**
	 * A másik szálon való futásért felelõs
	 * @author Albert
	 *
	 */
	private class Communicator extends AsyncTask<CommunicatorType, Void, Void> {
		public int helper1;
		public int helper2;
		@Override
		protected Void doInBackground(CommunicatorType... params) {
			if(params[0] == CommunicatorType.LOADTAXTYPES) {
				CommunicatorUserDetails comm = new CommunicatorUserDetails(context);
				final ResourceStorage taxTypes = comm.getTaxResources();
				context.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(mListener!=null)mListener.onTaxRefreshed(taxTypes);
					}
				});
			}
			else if(params[0] == CommunicatorType.SENDTAX) {
				CommunicatorUserDetails comm = new CommunicatorUserDetails(context);
				comm.setTaxResource(helper1, helper2);
				if(comm.isProblem()) {
					InformationDialog.errorToast(context, "Error in sending, please send again",Toast.LENGTH_LONG);
				}
			}
			return null;
		}
	}
}
