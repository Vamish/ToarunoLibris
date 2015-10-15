package cn.diviniti.toarunolibris;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import org.apache.http.HttpHeaders;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import cn.diviniti.toarunolibris.DB.BookListDAO;
import cn.diviniti.toarunolibris.RecyclerModel.BookSummary;
import cn.diviniti.toarunolibris.RecyclerModel.BookSummaryAdapter;
import jp.wasabeef.recyclerview.animators.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.adapters.ScaleInAnimationAdapter;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.Utils;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityBase;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityHelper;

public class SearchResultActivity extends AppCompatActivity implements SwipeBackActivityBase {
    private final static String VANGO_DEBUG_BOOK = "VANGO_DEBUG_BOOK";

    private final static int SOCKET_TIME_OUT = 0x000001;
    private final static int BOOK_NOT_FOUND = 0x000002;
    private final static int BOOKS_LIST_LOADING = 0x000003;
    private final static int BOOKS_LIST_LOADED = 0x000004;

    private Toolbar toolbar;

    private String searchKeyWords;
    private RecyclerView booksListView;
    private static BookSummaryAdapter adapter;
    private ScaleInAnimationAdapter animationAdapter;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView imageHolderForException;

    private SwipeBackActivityHelper mHelper;

    private SearchBox searchBox;

