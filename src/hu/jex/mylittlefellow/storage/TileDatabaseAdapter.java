package hu.jex.mylittlefellow.storage;

import hu.jex.mylittlefellow.model.Tile;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

/**
 * A területek adatbázisának mûveleteiért felelõs
 * @author Albert
 *
 */
public class TileDatabaseAdapter extends DatabaseAdapter<Tile> {

	private String[] allColumns = { SQLiteBase.TILE_ID, SQLiteBase.TILE_LATITUDE, SQLiteBase.TILE_LONGITUDE, SQLiteBase.TILE_RESOURCE1,
			SQLiteBase.TILE_RESOURCE2, SQLiteBase.TILE_RESOURCE3, SQLiteBase.TILE_TYPE, SQLiteBase.TILE_EXAMINED, SQLiteBase.TILE_OWNER};
	
	public TileDatabaseAdapter(Context context) {
		super(context);
	}

	@Override
	protected String[] getAllColumns() {
		return allColumns;
	}

	@Override
	protected String getTableName() {
		return SQLiteBase.TABLE_TILE;
	}

	@Override
	protected String getIdColumnName() {
		return SQLiteBase.TILE_ID;
	}

	@Override
	protected ContentValues getObjectContentValues(Tile object) {
		ContentValues values = new ContentValues();
		values.put(SQLiteBase.TILE_ID, object.getId());
		values.put(SQLiteBase.TILE_LATITUDE, object.getTileCenterLatitude());
		values.put(SQLiteBase.TILE_LONGITUDE, object.getTileCenterLongitude());
		values.put(SQLiteBase.TILE_RESOURCE1, object.getResource1());
		values.put(SQLiteBase.TILE_RESOURCE2, object.getResource2());
		values.put(SQLiteBase.TILE_RESOURCE3, object.getResource3());
		values.put(SQLiteBase.TILE_TYPE, object.getType());
		values.put(SQLiteBase.TILE_EXAMINED, object.isExamined());
		values.put(SQLiteBase.TILE_OWNER, object.getOwner());
		return values;
	}

	@Override
	protected Tile cursorToObject(Cursor cursor) {
		Tile tile = new Tile();
		resetIndex();
		tile.setId(cursor.getInt(getIndex()));
		tile.setTileCenterLatitude(Double.parseDouble(cursor.getString(getIndex())));
		tile.setTileCenterLongitude(Double.parseDouble(cursor.getString(getIndex())));
		tile.setType(cursor.getInt(getIndex()));
		tile.setResource1(cursor.getInt(getIndex()));
		tile.setResource2(cursor.getInt(getIndex()));
		tile.setResource3(cursor.getInt(getIndex()));
		tile.setExamined(cursor.getInt(getIndex()) == 1);
		tile.setOwner(cursor.getString(getIndex()));
		return tile;
	}
}
