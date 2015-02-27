package hu.jex.mylittlefellow.storage;

import hu.jex.mylittlefellow.model.SaveableObject;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Az adatb�zisok �soszt�lya
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
	 * Absztrakt f�ggv�ny, ami visszaadja az �sszes oszlopnevet
	 * @return
	 */
	protected abstract String[] getAllColumns();
	
	/**
	 * Absztrakt f�ggv�ny, ami visszaadja a t�bla nev�t
	 */
	protected abstract String getTableName();
	
	/**
	 * Absztrakt f�ggv�ny, ami visszaadja a t�bla id oszlop�nak nev�t
	 */
	protected abstract String getIdColumnName();
	
	/**
	 * A konstruktorban be�ll�tjuk az allColumns stringet, ami az �sszes t�bla nev�t tartalmazza
	 * �s p�ld�nyos�tja az adatb�zist
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
	 * Hozz�ad egy T t�pus� objektumot a t�bl�hoz
	 * @param row
	 * @return
	 * @throws DatabaseNotOpenException
	 */
	public boolean addRow(T row) {
		if(!isOpen()) {
			open();
		}
		//Logger.writeToLog("hozz�ad�s1: "+row.toString());
		if(!isExists(row)) {
			//Logger.writeToLog("hozz�ad�s2: "+row.toString());
			ContentValues values = getObjectContentValues(row);
			database.insert(tableName, null, values);
			return true;
		}
		return false;
	}
	
	/**
	 * Lek�rdezi az �sszes objektumot egy list�ba
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
	 * Lek�rdezi a megadott id-j� objektumot
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
	 * Lek�rdezi az objektumot a megadott where felt�tellel
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
	 * Fel�l�rja a t�bl�ban az objektumot a megadott id szerint, �s ha eddig nem l�tezett, akkor hozz�adja a t�bl�hoz
	 * @param object
	 * @throws DatabaseNotOpenException
	 */
	public void update(T object) {
		update(object, true);
	}
	/**
	 * Fel�l�rja a t�bl�ban az objektumot a megadott id szerint
	 * @param object az objektum id-je
	 * @param addRowIfNotExist true, ha nem l�tez�s eset�n hozz�adja a t�bl�hoz az objektumot
	 * @throws DatabaseNotOpenException ha nincs megnyitva az adatb�zis
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
	 * Visszaadja egy objektumr�l, hogy az l�tezik-e m�r a t�bl�ban
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
	 * Kit�rli a megadott objektumot a t�bl�b�l
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
	 * Kit�rli az �sszes objektumot a t�bl�b�l
	 * @throws DatabaseNotOpenException
	 */
	public void deleteAll(){
		if(!isOpen()) {
			open();
		}
		database.delete(tableName, null, null);
	}
	/**
	 * Igazzal t�r vissza, ha �res a t�bla
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
	 * Be�ll�tja a ContentValues-t a param�terben megadott objektumb�l
	 * @param t
	 * @return
	 */
	protected abstract ContentValues getObjectContentValues(T object);
	/**
	 * A kurzorb�l csin�l T objektumot
	 * @param cursor
	 * @return
	 */
	protected abstract T cursorToObject(Cursor cursor);
	
	
	
	/**
	 * Megnyitja az adatb�zist
	 * @throws SQLException
	 */
	public void open() throws SQLException {
		if(!isOpen()) {
			database = dbHelper.getWritableDatabase();
		}
	}
	
	/**
	 * Bez�rja az adatb�zist
	 */
	public void close() {
		dbHelper.close();
	}
	
	/**
	 * Lek�rdezi, hogy az adatb�zis meg van-e nyitva
	 * @return
	 */
	private boolean isOpen() {
		if(database == null) return false;
		else return database.isOpen();
	}
	
	/**
	 * A CursorToObject f�ggv�nyben haszn�lni kell indexeket, ami mindig 0-t�l n�vekszik
	 * Ahhoz, hogy ne kelljen mindig �rni indexet vezetek be, ami null�z�s ut�n minden lek�rdez�sn�l n�vekszik
	 * Ezzel a f�ggv�nnyel lehet null�zni
	 */
	public void resetIndex() {
		index = 0;
	}
	
	/**
	 * A CursorToObject f�ggv�nyben haszn�lni kell indexeket, ami mindig 0-t�l n�vekszik
	 * Ahhoz, hogy ne kelljen mindig �rni indexet vezetek be, ami null�z�s ut�n minden lek�rdez�sn�l n�vekszik
	 * Ezzel a f�ggv�nnyel lehet lek�rdezni az indexet
	 * @return
	 */
	public int getIndex() {
		index++;
		return index-1;
	}
}
