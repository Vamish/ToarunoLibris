package cn.diviniti.toarunolibris.Settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.SocketTimeoutException;

import cn.diviniti.toarunolibris.R;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.Utils;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityBase;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityHelper;

public class SettingsActivity extends AppCompatActivity implements SwipeBackActivityBase {
    private SwipeBackActivityHelper mHelper;

    private Toolbar toolbar;
    private TextView currentVerTextView;

    private SwitchCompat update_switch;

    //  TODO 测试用，快速跳过启动应用
    private RelativeLayout skipLayout;
    private SwitchCompat skipSwitch;
    private static String SKIP_WELCOME_ACTIVITY = "SKIP_WELCOME_ACTIVITY";

    private RelativeLayout updataLayout;
    private RelativeLayout authorLayout;
    private RelativeLayout openSourceLayout;
    private RelativeLayout aboutLibrisLayout;

    private String versionName = "";

    private static String USER_SETTINGS = "USER_SETTINGS";
    private static String ALLOW_AUTO_CHECK_UPDATE = "ALLOW_AUTO_CHECK_UPDATE";
    private SharedPreferences settings;

    private final static int NO_UPGRADE = 0x00;
    private final static int UPGRADE_AVAILABLE = 0x01;
    private final static int TIME_OUT_EXCEPTION = 0x03;
    private final static int UNKNOWN_EXCEPTION = 0x04;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initToolbar();
        initSwipeBack();
        initSettings();
        initSkip();
        initUpdate();
        initAuthor();
//        initGithub();
        initAbout();
        initOpenSource();
    }

    private void initAbout() {
        aboutLibrisLayout = (RelativeLayout) findViewById(R.id.about_libris);
        aboutLibrisLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AboutLibrisActivity.class));
            }
        });
    }

    private void initSkip() {
        skipLayout = (RelativeLayout) findViewById(R.id.skip_welcome);
        skipSwitch = (SwitchCompat) findViewById(R.id.skip_switch);
        skipSwitch.setChecked(settings.getBoolean(SKIP_WELCOME_ACTIVITY, false));
        skipSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(SKIP_WELCOME_ACTIVITY, isChecked);
                editor.apply();
            }
        });
    }

    private void initSettings() {
        settings = getSharedPreferences(USER_SETTINGS, MODE_PRIVATE);
    }

    private void initAuthor() {
        authorLayout = (RelativeLayout) findViewById(R.id.author_layout);
        authorLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://m.weibo.cn/2025660943");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }

    private void initOpenSource() {
        openSourceLayout = (RelativeLayout) findViewById(R.id.open_source_layout);
        openSourceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(SettingsActivity.this)
                        .title("Open Source Licence")
                        .customView(R.layout.layout_open_source_info, true)
                        .positiveText("确定")
                        .neutralText("Github")
                        .neutralColor(getResources().getColor(android.R.color.white))
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onNeutral(MaterialDialog dialog) {
                                Uri uri = Uri.parse("https://github.com/Vamish/ToarunoLibris");
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }
                        }).show();
            }
        });
    }

    private void initUpdate() {
        currentVerTextView = (TextView) findViewById(R.id.current_version_value);
        updataLayout = (RelativeLayout) findViewById(R.id.update_layout);
        update_switch = (SwitchCompat) findViewById(R.id.update_switch);
        update_switch.setChecked(Boolean.valueOf(settings.getString(ALLOW_AUTO_CHECK_UPDATE, "true")));

        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = info.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            versionName = "获取版本号出错( ＞﹏＜) ";
        }

        update_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                SharedPreferences.Editor editor = settings.edit();
                editor.putString(ALLOW_AUTO_CHECK_UPDATE, Boolean.toString(isChecked));
                editor.apply();

                if (isChecked) {
                    checkForUpdate();
                } else {
                    currentVerTextView.setText("当前版本：" + versionName);
                }
            }
        });

        if (update_switch.isChecked()) {
            checkForUpdate();
        } else {
            currentVerTextView.setText("当前版本：" + versionName);
        }
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initSwipeBack() {
        mHelper = new SwipeBackActivityHelper(this);
        mHelper.onActivityCreate();
        getSwipeBackLayout().setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.enter_lefttoright, R.anim.exit_lefttoright);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mHelper.onPostCreate();
    }

    @Override
    public View findViewById(int id) {
        View v = super.findViewById(id);
        if (v == null && mHelper != null)
            return mHelper.findViewById(id);
        return v;
    }

    @Override
    public SwipeBackLayout getSwipeBackLayout() {
        return mHelper.getSwipeBackLayout();
    }

    @Override
    public void setSwipeBackEnable(boolean b) {
        getSwipeBackLayout().setEnableGesture(b);
    }

    @Override
    public void scrollToFinishActivity() {
        Utils.convertActivityToTranslucent(SettingsActivity.this);
        getSwipeBackLayout().scrollToFinishActivity();
    }

    public void checkForUpdate() {
        currentVerTextView.setText("检查更新中.....");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Document doc = Jsoup.connect("http://toaru.diviniti.cn/Libris/ver/ver.json")
                            .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.99 Safari/537.36")
                            .ignoreContentType(true)
                            .timeout(1000 * 10)
                            .get();
                    JSONObject versionJson = new JSONObject(doc.body().text());
                    if (!versionName.equals(versionJson.getString("version_name"))) {
                        Message msg = new Message();
                        msg.obj = versionJson;
                        msg.what = UPGRADE_AVAILABLE;
                        handler.sendMessage(msg);
                    } else {
                        handler.sendEmptyMessage(NO_UPGRADE);
                    }
                } catch (SocketTimeoutException e) {
                    handler.sendEmptyMessage(TIME_OUT_EXCEPTION);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NO_UPGRADE:
                    currentVerTextView.setText("无更新 当前版本：" + versionName);
                    break;
                case UPGRADE_AVAILABLE:
                    JSONObject versionJson = (JSONObject) msg.obj;
                    try {
                        String versionName = versionJson.getString("version_name");
                        JSONArray versionIntro = versionJson.getJSONArray("version_intro");
                        String content = "";
                        for (int i = 0; i < versionIntro.length(); i++) {
                            content += versionIntro.get(i) + "\n";
                        }
                        currentVerTextView.setText("新版本：" + versionName + " 点击更新");
                        final String finalContent = content;
                        updataLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new MaterialDialog.Builder(SettingsActivity.this)
                                        .icon(getResources().getDrawable(R.drawable.ic_launcher))
                                        .title("新版本可更新")
                                        .content(finalContent)
                                        .positiveText("下载")
                                        .negativeText("这次算了")
                                        .callback(new MaterialDialog.ButtonCallback() {
                                            @Override
                                            public void onPositive(MaterialDialog dialog) {
                                                Uri uri = Uri.parse("http://toaru.diviniti.cn/Libris/");
                                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                                startActivity(intent);
                                            }
                                        })
                                        .show();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        handler.sendEmptyMessage(UNKNOWN_EXCEPTION);
                    }
                    break;
                case TIME_OUT_EXCEPTION:
                    currentVerTextView.setText("连接超时，稍后再试。");
                    break;
                case UNKNOWN_EXCEPTION:
                    currentVerTextView.setText("获取版本异常。");
                    break;
            }
        }
    };

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.enter_righttoleft, R.anim.exit_righttoleft);
    }

}
