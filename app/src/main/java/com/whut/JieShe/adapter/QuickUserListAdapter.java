package com.whut.JieShe.adapter;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.whut.JieShe.R;
import com.whut.JieShe.bean.MyUser;

import java.util.List;

public class QuickUserListAdapter extends BaseQuickAdapter<MyUser, BaseViewHolder> {

    private Context mContext;

    public QuickUserListAdapter(Context context, List<MyUser> myUsers) {
        super(R.layout.user_list_item, myUsers);
        mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder viewHolder, MyUser user) {


        viewHolder.setText(R.id.user_name, user.getUsername());
        if(user.getSignature()!=null){
            viewHolder.setText(R.id.user_signature,user.getSignature());
        }else{
            viewHolder.setText(R.id.user_signature,"这个家伙很懒，什么也没有留下。");
        }

        Glide.with(mContext)
                .load(user.getAvatarUrl())
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .crossFade()
                .into((ImageView) viewHolder.getView(R.id.avatar));
    }
}