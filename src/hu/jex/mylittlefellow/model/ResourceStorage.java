package hu.jex.mylittlefellow.model;

import java.util.ArrayList;
/**
 * Nyersanyagok �sszess�g�t t�rol
 * @author Albert
 *
 */
public class ResourceStorage {
	private ArrayList<Resource> storage; //A nyersanyagok t�mbje
	
	public ResourceStorage() {
		storage = new ArrayList<Resource>();
	}

	public ArrayList<Resource> getStorage() {
		return storage;
	}

	public void setStorage(ArrayList<Resource> storage) {
		this.storage = storage;
	}
	/**
	 * Lek�rdezi a nyersanyagok darabsz�m�nak �sszeg�t
	 * @return
	 */
	public int getStorageResourceCount() {
		int count = 0;
		for(Resource p : storage) {
			count += p.getAmount();
		}
		return count;
	}
	/**
	 * Hozz�ad egy nyersanyagot
	 * @param type nyersanyag t�pusa
	 * @param amount nyersanyag darabsz�ma
	 */
	public void add(int type, int amount) {
		if(storage == null) {
			storage = new ArrayList<Resource>();
		}
		Resource r = new Resource();
		r.setId(type);
		r.setAmount(amount);
		storage.add(r);
	}
	public int size() {
		return storage.size();
	}
	public void set(int type, int amount) {
		if(storage == null) {
			return;
		}
		for(Resource p : storage) {
			if(p.getId() == type) {
				p.setAmount(amount);
				break;
			}
		}
	}
	public int get(int type) {
		if(storage == null) {
			return 0;
		}
		for(Resource p : storage) {
			if(p.getId() == type) {
				return p.getAmount();
			}
		}
		return 0;
	}
	public Resource getByIndex(int index) {
		if(storage == null) {
			return null;
		}
		return storage.get(index);
	}
	public Resource getResource(int type) {
		if(storage == null) {
			return null;
		}
		for(Resource p : storage) {
			if(p.getId() == type) {
				return p;
			}
		}
		return null;
	}
	/**
	 * Visszaadja a le�r� stringet
	 * @return
	 */
	public String getResourcesInText() {
		StringBuilder sb = new StringBuilder();
		for(Resource p : storage) {
			sb.append(Resource.getNameOfType(p.getId()));
			sb.append(": ");
			sb.append(p.getAmount());
			sb.append("\n");
		}
		return sb.toString();
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Resource p : storage) {
			sb.append("type: ");
			sb.append(p.getId());
			sb.append(", amount: ");
			sb.append(p.getAmount());
			sb.append("\n");
		}
		return sb.toString();
	}
	/**
	 * Nyersanyagok t�mbj�t ilyen oszt�ly�ra �ll�tja be
	 * @param arrayList
	 * @return
	 */
	public static ResourceStorage arrayListToResourceStorage(ArrayList<Resource> arrayList) {
		ResourceStorage resStor = new ResourceStorage();
		for(Resource res : arrayList) {
			resStor.add(res.getId(), res.getAmount());
		}
		return resStor;
	}
}
