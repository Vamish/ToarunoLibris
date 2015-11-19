package cn.diviniti.toarunolibris;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpHeaders;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import cn.diviniti.toarunolibris.BooksList.BooksListActivity;
import cn.diviniti.toarunolibris.DB.UserInfoDAO;
import cn.diviniti.toarunolibris.FeedBack.FeedBackActivity;
import cn.diviniti.toarunolibris.HotSearch.HotSearchActivity;
import cn.diviniti.toarunolibris.Login.LoginActivity;
import cn.diviniti.toarunolibris.MyStatus.BorrowingStatusActivity;
import cn.diviniti.toarunolibris.RecyclerModel.HotTag;
import cn.diviniti.toarunolibris.RecyclerModel.HotTagAdapter;
import cn.diviniti.toarunolibris.Settings.SettingsActivity;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText searchInput;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;

    private LinearLayout drawerHeaderLayout;

    private RecyclerView hotTagView;
    private HotTagAdapter hotTagAdapter;

    private UserInfoDAO userInfoDAO;

    private final static int HOT_TAG_LOADED = 0x01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userInfoDAO = new UserInfoDAO(getApplicationContext());

        initSearchInputFocusCursor();
        initToolbar();
        initDrawerNav();
        initLoginArea();
        initHotTags();
    }

    private void initHotTags() {
        TextView hotTagTv = (TextView) findViewById(R.id.hot_tag_tv);
        hotTagTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), HotSearchActivity.class));
            }
        });

        hotTagView = (RecyclerView) findViewById(R.id.hot_tag_view);
        hotTagAdapter = new HotTagAdapter(getApplicationContext(), getHotTags());
        hotTagView.setAdapter(hotTagAdapter);
        hotTagView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
        hotTagView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), hotTagView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                TextView hot = (TextView) view.findViewById(R.id.hot_tag);
                String hotName = hot.getText().toString();
                startActivity(new Intent(getApplicationContext(), SearchResultActivity.class)
                        .putExtra("searchKeyWords", hotName));
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    private List<HotTag> getHotTags() {
        final List<HotTag> hotTags = new ArrayList<>();

        try {
            final String hotTagUrl = "http://smjslib.jmu.edu.cn/top100.aspx?sparaname=anywords";
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Document doc = Jsoup.connect(hotTagUrl)
                                .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.99 Safari/537.36")
                                .header(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.8,en;q=0.6")
                                .timeout(1000 * 30)
                                .get();

                        Elements tableBody = doc.select("#top100Inner tbody td");
                        int i = 0;  //计数器
                        for (Element td : tableBody) {
                            final HotTag hotTag = new HotTag();

                            if (i < 6) {
                                hotTag.tagName = td.text().split("\\(")[0];
                            } else {
                                break;
                            }
                            hotTags.add(hotTag);
                            i++;
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                hotTagAdapter.notifyItemInserted(hotTags.size());
                            }
                        });
                    } catch (SocketTimeoutException e) {
                        Log.d("VANGO_DEBUG", "HOT_TAG_SOCKET_TIMEOUT:请求超时");
                    } catch (NullPointerException e) {
                        Log.d("VANGO_DEBUG", "HOT_TAG_NULL_POINTER:未找到");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return hotTags;
    }

    private void initLoginArea() {
        ImageView imgUserUnlogged = (ImageView) findViewById(R.id.img_user_unlogged);
        if (userInfoDAO.hasUserLogin()) {
            imgUserUnlogged.setImageResource(R.drawable.no_books_here);
            imgUserUnlogged.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, BorrowingStatusActivity.class)
                            .putExtra("userInfo", userInfoDAO.searchUser()));
                }
            });
        } else {
            imgUserUnlogged.setImageResource(R.drawable.img_index_unlogged_hint);
            imgUserUnlogged.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            });
        }

    }


    private void initDrawerNav() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerHeaderLayout = (LinearLayout) findViewById(R.id.drawer_header_layout);
        final ImageView drawerHeaderIcon = (ImageView) findViewById(R.id.drawer_header_icon);
        drawerHeaderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerHeaderIcon.setPressed(true);
            }
        });

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(drawerToggle);
        drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                drawerToggle.syncState();
            }
        });

        final ColorStateList mainColorStateList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        getResources().getColor(R.color.mainPrimaryColor),
                        getResources().getColor(R.color.mainPrimaryTextColor)
                }
        );
        final ColorStateList statusColorList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        getResources().getColor(R.color.mainAccentColor),
                        getResources().getColor(R.color.mainPrimaryTextColor)
                }
        );
        final ColorStateList bookslistColorList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        getResources().getColor(R.color.refreshFourColors2),
                        getResources().getColor(R.color.mainPrimaryTextColor)
                }
        );
        navigationView = (NavigationView) findViewById(R.id.navigation_menu);
        navigationView.setItemIconTintList(null);
        navigationView.setItemTextColor(mainColorStateList);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                switch (menuItem.getTitle().toString()) {
                    case "首页":
                        drawerLayout.closeDrawers();
                        navigationView.setItemTextColor(mainColorStateList);
                        break;
                    case "借阅状态":
                        drawerLayout.closeDrawers();
                        startActivity(new Intent(getApplicationContext(), BorrowingStatusActivity.class)
                                .putExtra("userInfo", userInfoDAO.searchUser()));
                        navigationView.setItemTextColor(statusColorList);
                        break;
                    case "我的书单":
                        navigationView.setItemTextColor(bookslistColorList);
                        drawerLayout.closeDrawers();
                        startActivity(new Intent(getApplicationContext(), BooksListActivity.class));
                        break;
                    case "设置":
                        drawerLayout.closeDrawers();
                        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                        break;
                    case "反馈":
                        drawerLayout.closeDrawers();
                        startActivity(new Intent(getApplicationContext(), FeedBackActivity.class));
                        break;
                }
                return false;
            }
        });
    }

    private void initSearchInputFocusCursor() {
        searchInput = (EditText) findViewById(R.id.search_input);
        searchInput.clearFocus();
        searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    startActivity(new Intent(getApplicationContext(), SearchResultActivity.class).putExtra("searchKeyWords", searchInput.getText().toString().trim()));
                    return true;
                }
                return false;
            }
        });
    }

    public void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawers();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.enter_righttoleft, R.anim.exit_righttoleft);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HOT_TAG_LOADED:
                    break;
            }
        }
    };

    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {
        private GestureDetector gestureDetector;
        private ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    View item = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (item != null && clickListener != null) {
                        clickListener.onClick(item, recyclerView.getChildPosition(item));
                    }
                    return super.onSingleTapUp(e);
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View item = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (item != null && clickListener != null) {
                        clickListener.onLongClick(item, recyclerView.getChildPosition(item));
                    }
                    super.onLongPress(e);
                }
            });
        }

        @Override

        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View item = rv.findChildViewUnder(e.getX(), e.getY());
            if (item != null && clickListener != null & gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(item, rv.getChildPosition(item));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }


    @Override
    protected void onResume() {
        initLoginArea();
        super.onResume();
    }
}
