package com.whut.JieShe;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.UpdateListener;

public class ChangePswActivity extends AppCompatActivity {
    private EditText mCurrentPsw;
    private EditText mNewPsw;
    private EditText mRepeatPsw;
    private Button mSubmitPsw;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_psw);
        mCurrentPsw = (EditText)findViewById(R.id.text_current_psw);
        mNewPsw = (EditText)findViewById(R.id.text_new_psw);
        mRepeatPsw = (EditText)findViewById(R.id.text_repeat_psw);
        mSubmitPsw = (Button)findViewById(R.id.btn_submit_psw);

        mSubmitPsw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentPsw = mCurrentPsw.getText().toString();
                String newPsw = mNewPsw.getText().toString();
                String repeatPsw = mRepeatPsw.getText().toString();
                if(currentPsw.equals("")){
                    Toast.makeText(ChangePswActivity.this, "请输入原密码", Toast.LENGTH_SHORT).show();
                }
                else if(newPsw.equals("")){
                    Toast.makeText(ChangePswActivity.this, "请输入新密码", Toast.LENGTH_SHORT).show();
                }
                else if(repeatPsw.equals("")){
                    Toast.makeText(ChangePswActivity.this, "请重复新密码", Toast.LENGTH_SHORT).show();
                }
                else if(newPsw.length()<5){
                    Toast.makeText(ChangePswActivity.this, "密码长度过短哦", Toast.LENGTH_SHORT).show();
                }
                else if(newPsw.equals(repeatPsw)==false){
                    Toast.makeText(ChangePswActivity.this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                }
                else{
                    BmobUser.updateCurrentUserPassword(ChangePswActivity.this,currentPsw,newPsw,new UpdateListener(){
                        @Override
                        public void onSuccess() {
                            Toast.makeText(ChangePswActivity.this, "修改成功！请下次使用新密码登录", Toast.LENGTH_SHORT).show();
                            /*
                            此处添加退出应用的代码
                             */

                        }
                        @Override
                        public void onFailure(int i, String s) {
                            Toast.makeText(ChangePswActivity.this, "原密码错误", Toast.LENGTH_SHORT).show();
                        }


                    });
                }




            }
        });
    }
}
