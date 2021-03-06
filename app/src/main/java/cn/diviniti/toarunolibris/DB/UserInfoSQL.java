package cn.diviniti.toarunolibris.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class UserInfoSQL extends SQLiteOpenHelper {
    public UserInfoSQL(Context context) {
        super(context, "userInfo", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //user_num: 用来存储用户学号
        //user_pwd: 用来存储用户密码
        //user_name:用来存储用户姓名
        //user_col: 用来存储用户学院
        //user_login_status: 用来存储用户登录状态 0表示未登录，1表示登录
        db.execSQL("create table userInfo (" +
                "user_num nchar(20) primary key," +
                "user_pwd nchar(20)," +
                "user_name nchar(20)," +
                "user_col nchar(40), " +
                "user_login_status nchar(1)" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("SQLITE", oldVersion + "");
        Log.d("SQLITE", newVersion + "");
    }
}
