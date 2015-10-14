package cn.diviniti.toarunolibris.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;

public class SearchHistoryDAO {
    private SearchHistorySQL sql;

    public SearchHistoryDAO(Context context) {
        this.sql = new SearchHistorySQL(context);
    }

    //  增
    public void insertHistory(String searchKeyWord) {
        SQLiteDatabase db = sql.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("search_key_word", searchKeyWord);
        db.insert("searchHistory", null, values);
        db.close();
        Log.d("VANGO_DB_DEBUG", "搜索历史 - 存储成功");
    }

    //  TODO 删除方法
    public void deleteHistory() {
        SQLiteDatabase db = sql.getWritableDatabase();
        db.execSQL("delete from searchHistory");
        db.close();
        Log.d("VANGO_DB_DEBUG", "搜索历史 - 删除成功");
    }

    //  查 - 存在历史
    public boolean hasHistory() {
        SQLiteDatabase db = sql.getReadableDatabase();
        Cursor data = db.rawQuery("select * from searchHistory", null);
        boolean isFound = (data.getCount() != 0);
        db.close();
        return isFound;
    }

    //  查 - 返回全部
    public ArrayList<String> getHistory() {
        SQLiteDatabase db = sql.getReadableDatabase();
        Cursor data = db.rawQuery("select * from searchHistory", null);
        ArrayList<String> histories = new ArrayList<>();
        while (data.moveToNext()) {
            histories.add(data.getString(data.getColumnIndex("search_key_word")));
        }
        db.close();
        return histories;
    }

    //  查
    public boolean isKeyWordExist(String searchKeyWord) {
        SQLiteDatabase db = sql.getReadableDatabase();
        Cursor data = db.rawQuery("select * from searchHistory where search_key_word=?", new String[]{searchKeyWord});
        boolean isFound = (data.getCount() != 0);
        db.close();
        if (isFound) {
            Log.d("VANGO_DB_DEBUG", "找到");
        } else {
            Log.d("VANGO_DB_DEBUG", "没找到");
        }
        return isFound;
    }
}
