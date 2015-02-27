package hu.jex.mylittlefellow.model;

import hu.jex.mylittlefellow.R;

/**
 * Egy nyersanyag modellje
 * @author Albert
 *
 */
public class Resource extends SaveableObject {
	
	public final static int WOOD = 1;
	public final static int TWIG = 2;
	public final static int MEAT = 3;
	public final static int BERRY = 4;
	public final static int STONE = 5;
	public final static int COAL = 6;
	public final static int IRON = 7;
	public final static int GOLD = 8;
	public final static int FISH = 9;
	
	private int type; //a nyersanyag típusa
	private int amount; //a nyersanyag száma
	@Override
	public int getId() {
		return type;
	}
	@Override
	public void setId(int id) {
		type = id;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	/**
	 * Visszaadja, hogy hány nyersanyag sort kell megjeleníteni
	 * @param tile a területen
	 * @return A sorok száma (Benne kell hogy legyen a cím is)
	 */
	public static int getPlaceResourceNumber(Tile tile) {
		if(!tile.isExamined()) {
			return 2;
		}
		switch(tile.getType()) {
		case Tile.FOREST: return 3;
		case Tile.PRAIRIE: return 2;
		case Tile.SHRUBBY: return 3;
		case Tile.SAND: return 2;
		case Tile.STONE_QUARRY: return 2;
		case Tile.CAVE: return 2; //TODO
		case Tile.LAKE: return 2;
		case Tile.SETTLEMENT: return 2;
		case Tile.RIVER: return 2;
		}
		return 0;
	}
	/**
	 * Típus alapján visszaadja a nyersanyag nevét
	 * @param type A típus 
	 * @return A nyersanyag neve
	 */
	public static String getNameOfType(int type) {
		switch(type) {
			case WOOD: return "Wood";
			case TWIG: return "Twig";
			case MEAT: return "Meat";
			case BERRY: return "Berry";
			case STONE: return "Stone";
			case COAL: return "Coal";
			case IRON: return "Iron";
			case GOLD: return "Gold";
			case FISH: return "Fish";
			default: return "Unknown";
		}
	}
	/**
	 * Visszaadja típus alapján a nyersanyag képét
	 * @param type A típus
	 * @return A kép elérhetõsége
	 */
	public static int getResourceDrawableByType(int type) {
		switch(type) {
		case WOOD: return R.drawable.wood_icon_32;
		case TWIG: return R.drawable.twig_icon32;
		case MEAT: return R.drawable.meat_icon_32;
		case BERRY: return R.drawable.berry_icon_32;
		case STONE: return R.drawable.stone_icon_32;
		case FISH: return R.drawable.fish_icon_32;
		default: return R.drawable.blank_marker;
		}
	}
	/**
	 * Visszaadja a kiírandó nyersanyagot sorszám szerint
	 * @param position sorszám
	 * @param tile terület
	 * @return A kiírandó szöveg
	 */
	public static String getPlaceResourceByPosition(int position, Tile tile) {
		if(position == 0) {
			return "Resources:";
		}
		else if(position == 1) {
			if(!tile.isExamined()) {
				return "Not examined";
			}
			switch(tile.getType()) {
			case Tile.FOREST: return "Wood: "+tile.getResource1();
			case Tile.PRAIRIE: return "Animal: "+tile.getResource1();
			case Tile.SHRUBBY: return "Berry: "+tile.getResource1();
			case Tile.SAND: return "None";
			case Tile.STONE_QUARRY: return "Stone: "+tile.getResource1();
			case Tile.CAVE: return "Stone: "+tile.getResource1(); //TODO
			case Tile.LAKE: return "Fish: "+tile.getResource1();
			case Tile.SETTLEMENT: return "None";
			case Tile.RIVER: return "Fish: "+tile.getResource1();
			}
		}
		else if(position == 2) {
			switch(tile.getType()) {
			case Tile.FOREST: return "Twig: "+tile.getResource2();
			case Tile.SHRUBBY: return "Twig: "+tile.getResource2();
			}
		}
		return null;
	}
	/**
	 * Lekéri a getPlaceResourceByPosition egy szövegként
	 * @param tile
	 * @return
	 */
	public static String getPlaceResourceString(Tile tile) {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<Resource.getPlaceResourceNumber(tile); i++) {
			sb.append(Resource.getPlaceResourceByPosition(i, tile));
			sb.append("\n");
		}
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return "Resource [type=" + type + ", amount=" + amount + "]";
	}
	
}
