package hu.jex.mylittlefellow.model;

/**
 * Adatb�zisban menthet� logikai oszt�ly interf�sze
 * @author Albert
 *
 */
public abstract class SaveableObject {
	protected int id;
	public abstract int getId();
	public abstract void setId(int id);
}
