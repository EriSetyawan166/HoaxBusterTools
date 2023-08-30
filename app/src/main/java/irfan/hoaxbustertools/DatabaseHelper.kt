import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "Favorites.db"

        private const val TABLE_FAVORITES = "favorites"
        private const val COLUMN_NAME_ID = "name_id"
        private const val COLUMN_IS_FAVORITE = "is_favorite"

        private const val CREATE_FAVORITES_TABLE = (
                "CREATE TABLE $TABLE_FAVORITES (" +
                        "$COLUMN_NAME_ID TEXT PRIMARY KEY," +
                        "$COLUMN_IS_FAVORITE INTEGER)"
                )
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_FAVORITES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITES")
        onCreate(db)
    }

    fun updateFavoriteStatus(nameId: String, isFavorite: Boolean) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_IS_FAVORITE, if (isFavorite) 1 else 0)
        val updatedRowCount = db.update(TABLE_FAVORITES, values, "$COLUMN_NAME_ID = ?", arrayOf(nameId))
        db.close()

        Log.d("DatabaseHelper", "Updated $updatedRowCount rows for tool: $nameId, isFavorite: $isFavorite")

        if (updatedRowCount == 0) {
            Log.d("DatabaseHelper", "No rows were updated.")
        }
    }

    fun getFavoriteNameIds(): List<String> {
        val favoriteNameIds = mutableListOf<String>()
        val db = this.readableDatabase
        val query = "SELECT $COLUMN_NAME_ID FROM $TABLE_FAVORITES WHERE $COLUMN_IS_FAVORITE = 1"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex(COLUMN_NAME_ID)


            do {
                val nameId = cursor.getString(columnIndex)

                favoriteNameIds.add(nameId)
            } while (cursor.moveToNext())
        }


        cursor.close()
        db.close()

        return favoriteNameIds
    }

    fun isToolFavorite(nameId: String): Boolean {
        val db = readableDatabase
        val query = "SELECT $COLUMN_IS_FAVORITE FROM $TABLE_FAVORITES WHERE $COLUMN_NAME_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(nameId))
        var isFavorited = false

        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex(COLUMN_IS_FAVORITE)
            isFavorited = cursor.getInt(columnIndex) == 1
        }

        cursor.close()
        db.close()

        return isFavorited
    }

    fun insert(nameId: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NAME_ID, nameId)
        values.put(COLUMN_IS_FAVORITE, 0) // Set is_favorite to false
        db.insert(TABLE_FAVORITES, null, values)
        db.close()
    }

    fun isFavoritesTableEmpty(): Boolean {
        val db = this.readableDatabase
        val query = "SELECT COUNT(*) FROM $TABLE_FAVORITES"
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count == 0
    }




}
