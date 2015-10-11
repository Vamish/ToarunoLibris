package cn.diviniti.toarunolibris;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Timer;
import java.util.TimerTask;

public class WelcomeActivity extends AppCompatActivity {

    //      正式版
    //    private static final int SKIP_DELAY_TIME = 2000;
    private static final int SKIP_DELAY_TIME = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

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