package com.whut.JieShe.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.whut.JieShe.R;
import com.whut.JieShe.utils.TimeUtils;
import com.szd.messagebubble.MessageBubbleView;

import java.util.List;

import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMUserInfo;

public class QuickUserMessageAdapter extends BaseQuickAdapter<BmobIMConversation, BaseViewHolder> {

    private Context mContext;

    public QuickUserMessageAdapter(Context context, List<BmobIMConversation> conversations) {
        super(R.layout.recent_friends_list_item, conversations);
        mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder viewHolder, BmobIMConversation item) {
        BmobIMUserInfo info = BmobIM.getInstance().getUserInfo(item.getConversationId());
        item.setConversationTitle(info.getName());
        item.setConversationIcon(info.getAvatar());

        long unread = BmobIM.getInstance().getUnReadCount(item.getConversationId());
        Log.d(TAG, info.getName() + unread);
        MessageBubbleView view = (MessageBubbleView) viewHolder.getView(R.id.unread_msg_count);
        if(unread != 0){
            view.setVisibility(View.VISIBLE);
            view.setNumber(unread+"");
        }else{
            view.setVisibility(View.GONE);
        }

        viewHolder.setText(R.id.user_name, info.getName());

        List<BmobIMMessage> messages = item.getMessages();
        if(messages!=null && messages.size()!=0){
            viewHolder.setText(R.id.preview_message, messages.get(0).getContent());
            viewHolder.setText(R.id.time, TimeUtils.getDayAndTimeName(messages.get(0).getCreateTime()));
        }
        Glide.with(mContext)
                .load(info.getAvatar())
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .crossFade()
                .into((ImageView) viewHolder.getView(R.id.avatar));
    }
}