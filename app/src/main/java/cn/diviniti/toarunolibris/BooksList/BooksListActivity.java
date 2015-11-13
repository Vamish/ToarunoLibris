package cn.diviniti.toarunolibris.BooksList;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import cn.diviniti.toarunolibris.BookInfoActivity;
import cn.diviniti.toarunolibris.DB.BookListDAO;
import cn.diviniti.toarunolibris.R;
import cn.diviniti.toarunolibris.RecyclerModel.BookSummary;
import cn.diviniti.toarunolibris.RecyclerModel.BookSummaryAdapter;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.Utils;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityBase;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityHelper;

public class BooksListActivity extends AppCompatActivity implements SwipeBackActivityBase {

    private BookSummaryAdapter adapter;
    private RecyclerView booksListLayout;
    private BookListDAO bookListDAO;
    private SwipeRefreshLayout swipeRefreshLayout;

    private SwipeBackActivityHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books_list);
        initToolbar();
        initSwipeBack();
        initRefresh();
        initList();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.enter_lefttoright, R.anim.exit_lefttoright);
    }

    @Override
    protected void onResume() {
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadData();
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 920);
        super.onResume();
    }

    private void initRefresh() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.book_swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.refreshFourColors1,
                R.color.refreshFourColors2,
                R.color.refreshFourColors3,
                R.color.refreshFourColors4);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(true);
                    }
                });
                swipeRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadData();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 920);
            }
        });
    }

    private void initList() {
        bookListDAO = new BookListDAO(getApplicationContext());

        loadData();

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        booksListLayout.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), booksListLayout, new ClickListener() {
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
                        .putExtra("bookAuthor", bookAuthor)
                        .putExtra("bookPublisher", bookPublisher)
                        .putExtra("bookCallNumber", bookCallNumber));
            }

            @Override
            public void onLongClick(final View view, final int position) {
                TextView bookIDView = (TextView) view.findViewById(R.id.book_id);
                final String bookID = bookIDView.getText().toString();

                TextView bookNameView = (TextView) view.findViewById(R.id.book_name);
                final String bookName = bookNameView.getText().toString();

                final TextView bookCallNumberView = (TextView) view.findViewById(R.id.book_call_number);
                final String bookCallNumber = bookCallNumberView.getText().toString();

                new MaterialDialog.Builder(BooksListActivity.this)
                        .title("选择功能")
                        .items(R.array.bookslist_function)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                switch (which) {
                                    case 0:
                                        String shareString = "索书号：" + bookCallNumber + "\n" +
                                                "书名：" + bookName;
                                        shareBook(BooksListActivity.this, shareString);
                                        break;
                                    case 1:
                                        new MaterialDialog.Builder(BooksListActivity.this)
                                                .title("删除确认")
                                                .content("确认删除《" + bookName + "》？")
                                                .positiveText("是的")
                                                .negativeText("按错了")
                                                .callback(new MaterialDialog.ButtonCallback() {
                                                    @Override
                                                    public void onPositive(MaterialDialog dialog) {
                                                        bookListDAO.deleteBook(bookID);
                                                        swipeRefreshLayout.setRefreshing(true);
                                                        swipeRefreshLayout.postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                loadData();
                                                                swipeRefreshLayout.setRefreshing(false);
                                                            }
                                                        }, 920);
                                                        Toast.makeText(getApplicationContext(), "已删除", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .show();
                                        break;
                                }
                            }
                        })
                        .show();
            }
        }));
    }

    public void loadData() {

        booksListLayout = (RecyclerView) findViewById(R.id.books_result);
        List<BookSummary> bookSummaryList = bookListDAO.findBooks();

        if (bookSummaryList != null) {

            adapter = new BookSummaryAdapter(getApplicationContext(), bookListDAO.findBooks());
            booksListLayout.setAdapter(adapter);
            booksListLayout.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

            // 解决RecyclerView上拉和SwipeRefreshLayout的冲突
            booksListLayout.setOnScrollListener(new RecyclerView.OnScrollListener() {
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

        } else {
            booksListLayout.setVisibility(View.GONE);
            ImageView noBookHere = (ImageView) findViewById(R.id.no_books_here);
            noBookHere.setVisibility(View.VISIBLE);
        }
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
        getMenuInflater().inflate(R.menu.menu_books_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        if (id == R.id.action_delete_all) {
            new MaterialDialog.Builder(BooksListActivity.this)
                    .title("删除确认")
                    .content("确认删除所有收藏的书？")
                    .positiveText("确认")
                    .negativeText("按错了")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            bookListDAO.deleteAll();
                            swipeRefreshLayout.setRefreshing(true);
                            swipeRefreshLayout.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    loadData();
                                    swipeRefreshLayout.setRefreshing(false);
                                }
                            }, 920);
                            Toast.makeText(getApplicationContext(), "已删除", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
            return true;
        }

        if (id == R.id.action_share_all) {
            List<BookSummary> bookSummaryList = bookListDAO.findBooks();

            if (bookSummaryList != null) {
                String shareString = "";
                for (BookSummary book : bookSummaryList) {
                    shareString += "索书号：" + book.bookCallNumber
                            + "\n"
                            + "书名："
                            + book.bookName
                            + "\n\n";
                }
                shareBook(BooksListActivity.this, shareString);
            } else {
                Toast.makeText(getApplicationContext(), "书单里面没有图书可以分享", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
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

    public void shareBook(Context context, String shareText) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "分享图书信息");
        intent.putExtra(Intent.EXTRA_TEXT, shareText);
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        context.startActivity(Intent.createChooser(intent, "分享"));
    }

    private void initSwipeBack() {
        mHelper = new SwipeBackActivityHelper(this);
        mHelper.onActivityCreate();
        getSwipeBackLayout().setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        getSwipeBackLayout().setEdgeSize(10);
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
        Utils.convertActivityToTranslucent(BooksListActivity.this);
        getSwipeBackLayout().scrollToFinishActivity();
    }
}
