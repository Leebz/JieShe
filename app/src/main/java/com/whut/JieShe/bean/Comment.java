package com.whut.JieShe.bean;

import cn.bmob.v3.BmobObject;

/**
 * 实体类 评论，可以是对帖子的评论，也可以是对某一个评论的恢复
 * Created by sukai on 2017/04/21.
 */

public class Comment extends BmobObject {
    private MyUser author;            //评论作者
    private long time;                  //发评时间
    private String content;             //评论内容
    private Post post;                  //如果是对帖子的评论，指向该帖子
    private Comment comment;            //如果是对评论的回复，指向该评论

    public MyUser getAuthor() {
        return author;
    }

    public void setAuthor(MyUser author) {
        this.author = author;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }
}
