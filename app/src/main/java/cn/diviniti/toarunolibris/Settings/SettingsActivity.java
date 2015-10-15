package cn.diviniti.toarunolibris.Settings;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import cn.diviniti.toarunolibris.R;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.Utils;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityBase;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityHelper;

public class SettingsActivity extends AppCompatActivity implements SwipeBackActivityBase {
    private SwipeBackActivityHelper mHelper;

    private Toolbar toolbar;
    private TextView currentVerTextView;

    private RelativeLayout authorLayout;
    private RelativeLayout githubLinkLayout;
    private RelativeLayout openSourceLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initToolbar();
        initSwipeBack();
        initUpdate();
        initAuthor();
        initGithub();
        initOpenSource();
    }

    private void initGithub() {
        githubLinkLayout = (RelativeLayout) findViewById(R.id.github_link);
        githubLinkLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://github.com/Vamish/ToarunoLibris");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
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
                        .show();
            }
        });
    }

    private void initUpdate() {
        currentVerTextView = (TextView) findViewById(R.id.current_version_value);
        String versionName = "";
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = info.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            versionName = "获取版本号出错( ＞﹏＜) ";
        }
        currentVerTextView.setText("当前版本：" +
                versionName);

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
}
