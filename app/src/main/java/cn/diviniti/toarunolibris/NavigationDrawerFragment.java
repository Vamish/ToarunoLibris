package cn.diviniti.toarunolibris;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.diviniti.toarunolibris.RecyclerModel.DrawerList;
import cn.diviniti.toarunolibris.RecyclerModel.DrawerListAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class NavigationDrawerFragment extends android.support.v4.app.Fragment {

    public static final String PREFERENCE_FILE_NEME = "LIBRIS_PREFERENCE"; //   储存用户偏好的文件名
    public static final String KEY_USER_LEARNED_DRAWER = "user_learned_drawer"; //  用于存储偏好时候的关键字

    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private DrawerListAdapter adapter;
    private boolean isDrawerOpened;

    private View containerView;

    private boolean mUserLearnedDrawer;  // 用户知道Drawer 怎么用了
    private boolean mFromSavedInstanceState;    //  表示 是打开应用进入还是从别的应用切换过来

    public NavigationDrawerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserLearnedDrawer = Boolean.valueOf(readFromPreferences(getActivity(), KEY_USER_LEARNED_DRAWER, "false"));
        if (savedInstanceState != null) {
            //  表示从其他应用切换过来
            mFromSavedInstanceState = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        recyclerView = (RecyclerView) layout.findViewById(R.id.drawer_list);
        adapter = new DrawerListAdapter(getActivity(), getItemData());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Toast.makeText(getActivity(), "click" + position, Toast.LENGTH_SHORT).show();
                switch (position) {
                    case 0:
                        drawerLayout.closeDrawers();
                        break;
                    case 1:
                        startActivity(new Intent(getActivity(), SearchResultActivity.class));
                        break;
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                //  需要再加
//                Toast.makeText(getActivity(), "press", Toast.LENGTH_SHORT).show();
            }
        }));

        return layout;
    }

    public static List<DrawerList> getItemData() {
        List<DrawerList> list = new ArrayList<>();
        int[] iconId = {R.drawable.ic_account_balance_green_24dp,
                R.drawable.ic_schedule_amber_24dp,
                R.drawable.ic_loyalty_red_24dp,
                R.drawable.ic_settings_white_24dp,
                R.drawable.ic_bug_report_purple_24dp};
        int[] title = {R.string.drawer_list_index,
                R.string.drawer_list_status,
                R.string.drawer_list_mylist,
                R.string.drawer_list_settings,
                R.string.drawer_list_feedback};

        for (int i = 0; i < iconId.length && i < title.length; i++) {
            DrawerList current = new DrawerList();
            current.iconId = iconId[i];
            current.title = title[i];
            list.add(current);
        }

        return list;
    }

    public void setUp(int fragemntId, final DrawerLayout drawerLayout, final Toolbar toolbar) {
        containerView = getActivity().findViewById(fragemntId);
        this.drawerLayout = drawerLayout;
        drawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                isDrawerOpened = true;
                if (!mUserLearnedDrawer) {
                    mUserLearnedDrawer = true;
                    saveToPreferences(getActivity(), KEY_USER_LEARNED_DRAWER, mUserLearnedDrawer + "");
                }
                toolbar.setTitle("首页");
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                isDrawerOpened = false;
                toolbar.setTitle(R.string.app_name);
                getActivity().invalidateOptionsMenu();
            }
        };
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            drawerLayout.openDrawer(containerView);
        }
        drawerLayout.setDrawerListener(drawerToggle);
        drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                drawerToggle.syncState();
            }
        });
    }

    public void closeDrawerBeforeExit() {
        drawerLayout.closeDrawers();
    }

    public boolean getDrawerOpenedStatus() {
        return isDrawerOpened;
    }

    public static void saveToPreferences(Context context, String preferenceName, String preferenceValue) {
        // 储存用户偏好
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_FILE_NEME, Context.MODE_PRIVATE);     //Context.MODE_PRIVATE 表示只有本应用才能修改这个文件
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(preferenceName, preferenceValue);
        editor.apply();
    }

    public static String readFromPreferences(Context context, String preferenceName, String defaultValue) {
        // 读取用户偏好
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_FILE_NEME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(preferenceName, defaultValue);
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
            if (item != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
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

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }
}
