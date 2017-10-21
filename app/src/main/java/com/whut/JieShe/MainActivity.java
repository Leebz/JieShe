package com.whut.JieShe;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarTab;
import com.roughike.bottombar.OnTabSelectListener;
import com.whut.JieShe.adapter.FragmentViewPagerAdapter;
import com.whut.JieShe.bean.Organization;
import com.whut.JieShe.fragments.MainColumnFragment;
import com.whut.JieShe.fragments.MainListFragment;
import com.whut.JieShe.fragments.MainMenuFragment;
import com.whut.JieShe.fragments.MainMessageFragment;
import com.whut.JieShe.fragments.OnFragmentInteractionListener;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.newim.BmobIM;
import cn.bmob.newim.event.MessageEvent;
import cn.bmob.newim.event.OfflineMessageEvent;
import cn.bmob.newim.listener.BmobIMMessageHandler;
import cn.bmob.newim.listener.ConnectListener;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class MainActivity extends AppCompatActivity implements OnFragmentInteractionListener {

    private ViewPager mViewPager;
    private BottomBar mBottomBar;
    private Toolbar mToolbar;
    private ArrayList<Fragment> mFragments;
    private Button button ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final BmobUser bmobUser = BmobUser.getCurrentUser(this);
        if(bmobUser == null){
            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(intent);
            this.finish();
            return;
        }else{
            Log.d("MainActivity", bmobUser.getEmail()+"");
            Log.d("MainActivity", bmobUser.getUsername()+"");
        }

        isManager();

        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        MainListFragment f1 = MainListFragment.newInstance(this);
        MainColumnFragment f2 = MainColumnFragment.newInstance(this);
        MainMessageFragment f3 = MainMessageFragment.newInstance(this);
        MainMenuFragment f4 = MainMenuFragment.newInstance(this);


        mFragments = new ArrayList<>();
        mFragments.add(f1);
        mFragments.add(f2);
        mFragments.add(f3);
        mFragments.add(f4);

        FragmentViewPagerAdapter adapter = new FragmentViewPagerAdapter(this.getSupportFragmentManager(), mViewPager, mFragments);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int[] arr = {
                        R.id.navigation_list,
                        R.id.navigation_column,
                        R.id.navigation_message,
                        R.id.navigation_menu,
                };
                mBottomBar.setDefaultTab(arr[position]);
            }


            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mBottomBar = (BottomBar) findViewById(R.id.bottom_bar);
        mBottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                switch (tabId) {
                    case R.id.navigation_list:
                        mViewPager.setCurrentItem(0);
                        break;
                    case R.id.navigation_column:
                        mViewPager.setCurrentItem(1);
                        break;
                    case R.id.navigation_message:
                        mViewPager.setCurrentItem(2);
                        mBottomBar.getCurrentTab().removeBadge();
                        break;
                    case R.id.navigation_menu:
                        mViewPager.setCurrentItem(3);
                        break;
                }
                BottomBarTab tab = mBottomBar.getTabWithId(tabId);
                MainActivity.this.setTitle(tab.getTitle());
            }
        });

        IMMessageHandler.getInstance().setHandler(new BmobIMMessageHandler(){
            @Override
            public void onOfflineReceive(final OfflineMessageEvent event) {
                MainMessageFragment fragment = (MainMessageFragment)mFragments.get(2);
                fragment.newMessage(event);
            }

            @Override
            public void onMessageReceive(MessageEvent messageEvent) {
                super.onMessageReceive(messageEvent);
                MainMessageFragment fragment = (MainMessageFragment)mFragments.get(2);
                fragment.newMessage(messageEvent);
            }
        });

        BmobIM.connect(bmobUser.getObjectId(), new ConnectListener() {
            @Override
            public void done(String uid, BmobException e) {
                if (e == null) {
                    Log.d("MainActivity", "BmobIM connect success");
                } else {
                    Log.e("MainActivity", e.getErrorCode()+e.getMessage());
                }
            }
        });

        BottomBarTab tab = mBottomBar.getTabWithId(R.id.navigation_message);
        tab.setBadgeCount((int) BmobIM.getInstance().getAllUnReadCount());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void isManager() {
        BmobQuery<Organization> query = new BmobQuery<Organization>();
        query.addWhereEqualTo("manager",BmobUser.getCurrentUser(this));
        query.findObjects(this, new FindListener<Organization>() {
            @Override
            public void onSuccess(List<Organization> list) {
                if(list==null||list.size()==0){
                    BmobIMApplication.IS_MANAGER = false;
                }else{
                    BmobIMApplication.IS_MANAGER = true;
                }
            }
            @Override
            public void onError(int i, String s) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_search:
                Intent intent = new Intent(MainActivity.this,SearchActivity.class);
                startActivity(intent);
                return  true;

        }
        return MainActivity.super.onOptionsItemSelected(item);
    }
    @Override
    public void onFragmentInteraction(Bundle bundle) {
        int action = bundle.getInt("action",-1);
        if(action==OnFragmentInteractionListener.ACTION_LOGOUT){
            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(intent);
            MainActivity.this.finish();
        }
    }
}
