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
import com.whut.JieShe.PostDetailActivity;
import com.whut.JieShe.R;
import com.whut.JieShe.adapter.QuickPostListAdapter;
import com.whut.JieShe.bean.Post;
import com.tapadoo.alerter.Alerter;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

public class SearchPostFragment extends BaseSearchFragment implements BaseQuickAdapter.OnItemClickListener {

    private OnFragmentInteractionListener mListener;

    private RecyclerView mSearchPostListView;
    private SwipeRefreshLayout mRefreshLayout;
    private QuickPostListAdapter mPostAdapter;
    private List<Post> mAllPosts;

    public SearchPostFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     */
    // TODO: Rename and change types and number of parameters
    public static SearchPostFragment newInstance() {
        SearchPostFragment fragment = new SearchPostFragment();
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
        mSearchPostListView = (RecyclerView) rootView.findViewById(R.id.search_post_list);
        mSearchPostListView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        mPostAdapter = new QuickPostListAdapter(getContext(),new ArrayList<Post>());
        mSearchPostListView.setAdapter(mPostAdapter);
        mPostAdapter.setOnItemClickListener(this);

        loadData();

        return rootView;
    }

    private void loadData() {
        BmobQuery<Post> query = new BmobQuery<>();
        query.include("author,organization");
        query.order("-createdAt");
        query.findObjects(getContext(), new FindListener<Post>() {
            @Override
            public void onSuccess(List<Post> list) {
                mPostAdapter.getData().clear();
                mPostAdapter.addData(list);
                mRefreshLayout.setRefreshing(false);

                if(mAllPosts==null){
                    mAllPosts = new ArrayList<Post>();
                }
                mAllPosts.clear();
                mAllPosts.addAll(list);
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
        Post post = mPostAdapter.getData().get(position);
        Bundle bundle = new Bundle();
        bundle.putSerializable("post",post);
        Intent intent = new Intent(getContext(), PostDetailActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void filter(String keyWord){
        if(mAllPosts==null){
            return;
        }
        mPostAdapter.getData().clear();
        for(Post p:mAllPosts){
            if(p.getTitle().contains(keyWord)){
                mPostAdapter.addData(p);
            }
        }
    }
}
