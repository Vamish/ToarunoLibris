package cn.diviniti.toarunolibris.FeedBack;

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

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.diviniti.toarunolibris.R;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.Utils;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityBase;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityHelper;

public class FeedBackActivity extends AppCompatActivity implements SwipeBackActivityBase {

    private MaterialDialog dialog;

    private SwipeBackLayout swipeBackLayout;
    private SwipeBackActivityHelper mHelper;

    private static int TEST_TIME = 1000;

    final static int MAIL_SENT = 0x01;
    final static int MAIL_ERROR = 0x02;

    private MaterialDialog sendingDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_back);
        initToolbar();
        initSwipeBack();
        sendingDialog = new MaterialDialog.Builder(FeedBackActivity.this).build();
        initFeedBack();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.enter_lefttoright, R.anim.exit_lefttoright);
    }

    private void initFeedBack() {
        final EditText feedbackContent = (EditText) findViewById(R.id.feedback_content);
        CardView feedbackBtn = (CardView) findViewById(R.id.feedback_btn);

        feedbackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String content = feedbackContent.getText().toString();
                if (!content.equals("")) {
                    sendingDialog.getBuilder()
                            .content("发送中…")
                            .progress(true, 0)
                            .progressIndeterminateStyle(true)
                            .show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                MailSender sender = new MailSender();
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                                String sendContent = content + "\n\n\n" +
                                        "发送时间：" +
                                        sdf.format(new Date());
                                sender.sendMail(sendContent);
                                handler.sendEmptyMessage(MAIL_SENT);
                            } catch (Exception e) {
                                handler.sendEmptyMessage(MAIL_ERROR);
                                e.printStackTrace();
                            }
                        }
                    }).start();
                } else {
                    Toast.makeText(getApplicationContext(), "说完话再发嘛，不急。", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MAIL_SENT:
                    sendingDialog.dismiss();
                    new MaterialDialog.Builder(FeedBackActivity.this)
                            .content("发送成功")
                            .positiveText("好的")
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    FeedBackActivity.this.finish();
                                }
                            })
                            .show();
                    break;
                case MAIL_ERROR:
                    sendingDialog.dismiss();
                    new MaterialDialog.Builder(FeedBackActivity.this)
                            .content("发送失败")
                            .positiveText("检查一下")
                            .show();
                    break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_feed_back, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
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
        Utils.convertActivityToTranslucent(FeedBackActivity.this);
        getSwipeBackLayout().scrollToFinishActivity();
    }
}