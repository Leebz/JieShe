package com.whut.JieShe;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.whut.JieShe.adapter.ActivityViewPagerAdapter;
import com.whut.JieShe.fragments.ActivityJoinedOrRequestFragment;
import com.whut.JieShe.fragments.OnFragmentInteractionListener;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobUser;

public class MyActivityActivity extends AppCompatActivity implements OnFragmentInteractionListener {

    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private BmobUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_activity);

        if(Build.VERSION.SDK_INT >= 21) {
            if(getSupportActionBar()!=null){
                getSupportActionBar().setElevation(0);
            }
        }
        Bundle bundle = getIntent().getExtras();
        mUser = (BmobUser) bundle.get("user");
        BmobUser currentUser = BmobUser.getCurrentUser(this);
        if(!mUser.getObjectId().equals(currentUser.getObjectId())){
            setTitle("他的活动");
        }else{
            setTitle("我的活动");
        }

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);

        ActivityJoinedOrRequestFragment f1 = ActivityJoinedOrRequestFragment.newInstance(mUser,ActivityJoinedOrRequestFragment.TYPE_ACTIVITY_REQUEST);
        ActivityJoinedOrRequestFragment f2 = ActivityJoinedOrRequestFragment.newInstance(mUser,ActivityJoinedOrRequestFragment.TYPE_ACTIVITY_JOINED);
        final ArrayList<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(f1);
        fragments.add(f2);
        List<String> titles = new ArrayList<>();
        titles.add("已报名");
        titles.add("已参加");
        ActivityViewPagerAdapter adapter = new ActivityViewPagerAdapter(this,fragments,titles,this.getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void onFragmentInteraction(Bundle bundle) {

    }
}
