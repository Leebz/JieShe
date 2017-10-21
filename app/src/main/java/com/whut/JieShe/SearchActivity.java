package com.whut.JieShe;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.lapism.searchview.SearchHistoryTable;
import com.lapism.searchview.SearchView;
import com.whut.JieShe.adapter.ActivityViewPagerAdapter;
import com.whut.JieShe.fragments.BaseSearchFragment;
import com.whut.JieShe.fragments.OnFragmentInteractionListener;
import com.whut.JieShe.fragments.SearchOrganizationFragment;
import com.whut.JieShe.fragments.SearchPostFragment;
import com.whut.JieShe.fragments.SearchUserFragment;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements SearchView.OnOpenCloseListener, SearchView.OnQueryTextListener,OnFragmentInteractionListener {

    private Toolbar mToolbar;
    private SearchHistoryTable mHistoryDatabase;
    private SearchView mSearchView;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    private ArrayList<Fragment> mFragments;
    private int mCurrentTabPosition = 0;  //记录了当前tab所处的位置

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mHistoryDatabase = new SearchHistoryTable(this);
        mSearchView = (SearchView) findViewById(R.id.search_view);
        mSearchView.setHint("搜索");
        mSearchView.setOnOpenCloseListener(this);
        mSearchView.setOnQueryTextListener(this);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);

        mFragments = new ArrayList<>();
        mFragments.add(SearchPostFragment.newInstance());
        mFragments.add(SearchOrganizationFragment.newInstance());
        mFragments.add(SearchUserFragment.newInstance());

        List<String> titles = new ArrayList<>();
        titles.add("活动");
        titles.add("组织");
        titles.add("用户");
        ActivityViewPagerAdapter adapter = new ActivityViewPagerAdapter(this,mFragments,titles,this.getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mCurrentTabPosition = tab.getPosition();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public boolean onClose() {
        Log.d("SearchActivity", "onClose");
        return false;
    }

    @Override
    public boolean onOpen() {
        Log.d("SearchActivity", "onOpen");
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        ((BaseSearchFragment)mFragments.get(mCurrentTabPosition)).filter(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Log.d("SearchActivity", newText);
        return false;
    }

    @Override
    public void onFragmentInteraction(Bundle bundle) {

    }
}
