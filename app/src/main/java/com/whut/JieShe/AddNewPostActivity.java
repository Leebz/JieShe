package com.whut.JieShe;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.whut.JieShe.bean.Organization;
import com.whut.JieShe.bean.Post;
import com.whut.JieShe.utils.TimeUtils;
import com.tapadoo.alerter.Alerter;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class AddNewPostActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener,TimePickerDialog.OnTimeSetListener{

    private Organization organ;
    private ImageView mSetActivityTimeButton;
    private ImageView mSetActivityAddressButton;
    private ImageView mSetIsCheckButton;
    private ImageView mClearPostButton;
    private ImageView mGetHelpButton;
    private ImageView mSubmitButton;
    private EditText mPostTitle;
    private EditText mPostContent;
    private boolean mIsCheck ;
    private String mAddress="";
    private Post mPost;
    private boolean mIsModify;
    private long mActivityTime = 0;
    private long mTime;
    private TextView mTextAddress;
    private TextView mTextActivityTime;
    private String mDateStr;
    private TextView mTextNeedCheck;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("发布活动");
        setContentView(R.layout.activity_add_new_post);

        Bundle bundle = getIntent().getExtras();

        //判断是否进行修改
        if(bundle==null){
            mIsModify = false;
        }
        else{
            mPost = (Post)bundle.getSerializable("post");
            mIsModify = true;
        }

        mTextNeedCheck = (TextView)findViewById(R.id.text_need_check);
        mTextAddress = (TextView)findViewById(R.id.text_post_address);
        mTextActivityTime = (TextView)findViewById(R.id.text_post_time);

        mPostTitle = (EditText)findViewById(R.id.post_title);
        mPostContent = (EditText)findViewById(R.id.post_content);


        mSetActivityTimeButton = (ImageView)findViewById(R.id.set_activity_time);
        mSetActivityTimeButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN) {
                    mSetActivityTimeButton.setBackgroundColor(Color.GRAY);
                    return true;
                }

                else if(event.getAction()==MotionEvent.ACTION_UP){
                    Calendar now = Calendar.getInstance();
                    TimePickerDialog tpd = TimePickerDialog.newInstance(
                            AddNewPostActivity.this,
                            now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE),
                            true
                    );
                    tpd.setVersion(TimePickerDialog.Version.VERSION_2);
                    tpd.show(getFragmentManager(),"TimePicker");


                    DatePickerDialog dpd = DatePickerDialog.newInstance(
                            AddNewPostActivity.this,
                            now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH)
                    );
                    dpd.setVersion(DatePickerDialog.Version.VERSION_2);
                    dpd.show(getFragmentManager(),"DatePicker");
                    mSetActivityTimeButton.setBackgroundColor(Color.WHITE);
                    return true;
                }

                return false;
            }

        });

        mSetActivityAddressButton = (ImageView)findViewById(R.id.set_activity_address);
        mSetActivityAddressButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    mSetActivityAddressButton.setBackgroundColor(Color.GRAY);
                    return true;
                }
                else if(event.getAction()==MotionEvent.ACTION_UP){
                    mSetActivityAddressButton.setBackgroundColor(Color.WHITE);
                    new MaterialDialog.Builder(AddNewPostActivity.this)
                            .title("编辑活动地点")
                            .content("请输入活动的地点")
                            .inputType(InputType.TYPE_CLASS_TEXT)
                            .input("", mIsModify?mPost.getActivityAddress():"", new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                    if(TextUtils.isEmpty(input)){
                                        return;
                                    }
                                    mAddress = input.toString();
                                    mTextAddress.setText(mAddress);
                                }
                            }).show();
                    return true;
                }
                return false;
            }
        });

        mSetIsCheckButton = (ImageView)findViewById(R.id.set_need_check);
        mSetIsCheckButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    mSetIsCheckButton.setBackgroundColor(Color.GRAY);
                    return true;
                }
                else if(event.getAction()==MotionEvent.ACTION_UP){
                    mSetIsCheckButton.setBackgroundColor(Color.WHITE);
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddNewPostActivity.this);
                    final String[] choice={"请求无需验证","请求需要验证"};
                    builder.setItems(choice, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(which==0){
                                mIsCheck = false;
                                mTextNeedCheck.setText(choice[0]);

                            }
                            else if (which==1){
                                mIsCheck = true;
                                mTextNeedCheck.setText(choice[1]);
                            }
                        }
                    });

                    builder.setTitle("请求验证选项");

                    builder.show();
                    return true;
                }

                return false;
            }

        });


        mClearPostButton = (ImageView)findViewById(R.id.clear_post);
        mClearPostButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    mClearPostButton.setBackgroundColor(Color.GRAY);
                    return true;
                }
                else if(event.getAction()==MotionEvent.ACTION_UP){
                    mClearPostButton.setBackgroundColor(Color.WHITE);
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddNewPostActivity.this);
                    builder.setMessage("确认清除所有内容吗?");
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
                            mPostTitle.setText("");
                            mPostContent.setText("");
                        }

                    });
                    builder.create().show();
                    return true;

                }
                return false;
            }
        });


        mGetHelpButton = (ImageView)findViewById(R.id.get_markdown_help);
        mGetHelpButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    mGetHelpButton.setBackgroundColor(Color.GRAY);
                    return true;
                }
                else if(event.getAction()==MotionEvent.ACTION_UP){
                    mGetHelpButton.setBackgroundColor(Color.WHITE);
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddNewPostActivity.this);
                    builder.setMessage("将要打开MarkDown帮助网站，是否打开?");
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
                            Intent intent = new Intent();
                            intent.setAction("android.intent.action.VIEW");
                            Uri contnet_url = Uri.parse("http://www.cnblogs.com/math/p/se-tools-001.html");
                            intent.setData(contnet_url);
                            startActivity(intent);
                        }

                    });
                    builder.create().show();
                    return true;
                }
                return false;
            }
        });



        mSubmitButton = (ImageView)findViewById(R.id.submit_post_button);
        mSubmitButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    mSubmitButton.setBackgroundColor(Color.GRAY);
                    return true;
                }
                if (event.getAction()==MotionEvent.ACTION_UP){
                    mSubmitButton.setBackgroundColor(Color.WHITE);
                    if(checkPostValid()){
                        if(mIsModify){
                            //...对修改的数据实现Update方法
                            modifyPost(mPost);
                        }
                        else{
                            addPost();
                        }

                    }
                    else{
                        Alerter.create(AddNewPostActivity.this)
                                .setTitle("提示")
                                .setText("活动内容不完整哦，请检查输入内容和下方设置")
                                .show();
                    }
                    return true;
                }
                return false;
            }
        });



        if(mIsModify==true){
            reStorePage(mPost);
        }

    }




    public boolean checkPostValid(){
        if(mPostTitle.getText().toString().equals("")||mPostContent.getText().toString().equals("")||mAddress.equals("")||mActivityTime==0){
            return false;
        }
        return true;
    }


    public void addPost(){

        final BmobUser user = BmobUser.getCurrentUser(AddNewPostActivity.this);
        String userid = user.getObjectId();
        BmobQuery<Organization> query = new BmobQuery<>();
        query.addWhereEqualTo("manager",userid);
        query.findObjects(AddNewPostActivity.this, new FindListener<Organization>() {
            @Override
            public void onSuccess(List<Organization> list) {
                Post post = new Post();
                post.setTitle(mPostTitle.getText().toString());
                post.setActivityDate(mActivityTime);
                post.setContent(mPostContent.getText().toString());
                post.setNeedCheck(mIsCheck);
                post.setOrganization(list.get(0));
                post.setAuthor(user);
                post.setTime(new Date().getTime());
                post.setActivityAddress(mAddress);
                post.save(AddNewPostActivity.this, new SaveListener() {
                    @Override
                    public void onSuccess() {
                        Intent intent = new Intent(AddNewPostActivity.this,GroupActivity.class);
                        startActivity(intent);
                        System.exit(0);



                    }
                    @Override
                    public void onFailure(int i, String s) {
                        Alerter.create(AddNewPostActivity.this)
                                .setTitle("提示")
                                .setText("添加失败"+s)
                                .show();
                    }
                });
            }

            @Override
            public void onError(int i, String s) {

            }
        });

    }

    public void modifyPost(Post post){
        post.setTitle(mPostTitle.getText().toString());
        post.setContent(mPostContent.getText().toString());
        post.setActivityAddress(mAddress);
        post.setActivityDate(mActivityTime);
        post.setNeedCheck(mIsCheck);
        post.setTime(new Date().getTime());

        post.update(AddNewPostActivity.this, post.getObjectId(), new UpdateListener() {
            @Override
            public void onSuccess() {
                Intent intent = new Intent(AddNewPostActivity.this,GroupActivity.class);
                startActivity(intent);
                System.exit(0);

            }

            @Override
            public void onFailure(int i, String s) {
                Alerter.create(AddNewPostActivity.this)
                        .setTitle("提示")
                        .setText("活动更新失败")
                        .show();

            }
        });

    }

    public void reStorePage(Post post){
        mPostTitle.setText(post.getTitle().toString());
        mPostContent.setText(post.getContent().toString());
        mAddress = post.getActivityAddress();
        mIsCheck = post.isNeedCheck();
        mActivityTime = post.getActivityDate();
        mTime = post.getTime();
        mTextActivityTime.setText(TimeUtils.getDayAndTimeName(post.getActivityDate()));
        mTextAddress.setText(mAddress);
        mTextNeedCheck.setText(post.isNeedCheck()?"请求需要验证":"请求无需验证");
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        //yyyy-MM-dd HH:mm:ss
        String date = year+"-"+(monthOfYear+1)+"-"+dayOfMonth;
        try {
            Toast.makeText(this, "DATE"+ TimeUtils.stringToLong(date,"yyyy-MM-dd"), Toast.LENGTH_LONG).show();
            mDateStr = date;

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        //yyyy-MM-dd HH:mm:ss
        String time = hourOfDay+":"+minute+":"+second;
        try {
            mDateStr  = mDateStr + " " +time;
            mActivityTime = TimeUtils.stringToLong(mDateStr,"yyyy-MM-dd HH:mm:ss");
            mTextActivityTime.setText(mDateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
