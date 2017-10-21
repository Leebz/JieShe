package com.whut.JieShe;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;

import cn.bmob.newim.BmobIM;
import cn.bmob.v3.BmobUser;

import static cn.bmob.newim.core.BmobIMClient.getContext;


public class SystemConfigActivity extends AppCompatActivity {
    private BootstrapButton mExitBtn;
    private View mAboutUsBar;
    private View mCheckUpdateBar;
    private View mClearCacheBar;
    private View mChangeEmailBar;
    private View mChangePsdBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_config);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mExitBtn = (BootstrapButton) findViewById(R.id.exit_btn);
        mExitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BmobUser user = BmobUser.getCurrentUser(getContext());
                if(user!=null){
                    BmobIM.getInstance().disConnect();
                    user.logOut(getContext());
                    System.exit(0);
                }
            }
        });
        mAboutUsBar = findViewById(R.id.about_us_bar);
        mAboutUsBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SystemConfigActivity.this,AboutUsActivity.class);
                startActivity(intent);
                return;
            }
        });

        mCheckUpdateBar = findViewById(R.id.check_update_bar);
        mCheckUpdateBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SystemConfigActivity.this,CheckUpdateActivity.class);
                startActivity(intent);
                return;
            }
        });
        mClearCacheBar = findViewById(R.id.clear_cache_bar);
        mClearCacheBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SystemConfigActivity.this).setTitle("系统提示")//设置对话框标题
                        .setMessage("确认清除缓存？")//设置显示的内容
                        .setPositiveButton("确定",new DialogInterface.OnClickListener() {//添加确定按钮

                            //此处加入真正清除缓存代码

                            @Override

                            public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
                                // TODO Auto-generated method stub
                                //定义Toast显示时间属性
                                final int s = Toast.LENGTH_SHORT;
                                final int l = Toast.LENGTH_LONG;
                                //显示Toast显示内容属性
                                final String str = "已清除";
                                Toast t = Toast.makeText(getApplicationContext(), str, s);
                                //显示Toast
                                t.show();
                                return;
                            }

                        }).setNegativeButton("返回",new DialogInterface.OnClickListener() {//添加返回按钮



                    @Override

                    public void onClick(DialogInterface dialog, int which) {//响应事件

                    }

                }).show();//在按键响应事件中显示此对话框
            }
        });

        mChangePsdBar = findViewById(R.id.change_psd_bar);
        mChangePsdBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SystemConfigActivity.this, ChangePswActivity.class);
                startActivity(intent);
            }
        });

        mChangeEmailBar = findViewById(R.id.change_email_bar);
        mChangeEmailBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SystemConfigActivity.this, ChangeEmailActivity.class);
                startActivity(intent);
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
