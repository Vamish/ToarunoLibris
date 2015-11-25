package cn.diviniti.toarunolibris;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.afollestad.materialdialogs.MaterialDialog;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.apache.http.HttpHeaders;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.diviniti.toarunolibris.DB.BookListDAO;
import cn.diviniti.toarunolibris.RecyclerModel.BookInfo;
import cn.diviniti.toarunolibris.RecyclerModel.BookInfoAdapter;
import cn.diviniti.toarunolibris.Wechat.Constants;
import cn.diviniti.toarunolibris.Wechat.Util;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.Utils;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityBase;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityHelper;

public class BookInfoActivity extends AppCompatActivity implements SwipeBackActivityBase {
    private final static String VANGO_DEBUG_BOOK = "VANGO_DEBUG_BOOK";
    private final static int INFO_LOADED = 0x001;

    private CoordinatorLayout coordinatorLayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private FloatingActionButton fab;

    private ListView bookAvailable;
    private RecyclerView bookAuthor, bookPublisher;

    private static String BOOKID;
    private static String BOOK_NAME;
    private static String BOOK_AUTHOR;
    private static String BOOK_PUBLISHER;
    private static String BOOK_CALL_NUMBER;
    private boolean IS_SAVED_FLAG = false;

    private MaterialDialog dialog;

    private SwipeBackActivityHelper mHelper;

    private JSONObject shareBook;

