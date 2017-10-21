package com.whut.JieShe.adapter;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.whut.JieShe.R;
import com.whut.JieShe.bean.Organization;
import com.whut.JieShe.bean.Post;
import com.whut.JieShe.utils.MarkdownUtils;
import com.whut.JieShe.utils.TimeUtils;

import java.util.List;

public class QuickPostListAdapter extends BaseQuickAdapter<Post, BaseViewHolder> {

    private Context mContext;

    public QuickPostListAdapter(Context context, List<Post> posts) {
        super(R.layout.quick_post_list_item, posts);
        mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder viewHolder, Post post) {
        String[] data = MarkdownUtils.getConciseMarkdown(post.getContent());
        if(data==null){
            data = new String[]{"",""};
        }
        Organization org = post.getOrganization();
        viewHolder.setText(R.id.come_from_organization, "来自:"+(org==null?"null":org.getName()))
                .addOnClickListener(R.id.come_from_organization)
                .setText(R.id.post_title, post.getTitle())
                .setText(R.id.post_time, TimeUtils.getDayAndTimeName(post.getTime()))
                .setText(R.id.post_item_preview,data[1]);

        Glide.with(mContext)
                .load(data[0])
                .crossFade()
                .into((ImageView) viewHolder.getView(R.id.the_first_img_in_md));
        Glide.with(mContext)
                .load(org!=null?org.getLogoUrl():null)
                .placeholder(R.drawable.default_logo)
                .error(R.drawable.default_logo)
                .crossFade()
                .into((ImageView) viewHolder.getView(R.id.organization_logo));
    }
}