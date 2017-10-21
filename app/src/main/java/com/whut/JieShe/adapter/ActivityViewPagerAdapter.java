package com.whut.JieShe.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by sukaiyi on 2017/05/10.
 */

public class ActivityViewPagerAdapter extends FragmentPagerAdapter {

    private Context mContext;
    private List<Fragment> mFragments;
    private List<String> mTitles;

    public ActivityViewPagerAdapter(Context context, List<Fragment> fragmentList,List<String> title, FragmentManager fm) {
        super(fm);
        mContext = context;
        mFragments = fragmentList;
        mTitles = title;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(position);
    }
}