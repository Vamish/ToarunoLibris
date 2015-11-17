package cn.diviniti.toarunolibris.Login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.apache.http.HttpHeaders;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import cn.diviniti.toarunolibris.MyStatus.BorrowingStatusActivity;
import cn.diviniti.toarunolibris.R;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.Utils;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityBase;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityHelper;

public class LoginActivity extends AppCompatActivity implements SwipeBackActivityBase {

    private SwipeBackActivityHelper mHelper;

    private MaterialDialog logingDialog;

    private final static int USER_OR_PASSWORD_WRONG = 0x01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initToolbar();
        initSwipeBack();
        logingDialog = new MaterialDialog.Builder(LoginActivity.this).build();
        initLogin();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.enter_lefttoright, R.anim.exit_lefttoright);
    }

    private void initLogin() {
        final CardView btnArea = (CardView) findViewById(R.id.login_btn);

        final EditText userNameInput = (EditText) findViewById(R.id.login_username);
        final EditText userPasswordInput = (EditText) findViewById(R.id.login_password);


        btnArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnArea.setCardElevation(12);
                String userName = userNameInput.getText().toString().trim();
                String userPassword = userPasswordInput.getText().toString().trim();
                //TODO 记得把这个加上
                if (!userName.equals("") && !userPassword.equals("")) {
                    userLogin(userName, userPassword);
                } else {
                    Toast.makeText(getApplicationContext(), "别急，输完再登", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void userLogin(final String userName, final String userPassword) {
        logingDialog.getBuilder()
                .content("登录中")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .show();
        final String url_1 = "http://libinfo.jmu.edu.cn/cuser/";
        final String url_2 = "http://smjslib.jmu.edu.cn/user/userinfo.aspx";

        final Map<String, String> params = new LinkedHashMap<>();
        params.put("__VIEWSTATE", "/wEPDwULLTE3OTEyNjY3NjEPZBYCAgMPZBYCAgsPDxYGHgRUZXh0BRvnlKjmiLflkI3miJblr4bnoIHplJnor6/vvIEeCUZvcmVDb2xvcgqNAR4EXyFTQgIEZGRkei/L2q/Q2ShlyWBAMbzKVTaXfpgK3HpQkyBl5XSsMSc=");
        params.put("__EVENTVALIDATION", "/wEWBAKH14rCAQLcgpeMBwLGmdGVDAKM54rGBmcmiPP8UWvSgW6e+go9TWFHdtUYfvFJZ9Z1K+fsNwrE");
        //TODO 别傻了，这边得填他们的！
        params.put("user", userName);
        params.put("pwd", userPassword);
        params.put("Button1", "登录");

        new Thread(new Runnable() {
            @Override
            public void run() {
                Response response = null;
                try {
                    /* 页面会有两次跳转，所以
                     * 第一次访问获取cookie
                     * 第二次访问带着这个cookie
                     */
                    response = Jsoup.connect(url_1)
                            .method(Method.POST)
                            .header(HttpHeaders.CONTENT_TYPE, "x-www-form-urlencoded")
                            .header(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36")
                            .header(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.8,en;q=0.6")
                            .data(params)
                            .execute();

                    String iPlanetDirectoryPro = response.cookie("iPlanetDirectoryPro");

                    saveUserInfo();
                    startActivity(new Intent(getApplicationContext(), BorrowingStatusActivity.class)
                            .putExtra("cookie", iPlanetDirectoryPro));
                    finish();
                } catch (IllegalArgumentException e) {
                    //这种情况是用户名或者密码输入错误的
                    logingDialog.dismiss();
                    handler.sendEmptyMessage(USER_OR_PASSWORD_WRONG);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private void saveUserInfo() {
        //TODO 存储用户名和密码
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        if (id == R.id.action_login_announcement) {
            new MaterialDialog.Builder(LoginActivity.this)
                    .title(R.string.login_announcement)
                    .content("你所输入的账号密码 仅仅仅 供登入使用。【重要的事说三遍】")
                    .positiveText("知道了")
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    private void initSwipeBack() {
        mHelper = new SwipeBackActivityHelper(this);
        mHelper.onActivityCreate();
        getSwipeBackLayout().setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
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
        Utils.convertActivityToTranslucent(LoginActivity.this);
        getSwipeBackLayout().scrollToFinishActivity();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case USER_OR_PASSWORD_WRONG:
                    new MaterialDialog.Builder(LoginActivity.this)
                            .title("登录失败")
                            .content("检查一下学号还是密码，登不上去")
                            .positiveText("好的")
                            .show();
                    break;
            }
        }
    };
}
