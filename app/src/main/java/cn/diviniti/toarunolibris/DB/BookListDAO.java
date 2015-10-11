package cn.diviniti.toarunolibris.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.diviniti.toarunolibris.RecyclerModel.BookSummary;

/**
 * Created by Administrator on 2015-09-28.
 */
public class BookListDAO {
    private Context context;
    private BookListSQL sql;

    public BookListDAO(Context context) {
        this.sql = new BookListSQL(context);
    }

    //  增
    public void insertBook(String bookId, String bookName, String bookAuthor, String bookPubliser, String bookCallNumber) {
        SQLiteDatabase db = sql.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("book_id", bookId);
        values.put("book_name", bookName);
        values.put("book_author", bookAuthor);
        values.put("book_publisher", bookPubliser);
        values.put("book_call_number", bookCallNumber);
        db.insert("booksList", null, values);
        db.close();
        Log.d("VANGO_DB_DEBUG", "存储成功");
    }

    //  删
    public void deleteBook(String bookId) {
        SQLiteDatabase db = sql.getWritableDatabase();
        db.execSQL("delete from booksList where book_id=?", new Object[]{bookId});
        db.close();
    }

    //  删 - 所有
    public void deleteAll() {
        SQLiteDatabase db = sql.getWritableDatabase();
        db.execSQL("delete from booksList");
        db.close();
    }

    //  查 - 所有
    public List<BookSummary> findBooks() {
        SQLiteDatabase db = sql.getReadableDatabase();
        Cursor data = db.rawQuery("select * from booksList", null);
        List<BookSummary> booksList = new ArrayList<>();

        if (data.getCount() > 0) {
            while (data.moveToNext()) {
                BookSummary book = new BookSummary();
                String bookId = data.getString(data.getColumnIndex("book_id"));
                String booKName = data.getString(data.getColumnIndex("book_name"));
                String bookAuthor = data.getString(data.getColumnIndex("book_author"));
                String bookPublisher = data.getString(data.getColumnIndex("book_publisher"));
                String bookCallNumber = data.getString(data.getColumnIndex("book_call_number"));
                book.bookID = bookId;
                book.bookName = booKName;
                book.bookAuthor = bookAuthor;
                book.bookPublisher = bookPublisher;
                book.bookCallNumber = bookCallNumber;
                booksList.add(book);
            }
            return booksList;
        } else {
            Log.i("VANGO_DB_DEBUG", "没书啊！！！！！");
            return null;
        }
    }

    //  TODO 查 - 一本书
    public boolean findBookById(String bookId) {
        SQLiteDatabase db = sql.getReadableDatabase();
        boolean isFound;
        Cursor data = db.rawQuery("select * from booksList where book_id=?", new String[]{bookId});
        if (data.getCount() != 0) {
            isFound = true;
        } else {
            isFound = false;
        }
        db.close();
        return isFound;
    }

    //  改
    public void updateBook() {

    }

}
