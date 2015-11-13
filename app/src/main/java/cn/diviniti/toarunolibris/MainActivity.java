package cn.diviniti.toarunolibris;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.diviniti.toarunolibris.BooksList.BooksListActivity;
import cn.diviniti.toarunolibris.FeedBack.FeedBackActivity;
import cn.diviniti.toarunolibris.Settings.SettingsActivity;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText searchInput;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;

    private LinearLayout drawerHeaderLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initSearchInputFocusCursor();
        initToolbar();
        initDrawerNav();
        initLoginArea();
    }

    private void initLoginArea() {
        ImageView imgUserUnlogged = (ImageView) findViewById(R.id.img_user_unlogged);
        imgUserUnlogged.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
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
        Log.d("VANGO_ANIM_DEBUG", "IN IT");
        super.startActivity(intent);
        overridePendingTransition(R.anim.enter_righttoleft, R.anim.exit_righttoleft);

    }

}
