package com.whut.JieShe.bean;

import java.util.List;

import cn.bmob.v3.BmobUser;

/**
 * Created by 42910 on 2017/5/2.
 */

public class MyUser extends BmobUser {

    private String signature;
    private String realName;
    private String campu;
    private Boolean gender;
    private String avatarUrl;
    private String backgroundUrl;
    private List<String> postCollection;
    private List<String> organCollection;

    public List<String> getPostCollection() {
        return postCollection;
    }

    public void setPostCollection(List<String> postCollection) {
        this.postCollection = postCollection;
    }

    public List<String> getOrganCollection() {
        return organCollection;
    }

    public void setOrganCollection(List<String> organCollection) {
        this.organCollection = organCollection;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getCampu() {
        return campu;
    }

    public void setCampu(String campu) {
        this.campu = campu;
    }

    public Boolean getGender() {
        return gender;
    }

    public void setGender(Boolean gender) {
        this.gender = gender;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBackgroundUrl() {
        return backgroundUrl;
    }

    public void setBackgroundUrl(String backgroundUrl) {
        this.backgroundUrl = backgroundUrl;
    }
}
