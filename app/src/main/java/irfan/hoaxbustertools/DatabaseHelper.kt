import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

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
        db.update(TABLE_FAVORITES, values, "$COLUMN_NAME_ID = ?", arrayOf(nameId))
        db.close()
    }

    fun getFavoriteStatus(nameId: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_IS_FAVORITE FROM $TABLE_FAVORITES WHERE $COLUMN_NAME_ID = ?",
            arrayOf(nameId)
        )

        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex(COLUMN_IS_FAVORITE)
            if (columnIndex != -1) {
                val isFavorite = cursor.getInt(columnIndex) == 1
                cursor.close()
                db.close()
                return isFavorite
            }
        }

        cursor.close()
        db.close()
        return false
    }

}
