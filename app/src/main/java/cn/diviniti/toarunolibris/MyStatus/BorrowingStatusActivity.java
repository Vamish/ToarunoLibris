package cn.diviniti.toarunolibris.MyStatus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.apache.http.HttpHeaders;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.diviniti.toarunolibris.BookInfoActivity;
import cn.diviniti.toarunolibris.DB.BookListDAO;
import cn.diviniti.toarunolibris.DB.UserInfoDAO;
import cn.diviniti.toarunolibris.Login.LoginActivity;
import cn.diviniti.toarunolibris.Login.LoginUtil;
import cn.diviniti.toarunolibris.R;
import cn.diviniti.toarunolibris.RecyclerModel.BorrowedBook;
import cn.diviniti.toarunolibris.RecyclerModel.BorrowedBookAdapter;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.Utils;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityBase;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityHelper;

public class BorrowingStatusActivity extends AppCompatActivity implements SwipeBackActivityBase {
    private SwipeBackActivityHelper mHelper;
    private String cookie;
    private String userName;
    private String userPwd;

    private RecyclerView expiredRecyclerView;
    private BorrowedBookAdapter expiredBooksAdapter;

    private RecyclerView allBorrowedRecyclerView;
    private BorrowedBook allBorrowedBooks;
    private BorrowedBookAdapter allBorrowedBooksAdapter;

    private BookListDAO bookListDAO;

