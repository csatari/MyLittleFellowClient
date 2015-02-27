package hu.jex.mylittlefellow.storage;

import hu.jex.mylittlefellow.model.Resource;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

/**
 * A raktár adatbázisért felel
 * @author Albert
 *
 */
public class StorageDatabaseAdapter extends DatabaseAdapter<Resource> {

	private String[] allColumns = { SQLiteBase.STORAGE_TYPE, SQLiteBase.STORAGE_AMOUNT};
	
	public StorageDatabaseAdapter(Context context) {
		super(context);
	}
	
	@Override
	protected String[] getAllColumns() {
		return allColumns;
	}

	@Override
	protected String getTableName() {
		return SQLiteBase.TABLE_STORAGE;
	}

	@Override
	protected String getIdColumnName() {
		return SQLiteBase.STORAGE_TYPE;
	}

	@Override
	protected ContentValues getObjectContentValues(Resource object) {
		ContentValues values = new ContentValues();
		values.put(SQLiteBase.STORAGE_TYPE, object.getId());
		values.put(SQLiteBase.STORAGE_AMOUNT, object.getAmount());
		return values;
	}

	@Override
	protected Resource cursorToObject(Cursor cursor) {
		Resource res = new Resource();
		resetIndex();
		res.setId(cursor.getInt(getIndex()));
		res.setAmount(Integer.parseInt(cursor.getString(getIndex())));
		return res;
	}

}
