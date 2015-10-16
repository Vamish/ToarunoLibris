package cn.diviniti.toarunolibris.Welcome;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import cn.diviniti.toarunolibris.MainActivity;
import cn.diviniti.toarunolibris.R;

public class WelcomeActivity extends AppCompatActivity {

    //      正式版
    //    private static final int SKIP_DELAY_TIME = 2000;
//    private static final int SKIP_DELAY_TIME = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // TODO: 测试用 跳过启动动画
        String USER_SETTINGS = "USER_SETTINGS";
        SharedPreferences settings = getSharedPreferences(USER_SETTINGS, MODE_PRIVATE);
        int SKIP_DELAY_TIME;
        if (Boolean.valueOf(settings.getString("SKIP_WELCOME_ACTIVITY", "true"))) {
            SKIP_DELAY_TIME = 500;
        } else {
            SKIP_DELAY_TIME = 2000;
        }

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        };
        timer.schedule(task, SKIP_DELAY_TIME);
    }

    @Override
    public void startActivity(Intent intent) {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        super.startActivity(intent);
    }
}
