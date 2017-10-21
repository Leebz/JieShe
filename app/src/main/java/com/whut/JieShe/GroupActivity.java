package com.whut.JieShe;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.whut.JieShe.adapter.QuickPostListAdapter;
import com.whut.JieShe.bean.Organization;
import com.whut.JieShe.bean.Post;
import com.whut.JieShe.view.RecycleViewDivider;
import com.tapadoo.alerter.Alerter;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.FindListener;
public class GroupActivity extends AppCompatActivity implements BaseQuickAdapter.OnItemClickListener,BaseQuickAdapter.OnItemLongClickListener {

    private RecyclerView mRecyclerView;
    private FloatingActionButton mAddActivityButton;
    //    private List<Post> mPosts;
    private QuickPostListAdapter mPostAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        setTitle("社团活动管理");
        mAddActivityButton = (FloatingActionButton) findViewById(R.id.add_activity_button);
        mAddActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupActivity.this, AddNewPostActivity.class);
                startActivity(intent);
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.group_post_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(GroupActivity.this));
        mRecyclerView.addItemDecoration(new RecycleViewDivider(GroupActivity.this, LinearLayoutManager.VERTICAL, R.drawable.message_recyclerview_divider));


        mPostAdapter = new QuickPostListAdapter(GroupActivity.this, new ArrayList<Post>());
        mRecyclerView.setAdapter(mPostAdapter);
        mPostAdapter.setOnItemClickListener(this);
        mPostAdapter.setOnItemLongClickListener(this);
//

        queryPosts();
    }


    private void queryPosts() {
        //获取当前管理员管理的社团的ID
        BmobUser user = BmobUser.getCurrentUser(GroupActivity.this);
        String userid = user.getObjectId();
        BmobQuery<Organization> query = new BmobQuery<>();
        query.addWhereEqualTo("manager", userid);
        query.findObjects(GroupActivity.this, new FindListener<Organization>() {
            @Override
            public void onSuccess(List<Organization> list) {

                final Organization organization = list.get(0);

                //获取当前管理员社团的所有的活动
                BmobQuery<Post> activity_query = new BmobQuery<>();
                activity_query.addWhereEqualTo("organization", organization);
                activity_query.findObjects(GroupActivity.this, new FindListener<Post>() {
                    @Override
                    public void onSuccess(List<Post> list) {

                        for (int i=0;i<list.size();i++){
                            if (list.get(i).getOrganization()==null){
                                list.get(i).setOrganization(new Organization());
                            }
                            list.get(i).getOrganization().setName(organization.getName());
                        }
                        mPostAdapter.addData(list);
                    }

                    @Override
                    public void onError(int i, String s) {
                        Alerter.create(GroupActivity.this)
                                .setTitle("错误")
                                .setText("获取社团活动信息失败")
                                .show();
                    }
                });

            }

            @Override
            public void onError(int i, String s) {
                Alerter.create(GroupActivity.this)
                        .setTitle("错误")
                        .setText("获取管理员管理信息失败" + s)
                        .show();

            }
        });


    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        Post post = mPostAdapter.getData().get(position);
        Bundle bundle = new Bundle();
        bundle.putSerializable("post", post);
        Intent intent = new Intent(GroupActivity.this, PostDetailActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(BaseQuickAdapter adapter, final View view, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(GroupActivity.this);
        String[] choice = {"修改", "删除"};
        builder.setItems(choice, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Post post = mPostAdapter.getData().get(position);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("post", post);
                    Intent intent = new Intent(GroupActivity.this, AddNewPostActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);


                }

                if (which == 1) {


                    AlertDialog.Builder builder = new AlertDialog.Builder(GroupActivity.this);
                    builder.setMessage("确认删除吗?");
                    builder.setTitle("提示");

                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            //删除对应的帖子
                            Post deletepost = mPostAdapter.getData().get(position);
                            deletepost.setObjectId(deletepost.getObjectId());
                            deletepost.delete(GroupActivity.this, new DeleteListener() {
                                @Override
                                public void onSuccess() {
                                    mPostAdapter.remove(position);
                                    mPostAdapter.notifyDataSetChanged();
                                    Alerter.create(GroupActivity.this)
                                            .setTitle("提示")
                                            .setText("删除活动成功")
                                            .show();
                                }

                                @Override
                                public void onFailure(int i, String s) {
                                    Alerter.create(GroupActivity.this)
                                            .setTitle("提示")
                                            .setText("删除活动失败")
                                            .show();

                                }
                            });
                        }

                    });

                    builder.create().show();
                }

            }
        });
        builder.show();
        return true;
    }

}