package cn.diviniti.toarunolibris.HotSearch;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

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

public class HotSearchActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private RecyclerView hotSearchView;
    private HotTagAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot_search);
        initToolbar();
        initRecyclerView();
    }

    private void initRecyclerView() {
        hotSearchView = (RecyclerView) findViewById(R.id.hot_search_view);
        adapter = new HotTagAdapter(getApplicationContext(), getHotTags());
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

                            hotTag.tagName = td.text().split("\\(")[0];
                            hotTags.add(hotTag);
                            i++;
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
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
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

            }
        }
    };
}
