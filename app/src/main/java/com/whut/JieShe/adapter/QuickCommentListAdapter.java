package com.whut.JieShe.adapter;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.whut.JieShe.R;
import com.whut.JieShe.bean.Comment;
import com.whut.JieShe.utils.TimeUtils;

import java.util.List;

import cn.bmob.v3.BmobUser;

public class QuickCommentListAdapter extends BaseQuickAdapter<Comment, BaseViewHolder> {

    private Context mContext;

    public QuickCommentListAdapter(Context context, List<Comment> comments) {
        super(R.layout.post_comment_list_item, comments);
        mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder viewHolder, Comment comment) {
        viewHolder.setText(R.id.comment_author, comment.getAuthor().getUsername());
        viewHolder.addOnClickListener(R.id.avatar);
        Comment parent = comment.getComment();
        if(parent==null){
            viewHolder.setText(R.id.comment_content, comment.getContent());
            viewHolder.getView(R.id.parent_comment_container).setVisibility(View.GONE);
        }else {
            viewHolder.getView(R.id.parent_comment_container).setVisibility(View.VISIBLE);
            BmobUser parentAuthor = parent.getAuthor();
            String parentContent = parent.getContent();
            viewHolder.setText(R.id.parent_comment_author, parentAuthor.getUsername())
                    .setText(R.id.parent_comment_content,parentContent);
            String html = "回复"+"<font color='#58B2DC'>"+parentAuthor.getUsername()+"</font>:"+comment.getContent();
            viewHolder.setText(R.id.comment_content,Html.fromHtml(html));

        }
        viewHolder.setText(R.id.comment_time,TimeUtils.getDayAndTimeName(comment.getTime()));
        Glide.with(mContext)
                .load(comment.getAuthor().getAvatarUrl())
                .centerCrop()
                .placeholder(R.mipmap.ic_launcher)
                .crossFade()
                .into((ImageView) viewHolder.getView(R.id.avatar));
    }
}