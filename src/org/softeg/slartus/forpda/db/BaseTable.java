package org.softeg.slartus.forpda.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 22.11.12
 * Time: 10:59
 * To change this template use File | Settings | File Templates.
 */
public class BaseTable {
    public static int getRowsCount(SQLiteDatabase db, String tableName) {
        Cursor mcursor = null;
        try {
            String query = "SELECT count(*) FROM " + tableName;
            mcursor = db.rawQuery(query, null);

            mcursor.moveToFirst();
            return mcursor.getInt(0);
        } finally {
            mcursor.close();
        }
    }
}
