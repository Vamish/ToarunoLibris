package cn.diviniti.toarunolibris.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import java.util.ArrayList;

import cn.diviniti.toarunolibris.DB.SearchHistoryDAO;
import cn.diviniti.toarunolibris.R;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.Utils;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityBase;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityHelper;

public class TestActivity extends AppCompatActivity implements SwipeBackActivityBase {

    private SwipeBackActivityHelper mHelper;
    private SearchHistoryDAO searchHistoryDAO;
    private Toolbar toolbar;
    private SearchBox searchBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        searchHistoryDAO = new SearchHistoryDAO(getApplicationContext());
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                initSearch();
                openSearch();
                return true;
            }
        });
        initSwipeBack();
        initDelete();
    }

    private void initDelete() {
        Button btn = (Button) findViewById(R.id.delete_history);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TestActivity.this, "点了", Toast.LENGTH_SHORT).show();
                searchHistoryDAO.deleteHistory();
            }
        });
    }

    private void openSearch() {
        searchBox.revealFromMenuItem(R.id.action_search_reveal, TestActivity.this);
    }

    private void initSearch() {
        searchBox = (SearchBox) findViewById(R.id.searchbox);

        searchBox.setMenuListener(new SearchBox.MenuListener() {
            @Override
            public void onMenuClick() {
                Toast.makeText(getApplicationContext(), "Menu Clicked!", Toast.LENGTH_SHORT).show();
            }
        });
        searchBox.setSearchListener(new SearchBox.SearchListener() {
            @Override
            public void onSearchOpened() {
                if (searchHistoryDAO.hasHistory()) {
                    ArrayList<String> histories = searchHistoryDAO.getHistory();
                    for (int i = 0; i < histories.size(); i++) {
                        SearchResult history = new SearchResult(histories.get(i), getResources().getDrawable(R.drawable.ic_history_grey_24dp));
                        searchBox.addSearchable(history);
                    }
                }
                Toast.makeText(getApplicationContext(), "Search Opened!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSearchCleared() {
                Toast.makeText(getApplicationContext(), "Search Cleared!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSearchClosed() {
                searchBox.hideCircularlyToMenuItem(R.id.action_search_reveal, TestActivity.this);
                Toast.makeText(getApplicationContext(), "Search Closed!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSearchTermChanged(String s) {

            }

            @Override
            public void onSearch(String s) {
                if (!searchHistoryDAO.isKeyWordExist(s)) {
                    searchHistoryDAO.insertHistory(s);
                }
                Toast.makeText(getApplicationContext(), "Search:" + s, Toast.LENGTH_SHORT).show();
                searchBox.hideCircularlyToMenuItem(R.id.action_search_reveal, TestActivity.this);
                toolbar.setTitle(s);
            }

            @Override
            public void onResultClick(SearchResult searchResult) {
                Toast.makeText(getApplicationContext(), "Clicked:" + searchResult.title, Toast.LENGTH_SHORT).show();
                onSearch(searchResult.title);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search_reveal) {
            Toast.makeText(getApplicationContext(), "ACTION_SEARCH_CLICKED", Toast.LENGTH_SHORT).show();
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
        Utils.convertActivityToTranslucent(TestActivity.this);
        getSwipeBackLayout().scrollToFinishActivity();
    }
}
