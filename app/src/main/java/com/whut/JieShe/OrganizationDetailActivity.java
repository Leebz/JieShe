package com.whut.JieShe;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapLabel;
import com.beardedhen.androidbootstrap.api.defaults.DefaultBootstrapBrand;
import com.beardedhen.androidbootstrap.api.defaults.DefaultBootstrapHeading;
import com.bumptech.glide.Glide;
import com.hymane.expandtextview.ExpandTextView;
import com.whut.JieShe.adapter.QuickPostListAdapter;
import com.whut.JieShe.bean.MyUser;
import com.whut.JieShe.bean.Organization;
import com.whut.JieShe.bean.Post;
import com.whut.JieShe.view.MovingImageView;
import com.tapadoo.alerter.Alerter;
import com.wx.goodview.GoodView;
import com.wx.goodview.IGoodView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.listener.ConversationListener;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.UpdateListener;

public class OrganizationDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private Organization mOrganization;
    private ImageView mOrganizationLogo;
    private MovingImageView mOrganizationBackgroundView;
    private Toolbar mToolbar;
    private ExpandTextView mIntroduceView;
    private TextView mOrganizationHotView;
    private LinearLayout mOrganizationTagContainer;
    private BootstrapLabel mOrganizationManagerView;
    private RecyclerView mPostListView;
    private QuickPostListAdapter mPostListAdapter;
    private FloatingActionButton mCollectOrganizationFab;
    private FloatingActionButton mContactManagerFab;

    private DefaultBootstrapBrand mBrands[] = {
            DefaultBootstrapBrand.PRIMARY,
            DefaultBootstrapBrand.INFO,
            DefaultBootstrapBrand.SUCCESS,
            DefaultBootstrapBrand.REGULAR,
            DefaultBootstrapBrand.WARNING
    };
    private MyUser mMyUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        mOrganization = (Organization) bundle.get("organization");
        if(mOrganization==null){
            this.finish();
            return;
        }
        setContentView(R.layout.activity_organization_detail);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mOrganizationLogo = (ImageView) findViewById(R.id.organization_logo);
        Glide.with(this)
                .load(mOrganization.getLogoUrl())
                .placeholder(R.drawable.default_logo)
                .error(R.drawable.default_logo)
                .fitCenter()
                .into(mOrganizationLogo);

        mOrganizationBackgroundView = (MovingImageView) findViewById(R.id.organization_background);
        mOrganizationBackgroundView.getMovingAnimator().setInterpolator(new LinearInterpolator());
        mOrganizationBackgroundView.getMovingAnimator().setSpeed(20);
        mOrganizationBackgroundView.getMovingAnimator().addCustomMovement().
                addDiagonalMoveToDownRight().
                addHorizontalMoveToLeft().
                addDiagonalMoveToUpRight().
                addVerticalMoveToDown().
                addHorizontalMoveToLeft().
                addVerticalMoveToUp().
                start();
        Glide.with(this)
                .load(mOrganization.getBackgroundUrl())
                .placeholder(R.drawable.user_info_background)
                .error(R.drawable.user_info_background)
                .fitCenter()
                .into(mOrganizationBackgroundView);

        mCollectOrganizationFab = (FloatingActionButton) findViewById(R.id.collect_organization);
        mContactManagerFab = (FloatingActionButton) findViewById(R.id.contact_me);

        mOrganizationHotView = (TextView) findViewById(R.id.organization_hot);
        mIntroduceView = (ExpandTextView) findViewById(R.id.organization_introduce);
        mOrganizationManagerView = (BootstrapLabel) findViewById(R.id.organization_manager);
        mOrganizationTagContainer = (LinearLayout) findViewById(R.id.organization_tag_container);
        mPostListView = (RecyclerView) findViewById(R.id.post_list);
        mPostListView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        mPostListView.setNestedScrollingEnabled(false);

        mCollectOrganizationFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collectOrDisThisOrganization();
            }
        });

        mContactManagerFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contactManager();
            }
        });

        setTitle(mOrganization.getName());
        mOrganizationHotView.setText("活动参与:"+mOrganization.getTotalJoinedUser()+"人次");
        mIntroduceView.setContent(mOrganization.getIntroduction());
        mOrganizationManagerView.setText(mOrganization.getManager().toString());
        mOrganizationManagerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                    mOrganizationManagerView.setBootstrapBrand(DefaultBootstrapBrand.PRIMARY);
                }else if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                    mOrganizationManagerView.setBootstrapBrand(DefaultBootstrapBrand.INFO);
                    if(mOrganization.getManager()!=null){
                        Intent intent = new Intent(OrganizationDetailActivity.this,UserDetailInfoActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("user",mOrganization.getManager());
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                }
                return true;
            }
        });
        addTagsToContainer();

        mPostListAdapter = new QuickPostListAdapter(this,new ArrayList<Post>());
        mPostListView.setAdapter(mPostListAdapter);
        mPostListView.setOnClickListener(this);

        refreshManager();
        refreshPosts();
        refreshMyUser();
    }

    //联系管理员
    private void contactManager() {
        final BmobUser manager = mOrganization.getManager();
        BmobIMUserInfo userInfo = new BmobIMUserInfo();
        userInfo.setUserId(manager.getObjectId());
        userInfo.setName(manager.getUsername());
        BmobIM.getInstance().startPrivateConversation(userInfo, new ConversationListener() {
            @Override
            public void done(BmobIMConversation c, BmobException e) {
                if (e == null) {
                    Intent intent = new Intent(OrganizationDetailActivity.this, ChatActivity.class);
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


    private void refreshMyUser() {
        BmobUser user = BmobUser.getCurrentUser(this);
        BmobQuery<MyUser> userQuery = new BmobQuery<>();
        userQuery.getObject(this, user.getObjectId(), new GetListener<MyUser>() {
            @Override
            public void onSuccess(MyUser myUser) {
                mMyUser = myUser;
                final String tips;
//                if(mMyUser.getOrganCollection().contains(mOrganization.getObjectId())){
//                    mCollectOrganizationFab.setImageResource(R.drawable.ic_favorite_red_24dp);
//                }else{
//                    mCollectOrganizationFab.setImageResource(R.drawable.ic_bookmark_blue_24dp);
//                }
            }
            @Override
            public void onFailure(int i, String s) {
                Alerter.create(OrganizationDetailActivity.this)
                        .setTitle("获取用户信息失败").setText(s).show();
            }
        });
    }

    /**
     * 收藏/取消收藏这个社团
     */
    private void collectOrDisThisOrganization() {
        BmobUser user = BmobUser.getCurrentUser(this);
        BmobQuery<MyUser> userQuery = new BmobQuery<>();
        userQuery.getObject(this, user.getObjectId(), new GetListener<MyUser>() {
            @Override
            public void onSuccess(MyUser myUser) {
                mMyUser = myUser;
                final String tips;
                if(mMyUser.getOrganCollection()!=null && mMyUser.getOrganCollection().contains(mOrganization.getObjectId())){
                    String[] ids = {mOrganization.getObjectId()};
                    mMyUser.removeAll("organCollection", Arrays.asList(ids));
                    tips = "取消收藏";
                    mCollectOrganizationFab.setImageResource(R.drawable.ic_bookmark_blue_24dp);
                }else{
                    mMyUser.addUnique("organCollection",mOrganization.getObjectId());
                    tips = "收藏";
                    mCollectOrganizationFab.setImageResource(R.drawable.ic_favorite_red_24dp);
                }
                mMyUser.update(OrganizationDetailActivity.this, new UpdateListener() {
                    @Override
                    public void onSuccess() {
                        GoodView goodView = new GoodView(OrganizationDetailActivity.this);
                        goodView.setTextInfo(tips+"成功", Color.RED, IGoodView.TEXT_SIZE);
                        goodView.setDuration(800);
                        goodView.show(mCollectOrganizationFab);
                    }
                    @Override
                    public void onFailure(int i, String s) {
                        Alerter.create(OrganizationDetailActivity.this)
                                .setTitle(tips+"失败")
                                .setText(s)
                                .show();
                    }
                });
            }
            @Override
            public void onFailure(int i, String s) {
                Alerter.create(OrganizationDetailActivity.this)
                        .setTitle("获取用户信息失败").setText(s).show();
            }
        });
    }

    private void refreshPosts() {
        BmobQuery<Post> query = new BmobQuery<>();
        query.addWhereEqualTo("organization",mOrganization);
        query.setLimit(5);
        query.include("author,organization");
        query.findObjects(this, new FindListener<Post>() {
            @Override
            public void onSuccess(List<Post> list) {
                mPostListAdapter.getData().clear();
                mPostListAdapter.addData(list);
            }

            @Override
            public void onError(int i, String s) {
                Alerter.create(OrganizationDetailActivity.this)
                        .setTitle("获取活动失败")
                        .setText(s)
                        .show();
            }
        });
    }

    private void refreshManager() {
        BmobQuery<Organization> query = new BmobQuery<>();
        query.addQueryKeys("manager");
        query.include("manager");
        query.getObject(this, mOrganization.getObjectId(), new GetListener<Organization>() {
            @Override
            public void onSuccess(Organization organization) {
                mOrganization.setManager(organization.getManager());
                mOrganizationManagerView.setText(mOrganization.getManager().getUsername());
            }

            @Override
            public void onFailure(int i, String s) {
                Alerter.create(OrganizationDetailActivity.this)
                        .setTitle("获取管理员失败")
                        .setText(s)
                        .show();
            }
        });
    }

    private void addTagsToContainer() {
        if(mOrganization.getTags()==null){
            return;
        }
        for(String tag:mOrganization.getTags()){
            BootstrapLabel l = new BootstrapLabel(this);
            l.setText(tag);
            l.setBootstrapBrand(mBrands[(int)(Math.random()*mBrands.length)]);
            l.setBootstrapHeading(DefaultBootstrapHeading.H6);
            l.setPadding(10,10,10,10);
            mOrganizationTagContainer.addView(l);
            mOrganizationTagContainer.addView(new View(this),new LinearLayout.LayoutParams(5, LinearLayout.LayoutParams.MATCH_PARENT));
        }
    }

    @Override
    public void onClick(View view) {
        Post post = (Post) view.getTag();
        Bundle bundle = new Bundle();
        bundle.putSerializable("post",post);
        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
