package com.whut.JieShe.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import java.util.Arrays;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link ActivityJoinedOrRequestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ActivityJoinedOrRequestFragment extends Fragment implements BaseQuickAdapter.OnItemClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final int TYPE_ACTIVITY_JOINED = 0;
    public static final int TYPE_ACTIVITY_REQUEST = 1;

    private int type = TYPE_ACTIVITY_JOINED;
    private OnFragmentInteractionListener mListener;

    private RecyclerView mMyActivityList;
    private SwipeRefreshLayout mRefreshLayout;
    private QuickPostListAdapter mPostAdapter;
    private BmobUser mUser;

    public ActivityJoinedOrRequestFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     */
    // TODO: Rename and change types and number of parameters
    public static ActivityJoinedOrRequestFragment newInstance(BmobUser user,int type) {
        ActivityJoinedOrRequestFragment fragment = new ActivityJoinedOrRequestFragment();
        fragment.type = type;
        fragment.mUser = user;
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
        View rootView = inflater.inflate(R.layout.fragment_activity_request_or_joined, container, false);

        mRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });
        mMyActivityList = (RecyclerView) rootView.findViewById(R.id.my_activity_list);
        mMyActivityList.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        mPostAdapter = new QuickPostListAdapter(getContext(),new ArrayList<Post>());
        mMyActivityList.setAdapter(mPostAdapter);
        mPostAdapter.setOnItemClickListener(this);
        loadData();

        return rootView;
    }
    private void loadData() {
        if(mUser==null){
            return;
        }
        BmobQuery<Post> query = new BmobQuery<>();
        String[] users = {mUser.getObjectId()};
        if(type==TYPE_ACTIVITY_JOINED){
            query.addWhereContainsAll("joinedUsers", Arrays.asList(users));
        }else if(type==TYPE_ACTIVITY_REQUEST){
            query.addWhereContainsAll("requestJoinUsers", Arrays.asList(users));
        }

        query.include("author,organization");
        query.findObjects(getContext(), new FindListener<Post>(){
            @Override
            public void onSuccess(List<Post> list) {
                mPostAdapter.getData().clear();
                mPostAdapter.addData(list);
                mPostAdapter.notifyDataSetChanged();
                mRefreshLayout.setRefreshing(false);
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
}
