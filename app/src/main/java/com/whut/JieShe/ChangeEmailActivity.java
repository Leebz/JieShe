package com.whut.JieShe;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tapadoo.alerter.Alerter;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.UpdateListener;

public class ChangeEmailActivity extends AppCompatActivity {

    private EditText mCurrentEmail;
    private EditText mNewEmail;
    private EditText mRepeatEmail;
    private Button mSubmitEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email);
        mCurrentEmail = (EditText)findViewById(R.id.text_current_email);
        mNewEmail = (EditText)findViewById(R.id.text_new_email);
        mRepeatEmail = (EditText)findViewById(R.id.text_repeat_email);
        mSubmitEmail = (Button)findViewById(R.id.btn_submit_email);

        mSubmitEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentEmail = mCurrentEmail.getText().toString();
                String newEmail = mNewEmail.getText().toString();
                String repeatEmail = mRepeatEmail.getText().toString();
                if(currentEmail.equals("")){
                    Toast.makeText(ChangeEmailActivity.this, "请输入原邮箱", Toast.LENGTH_SHORT).show();
                }
                else if(newEmail.equals("")){
                    Toast.makeText(ChangeEmailActivity.this, "请输入新邮箱", Toast.LENGTH_SHORT).show();
                }
                else if(repeatEmail.equals("")){
                    Toast.makeText(ChangeEmailActivity.this, "请重复新邮箱", Toast.LENGTH_SHORT).show();
                }
                else if(newEmail.length()<5){
                    Toast.makeText(ChangeEmailActivity.this, "邮箱长度过短哦", Toast.LENGTH_SHORT).show();
                }
                else if(newEmail.equals(repeatEmail)==false){
                    Toast.makeText(ChangeEmailActivity.this, "两次输入的邮箱不一致", Toast.LENGTH_SHORT).show();
                }
                else{
                    BmobUser currentUser = BmobUser.getCurrentUser(ChangeEmailActivity.this);
                    String storedemail = currentUser.getEmail();

                    if(storedemail.equals(currentEmail)){
                        currentUser.setEmail(newEmail);
                        currentUser.update(ChangeEmailActivity.this, currentUser.getObjectId(), new UpdateListener() {
                            @Override
                            public void onSuccess() {
                                Alerter.create(ChangeEmailActivity.this)
                                        .setTitle("修改邮箱")
                                        .setText("修改邮箱成功")
                                        .setIcon(R.drawable.alerter_ic_face)
                                        .show();
                            }

                            @Override
                            public void onFailure(int i, String s) {

                            }
                        });
                        Toast.makeText(ChangeEmailActivity.this, "修改邮箱成功", Toast.LENGTH_SHORT).show();

                    }
                    else{
                        Toast.makeText(ChangeEmailActivity.this, "原邮箱错误", Toast.LENGTH_SHORT).show();

                    }

                }




            }
        });
    }
}
