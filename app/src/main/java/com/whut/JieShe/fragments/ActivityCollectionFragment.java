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
import com.whut.JieShe.bean.MyUser;
import com.whut.JieShe.bean.Post;
import com.tapadoo.alerter.Alerter;

import java.util.ArrayList;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobQueryResult;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.SQLQueryListener;

public class ActivityCollectionFragment extends Fragment implements BaseQuickAdapter.OnItemClickListener {

    private OnFragmentInteractionListener mListener;

    private RecyclerView mMyActivityList;
    private SwipeRefreshLayout mRefreshLayout;
    private QuickPostListAdapter mPostAdapter;
    private BmobUser mUser;
    private MyUser mMyUser;

    public ActivityCollectionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     */
    // TODO: Rename and change types and number of parameters
    public static ActivityCollectionFragment newInstance(BmobUser user) {
        ActivityCollectionFragment fragment = new ActivityCollectionFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_activity_collection, container, false);

        mRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        mMyActivityList = (RecyclerView) rootView.findViewById(R.id.my_activity_collection_list);
        mMyActivityList.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        mPostAdapter = new QuickPostListAdapter(getContext(),new ArrayList<Post>());
        mMyActivityList.setAdapter(mPostAdapter);
        mPostAdapter.setOnItemClickListener(this);

        refresh();

        return rootView;
    }

    private void refresh(){
        BmobQuery<MyUser> query = new BmobQuery<MyUser>();
        query.getObject(getContext(), mUser.getObjectId(), new GetListener<MyUser>() {
            @Override
            public void onSuccess(MyUser myUser) {
                mMyUser = myUser;
                loadData();
            }

            @Override
            public void onFailure(int i, String s) {
                Alerter.create(getActivity()).setTitle("获取信息失败").setText(s).show();
            }
        });
    }

    private void loadData() {
        if(mMyUser.getPostCollection()==null||mMyUser.getPostCollection().size()==0){
            return;
        }
        BmobQuery<Post> query = new BmobQuery<>();
        StringBuffer sb = new StringBuffer();
        sb.append("select * from Post where ");
        for(String id:mMyUser.getPostCollection()){
            sb.append("objectId='");
            sb.append(id);
            sb.append("' ");
            sb.append("or ");
        }
        sb.delete(sb.length()-4,sb.length()-1);

        query.setSQL(sb.toString());
        query.doSQLQuery(getContext(), new SQLQueryListener<Post>(){
            @Override
            public void done(BmobQueryResult<Post> bmobQueryResult, BmobException e) {
                if(e ==null){
                    mPostAdapter.getData().clear();
                    mPostAdapter.addData(bmobQueryResult.getResults());
                    mRefreshLayout.setRefreshing(false);
                }else{
                    Alerter.create(getActivity())
                            .setTitle("刷新失败")
                            .setText(e.getMessage())
                            .show();
                    mRefreshLayout.setRefreshing(false);
                }
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
