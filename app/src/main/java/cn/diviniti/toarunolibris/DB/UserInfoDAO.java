package cn.diviniti.toarunolibris.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class UserInfoDAO {
    private static String DEBUG = "VANGO_DB_USERINFO_DEBUG";
    private UserInfoSQL sql;

    public UserInfoDAO(Context context) {
        this.sql = new UserInfoSQL(context);
    }

    //增
    public void insertUser(String userNum, String userPwd, String userName, String userCol) {
        SQLiteDatabase db = sql.getWritableDatabase();
        //只能存一个用户
        db.execSQL("delete from userInfo");
        ContentValues values = new ContentValues();
        values.put("user_num", userNum);
        values.put("user_pwd", userPwd);
        values.put("user_name", userName);
        values.put("user_col", userCol);
        values.put("user_login_status", "1");
        db.insert("userInfo", null, values);
        db.close();
        Log.d(DEBUG, "用户:" + userNum + " 存储成功");
        Log.d(DEBUG, userPwd);
        Log.d(DEBUG, userName);
        Log.d(DEBUG, userCol);
    }

    //删
    public void deleteUser(String userNum) {
        SQLiteDatabase db = sql.getWritableDatabase();
        db.execSQL("delete from userInfo where user_num=?", new Object[]{userNum});
        db.close();
    }

    //查
    public String searchUser() {
        SQLiteDatabase db = sql.getReadableDatabase();
        Cursor data = db.rawQuery("select * from userInfo where user_login_status=1", null);
        StringBuilder userMsg = new StringBuilder();

        while (data.moveToNext()) {
            String userNum = data.getString(data.getColumnIndex("user_num"));
            String userPwd = data.getString(data.getColumnIndex("user_pwd"));
            userMsg.append(userNum);
            userMsg.append("&&");
            userMsg.append(userPwd);
        }
        db.close();

        return userMsg.toString();
    }

    //查 - 是否有用户还在登录状态
    public boolean hasUserLogin() {
        SQLiteDatabase db = sql.getReadableDatabase();
        Cursor data = db.rawQuery("select user_num from userInfo where user_login_status='1'", null);

        boolean flag = (data.getCount() > 0);
        return flag;
    }

    //查 - 当前用户名
    public String getUserName() {
        SQLiteDatabase db = sql.getReadableDatabase();
        Cursor data = db.rawQuery("select user_name from userInfo", null);

        String userName = null;
        while (data.moveToNext()) {
            userName = data.getString(data.getColumnIndex("user_name"));
        }
        return userName;
    }

    //改 - 主要用来修改用户登录状态
    public void updateUserLoginStatus(String userNum, boolean isLogin) {
        SQLiteDatabase db = sql.getWritableDatabase();
        if (isLogin) {
            db.execSQL("update userInfo set user_login_status=1 where user_num=?", new Object[]{userNum});
        } else {
            db.execSQL("update userInfo set user_login_status=0 where user_num=?", new Object[]{userNum});
        }
    }

}
