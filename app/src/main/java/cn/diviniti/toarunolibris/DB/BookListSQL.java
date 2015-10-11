package cn.diviniti.toarunolibris.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BookListSQL extends SQLiteOpenHelper {
    public BookListSQL(Context context) {
        super(context, "booksList", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table booksList (book_id nchar(10) primary key,book_name nchar(20),book_author nchar(10),book_publisher nchar(20),book_call_number nchar(20))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
