package hu.jex.mylittlefellow.model;

/**
 * Adatbázisban menthetõ logikai osztály interfésze
 * @author Albert
 *
 */
public abstract class SaveableObject {
	protected int id;
	public abstract int getId();
	public abstract void setId(int id);
}
