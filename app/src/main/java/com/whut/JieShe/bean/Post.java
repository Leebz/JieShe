package com.whut.JieShe.bean;

import java.util.List;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobUser;

/**
 * 活动帖子
 * Created by sukai on 2017/04/21.
 */

public class Post extends BmobObject {
    private String title;                    //帖子标题
    private String content;                  //帖子内容 （markdown格式）
    private long time;                       //创建时间
    private long activityDate;               //活动时间
    private String activityAddress;          //活动地点
    private BmobUser author;                 //发帖人
    private Organization organization;       //所属的社团
    private List<String> joinedUsers;        //已经参见的人(审核通过)
    private List<String> requestJoinUsers;   //已经报名的人（待审核）
    private boolean needCheck;               //报名是否需要审核

    public Post(){
        needCheck = true;
    }

    public boolean isNeedCheck() {
        return needCheck;
    }

    public void setNeedCheck(boolean needCheck) {
        this.needCheck = needCheck;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Organization getOrganization() {
        return organization;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public BmobUser getAuthor() {
        return author;
    }

    public void setAuthor(BmobUser author) {
        this.author = author;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public List<String> getJoinedUsers() {
        return joinedUsers;
    }

    public void setJoinedUsers(List<String> joinedUsers) {
        this.joinedUsers = joinedUsers;
    }


    public List<String> getRequestJoinUsers() {
        return requestJoinUsers;
    }

    public void setRequestJoinUsers(List<String> requestJoinUsers) {
        this.requestJoinUsers = requestJoinUsers;
    }

    public long getActivityDate() {
        return activityDate;
    }

    public void setActivityDate(long activityDate) {
        this.activityDate = activityDate;
    }

    public String getActivityAddress() {
        return activityAddress;
    }

    public void setActivityAddress(String activityAddress) {
        this.activityAddress = activityAddress;
    }
}
