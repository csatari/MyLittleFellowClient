package hu.jex.mylittlefellow.model;

import hu.jex.mylittlefellow.communicator.CommunicatorUserDetails;
import hu.jex.mylittlefellow.storage.Storage;
import hu.jex.mylittlefellow.storage.StorageDatabaseAdapter;
import android.app.Activity;
import android.os.AsyncTask;

/**
 * Rakt�rhoz tartoz� modell
 * @author Albert
 *
 */
public class StorageModel {
	
	private enum CommunicatorType {
		LOADSTORAGE,REMOVEFROMSTORAGE
	}
	
	private static Activity context;
	
	private static ResourceStorage storage; //a nyersanyagok t�mbje
	private static int storageLimit; //rakt�r m�rete
	private static int ipo;//intelligenciapontok sz�ma
	
	static OnStorageEvent mListener;
	public interface OnStorageEvent {
		/**
		 * Sikeresen let�lt�d�tt a rakt�r
		 * @param storage A rakt�r
		 * @param ipo Intelligenciapontok sz�ma
		 * @param storagelimit A rakt�r m�rete
		 */
		public void onStorageDownloaded(ResourceStorage storage, int ipo, int storagelimit);
	}
	/**
	 * Esem�nykezel� be�ll�t�sa
	 * @param eventListener
	 */
	public void setCustomEventListener(OnStorageEvent eventListener) {
		mListener=eventListener;
	}
	
	public StorageModel(Activity context) {
		StorageModel.context = context;
	}
	/**
	 * Lek�rdezi a h�tteret
	 * @return
	 */
	public int getBackground() {
		int type = Tile.getTileFromAllTilesById(VisibleTileAdapter.getAllTiles(), Storage.getUserTileId(context)).getType();
		return Tile.getTileBackground(type);
	}
	/**
	 * Miel�tt let�lt�tt volna a rakt�r, az el�z� �llapotot mutatja
	 */
	public void storagePreview() {
		StorageDatabaseAdapter storageDb = new StorageDatabaseAdapter(context);
        ResourceStorage storage = ResourceStorage.arrayListToResourceStorage(storageDb.getAll());
        int storageLimit = Storage.getStorageLimit(context);
        int ipo = Storage.getIntelligencePoints(context);
        if(mListener!=null)mListener.onStorageDownloaded(storage,ipo, storageLimit);
        storageDb.close();
	}
	/**
	 * Megkezdi a rakt�r let�lt�s�t
	 */
	public void downloadStorage() {
		Communicator comm = new Communicator();
        comm.execute(CommunicatorType.LOADSTORAGE);
	}
	/**
	 * Megkezdi a rakt�rb�l bizonyos nyersanyag eldob�s�t
	 * @param resourceId
	 * @param amount
	 */
	public void removeFromStorage(int resourceId, int amount) {
		Communicator comm = new Communicator();
		comm.resourceid = resourceId;
		comm.amount = amount;
		comm.execute(CommunicatorType.REMOVEFROMSTORAGE);
	}
	/**
	 * A rakt�r m�rete
	 * @return m�ret
	 */
	public int getStorageCount() {
		if(storage == null) {
			return 0;
		}
		return storage.size();
	}
	/**
	 * Lek�rdez egy nyersanyagot poz�ci� alapj�n
	 * @param position poz�ci�
	 * @return A nyersanyag
	 */
	public Resource getResourceByPosition(int position) {
		if(storage == null) {
			return null;
		}
		return storage.getStorage().get(position);
		
	}
	
	/**
	 * A m�sik sz�lon val� fut�s�rt felel�s
	 * @author Albert
	 *
	 */
	private class Communicator extends AsyncTask<CommunicatorType, Void, Void> {
		public int resourceid;
		public int amount = 1;
		@Override
		protected Void doInBackground(CommunicatorType... params) {
			if(params[0] == CommunicatorType.LOADSTORAGE) {
				CommunicatorUserDetails comm = new CommunicatorUserDetails(context);
				storage = comm.getAllStorage();
				storageLimit = comm.getStorageLimit();
				ipo = comm.getIpo();
				Storage.setStorageLimit(context, storageLimit);
				Storage.setIntelligencePoints(context, ipo);
				StorageDatabaseAdapter storageDb = new StorageDatabaseAdapter(context);
				storageDb.deleteAll();
				if(storage != null) {
					for(Resource res : storage.getStorage()) {
						storageDb.addRow(res);
					}
				}
				else {
					storage = new ResourceStorage();
				}
		        storageDb.close();
				context.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(mListener!=null)mListener.onStorageDownloaded(storage,ipo, storageLimit);
					}
				});
			}
			else if(params[0] == CommunicatorType.REMOVEFROMSTORAGE) {
				Resource res = storage.getResource(resourceid);
				if(res.getAmount() <= 0) {
					return null;
				}
				if(res.getAmount() - amount < 0) {
					amount = res.getAmount();
				}
				res.setAmount(res.getAmount()-amount);
				storage.set(res.getId(), res.getAmount());
				context.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(mListener!=null)mListener.onStorageDownloaded(storage, ipo, storageLimit);
					}
				});
				CommunicatorUserDetails comm = new CommunicatorUserDetails(context);
				comm.setResource(res);
				storage = comm.getAllStorage();
				context.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(mListener!=null)mListener.onStorageDownloaded(storage,ipo, storageLimit);
					}
				});
			}
			return null;
		}
	}
}
