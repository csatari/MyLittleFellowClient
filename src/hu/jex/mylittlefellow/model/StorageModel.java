package hu.jex.mylittlefellow.model;

import hu.jex.mylittlefellow.communicator.CommunicatorUserDetails;
import hu.jex.mylittlefellow.storage.Storage;
import hu.jex.mylittlefellow.storage.StorageDatabaseAdapter;
import android.app.Activity;
import android.os.AsyncTask;

/**
 * Raktárhoz tartozó modell
 * @author Albert
 *
 */
public class StorageModel {
	
	private enum CommunicatorType {
		LOADSTORAGE,REMOVEFROMSTORAGE
	}
	
	private static Activity context;
	
	private static ResourceStorage storage; //a nyersanyagok tömbje
	private static int storageLimit; //raktár mérete
	private static int ipo;//intelligenciapontok száma
	
	static OnStorageEvent mListener;
	public interface OnStorageEvent {
		/**
		 * Sikeresen letöltõdött a raktár
		 * @param storage A raktár
		 * @param ipo Intelligenciapontok száma
		 * @param storagelimit A raktár mérete
		 */
		public void onStorageDownloaded(ResourceStorage storage, int ipo, int storagelimit);
	}
	/**
	 * Eseménykezelõ beállítása
	 * @param eventListener
	 */
	public void setCustomEventListener(OnStorageEvent eventListener) {
		mListener=eventListener;
	}
	
	public StorageModel(Activity context) {
		StorageModel.context = context;
	}
	/**
	 * Lekérdezi a hátteret
	 * @return
	 */
	public int getBackground() {
		int type = Tile.getTileFromAllTilesById(VisibleTileAdapter.getAllTiles(), Storage.getUserTileId(context)).getType();
		return Tile.getTileBackground(type);
	}
	/**
	 * Mielõtt letöltött volna a raktár, az elõzõ állapotot mutatja
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
	 * Megkezdi a raktár letöltését
	 */
	public void downloadStorage() {
		Communicator comm = new Communicator();
        comm.execute(CommunicatorType.LOADSTORAGE);
	}
	/**
	 * Megkezdi a raktárból bizonyos nyersanyag eldobását
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
	 * A raktár mérete
	 * @return méret
	 */
	public int getStorageCount() {
		if(storage == null) {
			return 0;
		}
		return storage.size();
	}
	/**
	 * Lekérdez egy nyersanyagot pozíció alapján
	 * @param position pozíció
	 * @return A nyersanyag
	 */
	public Resource getResourceByPosition(int position) {
		if(storage == null) {
			return null;
		}
		return storage.getStorage().get(position);
		
	}
	
	/**
	 * A másik szálon való futásért felelõs
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
