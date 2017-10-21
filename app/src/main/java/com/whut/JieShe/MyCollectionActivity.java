package com.whut.JieShe;

import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.whut.JieShe.adapter.ActivityViewPagerAdapter;
import com.whut.JieShe.fragments.ActivityCollectionFragment;
import com.whut.JieShe.fragments.OnFragmentInteractionListener;
import com.whut.JieShe.fragments.OrganizationCollectionFragment;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobUser;

public class MyCollectionActivity extends AppCompatActivity implements OnFragmentInteractionListener {

    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private BmobUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_collection);

        if(Build.VERSION.SDK_INT >= 21) {
            if(getSupportActionBar()!=null){
                getSupportActionBar().setElevation(0);
            }
        }
        Bundle bundle = getIntent().getExtras();
        mUser = (BmobUser) bundle.get("user");
        BmobUser currentUser = BmobUser.getCurrentUser(this);
        if(!mUser.getObjectId().equals(currentUser.getObjectId())){
            setTitle("他的收藏");
        }else{
            setTitle("我的收藏");
        }

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);

        final ArrayList<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(ActivityCollectionFragment.newInstance(mUser));
        fragments.add(OrganizationCollectionFragment.newInstance(mUser));
        List<String> titles = new ArrayList<>();
        titles.add("收藏的活动");
        titles.add("收藏的社团");
        ActivityViewPagerAdapter adapter = new ActivityViewPagerAdapter(this,fragments,titles,this.getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void onFragmentInteraction(Bundle bundle) {

    }
}
