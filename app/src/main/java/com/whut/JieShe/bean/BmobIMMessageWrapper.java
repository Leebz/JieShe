package com.whut.JieShe.bean;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import cn.bmob.newim.bean.BmobIMMessage;

/**
 * Created by sukaiyi on 2017/05/16.
 */

public class BmobIMMessageWrapper implements MultiItemEntity {

    public static final int SEND = 1;
    public static final int RECEIVE = 2;

    private BmobIMMessage message;
    private int itemType;

    public BmobIMMessageWrapper(){

    }

    public BmobIMMessageWrapper(int type, BmobIMMessage message) {
        this.itemType = type;
        this.message = message;
    }

    @Override
    public int getItemType() {
        return itemType;
    }

    public BmobIMMessage getMessage() {
        return message;
    }

    public void setMessage(BmobIMMessage message) {
        this.message = message;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }
}