    private IWXAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_info);
        api = WXAPIFactory.createWXAPI(this, Constants.APP_ID);
        Intent intent = getIntent();
        BOOKID = intent.getExtras().get("bookID").toString();
        BOOK_NAME = intent.getExtras().get("bookName").toString();
        BOOK_AUTHOR = intent.getExtras().get("bookAuthor").toString();
        BOOK_PUBLISHER = intent.getExtras().get("bookPublisher").toString();
        BOOK_CALL_NUMBER = intent.getExtras().get("bookCallNumber").toString();

        //分享的书JSON信息
        shareBook = new JSONObject();
        try {
            shareBook.put("t", BOOK_NAME);  //t:书名
            shareBook.put("a", BOOK_AUTHOR);    //a:作者
            shareBook.put("p", BOOK_PUBLISHER); //p:出版社
            shareBook.put("c", BOOK_CALL_NUMBER);   //c:索书号
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("VNA", "BookInfoActivity.class: JSON 基本信息出现错误");
        }

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.book_content);

        dialog = new MaterialDialog.Builder(this)
                .title("加载馆藏中")
                .content("请稍候")
                .progress(true, 0)
                .autoDismiss(false)
                .show();

        initToolbar();
        initSwipeBack();
        initFab();

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(BOOK_NAME);

        initRecyclerView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.enter_lefttoright, R.anim.exit_lefttoright);
    }

    private void initFab() {
        final BookListDAO bookListDAO = new BookListDAO(BookInfoActivity.this);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        if (bookListDAO.findBookById(BOOKID)) {
            fab.setImageResource(R.drawable.ic_check_white_24dp);
            IS_SAVED_FLAG = true;
        } else {
            fab.setImageResource(R.drawable.ic_book_white_24dp);
            IS_SAVED_FLAG = false;
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  保存如数据库
                if (!IS_SAVED_FLAG) {
                    //  未保存
                    Snackbar.make(collapsingToolbarLayout, "成功加入我的书单", Snackbar.LENGTH_SHORT).setCallback(new Snackbar.Callback() {
                        @Override
                        public void onShown(Snackbar snackbar) {
                            bookListDAO.insertBook(BOOKID, BOOK_NAME, BOOK_AUTHOR, BOOK_PUBLISHER, BOOK_CALL_NUMBER);
                            fab.setImageResource(R.drawable.ic_check_white_24dp);
                            IS_SAVED_FLAG = true;
                        }
                    }).setAction("撤销", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            bookListDAO.deleteBook(BOOKID);
                            fab.setImageResource(R.drawable.ic_book_white_24dp);
                        }
                    }).show();
                } else {
                    Snackbar.make(collapsingToolbarLayout, "从书单中删除", Snackbar.LENGTH_SHORT).setCallback(new Snackbar.Callback() {
                        @Override
                        public void onShown(Snackbar snackbar) {
                            bookListDAO.deleteBook(BOOKID);
                            fab.setImageResource(R.drawable.ic_book_white_24dp);
                        }
                    }).setAction("撤销", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            bookListDAO.insertBook(BOOKID, BOOK_NAME, BOOK_AUTHOR, BOOK_PUBLISHER, BOOK_CALL_NUMBER);
                            fab.setImageResource(R.drawable.ic_check_white_24dp);
                        }
                    }).show();
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

    private void initRecyclerView() {
        bookAvailable = (ListView) findViewById(R.id.books_available_msg);
        bookAuthor = (RecyclerView) findViewById(R.id.books_author_msg);
        bookPublisher = (RecyclerView) findViewById(R.id.books_publisher_msg);

        getAvailableMsg();

        BookInfo authorMsg = new BookInfo();
        if (!BOOK_AUTHOR.equals("")) {
            authorMsg.title = BOOK_AUTHOR;
        } else {
            authorMsg.title = "没有找到";
        }
        authorMsg.subTitle = "作者";
        List<BookInfo> authorList = new ArrayList<>();
        authorList.add(authorMsg);

        BookInfo publisherMsg = new BookInfo();
        publisherMsg.title = BOOK_PUBLISHER;
        publisherMsg.subTitle = "出版社";
        List<BookInfo> publisherList = new ArrayList<>();
        publisherList.add(publisherMsg);

        BookInfoAdapter authorAdapter = new BookInfoAdapter(getApplicationContext(), authorList);
        BookInfoAdapter publisherAdapter = new BookInfoAdapter(getApplicationContext(), publisherList);

        bookAuthor.setAdapter(authorAdapter);
        bookAuthor.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        bookPublisher.setAdapter(publisherAdapter);
        bookPublisher.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_book_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (item.getItemId() == R.id.action_share) {
            shareBook(BookInfoActivity.this, BOOK_CALL_NUMBER, BOOK_NAME);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void shareBook(final Context context, final String bookCallNum, final String bookName) {
//        String shareString = "索书号：" + bookCallNum + "\n" +
//                "书名：" + bookName;
//        String shareString = shareBook.toString();
        final String shareJson = URLEncoder.encode(shareBook.toString());
////        Log.d("VNA", shareJson);
//        Intent intent = new Intent(Intent.ACTION_SEND);
//        intent.setType("text/html");
//        intent.putExtra(Intent.EXTRA_SUBJECT, "分享图书信息");
//        intent.putExtra(Intent.EXTRA_TEXT, "http://toaru.diviniti.cn/Libris/book.html?" + shareJson);
//        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
//        context.startActivity(Intent.createChooser(intent, "分享"));
//
//        Uri uri = Uri.parse("http://toaru.diviniti.cn/Libris/book.html?" + shareJson);
//        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//        startActivity(intent);

//        api.registerApp(Constants.APP_ID);
//        Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.ic_wechat_share);
//
//        WXWebpageObject webPage = new WXWebpageObject();
//        webPage.webpageUrl = "http://toaru.diviniti.cn/Libris/book.html?" + shareJson;
//
//        WXMediaMessage msg = new WXMediaMessage(webPage);
//        msg.title = bookName + " - ToarunoLibris";
//        msg.description = "来自 愚者の图书馆";
//        msg.thumbData = Util.bmpToByteArray(thumb, true);
//
//        SendMessageToWX.Req req = new SendMessageToWX.Req();
//        req.transaction = buildTransaction("webpage");
//        req.scene = SendMessageToWX.Req.WXSceneSession;
//        req.message = msg;
//        api.sendReq(req);
        new MaterialDialog.Builder(BookInfoActivity.this)
                .title("分享到...")
                .items(R.array.share_list)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int which, CharSequence charSequence) {

                        api.registerApp(Constants.APP_ID);
                        Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.ic_wechat_share);

                        WXWebpageObject webPage = new WXWebpageObject();
                        webPage.webpageUrl = "http://toaru.diviniti.cn/Libris/book.html?" + shareJson;

                        WXMediaMessage msg = new WXMediaMessage(webPage);
                        msg.title = bookName + " - ToarunoLibris";
                        msg.description = "来自 愚者の图书馆";
                        msg.thumbData = Util.bmpToByteArray(thumb, true);

                        SendMessageToWX.Req req = new SendMessageToWX.Req();
                        req.transaction = buildTransaction("webpage");
                        req.message = msg;
                        switch (which) {
                            case 0:
                                req.scene = SendMessageToWX.Req.WXSceneTimeline;
                                api.sendReq(req);
                                break;
                            case 1:
                                req.scene = SendMessageToWX.Req.WXSceneSession;
                                api.sendReq(req);
                                break;
                            case 2:
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                String shareString = "索书号：" + bookCallNum + "\n" +
                                        "书名：" + bookName;
                                intent.putExtra(Intent.EXTRA_SUBJECT, "分享图书信息");
                                intent.putExtra(Intent.EXTRA_TEXT, shareString);
                                intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                context.startActivity(Intent.createChooser(intent, "分享"));
                                break;
                        }
                    }
                }).show();
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    public void getAvailableMsg() {

        final List<Map<String, String>> bookInfos = new ArrayList<>();
        final String url = "http://smjslib.jmu.edu.cn/bookinfo.aspx?ctrlno=" + BOOKID;

        final JSONArray detailArray = new JSONArray();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Document doc = Jsoup.connect(url)
                            .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.93 Safari/537.36")
                            .header(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.8,en;q=0.6")
                            .timeout(1000 * 30)
                            .get();

                    Elements libraries = doc.getElementsByTag("tbody");
                    Elements library = libraries.get(libraries.size() - 1).getElementsByTag("tr");
                    for (Element book : library) {
                        Map<String, String> bookInfo = new HashMap<String, String>();

                        //这个JSON存详细信息
                        JSONObject shareBookDetail = new JSONObject();
                        Log.d(VANGO_DEBUG_BOOK, "==========图书信息==========");

                        /*  TODO 每次循环都是第6个左右出现问题
                                *   这个只是在第6个的时候做一些处理
                                *   算不上优雅的做法，待后期优化
                                */
                        //  图书状态
                        Elements test = book.getElementsByIndexEquals(5);
                        for (Element e : test) {
                            bookInfo.put("bookStatus", e.text());
                        }
                        shareBookDetail.put("m", bookInfo.get("bookStatus"));
                        Log.d(VANGO_DEBUG_BOOK, "图书状态：" + bookInfo.get("bookStatus"));

                        Elements locations = book.getElementsByIndexEquals(0);
                        for (Element location : locations) {
                            //  判断如果以非20开头，则该书已经借出
                            if (!bookInfo.get("bookStatus").matches("^20")) {
                                //  这时候显示的标题应该为 ‘索书号 可供出借’或其他
                                bookInfo.put("title", BOOK_CALL_NUMBER + " " + bookInfo.get("bookStatus"));
                            } else {
                                bookInfo.put("title", bookInfo.get("bookStatus"));
                            }
                            bookInfo.put("subtitle", location.text());
                            shareBookDetail.put("l", bookInfo.get("subtitle"));
                        }
                        Log.d(VANGO_DEBUG_BOOK, "藏书地点：" + bookInfo.get("subtitle"));
                        Log.d(VANGO_DEBUG_BOOK, "==========E  N  D==========");
                        bookInfos.add(bookInfo);
                        detailArray.put(shareBookDetail);
                    }
                    shareBook.put("s", detailArray);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(), bookInfos,
                                    R.layout.layout_book_info,
                                    new String[]{"title", "subtitle"},
                                    new int[]{R.id.book_info_item_title, R.id.book_info_item_subtitle});
                            bookAvailable.setAdapter(adapter);
                            dialog.dismiss();
                        }
                    });
                } catch (ArrayIndexOutOfBoundsException e) {
                    Log.d(VANGO_DEBUG_BOOK, "没有馆藏");
                    Map<String, String> bookInfo = new HashMap<String, String>();
                    bookInfo.put("title", "没有馆藏");
                    bookInfo.put("subtitle", "藏书点");
                    bookInfos.add(bookInfo);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(), bookInfos,
                                    R.layout.layout_book_info,
                                    new String[]{"title", "subtitle"},
                                    new int[]{R.id.book_info_item_title, R.id.book_info_item_subtitle});
                            bookAvailable.setAdapter(adapter);
                            dialog.dismiss();
                        }
                    });
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    Handler handler = new Handler() {
    };

    private void initSwipeBack() {
        mHelper = new SwipeBackActivityHelper(this);
        mHelper.onActivityCreate();
        getSwipeBackLayout().setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        getSwipeBackLayout().setEdgeSize(100);
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
        Utils.convertActivityToTranslucent(BookInfoActivity.this);
        getSwipeBackLayout().scrollToFinishActivity();
    }
}
