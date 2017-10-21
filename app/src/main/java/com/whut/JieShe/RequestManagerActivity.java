package com.whut.JieShe;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.whut.JieShe.adapter.RequestListAdapter;
import com.whut.JieShe.bean.MyUser;
import com.whut.JieShe.bean.Organization;
import com.whut.JieShe.bean.Post;
import com.whut.JieShe.bean.RequestInfo;
import com.whut.JieShe.view.RecycleViewDivider;
import com.tapadoo.alerter.Alerter;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.UpdateListener;

import static java.lang.Thread.sleep;

public class RequestManagerActivity extends AppCompatActivity  {

    private RecyclerView mRecyclerView;
    private RequestListAdapter mReqAdapter;
    private FloatingActionButton mAcceptAll;
    private FloatingActionButton mRefuseAll;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_manager);

        setTitle("用户请求管理");
        mRecyclerView = (RecyclerView)findViewById(R.id.request_info_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(RequestManagerActivity.this));
        mRecyclerView.addItemDecoration(new RecycleViewDivider(RequestManagerActivity.this,LinearLayoutManager.VERTICAL,R.drawable.message_recyclerview_divider));


        mReqAdapter = new RequestListAdapter(RequestManagerActivity.this, new ArrayList<RequestInfo>());
        mRecyclerView.setAdapter(mReqAdapter);

        getActivities();

        mAcceptAll = (FloatingActionButton)findViewById(R.id.btn_accept_all);
        mRefuseAll = (FloatingActionButton)findViewById(R.id.btn_refuse_all);

        mAcceptAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processAll(1);

            }
        });

        mRefuseAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processAll(0);

            }
        });

        mReqAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(final BaseQuickAdapter adapter, View view, final int position) {
                if(view.getId()==R.id.text_request_username){
                    RequestInfo info = (RequestInfo) adapter.getData().get(position);
                    BmobUser user = info.getUser();
                    Intent intent = new Intent(RequestManagerActivity.this,UserDetailInfoActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("user",user);
                    intent.putExtras(bundle);
                    startActivity(intent);

                }
                if(view.getId()==R.id.text_request_activity){
                    RequestInfo info = (RequestInfo)adapter.getData().get(position);
                    Post post = info.getPost();
                    Intent intent = new Intent(RequestManagerActivity.this,PostDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("post",post);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }

                if(view.getId()==R.id.btn_accept_request||view.getId()==R.id.btn_refuse_requst){
                    processReq(adapter,view,position);
                }


            }
        });


    }
    /*

        查到当前管理员管理的社团包含的所有活动
     */

    public void getActivities(){
        //获取当前管理员管理的社团
        BmobUser user = BmobUser.getCurrentUser(RequestManagerActivity.this);
        String userid = user.getObjectId();
        BmobQuery<Organization> query = new BmobQuery<>();
        query.addWhereEqualTo("manager",userid);
        query.findObjects(RequestManagerActivity.this, new FindListener<Organization>() {
            @Override
            public void onSuccess(List<Organization> list) {
                if(list.isEmpty()){
                    return;
                }
                Organization organization = list.get(0);
                //获取社团的所有活动
                BmobQuery<Post> activity_query = new BmobQuery<>();
                activity_query.addWhereEqualTo("organization",organization);
                activity_query.findObjects(RequestManagerActivity.this, new FindListener<Post>() {
                    @Override
                    public void onSuccess(List<Post> list) {
                        getReqList(list);
                    }

                    @Override
                    public void onError(int i, String s) {
                        Alerter.create(RequestManagerActivity.this)
                                .setTitle("错误")
                                .setText("获取社团活动信息失败")
                                .show();
                    }
                });


            }

            @Override
            public void onError(int i, String s) {
                Alerter.create(RequestManagerActivity.this)
                        .setTitle("错误")
                        .setText("获取管理员管理信息失败")
                        .show();

            }
        });

    }

    public void processReq(BaseQuickAdapter adapter , View view, final int position){
        RequestInfo info = (RequestInfo) mReqAdapter.getData().get(position);
        MyUser user = info.getUser();
        Post post = info.getPost();
        String userid = user.getObjectId();
        String postid = post.getObjectId();
        List<String> request;
        List<String> join;
        //判断是否为空表,否则会出错
        if(post.getRequestJoinUsers()==null){
            request = new ArrayList<String>();
        }
        else{
            request = post.getRequestJoinUsers();
        }

        if(post.getJoinedUsers()==null){
            join = new ArrayList<String>();
        }
        else{
            join = post.getJoinedUsers();
        }
        request.remove(userid);
        post.setRequestJoinUsers(request);

        if(view.getId()==R.id.btn_accept_request){
            join.add(userid);
            post.setJoinedUsers(join);
        }
        post.update(RequestManagerActivity.this, postid, new UpdateListener() {
            @Override
            public void onSuccess() {
                mReqAdapter.remove(position);
                mReqAdapter.notifyDataSetChanged();

            }

            @Override
            public void onFailure(int i, String s) {



            }
        });
    }

    public void getReqList(final List<Post> list){
        int activity_num = list.size();
        for(int i=0;i<activity_num;i++){
            if(list.get(i).getRequestJoinUsers()==null){
                continue;
            }
            if(list.get(i).getRequestJoinUsers().size()!=0){
                List<String> useridlist = list.get(i).getRequestJoinUsers();
                //依次获取用户对象

                for(int j=0;j<useridlist.size();j++){
                    BmobQuery<MyUser> query = new BmobQuery<>();
                    final int finalI = i;
                    query.getObject(this, useridlist.get(j), new GetListener<MyUser>() {
                        @Override
                        public void onSuccess(MyUser myUser) {
                            //将username和post拼装add进List
                            RequestInfo reqinfo = new RequestInfo();
                            reqinfo.setUser(myUser);
                            reqinfo.setPost(list.get(finalI));
                            mReqAdapter.addData(reqinfo);
                        }

                        @Override
                        public void onFailure(int i, String s) {

                        }
                    });

                }
            }

        }
    }

    /*

    处理一键同意或一键拒绝
    type==1同意
     */

    public void processAll(final int type){
        //获取当前管理员管理的社团
        BmobUser user = BmobUser.getCurrentUser(RequestManagerActivity.this);
        String userid = user.getObjectId();
        BmobQuery<Organization> query = new BmobQuery<>();
        query.addWhereEqualTo("manager",userid);
        query.findObjects(RequestManagerActivity.this, new FindListener<Organization>() {
            @Override
            public void onSuccess(List<Organization> list) {
                if(list.isEmpty()){
                    return;
                }
                Organization organization = list.get(0);
                //获取社团的所有活动
                BmobQuery<Post> activity_query = new BmobQuery<>();
                activity_query.addWhereEqualTo("organization",organization);
                activity_query.findObjects(RequestManagerActivity.this, new FindListener<Post>() {
                    @Override
                    public void onSuccess(List<Post> list) {
                        //获取社团活动数
                        int num = list==null?0:list.size();
                        for(int i=0;i<num;i++){

                            if(list.get(i).getRequestJoinUsers().size()!=0){
                                Post post = list.get(i);
                                //获取请求人员list
                                List<String> source = list.get(i).getRequestJoinUsers();
                                if(type==1){
                                    //获取已加入的人员list
                                    List<String> target = list.get(i).getJoinedUsers();
                                    if(target==null){
                                        target = new ArrayList<String>();
                                    }
                                    //加入目标列
                                    target.addAll(source);
                                    //更新数据库
                                    post.setJoinedUsers(target);
                                }
                                source.clear();
                                //更新数据库
                                post.setRequestJoinUsers(source);
                                post.update(RequestManagerActivity.this, new UpdateListener() {
                                    @Override
                                    public void onSuccess() {
                                        mReqAdapter.setNewData(null);
                                        mReqAdapter.notifyDataSetChanged();


                                    }

                                    @Override
                                    public void onFailure(int i, String s) {

                                    }
                                });





                            }
                        }
                    }

                    @Override
                    public void onError(int i, String s) {
                        Alerter.create(RequestManagerActivity.this)
                                .setTitle("错误")
                                .setText("获取社团活动信息失败")
                                .show();
                    }
                });


            }

            @Override
            public void onError(int i, String s) {
                Alerter.create(RequestManagerActivity.this)
                        .setTitle("错误")
                        .setText("获取管理员管理信息失败")
                        .show();

            }
        });
    }

}
