package cn.diviniti.toarunolibris.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SearchHistorySQL extends SQLiteOpenHelper {
    public SearchHistorySQL(Context context) {super(context, "searchHistory", null, 1);}

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table searchHistory (" +
                "search_key_word nchar(20) primary key" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
