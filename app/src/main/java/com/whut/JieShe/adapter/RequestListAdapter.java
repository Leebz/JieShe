package com.whut.JieShe.adapter;

import android.content.Context;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.whut.JieShe.R;
import com.whut.JieShe.bean.RequestInfo;

import java.util.List;

/**
 * Created by 42910 on 2017/5/7.
 */

public class RequestListAdapter extends BaseQuickAdapter<RequestInfo,BaseViewHolder>{

    private Context mContext;
    public RequestListAdapter(Context context, List<RequestInfo> reqlist) {
        super(R.layout.request_item,reqlist);
        mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder viewHolder, RequestInfo item) {

        viewHolder.setText(R.id.text_request_username,item.getUser().getUsername())
                .setText(R.id.text_request_activity,item.getPost().getTitle())
                .addOnClickListener(R.id.text_request_username)
                .addOnClickListener(R.id.text_request_activity)
                .addOnClickListener(R.id.btn_accept_request)
                .addOnClickListener(R.id.btn_refuse_requst);

    }
}
