package com.whut.JieShe.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.whut.JieShe.OrganizationDetailActivity;
import com.whut.JieShe.PostDetailActivity;
import com.whut.JieShe.R;
import com.whut.JieShe.adapter.QuickPostListAdapter;
import com.whut.JieShe.bean.Organization;
import com.whut.JieShe.bean.Post;
import com.whut.JieShe.view.RecycleViewDivider;
import com.tapadoo.alerter.Alerter;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainListFragment extends Fragment implements BaseQuickAdapter.OnItemClickListener, BaseQuickAdapter.OnItemChildClickListener {

    private OnFragmentInteractionListener mListener;
    private RecyclerView mPostListView;
    private QuickPostListAdapter mPostAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private int mCountEveryTime = 10; //每次加载条数
    private int mCurrentCount = 0;    //已加载条数
    private int mTotalCount = -1;    //全部数据条数

    public MainListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MainListFragment.
     */
    public static MainListFragment newInstance(OnFragmentInteractionListener listener) {
        MainListFragment fragment = new MainListFragment();
        fragment.setArguments(new Bundle());
        fragment.mListener = listener;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_list, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);
        mPostListView = (RecyclerView) rootView.findViewById(R.id.post_list);
        mPostListView.setLayoutManager(new LinearLayoutManager(getContext()));
        mPostListView.addItemDecoration(new RecycleViewDivider(getContext(), LinearLayoutManager.VERTICAL, R.drawable.message_recyclerview_divider));

        mPostAdapter = new QuickPostListAdapter(getContext(),new ArrayList<Post>());
        mPostListView.setAdapter(mPostAdapter);
        mPostAdapter.setOnItemClickListener(this);
        mPostAdapter.setOnItemChildClickListener(this);
        mPostAdapter.setAutoLoadMoreSize(1);
        mPostAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                if(mTotalCount==-1){
                    BmobQuery<Post> query = new BmobQuery<>();
                    query.count(getContext(),Post.class,new CountListener() {
                        @Override
                        public void onSuccess(int i) {
                            mTotalCount = i;
                            loadMore();
                        }

                        @Override
                        public void onFailure(int i, String s) {

                        }
                    });
                }else{
                    loadMore();
                }
            }
        }, mPostListView);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        refresh();
        return rootView;
    }

    private void refresh(){
        mCurrentCount = 0;
        queryPosts(new FindListener<Post>() {
            @Override
            public void onSuccess(List<Post> list) {
                mPostAdapter.getData().clear();
                mPostAdapter.addData(list);
                mCurrentCount = mPostAdapter.getData().size();
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onError(int i, String s) {
                mPostAdapter.loadMoreFail();
                Alerter.create(getActivity())
                        .setTitle("刷新失败")
                        .setText(s)
                        .show();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        //顺便刷新一下mTotalCount
        BmobQuery<Post> query = new BmobQuery<>();
        query.count(getContext(),Post.class,new CountListener() {
            @Override
            public void onSuccess(int i) {
                mTotalCount = i;
            }

            @Override
            public void onFailure(int i, String s) {

            }
        });
    }

    private void loadMore() {
        if(mCurrentCount >= mTotalCount){
            mPostAdapter.loadMoreEnd();//数据全部加载完毕
        }else{
            queryPosts(new FindListener<Post>() {
                @Override
                public void onSuccess(List<Post> list) {
                    mPostAdapter.addData(list);
                    mCurrentCount = mPostAdapter.getData().size();
                    mPostAdapter.loadMoreComplete();
                }

                @Override
                public void onError(int i, String s) {
                    mPostAdapter.loadMoreFail();
                    Alerter.create(getActivity())
                            .setTitle("加载失败")
                            .setText(s)
                            .show();
                    mPostAdapter.loadMoreFail();
                }
            });
        }
    }

    private void queryPosts(FindListener<Post> listener){
        BmobQuery<Post> query = new BmobQuery<>();
        query.setSkip(mCurrentCount);
        query.setLimit(mCountEveryTime);
        query.order("-time");
        query.include("author,organization");
        query.findObjects(getContext(), listener);
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        Organization org = mPostAdapter.getData().get(position).getOrganization();
        if(org == null){
            return;
        }
        Intent intent = new Intent(getContext(),OrganizationDetailActivity.class);
        intent.putExtra("organization",org);
        startActivity(intent);
    }
}
