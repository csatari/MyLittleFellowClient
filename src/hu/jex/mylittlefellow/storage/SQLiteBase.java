package hu.jex.mylittlefellow.storage;

import hu.jex.mylittlefellow.model.Logger;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
/**
 * Az adatbázisok alacsonyabb szintû kezelõje
 * @author Albert
 *
 */
public class SQLiteBase extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "cache.db";
	private static final int DATABASE_VERSION = 9;
	
	//tile adatbázis struktúrája
	public static final String TABLE_TILE = "tile";
	public static final String TILE_ID = "id";
	public static final String TILE_LATITUDE = "latitude";
	public static final String TILE_LONGITUDE = "longitude";
	public static final String TILE_TYPE = "type";
	public static final String TILE_RESOURCE1 = "resource1";
	public static final String TILE_RESOURCE2 = "resource2";
	public static final String TILE_RESOURCE3 = "resource3";
	public static final String TILE_EXAMINED = "examined";
	public static final String TILE_OWNER = "owner";
	
	//storage adatbázis struktúrája
	public static final String TABLE_STORAGE = "storage";
	public static final String STORAGE_TYPE = "type";
	public static final String STORAGE_AMOUNT = "amount";
	
	//building adatbázis struktúrája
	public static final String TABLE_BUILDING = "building";
	public static final String BUILDING_ID = "id";
	public static final String BUILDING_TILEID = "tileid";
	public static final String BUILDING_SLICEID = "sliceid";
	public static final String BUILDING_TYPE = "type";
	public static final String BUILDING_LEVEL = "level";
	public static final String BUILDING_FINISHED = "finished";
	
	private static final String DATABASE_TILE_CREATE = "create table " + TABLE_TILE + "(" 
			+ TILE_ID + " integer, "
			+ TILE_LATITUDE + " text, "
			+ TILE_LONGITUDE + " text, "
			+ TILE_TYPE + " integer, "
			+ TILE_RESOURCE1 + " integer, "
			+ TILE_RESOURCE2 + " integer, "
			+ TILE_RESOURCE3 + " integer, "
			+ TILE_EXAMINED + " integer, "
			+ TILE_OWNER + " text, "
			+ " UNIQUE("+TILE_LATITUDE+","+TILE_LONGITUDE+") ON CONFLICT IGNORE);";
	
	private static final String DATABASE_STORAGE_CREATE = "create table " + TABLE_STORAGE + "(" 
			+ STORAGE_TYPE + " integer, "
			+ STORAGE_AMOUNT + " integer, "
			+ " UNIQUE("+STORAGE_TYPE+") ON CONFLICT IGNORE);";
	
	private static final String DATABASE_BUILDING_CREATE = "create table " + TABLE_BUILDING + "(" 
			+ BUILDING_ID + " integer, "
			+ BUILDING_TILEID + " integer, "
			+ BUILDING_SLICEID + " integer, "
			+ BUILDING_TYPE + " integer, "
			+ BUILDING_LEVEL + " integer, "
			+ BUILDING_FINISHED + " integer, "
			+ " UNIQUE("+BUILDING_ID+") ON CONFLICT IGNORE);";

	public SQLiteBase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_TILE_CREATE);
		db.execSQL(DATABASE_STORAGE_CREATE);
		db.execSQL(DATABASE_BUILDING_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Logger.writeToLog("Régi adatbázis felülírása " + oldVersion + " verzióról "
				+ newVersion + " verzióra.");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TILE);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_STORAGE);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUILDING);
		onCreate(db);
	}
	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Logger.writeToLog("Régi adatbázis felülírása " + oldVersion + " verzióról "
				+ newVersion + " verzióra.");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TILE);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_STORAGE);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUILDING);
		onCreate(db);
	}
	
}