    private final static int EXPIRED_BOOKS_NUMBER = 0x01;
    private final static int ALL_BOOKS_NUMBER = 0x02;
    private final static int USER_INFO_LOADED = 0x03;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrowing_status);
        initToolbar();
        initSwipeBack();
        if (getIntent().getStringExtra("cookie") != null) {
            cookie = getIntent().getStringExtra("cookie");
            initExpiredMsg();
            initAllBorrowedMsg();
        } else if (getIntent().getStringExtra("userInfo") != null) {
            String userInfo = getIntent().getStringExtra("userInfo");
            if (userInfo.equals("")) {
                Toast.makeText(getApplicationContext(), "请先登录", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            } else {
                userName = userInfo.split("&&")[0];
                userPwd = userInfo.split("&&")[1];
                Log.d("VNA", userName);
                Log.d("VNA", userPwd);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        LoginUtil loginUtil = new LoginUtil(userName, userPwd);
                        try {
                            cookie = loginUtil.getSession();
                            handler.sendEmptyMessage(USER_INFO_LOADED);
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    }

    //
    private void initAllBorrowedMsg() {
//        allBorrowedRecyclerView = (RecyclerView) findViewById(R.id.current_borrowed_list);
//        allBorrowedBooksAdapter = new BorrowedBookAdapter(getApplicationContext(), getAllBorrowed());
//        allBorrowedRecyclerView.setAdapter(allBorrowedBooksAdapter);
//        allBorrowedRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<BorrowedBook> list = getAllBorrowed();
            }
        }).start();
    }

    private void initExpiredMsg() {
        expiredRecyclerView = (RecyclerView) findViewById(R.id.expired_book_list);
        expiredBooksAdapter = new BorrowedBookAdapter(getApplicationContext(), getExpiredInfo());
        expiredRecyclerView.setAdapter(expiredBooksAdapter);
        expiredRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        expiredRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), expiredRecyclerView, new ClickListener() {
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
                    new MaterialDialog.Builder(BorrowingStatusActivity.this)
                            .title("选择功能")
                            .items(R.array.search_function)
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                    switch (which) {
                                        case 0:
                                            shareBook(BorrowingStatusActivity.this, bookCallNumber, bookName);
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
                    new MaterialDialog.Builder(BorrowingStatusActivity.this)
                            .title("选择功能")
                            .items(R.array.search_function_done)
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                    switch (which) {
                                        case 0:
                                            shareBook(BorrowingStatusActivity.this, bookCallNumber, bookName);
                                            break;
                                    }
                                }
                            })
                            .show();
                }
            }
        }));
    }

    private List<BorrowedBook> getExpiredInfo() {
        final List<BorrowedBook> expiredBooks = new ArrayList<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                String expiredUrl = "http://smjslib.jmu.edu.cn/user/chtsmessage.aspx";

                Document document = null;
                try {
                    document = Jsoup.connect(expiredUrl)
                            .header(HttpHeaders.CONTENT_TYPE, "x-www-form-urlencoded")
                            .header(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36")
                            .header(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.8,en;q=0.6")
                            .cookie("iPlanetDirectoryPro", cookie)
                            .post();

                    final Elements expiredTable = document.select("#UserMasterRight tbody tr");

                    int i = 1;
                    for (Element expiredBookElement : expiredTable) {
                        BorrowedBook expiredBook = new BorrowedBook();
                        //获取书名
                        Log.d("VANGO_BORROW_DEBUG", "=============" + i + "=============");

                        Elements bookNames = expiredBookElement.getElementsByIndexEquals(4);
                        for (Element bookName : bookNames) {
                            expiredBook.bookName = bookName.text().split("／")[0];
                        }
                        Log.d("VANGO_BORROW_DEBUG", "书名: " + expiredBook.bookName);

                        //获取馆藏地
                        Elements locations = expiredBookElement.getElementsByIndexEquals(2);
                        for (Element location : locations) {
                            expiredBook.generalInfo = location.text();
                        }
                        Log.d("VANGO_BORROW_DEBUG", "馆藏地: " + expiredBook.generalInfo);

                        //获取应还时间
                        Elements returnTimes = expiredBookElement.getElementsByIndexEquals(3);
                        for (Element returnTime : returnTimes) {
                            expiredBook.returnTime = returnTime.text();
                        }
                        Log.d("VANGO_BORROW_DEBUG", "返还时间: " + expiredBook.returnTime);

                        //获取图书ID
                        Elements bookID = expiredBookElement.getElementsByIndexEquals(4).select("a");
                        for (Element href : bookID) {
                            expiredBook.bookID = href.attr("href").split("=")[1];
                        }
                        Log.d("VANGO_BORROW_DEBUG", "书本ID : " + expiredBook.bookID);


                        Log.d("VANGO_BORROW_DEBUG", "=============" + "END" + "=============");
                        i++;
                        expiredBooks.add(expiredBook);
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Message msg = new Message();
                            msg.what = EXPIRED_BOOKS_NUMBER;
                            msg.obj = expiredTable.size();
                            expiredBooksAdapter.notifyItemInserted(expiredBooks.size());
                            handler.sendMessage(msg);
                        }
                    });

                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();

        return expiredBooks;
    }

    private List<BorrowedBook> getAllBorrowed() {
        final List<BorrowedBook> allBorrowedBooks = new ArrayList<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                String allBorrowedUrl = "http://smjslib.jmu.edu.cn/user/bookborrowed.aspx";

                Document document = null;
                try {
                    document = Jsoup.connect(allBorrowedUrl)
                            .header(HttpHeaders.CONTENT_TYPE, "x-www-form-urlencoded")
                            .header(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36")
                            .header(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.8,en;q=0.6")
                            .cookie("iPlanetDirectoryPro", cookie)
                            .post();

                    final Elements allBorrowedTable = document.select("#borrowedcontent tbody tr");

                    int i = 1;
                    for (Element allBookElement : allBorrowedTable) {
                        BorrowedBook borrowedBook = new BorrowedBook();
                        //获取书名
                        Log.d("VANGO_BORROW_DEBUG", "=============" + i + "=============");

                        Elements bookNames = allBookElement.getElementsByIndexEquals(2);
                        for (Element bookName : bookNames) {
                            borrowedBook.bookName = bookName.text().split("／")[0];
                            borrowedBook.generalInfo = bookName.text().split("／")[1];
                        }
                        Log.d("VANGO_BORROW_DEBUG", "书名: " + borrowedBook.bookName);
                        Log.d("VANGO_BORROW_DEBUG", "作者: " + borrowedBook.generalInfo);

                        //获取应还时间
                        Elements returnTimes = allBookElement.getElementsByIndexEquals(1);
                        for (Element returnTime : returnTimes) {
                            borrowedBook.returnTime = returnTime.text();
                        }
                        Log.d("VANGO_BORROW_DEBUG", "返还时间: " + borrowedBook.returnTime);

                        //获取图书ID
                        Elements bookID = allBookElement.getElementsByIndexEquals(2).select("a");
                        for (Element href : bookID) {
                            borrowedBook.bookID = href.attr("href").split("=")[1];
                        }
                        Log.d("VANGO_BORROW_DEBUG", "书本ID : " + borrowedBook.bookID);

                        Log.d("VANGO_BORROW_DEBUG", "=============" + "END" + "=============");
                        i++;
                        allBorrowedBooks.add(borrowedBook);
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Message msg = new Message();
                            msg.what = ALL_BOOKS_NUMBER;
                            msg.obj = allBorrowedTable.size();
                            handler.sendMessage(msg);
//                            allBorrowedBooksAdapter.notifyItemInserted(allBorrowedBooks.size());
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
        return null;
    }


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
        Utils.convertActivityToTranslucent(BorrowingStatusActivity.this);
        getSwipeBackLayout().scrollToFinishActivity();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.enter_lefttoright, R.anim.exit_lefttoright);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_borrowing_status, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        if (id == R.id.action_exit) {
            //TODO 退出
            UserInfoDAO userInfoDAO = new UserInfoDAO(BorrowingStatusActivity.this);
            userInfoDAO.deleteUser(userName);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EXPIRED_BOOKS_NUMBER:
                    TextView expiredBook = (TextView) findViewById(R.id.expired_book);
                    expiredBook.setText(msg.obj.toString());
                    break;
                case ALL_BOOKS_NUMBER:
                    TextView allBorrowedBook = (TextView) findViewById(R.id.current_borrowed);
                    allBorrowedBook.setText(msg.obj.toString());
                    break;
                case USER_INFO_LOADED:
                    initExpiredMsg();
                    initAllBorrowedMsg();
                    break;
            }
        }
    };
}

