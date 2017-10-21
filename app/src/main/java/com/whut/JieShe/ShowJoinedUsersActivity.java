package com.whut.JieShe;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.whut.JieShe.bean.MyUser;
import com.whut.JieShe.bean.Post;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.GetListener;

public class ShowJoinedUsersActivity extends AppCompatActivity {

    private List<MyUser> mJoinedUserList;
    private GridView mGridMain;
    private JoinedUsersGridAdapter mGridAdapter;
    private Post mPost;
    private static int mDateSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_joined_users);
        mJoinedUserList = new ArrayList<>();

        Bundle bundle = getIntent().getExtras();
        mPost = (Post)bundle.getSerializable("post");
        if(mPost.getJoinedUsers()!=null){
            mDateSize = mPost.getJoinedUsers().size();
        }
        else{
            mDateSize = 0;
        }
        setTitle("已审核通过成员");



        mGridMain = (GridView)findViewById(R.id.grid_main);
        mGridAdapter = new JoinedUsersGridAdapter();
        mGridMain.setAdapter(mGridAdapter);

        if(mDateSize!=0){
            getJoinedUser(mPost);
        }

        mGridMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MyUser user = mJoinedUserList.get(position);
                Intent intent = new Intent(ShowJoinedUsersActivity.this,UserDetailInfoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("user",user);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

    }

    public void getJoinedUser(Post post){
        List<String> mJoinedUserIdList = post.getJoinedUsers();
        for(int i=0;i<mJoinedUserIdList.size();i++){
            BmobQuery<MyUser> query = new BmobQuery<>();
            query.getObject(ShowJoinedUsersActivity.this, mJoinedUserIdList.get(i), new GetListener<MyUser>() {
                @Override
                public void onSuccess(MyUser myUser) {
                    mJoinedUserList.add(myUser);
                    mGridAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(int i, String s) {

                }
            });

        }
    }

    public String modifyName(MyUser user){
        String name;
        if(user.getUsername().length()>=6){
            name = user.getUsername().substring(0,4)+"...";
        }
        else{
            name = user.getUsername();
        }
        return name;
    }






    class JoinedUsersGridAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mDateSize;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = View.inflate(ShowJoinedUsersActivity.this, R.layout.joined_users_grid_item,null);
            TextView username = (TextView)view.findViewById(R.id.grid_user_name);
            ImageView userimg = (ImageView)view.findViewById(R.id.grid_user_image);
            if(mDateSize!=0 && mJoinedUserList.size()==mDateSize){
                username.setText(modifyName(mJoinedUserList.get(position)));
                Glide.with(ShowJoinedUsersActivity.this)
                        .load(mJoinedUserList.get(position).getAvatarUrl())
                        .placeholder(R.drawable.default_avatar)
                        .crossFade()
                        .into(userimg);
            }


            return view;
        }
    }

}


