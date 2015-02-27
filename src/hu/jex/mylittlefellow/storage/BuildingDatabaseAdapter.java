package hu.jex.mylittlefellow.storage;

import hu.jex.mylittlefellow.model.Building;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

/**
 * Az épület adatbázismûveletei
 * @author Albert
 *
 */
public class BuildingDatabaseAdapter extends DatabaseAdapter<Building>{

	public BuildingDatabaseAdapter(Context context) {
		super(context);
	}
	/**
	 * Az összes oszlop neve
	 */
	private String[] allColumns = { SQLiteBase.BUILDING_ID, SQLiteBase.BUILDING_TILEID, SQLiteBase.BUILDING_SLICEID, SQLiteBase.BUILDING_TYPE,
			SQLiteBase.BUILDING_LEVEL, SQLiteBase.BUILDING_FINISHED};
	
	@Override
	protected String[] getAllColumns() {
		return allColumns;
	}

	@Override
	protected String getTableName() {
		return SQLiteBase.TABLE_BUILDING;
	}

	@Override
	protected String getIdColumnName() {
		return SQLiteBase.BUILDING_ID;
	}

	@Override
	protected ContentValues getObjectContentValues(Building object) {
		ContentValues values = new ContentValues();
		values.put(SQLiteBase.BUILDING_ID, object.getId());
		values.put(SQLiteBase.BUILDING_TILEID, object.getTileId());
		values.put(SQLiteBase.BUILDING_SLICEID, object.getSliceId());
		values.put(SQLiteBase.BUILDING_TYPE, object.getType());
		values.put(SQLiteBase.BUILDING_LEVEL, object.getLevel());
		values.put(SQLiteBase.BUILDING_FINISHED, object.isFinished());
		return values;
	}

	@Override
	protected Building cursorToObject(Cursor cursor) {
		Building building = new Building();
		resetIndex();
		building.setId(cursor.getInt(getIndex()));
		building.setTileId(cursor.getInt(getIndex()));
		building.setSliceId(cursor.getInt(getIndex()));
		building.setType(cursor.getInt(getIndex()));
		building.setLevel(cursor.getInt(getIndex()));
		building.setFinished(cursor.getInt(getIndex()) == 1);
		return building;
	}
	/**
	 * Lekérdezi az összes épületet a megadott területen
	 * @param tile a terület
	 * @return az összes épület tömbje
	 */
	public ArrayList<Building> getBuildingsOnTile(int tile) {
		ArrayList<Building> buildings = getByWhereClause(SQLiteBase.BUILDING_TILEID + " = "+tile);
		return buildings;
	}

}
