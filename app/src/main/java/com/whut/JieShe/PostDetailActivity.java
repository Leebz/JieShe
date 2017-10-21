package com.whut.JieShe;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapLabel;
import com.beardedhen.androidbootstrap.api.defaults.DefaultBootstrapBrand;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.mukesh.MarkdownView;
import com.shehabic.droppy.DroppyClickCallbackInterface;
import com.shehabic.droppy.DroppyMenuPopup;
import com.shehabic.droppy.views.DroppyMenuContainerView;
import com.shehabic.droppy.views.DroppyMenuItemTitleView;
import com.shehabic.droppy.views.DroppyMenuItemView;
import com.shehabic.droppy.views.DroppyMenuPopupView;
import com.whut.JieShe.adapter.QuickCommentListAdapter;
import com.whut.JieShe.bean.Comment;
import com.whut.JieShe.bean.MyUser;
import com.whut.JieShe.bean.Post;
import com.whut.JieShe.utils.TimeUtils;
import com.whut.JieShe.view.PostMoreActionDownMenuAnimation;
import com.whut.JieShe.view.RecycleViewDivider;
import com.tapadoo.alerter.Alerter;
import com.wx.goodview.GoodView;
import com.wx.goodview.IGoodView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Date;

import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.listener.ConversationListener;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class PostDetailActivity extends AppCompatActivity implements View.OnClickListener, BaseQuickAdapter.OnItemClickListener, BaseQuickAdapter.OnItemChildClickListener {

    private MarkdownView mPostDetailView;
    private TextView mComeFromView;
    private TextView mTitleView;
    private TextView mCreateTime;
    private BootstrapLabel mAuthorView;
    private TextView mNumOfJoinedView;
    private TextView mActivityDate;
    private TextView mActivityAddress;
    private BootstrapButton mIWantJoinButton;
    private RecyclerView mPostCommentsView;
    private View mLoadingView;

    private BootstrapButton mMoreActionDButton;
    private DroppyMenuPopup mDropMenu;
    //用于标志mDropMenu菜单是否展开，用户判断按下返回键时的动作（返回上一页 or 收回菜单）
    private boolean isDropMenuExpand = false;

    private MyUser mMyUser;

    private Post mPost;
    private List<String> mJoinedUsers;
    private List<String> mRequestJoinUsers;
    private QuickCommentListAdapter mCommentListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("活动详情");

        Bundle bundle = getIntent().getExtras();
        mPost = (Post) bundle.getSerializable("post");
        if(mPost == null){
            Alerter.create(this)
                    .setTitle("参数错误")
                    .setText("未知的活动")
                    .show();
            this.finish();
            return;
        }

        mActivityDate = (TextView) findViewById(R.id.activity_date);
        mActivityAddress = (TextView) findViewById(R.id.activity_address);
        mComeFromView = (TextView) findViewById(R.id.come_from_organization);
        mTitleView = (TextView) findViewById(R.id.post_title);
        mCreateTime = (TextView) findViewById(R.id.create_time);
        mPostDetailView = (MarkdownView) findViewById(R.id.post_detail_view);
        mLoadingView = findViewById(R.id.loading_container);
        mAuthorView = (BootstrapLabel) findViewById(R.id.author);
        mAuthorView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                    mAuthorView.setBootstrapBrand(DefaultBootstrapBrand.PRIMARY);
                }else if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                    mAuthorView.setBootstrapBrand(DefaultBootstrapBrand.INFO);
                    if(mPost.getAuthor()!=null){
                        Intent intent = new Intent(PostDetailActivity.this, UserDetailInfoActivity.class);
                        intent.putExtra("user",mPost.getAuthor());
                        startActivity(intent);
                    }
                }
                return true;
            }
        });

        mNumOfJoinedView = (TextView) findViewById(R.id.num_of_joined);
        mNumOfJoinedView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    mNumOfJoinedView.setTextColor(Color.RED);
                }
                else if(event.getAction()==MotionEvent.ACTION_UP){
                    mNumOfJoinedView.setTextColor(Color.WHITE);
                    Intent intent = new Intent(PostDetailActivity.this,ShowJoinedUsersActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("post",mPost);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                return true;
            }
        });
        mIWantJoinButton = (BootstrapButton) findViewById(R.id.i_want_join);
        mIWantJoinButton.setClickable(false);
        mIWantJoinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mIWantJoinButton.getText().equals("我要报名")){
                    return;
                }
                if(mPost.isNeedCheck()){
                    mPost.add("requestJoinUsers",BmobUser.getCurrentUser(PostDetailActivity.this).getObjectId());
                }else{
                    mPost.add("joinedUsers",BmobUser.getCurrentUser(PostDetailActivity.this).getObjectId());
                }
                mPost.update(PostDetailActivity.this, new UpdateListener() {
                    @Override
                    public void onSuccess() {
                        if(mPost.isNeedCheck()){
                            mIWantJoinButton.setText("报名成功，等待审核");
                            Alerter.create(PostDetailActivity.this)
                                    .setTitle("报名成功")
                                    .setText("等待管理员审核")
                                    .setIcon(R.drawable.alerter_ic_face)
                                    .show();
                        }else{
                            mIWantJoinButton.setText("报名成功");
                            Alerter.create(PostDetailActivity.this)
                                    .setTitle("报名成功")
                                    .setText("请按时参加活动")
                                    .setIcon(R.drawable.alerter_ic_face)
                                    .show();
                        }
                        mIWantJoinButton.setEnabled(false);
                        mNumOfJoinedView.setText("已有"+(mJoinedUsers.size()+mRequestJoinUsers.size()+1)+"人报名");
                    }

                    @Override
                    public void onFailure(int i, String s) {
                        Alerter.create(PostDetailActivity.this)
                                .setTitle("报名失败")
                                .setBackgroundColor(R.color.colorAccent)
                                .setText(s)
                                .show();
                    }
                });

                mPost.getOrganization().setTotalJoinedUser( mPost.getOrganization().getTotalJoinedUser()+1);
                mPost.getOrganization().update(PostDetailActivity.this);
            }
        });

        mPostCommentsView = (RecyclerView) findViewById(R.id.post_comment);
        mPostCommentsView.setNestedScrollingEnabled(false);
        mPostCommentsView.setLayoutManager(new LinearLayoutManager(this));
        mPostCommentsView.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.VERTICAL, R.drawable.message_recyclerview_divider));

        mCommentListAdapter = new QuickCommentListAdapter(this,new ArrayList<Comment>());
        mPostCommentsView.setAdapter(mCommentListAdapter);
        mCommentListAdapter.setOnItemClickListener(this);
        mCommentListAdapter.setOnItemChildClickListener(this);

        mMoreActionDButton = (BootstrapButton) findViewById(R.id.more_action_drop_down);
        mDropMenu = new DroppyMenuPopup.Builder(PostDetailActivity.this, mMoreActionDButton)
                .fromMenu(R.menu.post_action_dropdown_menu)
                .setOnClick(new DroppyClickCallbackInterface() {
                    @Override
                    public void call(View v, int id) {
                        if(id==R.id.write_comment){
                            writeComment();
                        }else if(id==R.id.collect){
                            collectOrDisThisPost();
                        }else if(id==R.id.advisory){
                            contactManager();
                        }else if(id==R.id.report){

                        }
                    }
                })
                .setOnDismissCallback(new DroppyMenuPopup.OnDismissCallback() {
                    @Override
                    public void call() {
                        //菜单关闭时设置标志
                        isDropMenuExpand = false;
                    }
                })
                .setPopupAnimation(new PostMoreActionDownMenuAnimation())
                .build();
        mMoreActionDButton.setOnClickListener(this);


        //获得MyUser的信息
        BmobUser user = BmobUser.getCurrentUser(this);
        BmobQuery<MyUser> userQuery = new BmobQuery<>();
        userQuery.getObject(this, user.getObjectId(), new GetListener<MyUser>() {
            @Override
            public void onSuccess(MyUser myUser) {
                mMyUser = myUser;
            }
            @Override
            public void onFailure(int i, String s) {
                Alerter.create(PostDetailActivity.this)
                        .setTitle("获取用户信息失败").setText(s).show();
            }
        });

        //更新活动的信息
        BmobQuery<Post> query = new BmobQuery<>();
        query.include("author,organization,organization.manager");
        query.getObject(this, mPost.getObjectId(), new GetListener<Post>() {
            @Override
            public void onSuccess(Post post) {
                mLoadingView.setVisibility(View.GONE);
                PostDetailActivity.this.mPost = post;
                refreshComment();
                updateView();
                mIWantJoinButton.setClickable(true);
            }

            @Override
            public void onFailure(int i, String s) {
                Alerter.create(PostDetailActivity.this)
                        .setTitle("获取活动失败")
                        .setBackgroundColor(R.color.colorAccent)
                        .setText(s)
                        .show();
            }
        });
    }

    /**
     * 运行时更改 DropMenu的菜单项，作者竟然没有提供接口真是坑爹
     * @param index
     * @param title
     */
    private void setDropMenuTitle(int index,String title){
        DroppyMenuPopupView popupView = (DroppyMenuPopupView) mDropMenu.getMenuView();
        if(popupView==null){
            return;
        }
        DroppyMenuContainerView containerView = (DroppyMenuContainerView) popupView.getChildAt(0);
        if(containerView==null){
            return;
        }
        DroppyMenuItemView itemView = (DroppyMenuItemView) containerView.getChildAt(index);
        if(itemView==null){
            return;
        }
        DroppyMenuItemTitleView titleView = (DroppyMenuItemTitleView) itemView.getChildAt(1);
        if(titleView==null){
            return;
        }
        titleView.setText(title);
    }

    @Override
    public void onBackPressed() {
        if(isDropMenuExpand){
            mDropMenu.dismiss(false);
            isDropMenuExpand = false;
        }else{
            super.onBackPressed();
        }
    }

    //联系管理员
    private void contactManager() {
        final BmobUser manager = mPost.getOrganization().getManager();
        BmobIMUserInfo userInfo = new BmobIMUserInfo();
        userInfo.setUserId(manager.getObjectId());
        userInfo.setName(manager.getUsername());
        BmobIM.getInstance().startPrivateConversation(userInfo, new ConversationListener() {
            @Override
            public void done(BmobIMConversation c, BmobException e) {
                if (e == null) {
                    Intent intent = new Intent(PostDetailActivity.this, ChatActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("BmobUser",manager);
                    bundle.putSerializable("BmobIMConversation",c);
                    intent.putExtras(bundle);
                    startActivity(intent);
                } else {
                    Log.d("UserDetailInfoActivity", e.getMessage() + "(" + e.getErrorCode() + ")");
                }
            }
        });
    }

    /**
     * 收藏/取消收藏这个活动
     */
    private void collectOrDisThisPost() {
        BmobUser user = BmobUser.getCurrentUser(this);
        BmobQuery<MyUser> userQuery = new BmobQuery<>();
        userQuery.getObject(this, user.getObjectId(), new GetListener<MyUser>() {
            @Override
            public void onSuccess(MyUser myUser) {
                mMyUser = myUser;
                final String tips;
                if(mMyUser.getPostCollection().contains(mPost.getObjectId())){
                    String[] ids = {mPost.getObjectId()};
                    mMyUser.removeAll("postCollection",Arrays.asList(ids));
                    tips = "取消收藏";
                }else{
                    mMyUser.addUnique("postCollection",mPost.getObjectId());
                    tips = "收藏";
                }
                mMyUser.update(PostDetailActivity.this, new UpdateListener() {
                    @Override
                    public void onSuccess() {
                        GoodView goodView = new GoodView(PostDetailActivity.this);
                        goodView.setTextInfo(tips+"成功",Color.RED, IGoodView.TEXT_SIZE);
                        goodView.setDuration(800);
                        goodView.show(mMoreActionDButton);
                    }
                    @Override
                    public void onFailure(int i, String s) {
                        Alerter.create(PostDetailActivity.this)
                                .setTitle(tips+"失败")
                                .setText(s)
                                .show();
                    }
                });
            }
            @Override
            public void onFailure(int i, String s) {
                Alerter.create(PostDetailActivity.this)
                        .setTitle("获取用户信息失败").setText(s).show();
            }
        });
    }

    private void refreshComment(){
        BmobQuery<Comment> commentBmobQuery = new BmobQuery<>();
        commentBmobQuery.addWhereEqualTo("post",mPost);
        commentBmobQuery.include("author,comment,comment.author");
        commentBmobQuery.setLimit(20);
        commentBmobQuery.order("-time");
        commentBmobQuery.findObjects(this, new FindListener<Comment>() {
            @Override
            public void onSuccess(List<Comment> list) {
                mCommentListAdapter.getData().clear();
                mCommentListAdapter.addData(list);
            }

            @Override
            public void onError(int i, String s) {
                Alerter.create(PostDetailActivity.this)
                        .setTitle("获取评论失败")
                        .setText(s)
                        .show();
            }
        });
    }

    private void updateView(){
        mActivityAddress.setText(mPost.getActivityAddress());
        mActivityDate.setText(TimeUtils.getDayAndTimeName(mPost.getActivityDate()));
        mComeFromView.setText("来自:"+mPost.getOrganization().getName());
        mTitleView.setText(mPost.getTitle());
        mCreateTime.setText(TimeUtils.getDayAndTimeName(mPost.getTime()));
        mPostDetailView.setMarkDownText(mPost.getContent()==null?"":mPost.getContent());
        mAuthorView.setText(mPost.getAuthor().getUsername());

        if(mJoinedUsers==null){
            mJoinedUsers = new ArrayList<>();
        }
        if(mRequestJoinUsers==null){
            mRequestJoinUsers = new ArrayList<>();
        }

        mJoinedUsers.addAll(mPost.getJoinedUsers()==null?new ArrayList<String>():mPost.getJoinedUsers());
        mRequestJoinUsers.addAll(mPost.getRequestJoinUsers()==null?new ArrayList<String>():mPost.getRequestJoinUsers());

        int userCount = mJoinedUsers.size()+mRequestJoinUsers.size();
        mNumOfJoinedView.setText("已有"+userCount+"人报名");

        BmobUser currentUser = BmobUser.getCurrentUser(PostDetailActivity.this);
        for(String userId:mRequestJoinUsers){
            if(userId.equals(currentUser.getObjectId())){
                mIWantJoinButton.setText("等待审核");
                mIWantJoinButton.setEnabled(false);
                break;
            }
        }
        for(String userId:mJoinedUsers){
            if(userId.equals(currentUser.getObjectId())){
                mIWantJoinButton.setText("您已报名");
                mIWantJoinButton.setEnabled(false);
                break;
            }
        }
    }

    private void writeComment(){
        new MaterialDialog.Builder(this)
                .title("写评论")
                .content("写下自己的看法^_^")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("", "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if(TextUtils.isEmpty(input)){
                            return;
                        }
                        Comment comment = new Comment();
                        comment.setPost(mPost);
                        comment.setTime(new Date().getTime());
                        comment.setContent(input.toString());
                        comment.setAuthor(BmobUser.getCurrentUser(PostDetailActivity.this,MyUser.class));
                        comment.save(PostDetailActivity.this, new SaveListener() {
                            @Override
                            public void onSuccess() {
                                refreshComment();
                                Toast.makeText(PostDetailActivity.this, "评论成功", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(int i, String s) {
                                Toast.makeText(PostDetailActivity.this, "评论失败:" + s, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).show();
    }

    /**
     * mMoreActionDButton
     * 点击该按钮时展开DropMenu
     * 然后额外做三件事情
     * 1. 更新MyUser的信息
     * 2. 判断MyUser的postCollection字段是否包含当前帖子的ID（用户是否收藏帖子）
     * 3. （1）若是：更改DropMenu中菜单项“收藏”为“取消收藏”
     *    （2）若否：更改DropMenu中菜单项“取消收藏”为“收藏”
     * @param view
     */
    @Override
    public void onClick(View view) {
        if(view.equals(mMoreActionDButton)){
            isDropMenuExpand = true;
            mDropMenu.show();
            BmobUser user = BmobUser.getCurrentUser(this);
            BmobQuery<MyUser> userQuery = new BmobQuery<>();
            userQuery.getObject(this, user.getObjectId(), new GetListener<MyUser>() {
                @Override
                public void onSuccess(MyUser myUser) {
                    mMyUser = myUser;
                    if(mMyUser.getPostCollection()==null){
                        setDropMenuTitle(1,"收藏");
                        return;
                    }
                    if(mMyUser.getPostCollection().contains(mPost.getObjectId())){
                        setDropMenuTitle(1,"取消收藏");
                    }else{
                        setDropMenuTitle(1,"收藏");
                    }
                }

                @Override
                public void onFailure(int i, String s) {

                }
            });
        }
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        final Comment comment = mCommentListAdapter.getData().get(position);
        new MaterialDialog.Builder(this)
                .title("回复"+comment.getAuthor().getUsername()+":")
                .content(comment.getContent())
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("", "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if(TextUtils.isEmpty(input)){
                            return;
                        }
                        Comment c = new Comment();
                        c.setPost(mPost);
                        c.setTime(new Date().getTime());
                        c.setContent(input.toString());
                        c.setComment(comment);
                        c.setAuthor(BmobUser.getCurrentUser(PostDetailActivity.this,MyUser.class));
                        c.save(PostDetailActivity.this, new SaveListener() {
                            @Override
                            public void onSuccess() {
                                refreshComment();
                                Toast.makeText(PostDetailActivity.this, "回复成功", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(int i, String s) {
                                Toast.makeText(PostDetailActivity.this, "回复失败:" + s, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).show();
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        if(view.getId()==R.id.avatar){
            Intent intent = new Intent(PostDetailActivity.this, UserDetailInfoActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("user",mCommentListAdapter.getData().get(position).getAuthor());
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }
}
