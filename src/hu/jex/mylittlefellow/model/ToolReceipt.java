package hu.jex.mylittlefellow.model;

public class ToolReceipt {
	private int id; //azonosító
	private String name; //az eszköz neve
	private int ipo; //az eszközhöz szükséges intelligenciapont
	private ResourceStorage resources; //a szükséges nyersanyag
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
	public ResourceStorage getResources() {
		return resources;
	}
	public void setResources(ResourceStorage resources) {
		this.resources = resources;
	}
	@Override
	public String toString() {
		return "ToolReceipt [id=" + id + ", name=" + name + ", ipo=" + ipo
				+ ", resources=" + resources.toString() + "]";
	}
	
	
}