    private BookListDAO bookListDAO;
    private int currentPage = 1;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_result);
        Intent intent = getIntent();
        if (intent != null) {
            searchKeyWords = intent.getExtras().get("searchKeyWords").toString();
        }

        //  Toolbar初始化
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(searchKeyWords.toUpperCase());
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                openSearch();
                return true;
            }
        });

        initSwipeBack();

        booksListView = (RecyclerView) findViewById(R.id.books_result);
        adapter = new BookSummaryAdapter(getApplicationContext(), getPageBooksListData(searchKeyWords, currentPage));
        // RecyclerView动画
        AlphaInAnimationAdapter animator = new AlphaInAnimationAdapter(adapter);
        animator.setDuration(230);
        animationAdapter = new ScaleInAnimationAdapter(animator);
        booksListView.setAdapter(animationAdapter);
        booksListView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        // recyclerView 上的点击事件处理
        booksListView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), booksListView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                TextView bookIDView = (TextView) view.findViewById(R.id.book_id);
                TextView bookNameView = (TextView) view.findViewById(R.id.book_name);
                TextView bookAuthorView = (TextView) view.findViewById(R.id.book_author);
                TextView bookPublisherView = (TextView) view.findViewById(R.id.book_publisher);
                TextView bookCallNumberView = (TextView) view.findViewById(R.id.book_call_number);
                String bookID = bookIDView.getText().toString();
                String bookName = bookNameView.getText().toString();
                String bookAuthor = bookAuthorView.getText().toString();
                String bookPublisher = bookPublisherView.getText().toString();
                String bookCallNumber = bookCallNumberView.getText().toString();
                startActivity(new Intent(getApplicationContext(), BookInfoActivity.class)
                        .putExtra("bookID", bookID)
                        .putExtra("bookName", bookName)
                        .putExtra("bookPublisher", bookPublisher)
                        .putExtra("bookAuthor", bookAuthor)
                        .putExtra("bookCallNumber", bookCallNumber));
            }

            @Override
            public void onLongClick(View view, int position) {
                bookListDAO = new BookListDAO(getApplicationContext());
                TextView bookIDView = (TextView) view.findViewById(R.id.book_id);
                TextView bookNameView = (TextView) view.findViewById(R.id.book_name);
                TextView bookAuthorView = (TextView) view.findViewById(R.id.book_author);
                TextView bookPublisherView = (TextView) view.findViewById(R.id.book_publisher);
                TextView bookCallNumberView = (TextView) view.findViewById(R.id.book_call_number);
                final String bookID = bookIDView.getText().toString();
                final String bookName = bookNameView.getText().toString();
                final String bookAuthor = bookAuthorView.getText().toString();
                final String bookPublisher = bookPublisherView.getText().toString();
                final String bookCallNumber = bookCallNumberView.getText().toString();

                if (!bookListDAO.findBookById(bookID)) {
                    new MaterialDialog.Builder(SearchResultActivity.this)
                            .title("选择功能")
                            .items(R.array.search_function)
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                    switch (which) {
                                        case 0:
                                            shareBook(SearchResultActivity.this, bookCallNumber, bookName);
                                            break;
                                        case 1:
                                            bookListDAO.insertBook(bookID, bookName, bookAuthor, bookPublisher, bookCallNumber);
                                            Toast.makeText(getApplicationContext(), "已保存", Toast.LENGTH_SHORT).show();
                                            break;
                                    }
                                }
                            })
                            .show();
                } else {
                    new MaterialDialog.Builder(SearchResultActivity.this)
                            .title("选择功能")
                            .items(R.array.search_function_done)
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                    switch (which) {
                                        case 0:
                                            shareBook(SearchResultActivity.this, bookCallNumber, bookName);
                                            break;
                                    }
                                }
                            })
                            .show();
                }
            }
        }));

        // 解决RecyclerView上拉和SwipeRefreshLayout的冲突
        booksListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int topRowVerticalPosition =
                        (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(topRowVerticalPosition >= 0);
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.book_swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.refreshFourColors1,
                R.color.refreshFourColors2,
                R.color.refreshFourColors3,
                R.color.refreshFourColors4);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getApplicationContext(), "是最新内容了", Toast.LENGTH_SHORT).show();
                }
            }
        });
        imageHolderForException = (ImageView) findViewById(R.id.img_book_not_found);
    }

    private void openSearch() {
        searchBox = (SearchBox) findViewById(R.id.searchbox);
        // TODO 这里要用sqlite数据代替
        for (int i = 0; i < 5; i++) {
            SearchResult option = new SearchResult("TestResult " + Integer.toString(i), getResources().getDrawable(R.drawable.ic_history_grey_24dp));
            searchBox.addSearchable(option);
        }
        searchBox.revealFromMenuItem(R.id.action_search, this);

        searchBox.setMenuListener(new SearchBox.MenuListener() {
            @Override
            public void onMenuClick() {
                searchBox.hideCircularlyToMenuItem(R.id.action_search, SearchResultActivity.this);
            }
        });
        searchBox.setSearchListener(new SearchBox.SearchListener() {
            @Override
            public void onSearchOpened() {

            }

            @Override
            public void onSearchCleared() {

            }

            @Override
            public void onSearchClosed() {
                searchBox.hideCircularlyToMenuItem(R.id.action_search_reveal, SearchResultActivity.this);
            }

            @Override
            public void onSearchTermChanged(String s) {

            }

            @Override
            public void onSearch(String searchKeyWord) {
                toolbar.setTitle(searchKeyWord.toUpperCase());
                searchBox.hideCircularlyToMenuItem(R.id.action_search, SearchResultActivity.this);
                swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(true);
                    }
                });
                adapter = new BookSummaryAdapter(getApplicationContext(), getPageBooksListData(searchKeyWord, currentPage));
                booksListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onResultClick(SearchResult searchResult) {
                onSearch(searchResult.title);
            }
        });

    }

    public void shareBook(Context context, String bookCallNum, String bookName) {
        String shareString = "索书号：" + bookCallNum + "\n" +
                "书名：" + bookName;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "分享图书信息");
        intent.putExtra(Intent.EXTRA_TEXT, shareString);
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        context.startActivity(Intent.createChooser(intent, "分享"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.enter_lefttoright, R.anim.exit_lefttoright);
    }

    public List<BookSummary> getPageBooksListData(String searchKeyWord, final int currentPage) {

        final List<BookSummary> booksList = new ArrayList<>();

        try {
            searchKeyWord = URLEncoder.encode(searchKeyWord, "UTF-8");
            final String url = "http://smjslib.jmu.edu.cn/searchresult.aspx?" +
                    "anywords=" + searchKeyWord +
                    "&dt=ALL" +
                    "&cl=ALL" +
                    "&dp=50" +
                    "&sf=M_PUB_YEAR" +
                    "&ob=DESC" +
                    "&sm=table" +
                    "&dept=ALL" +
                    "&code=UTF-8" +
                    "&page=" + currentPage;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    handler.sendEmptyMessage(BOOKS_LIST_LOADING);

                    try {
                        Document doc = Jsoup.connect(url)
                                .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.99 Safari/537.36")
                                .header(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.8,en;q=0.6")
                                .timeout(1000 * 30)
                                .get();

                        Log.d(VANGO_DEBUG_BOOK, "加载第" + currentPage + "页");

                        final Element findBooksTotal = doc.getElementById("ctl00_ContentPlaceHolder1_countlbl");
                        int totalPages = Integer.parseInt(doc.getElementById("ctl00_ContentPlaceHolder1_gplblfl1").text());
                        Log.d(VANGO_DEBUG_BOOK, "共" + totalPages + "页");

                        Elements elements = doc.select("tbody tr");
                        int i = 1;  // 调试用 无实际功能
                        for (Element element : elements) {
                            Log.d(VANGO_DEBUG_BOOK, "==========第 " + i + " 本==========");
                            final BookSummary book = new BookSummary();

                            //  书的ID、书名
                            Elements hrefs = element.getElementsByClass("title");
                            for (Element href : hrefs) {
                                book.bookID = href.child(0).attr("href").split("=")[1];
                                Log.d(VANGO_DEBUG_BOOK, "ID:\t\t\t" + book.bookID);

                                book.bookName = href.text();
                                Log.d(VANGO_DEBUG_BOOK, "NAME:\t\t" + book.bookName);
                            }

                            //  书的作者
                            Elements authorElements = element.getElementsByIndexEquals(2);
                            for (Element author : authorElements) {
                                book.bookAuthor = author.text();
                                Log.d(VANGO_DEBUG_BOOK, "AUTHOR:\t\t" + book.bookAuthor);
                            }

                            //  书的出版社
                            Elements publisherElements = element.getElementsByIndexEquals(3);
                            for (Element publisher : publisherElements) {
                                book.bookPublisher = publisher.text();
                                Log.d(VANGO_DEBUG_BOOK, "PUBLISHER:\t" + book.bookPublisher);
                            }

                            //  书的索书号、可借数、总数
                            Elements booksDetails = element.getElementsByClass("tbr");
                            book.bookCallNumber = booksDetails.get(0).text();
                            Log.d(VANGO_DEBUG_BOOK, "CALLNUMBER:\t" + book.bookCallNumber);

                            book.bookTotal = booksDetails.get(1).text();
                            Log.d(VANGO_DEBUG_BOOK, "TOTAL:\t\t" + book.bookTotal);

                            book.bookAvailable = booksDetails.get(2).text();
                            Log.d(VANGO_DEBUG_BOOK, "AVAILABLE:\t" + book.bookAvailable);

                            booksList.add(book);
                            Log.d(VANGO_DEBUG_BOOK, "========== E N D ==========");

                            i++;    //调试用 无实际功能
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                animationAdapter.notifyItemInserted(booksList.size());
                                handler.sendEmptyMessage(BOOKS_LIST_LOADED);
                            }
                        });

                    } catch (SocketTimeoutException e) {
                        Log.d(VANGO_DEBUG_BOOK, "socket_time_out: 请求超时");
                        handler.sendEmptyMessage(SOCKET_TIME_OUT);
                    } catch (NullPointerException e) {
                        Log.d(VANGO_DEBUG_BOOK, "book_not_found: 未找到图书");
                        handler.sendEmptyMessage(BOOK_NOT_FOUND);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return booksList;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SOCKET_TIME_OUT:
                    swipeRefreshLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                    Toast.makeText(SearchResultActivity.this, "请求超时，请检查网络", Toast.LENGTH_SHORT).show();
                    imageHolderForException.setImageResource(R.drawable.socket_time_out);
                    imageHolderForException.setVisibility(View.VISIBLE);
                    break;
                case BOOK_NOT_FOUND:
                    swipeRefreshLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                    Toast.makeText(SearchResultActivity.this, "没找到 :P", Toast.LENGTH_SHORT).show();
                    imageHolderForException.setImageResource(R.drawable.book_not_found);
                    imageHolderForException.setVisibility(View.VISIBLE);
                    break;
                case BOOKS_LIST_LOADING:
                    swipeRefreshLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(true);
                        }
                    });
                    break;
                case BOOKS_LIST_LOADED:
                    swipeRefreshLayout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }, 500);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_result, menu);
        return true;
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
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.enter_righttoleft, R.anim.exit_righttoleft);

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
        Utils.convertActivityToTranslucent(SearchResultActivity.this);
        getSwipeBackLayout().scrollToFinishActivity();
    }
}
