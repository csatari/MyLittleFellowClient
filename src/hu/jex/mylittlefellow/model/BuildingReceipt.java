package hu.jex.mylittlefellow.model;

/**
 * Egy �p�lethez sz�ks�ges nyersanyagokat tartalmazza
 * @author Albert
 *
 */
public class BuildingReceipt {
	private int type; //Az �p�let t�pusa
	private int level; //Az �p�let szintje
	private String name; //Az �p�let neve
	private int ipo; //Az �p�lethez sz�ks�ges intelligenciapontok sz�ma
	private ResourceStorage resources; //Az �p�lethez sz�ks�ges nyersanyagok
	
	public BuildingReceipt() {}
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public ResourceStorage getResources() {
		return resources;
	}
	public void setResources(ResourceStorage resources) {
		this.resources = resources;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getIpo() {
		return ipo;
	}
	public void setIpo(int ipo) {
		this.ipo = ipo;
	}

	@Override
	public String toString() {
		return "BuildingReceipt [type=" + type + ", level=" + level + ", name="
				+ name + ", ipo=" + ipo + ", resources=" + resources.toString() + "]";
	}
	
	
}
