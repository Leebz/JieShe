package com.whut.JieShe.adapter;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.whut.JieShe.R;
import com.whut.JieShe.bean.BmobIMMessageWrapper;
import com.whut.JieShe.bean.MyUser;

import java.util.List;

import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.v3.BmobUser;

public class QuickMessageListAdapter extends BaseMultiItemQuickAdapter<BmobIMMessageWrapper, BaseViewHolder> {

    private Context mContext;

    public QuickMessageListAdapter(Context context, List<BmobIMMessageWrapper> messageWrapper) {
        super(messageWrapper);
        addItemType(BmobIMMessageWrapper.SEND, R.layout.message_item_right_item);
        addItemType(BmobIMMessageWrapper.RECEIVE, R.layout.message_item_left_item);
        this.mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder viewHolder, BmobIMMessageWrapper wrapper) {
        switch (viewHolder.getItemViewType()) {
            case BmobIMMessageWrapper.SEND:
                MyUser user = BmobUser.getCurrentUser(mContext, MyUser.class);
                Glide.with(mContext)
                        .load(user.getAvatarUrl())
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .crossFade()
                        .into((ImageView)viewHolder.getView(R.id.avatar));
                viewHolder.setText(R.id.message_text,wrapper.getMessage().getContent());
                viewHolder.addOnClickListener(R.id.avatar);
                break;

            case BmobIMMessageWrapper.RECEIVE:
                BmobIMUserInfo info = BmobIM.getInstance().getUserInfo(wrapper.getMessage().getConversationId());
                Glide.with(mContext)
                        .load(info.getAvatar())
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .crossFade()
                        .into((ImageView)viewHolder.getView(R.id.avatar));
                viewHolder.setText(R.id.message_text,wrapper.getMessage().getContent());
                viewHolder.addOnClickListener(R.id.avatar);
                break;
        }
    }
}