package hu.jex.mylittlefellow.storage;

import hu.jex.mylittlefellow.model.SaveableObject;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Az adatbázisok õsosztálya
 * @author Albert
 *
 * @param <T>
 */
public abstract class DatabaseAdapter<T extends SaveableObject> {
	protected SQLiteDatabase database;
	protected SQLiteBase dbHelper;
	
	protected String[] allColumns;
	private String tableName;
	private String idColumnName;
	
	private int index;
	/**
	 * Absztrakt függvény, ami visszaadja az összes oszlopnevet
	 * @return
	 */
	protected abstract String[] getAllColumns();
	
	/**
	 * Absztrakt függvény, ami visszaadja a tábla nevét
	 */
	protected abstract String getTableName();
	
	/**
	 * Absztrakt függvény, ami visszaadja a tábla id oszlopának nevét
	 */
	protected abstract String getIdColumnName();
	
	/**
	 * A konstruktorban beállítjuk az allColumns stringet, ami az összes tábla nevét tartalmazza
	 * és példányosítja az adatbázist
	 * @param context
	 */
	public DatabaseAdapter(Context context) {
		allColumns = getAllColumns();
		tableName = getTableName();
		idColumnName = getIdColumnName();
		dbHelper = new SQLiteBase(context);
		index = 0;
	}
	
	/**
	 * Hozzáad egy T típusú objektumot a táblához
	 * @param row
	 * @return
	 * @throws DatabaseNotOpenException
	 */
	public boolean addRow(T row) {
		if(!isOpen()) {
			open();
		}
		//Logger.writeToLog("hozzáadás1: "+row.toString());
		if(!isExists(row)) {
			//Logger.writeToLog("hozzáadás2: "+row.toString());
			ContentValues values = getObjectContentValues(row);
			database.insert(tableName, null, values);
			return true;
		}
		return false;
	}
	
	/**
	 * Lekérdezi az összes objektumot egy listába
	 * @return
	 */
	public ArrayList<T> getAll() {
		if(!isOpen()) {
			open();
		}
		return getByWhereClause(null);
		
		/*ArrayList<T> objectList = new ArrayList<T>();
		Cursor cursor = database.rawQuery("SELECT * FROM " + tableName, null);
		if(cursor.moveToFirst()) {
			while (cursor.isAfterLast() == false) {
				T object = cursorToObject(cursor);
				objectList.add(object);
                cursor.moveToNext();
            }
		}
		cursor.close();
		return objectList;*/
	}
	
	/**
	 * Lekérdezi a megadott id-jû objektumot
	 * @param id az objektum id-je
	 * @return
	 */
	public T getById(int id) {
		if(!isOpen()) {
			open();
			//throw new DatabaseNotOpenException("getObjectById");
		}
		Cursor cursor = database.query(tableName,
				allColumns, idColumnName + " = " + id, null, null, null, null);
		if(cursor.moveToFirst()) {
			T object = cursorToObject(cursor);
			cursor.close();
			return object;
		}
		cursor.close();
		return null;
	}
	/**
	 * Lekérdezi az objektumot a megadott where feltétellel
	 * @param whereClause
	 * @return
	 */
	public ArrayList<T> getByWhereClause(String whereClause) {
		if(!isOpen()) {
			open();
		}
		ArrayList<T> objectList = new ArrayList<T>();
		Cursor cursor = database.query(tableName,
				allColumns, whereClause, null, null, null, null);
		cursor.moveToFirst();
		while (cursor.isAfterLast() == false) {
			T object = cursorToObject(cursor);
			objectList.add(object);
            cursor.moveToNext();
        }
		cursor.close();
		return objectList;
	}
	
	/**
	 * Felülírja a táblában az objektumot a megadott id szerint, és ha eddig nem létezett, akkor hozzáadja a táblához
	 * @param object
	 * @throws DatabaseNotOpenException
	 */
	public void update(T object) {
		update(object, true);
	}
	/**
	 * Felülírja a táblában az objektumot a megadott id szerint
	 * @param object az objektum id-je
	 * @param addRowIfNotExist true, ha nem létezés esetén hozzáadja a táblához az objektumot
	 * @throws DatabaseNotOpenException ha nincs megnyitva az adatbázis
	 */
	public void update(T object, boolean addRowIfNotExist) {
		if(!isOpen()) {
			open();
		}
		if(!isExists(object) && addRowIfNotExist) {
			addRow(object);
		}
		ContentValues values = getObjectContentValues(object);
		database.update(tableName, values, idColumnName + " = " + object.getId(), null);
	}
	
	/**
	 * Visszaadja egy objektumról, hogy az létezik-e már a táblában
	 * @param object
	 * @return
	 * @throws DatabaseNotOpenException
	 */
	public boolean isExists(T object) {
		T object2 = getById(object.getId());
		if(object2 == null) return false;
		else return true;
	}
	
	/**
	 * Kitörli a megadott objektumot a táblából
	 * @param object
	 * @throws DatabaseNotOpenException
	 */
	public void delete(T object) throws DatabaseNotOpenException {
		if(!isOpen()) {
			throw new DatabaseNotOpenException("updateObjectById");
		}
		database.delete(tableName, idColumnName + " = " + object.getId(), null);
	}
	
	/**
	 * Kitörli az összes objektumot a táblából
	 * @throws DatabaseNotOpenException
	 */
	public void deleteAll(){
		if(!isOpen()) {
			open();
		}
		database.delete(tableName, null, null);
	}
	/**
	 * Igazzal tér vissza, ha üres a tábla
	 * @return
	 */
	public boolean isEmpty() {
		Cursor cur = database.rawQuery("SELECT COUNT(*) FROM "+tableName, null);
		if (cur != null) {
		    cur.moveToFirst();               
		    if (cur.getInt(0) == 0) {               
		    	return true;
		    }
		    else {
		    	return false;
		    }
		}
		return false;
	}
	
	/**
	 * Beállítja a ContentValues-t a paraméterben megadott objektumból
	 * @param t
	 * @return
	 */
	protected abstract ContentValues getObjectContentValues(T object);
	/**
	 * A kurzorból csinál T objektumot
	 * @param cursor
	 * @return
	 */
	protected abstract T cursorToObject(Cursor cursor);
	
	
	
	/**
	 * Megnyitja az adatbázist
	 * @throws SQLException
	 */
	public void open() throws SQLException {
		if(!isOpen()) {
			database = dbHelper.getWritableDatabase();
		}
	}
	
	/**
	 * Bezárja az adatbázist
	 */
	public void close() {
		dbHelper.close();
	}
	
	/**
	 * Lekérdezi, hogy az adatbázis meg van-e nyitva
	 * @return
	 */
	private boolean isOpen() {
		if(database == null) return false;
		else return database.isOpen();
	}
	
	/**
	 * A CursorToObject függvényben használni kell indexeket, ami mindig 0-tól növekszik
	 * Ahhoz, hogy ne kelljen mindig írni indexet vezetek be, ami nullázás után minden lekérdezésnél növekszik
	 * Ezzel a függvénnyel lehet nullázni
	 */
	public void resetIndex() {
		index = 0;
	}
	
	/**
	 * A CursorToObject függvényben használni kell indexeket, ami mindig 0-tól növekszik
	 * Ahhoz, hogy ne kelljen mindig írni indexet vezetek be, ami nullázás után minden lekérdezésnél növekszik
	 * Ezzel a függvénnyel lehet lekérdezni az indexet
	 * @return
	 */
	public int getIndex() {
		index++;
		return index-1;
	}
}
