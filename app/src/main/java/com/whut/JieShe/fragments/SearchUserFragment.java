package com.whut.JieShe.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.whut.JieShe.R;
import com.whut.JieShe.UserDetailInfoActivity;
import com.whut.JieShe.adapter.QuickUserListAdapter;
import com.whut.JieShe.bean.MyUser;
import com.tapadoo.alerter.Alerter;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

public class SearchUserFragment extends BaseSearchFragment implements BaseQuickAdapter.OnItemClickListener {

    private OnFragmentInteractionListener mListener;

    private RecyclerView mSearchOrganizationListView;
    private SwipeRefreshLayout mRefreshLayout;
    private QuickUserListAdapter mUserListAdapter;
    private List<MyUser> mAllUsers;

    public SearchUserFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     */
    // TODO: Rename and change types and number of parameters
    public static SearchUserFragment newInstance() {
        SearchUserFragment fragment = new SearchUserFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        mRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });
        mSearchOrganizationListView = (RecyclerView) rootView.findViewById(R.id.search_post_list);
        mSearchOrganizationListView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        mUserListAdapter = new QuickUserListAdapter(getContext(),new ArrayList<MyUser>());
        mSearchOrganizationListView.setAdapter(mUserListAdapter);
        mUserListAdapter.setOnItemClickListener(this);

        loadData();

        return rootView;
    }

    private void loadData() {
        BmobQuery<MyUser> query = new BmobQuery<>();
        query.order("createdAt");
        query.findObjects(getContext(), new FindListener<MyUser>() {
            @Override
            public void onSuccess(List<MyUser> list) {
                mUserListAdapter.getData().clear();
                mUserListAdapter.addData(list);
                mRefreshLayout.setRefreshing(false);

                if(mAllUsers ==null){
                    mAllUsers = new ArrayList<>();
                }
                mAllUsers.clear();
                mAllUsers.addAll(list);
            }
            @Override
            public void onError(int i, String s) {
                Alerter.create(getActivity())
                        .setTitle("刷新失败")
                        .setText(s)
                        .show();
                mRefreshLayout.setRefreshing(false);
            }
        });
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(null);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        Intent intent = new Intent(getContext(), UserDetailInfoActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user",mUserListAdapter.getData().get(position));
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void filter(String keyWord){
        if(mAllUsers ==null){
            return;
        }
        mUserListAdapter.getData().clear();
        for(MyUser u: mAllUsers){
            if(u.getUsername().contains(keyWord) ||
                    u.getObjectId().equals(keyWord)){
                mUserListAdapter.addData(u);
            }
        }
    }
}
