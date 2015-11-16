package cn.diviniti.toarunolibris.HotSearch;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import org.apache.http.HttpHeaders;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import cn.diviniti.toarunolibris.R;
import cn.diviniti.toarunolibris.RecyclerModel.HotTag;
import cn.diviniti.toarunolibris.RecyclerModel.HotTagAdapter;
import cn.diviniti.toarunolibris.SearchResultActivity;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.Utils;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityBase;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityHelper;

public class HotSearchActivity extends AppCompatActivity implements SwipeBackActivityBase {

    private Toolbar toolbar;

    private RecyclerView hotSearchView;
    private HotTagAdapter adapter;

    private SwipeRefreshLayout refreshLayout;

    private SwipeBackActivityHelper mHelper;

    private final static int HOT_TAG_LOADING = 0x01;
    private final static int HOT_TAG_LOADED = 0x02;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot_search);
        initToolbar();
        initSwipeBack();
        initRecyclerView();
        initRefresh();
    }

    private void initRefresh() {
        // 解决RecyclerView上拉和SwipeRefreshLayout的冲突
        hotSearchView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int topRowVerticalPosition =
                        (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                hotSearchView.setEnabled(topRowVerticalPosition >= 0);
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.hot_refresh_layout);
        refreshLayout.setColorSchemeResources(R.color.refreshFourColors1,
                R.color.refreshFourColors2,
                R.color.refreshFourColors3,
                R.color.refreshFourColors4);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (refreshLayout.isRefreshing()) {
                    refreshLayout.setRefreshing(false);
                }
            }
        });

    }

    private void initRecyclerView() {
        hotSearchView = (RecyclerView) findViewById(R.id.hot_search_view);
        adapter = new HotTagAdapter(getApplicationContext(), getHotTags());
        hotSearchView.setAdapter(adapter);
        hotSearchView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
        hotSearchView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), hotSearchView, new ClickListener() {
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
        handler.sendEmptyMessage(HOT_TAG_LOADING);
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

                            hotTag.tagName = td.text().split("\\(")[0];
                            hotTags.add(hotTag);
                            i++;
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                handler.sendEmptyMessage(HOT_TAG_LOADED);
                                adapter.notifyItemInserted(hotTags.size());
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


    public void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HOT_TAG_LOADING:
                    refreshLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            refreshLayout.setRefreshing(true);
                        }
                    });
                    break;
                case HOT_TAG_LOADED:
                    refreshLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            refreshLayout.setRefreshing(false);
                        }
                    });
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
        Utils.convertActivityToTranslucent(HotSearchActivity.this);
        getSwipeBackLayout().scrollToFinishActivity();
    }
}
