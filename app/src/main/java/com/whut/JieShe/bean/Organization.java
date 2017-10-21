package com.whut.JieShe.bean;

import java.util.List;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobUser;

/**
 * 实体类，一个社团、组织、团队、俱乐部......
 * Created by sukai on 2017/04/21.
 */

public class Organization extends BmobObject{

    public BmobUser getManager() {
        return manager;
    }

    public void setManager(BmobUser manager) {
        this.manager = manager;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public long getTotalJoinedUser() {
        return totalJoinedUser;
    }

    public void setTotalJoinedUser(long totalJoinedUser) {
        this.totalJoinedUser = totalJoinedUser;
    }

    public long getRecentActivityTime() {
        return recentActivityTime;
    }

    public void setRecentActivityTime(long recentActivityTime) {
        this.recentActivityTime = recentActivityTime;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getBackgroundUrl() {
        return backgroundUrl;
    }

    public void setBackgroundUrl(String backgroundUrl) {
        this.backgroundUrl = backgroundUrl;
    }

    private BmobUser manager;      //管理员
    private String name;           //名称
    private String introduction;   //简介
    private List<String> tags;     //标签
    private long totalJoinedUser;   //总参与人次
    private long recentActivityTime;//上次活动时间
    private String logoUrl;         //logo的url
    private String backgroundUrl;   //背景的url

}
