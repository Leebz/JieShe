package com.whut.JieShe.adapter;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.whut.JieShe.R;
import com.whut.JieShe.bean.Organization;
import com.whut.JieShe.utils.TimeUtils;

import java.util.List;

public class QuickOrganizationAdapter extends BaseQuickAdapter<Organization, BaseViewHolder> {

    private Context mContext;

    public QuickOrganizationAdapter(Context context, List<Organization> organizations) {
        super(R.layout.organization_list_item, organizations);
        mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder viewHolder, Organization organization) {
        viewHolder.setText(R.id.organization_title, organization.getName())
                .setText(R.id.organization_item_introduce, organization.getIntroduction())
                .setText(R.id.organization_hot, "活动参与:"+organization.getTotalJoinedUser()+"人次")
                .setText(R.id.organization_recent_activity_time,"最近活动:"+ TimeUtils.getDayAndTimeName(organization.getRecentActivityTime()));
        Glide.with(mContext)
                .load(organization.getLogoUrl())
                .centerCrop()
                .placeholder(R.drawable.default_logo)
                .error(R.drawable.default_logo)
                .crossFade()
                .into((ImageView) viewHolder.getView(R.id.organization_logo));
    }
}