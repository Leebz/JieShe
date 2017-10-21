package com.whut.JieShe;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.whut.JieShe.adapter.QuickCommentListAdapter;
import com.whut.JieShe.bean.Comment;
import com.tapadoo.alerter.Alerter;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;

public class MyDynamicsActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mRefreshLayout;
    private QuickCommentListAdapter mCommentListAdapter;

    private BmobUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_dynamics);

        Bundle bundle = getIntent().getExtras();
        mUser = (BmobUser) bundle.get("user");
        BmobUser currentUser = BmobUser.getCurrentUser(this);
        if(!mUser.getObjectId().equals(currentUser.getObjectId())){
            setTitle("他的动态");
        }else{
            setTitle("我的动态");
        }

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });
        mRecyclerView = (RecyclerView)findViewById(R.id.my_dynamic_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mCommentListAdapter = new QuickCommentListAdapter(this,new ArrayList<Comment>());
        mRecyclerView.setAdapter(mCommentListAdapter);

        loadData();
    }

    private void loadData() {
        BmobQuery<Comment> query = new BmobQuery<>();
        query.include("author,comment,comment.author");
        query.order("-time");
        query.addWhereEqualTo("author",mUser);
        query.findObjects(this, new FindListener<Comment>() {
            @Override
            public void onSuccess(List<Comment> list) {
                mCommentListAdapter.getData().clear();
                mCommentListAdapter.addData(list);
                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onError(int i, String s) {
                Alerter.create(MyDynamicsActivity.this)
                        .setTitle("获取信息失败")
                        .setText(s)
                        .show();
                mRefreshLayout.setRefreshing(false);
            }
        });
    }
}
