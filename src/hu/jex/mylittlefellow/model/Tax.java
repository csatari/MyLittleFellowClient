package hu.jex.mylittlefellow.model;

import hu.jex.mylittlefellow.communicator.CommunicatorUserDetails;
import hu.jex.mylittlefellow.gui.InformationDialog;
import hu.jex.mylittlefellow.storage.Storage;
import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Toast;

/**
 * Az ad� be�ll�t� oszt�ly modellje
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
		 * Sikeresen friss�lt az ad�
		 * @param taxes
		 */
		public void onTaxRefreshed(ResourceStorage taxes);
	}
	/**
	 * Az esem�nykezel� be�ll�t�sa
	 * @param eventListener
	 */
	public void setCustomEventListener(OnTaxEvent eventListener) {
		mListener=eventListener;
	}
	
	public Tax(Activity context) {
		Tax.context = context;
	}
	/**
	 * Be�ll�tja az adott nyersanyagot ad�nak
	 * @param id nyersanyag azonos�t�ja
	 * @param amount nyersanyag darabsz�ma
	 */
	public void setTax(int id, int amount) {
		Communicator comm = new Communicator();
		comm.helper1 = id;
		comm.helper2 = amount;
		comm.execute(CommunicatorType.SENDTAX);
	}
	/**
	 * Elkezdi let�lteni az ad�t
	 */
	public void loadTax() {
		Communicator comm = new Communicator();
        comm.execute(CommunicatorType.LOADTAXTYPES);
	}
	/**
	 * Lek�rdezi a h�tteret
	 * @return A h�tt�r azonos�t�ja
	 */
	public int getBackground() {
		int type = Tile.getTileFromAllTilesById(VisibleTileAdapter.getAllTiles(), Storage.getUserTileId(context)).getType();
		return Tile.getTileBackground(type);
	}
	
	/**
	 * A m�sik sz�lon val� fut�s�rt felel�s
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
