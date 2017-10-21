package com.whut.JieShe;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.whut.JieShe.bean.Organization;
import com.tapadoo.alerter.Alerter;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;


public class AdminActivity extends AppCompatActivity implements BaseQuickAdapter.OnItemClickListener {

    private RecyclerView mRecyclerView;
    private List<String> mTypeName;
    private ActionAdapter mAdapter;
    private Organization mOrganization;
    private ImageView mOrganizationLogoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("社团管理");

        initData();

        mRecyclerView = (RecyclerView)findViewById(R.id.group_detail_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter = new ActionAdapter(mTypeName));
        mAdapter.setOnItemClickListener(this);
        mOrganizationLogoView = (ImageView) findViewById(R.id.organization_logo);
    }

    protected void initData(){
        BmobUser user = BmobUser.getCurrentUser(this);
        BmobQuery<Organization> query = new BmobQuery<>();
        query.addWhereEqualTo("manager",user);
        query.findObjects(this, new FindListener<Organization>() {
            @Override
            public void onSuccess(List<Organization> list) {
                if(list.size()==0){
                    Alerter.create(AdminActivity.this)
                            .setTitle("错误")
                            .setText("你根本不是管理员!")
                            .show();
                }else{
                    mOrganization = list.get(0);
                    AdminActivity.this.setTitle(mOrganization.getName());
                    //加载LOGO
                    Glide.with(AdminActivity.this)
                            .load(mOrganization.getLogoUrl())
                            .placeholder(R.drawable.default_logo)
                            .error(R.drawable.default_logo)
                            .crossFade()
                            .into(mOrganizationLogoView);
                }
            }

            @Override
            public void onError(int i, String s) {
                Alerter.create(AdminActivity.this)
                        .setTitle("错误")
                        .setText(s)
                        .show();
            }
        });

        mTypeName = new ArrayList<>();
        mTypeName.add("社团活动管理");
        mTypeName.add("请求管理");
        mTypeName.add("社团基本信息管理");
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        if(mOrganization == null){
            return;
        }
        String which = mAdapter.getItem(position);
        if("社团活动管理".equals(which)){
            Intent intent = new Intent(AdminActivity.this,GroupActivity.class);
            intent.putExtra("organization",mOrganization);
            startActivity(intent);
        }
        if("请求管理".equals(which)){
            Intent intent = new Intent(AdminActivity.this,RequestManagerActivity.class);
            intent.putExtra("organization",mOrganization);
            startActivity(intent);
        }
        if("社团基本信息管理".equals(which)){
            Intent intent = new Intent(AdminActivity.this,GroupInformationActivity.class);
            intent.putExtra("organization",mOrganization);
            startActivity(intent);
        }
    }

    private class ActionAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

        public ActionAdapter(List<String> data){
            super(R.layout.user_detail_item, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, String item) {
            helper.setText(R.id.item_type,item)
                    .setText(R.id.type_value,"");

        }
    }
}
