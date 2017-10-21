package com.whut.JieShe;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.soundcloud.android.crop.Crop;
import com.whut.JieShe.bean.MyUser;
import com.whut.JieShe.view.MovingImageView;
import com.tapadoo.alerter.Alerter;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.filter.Filter;
import com.zhihu.matisse.internal.entity.Item;
import com.zhihu.matisse.internal.entity.UncapableCause;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.listener.ConversationListener;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

/**
 * 改Activity主要任务是显示用户信息，其中包含显示自己的信息和他人的信息
 * 若为自己的信息：则自己可点击相应的项目并且修改
 * 若为他人的信息：相应的项目不可修改，并且添加三个项目（他的动态、他的活动、他的收藏）
 */
public class UserDetailInfoActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ASK_WRITE_EXTERNAL_STORAGE = 0;
    private static final int REQUEST_CODE_PICK_IMAGE_AVATAR = 1;
    private static final int REQUEST_CODE_PICK_IMAGE_BACKGROUND = 2;
    private RecyclerView mRecyclerView;
    private FloatingActionButton mContactHimButton;
    private List<ListItem> mListItems;
    private UserDetailInfoAdapter mAdapter;
    private ImageView mUserAvatarView;
    private MovingImageView mUserBackgroundView;
    private CollapsingToolbarLayout mToolbarLayout;
    private BmobUser mUser;
    private MyUser mMyUser;

    private int mPickType = -1; //记录了当前选择的是头像还是背景

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail_info);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();
        mUser = (BmobUser) bundle.getSerializable("user");
        if(mUser==null){
           this.finish();
            return;
        }
        setTitle(mUser.getUsername());

        mToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        mToolbarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickToolbar();
            }
        });
        mUserAvatarView = (ImageView) findViewById(R.id.user_avatar);
        mUserBackgroundView = (MovingImageView) findViewById(R.id.user_background);
        mUserBackgroundView.getMovingAnimator().setInterpolator(new LinearInterpolator());
        mUserBackgroundView.getMovingAnimator().setSpeed(20);
        mUserBackgroundView.getMovingAnimator().addCustomMovement().
                addDiagonalMoveToDownRight().
                addHorizontalMoveToLeft().
                addDiagonalMoveToUpRight().
                addVerticalMoveToDown().
                addHorizontalMoveToLeft().
                addVerticalMoveToUp().
                start();
        mContactHimButton = (FloatingActionButton) findViewById(R.id.contact_him);
        mRecyclerView = (RecyclerView)findViewById(R.id.group_detail_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter = new UserDetailInfoAdapter());
        refreshData();
    }

    /**
     * 弹出一个菜单让用户选择更换头像 or 更换背景
     */
    private void onClickToolbar(){
        DialogPlus dialog = DialogPlus.newDialog(this)
                .setContentHolder(new com.orhanobut.dialogplus.ViewHolder(R.layout.dialog_user_detail_info_menu))
                .setGravity(Gravity.BOTTOM)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(DialogPlus dialog, View view) {
                        dialog.dismiss();
                        switch (view.getId()){
                            case R.id.change_background_item:
                                if(checkPermission()){
                                    pickAImage(REQUEST_CODE_PICK_IMAGE_BACKGROUND);
                                }
                                break;
                            case R.id.change_avatar_item:
                                if(checkPermission()){
                                    pickAImage(REQUEST_CODE_PICK_IMAGE_AVATAR);
                                }
                                break;
                        }
                    }
                })
                .create();
        dialog.show();
    }

    /**
     * @return 运行时检查是否具有WRITE_EXTERNAL_STORAGE和READ_EXTERNAL_STORAGE权限
     */
    private boolean checkPermission(){
        if (Build.VERSION.SDK_INT >= 23) {
            int checkWriteExternalStoragePermission = ContextCompat.checkSelfPermission(
                    UserDetailInfoActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            );
            int checkReadExternalStoragePermission = ContextCompat.checkSelfPermission(
                    UserDetailInfoActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            );
            if(checkWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED ||
                    checkReadExternalStoragePermission != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(
                        UserDetailInfoActivity.this,
                        new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                        },
                        REQUEST_CODE_ASK_WRITE_EXTERNAL_STORAGE);
                return false;
            }else{
                return true;
            }
        } else {
            return true;
        }
    }

    /**
     * 权限检查回掉
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_WRITE_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    pickAImage();
                } else {
                    Alerter.create(UserDetailInfoActivity.this)
                            .setTitle("权限获取失败")
                            .setText("不允许权限将无法获取手机上的照片")
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void pickAImage(int type){
        Matisse.from(UserDetailInfoActivity.this)
                .choose(MimeType.allOf())
                .countable(true)
                .maxSelectable(1)
                .addFilter(new Filter() {
                    @Override
                    protected Set<MimeType> constraintTypes() {
                        return new HashSet<MimeType>() {{
                            add(MimeType.GIF);
                        }};
                    }

                    @Override
                    public UncapableCause filter(Context context, Item item) {
                        return null;
                    }
                })
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new GlideEngine())
                .forResult(type);
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode!=RESULT_OK){
            return;
        }
        //选择图片之后，裁剪
        if(requestCode==REQUEST_CODE_PICK_IMAGE_AVATAR||requestCode==REQUEST_CODE_PICK_IMAGE_BACKGROUND){
            mPickType = requestCode;
            List<Uri> mSelected = Matisse.obtainResult(data);
            Uri uri = mSelected.get(0);

            if(requestCode==REQUEST_CODE_PICK_IMAGE_AVATAR){
                Crop.of(uri, Uri.fromFile(new File(getCacheDir(), "crop_image"))).asSquare().start(this);
            } else {
                Crop.of(uri, Uri.fromFile(new File(getCacheDir(), "crop_image"))).start(this);
            }
            //裁剪之后压缩，压缩之后上传
        }else if (requestCode == Crop.REQUEST_CROP) {
            Uri uri = Crop.getOutput(data);
            if(uri==null){
                return;
            }
            File file = new File(uri.getPath());
            Luban.get(this)
                    .load(file)                     //传人要压缩的图片
                    .putGear(Luban.THIRD_GEAR)      //设定压缩档次，默认三挡
                    .setCompressListener(new OnCompressListener() {
                        @Override
                        public void onStart() {
                            Log.d("UserDetailInfoActivity", "开始压缩");
                        }

                        @Override
                        public void onSuccess(File file) {
                            Log.d("UserDetailInfoActivity", "压缩完成：" + file.getAbsoluteFile());
                            uploadFile(file);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d("UserDetailInfoActivity", "压缩失败"+e.getMessage());
                        } //设置回调
                    }).launch();
        }
    }

    private void uploadFile(File file){
        final BmobFile bmobFile = new BmobFile(file);
        bmobFile.uploadblock(this, new UploadFileListener() {
            @Override
            public void onSuccess() {
                if(mPickType==UserDetailInfoActivity.REQUEST_CODE_PICK_IMAGE_AVATAR){
                    Log.d("UserDetailInfoActivity", "头像上传完成");
                    updateAvatarUrl(bmobFile.getUrl());
                }else if(mPickType==UserDetailInfoActivity.REQUEST_CODE_PICK_IMAGE_BACKGROUND){
                    Log.d("UserDetailInfoActivity", "背景上传完成");
                    updateBackgroundUrl(bmobFile.getUrl());
                }
            }

            @Override
            public void onFailure(int i, String s) {
                Log.d("UserDetailInfoActivity", "上传失败");
            }

            @Override
            public void onProgress(Integer value) {
                super.onProgress(value);
                Log.d("UserDetailInfoActivity", "onProgress:" + value);
            }
        });
    }

    //更新用户的头像的url，并将用户原先的头像文件删除
    private void updateAvatarUrl(String url){
        if(!TextUtils.isEmpty(mMyUser.getAvatarUrl())){
            BmobFile file = new BmobFile();
            file.setUrl(mMyUser.getAvatarUrl());
            file.delete(this);
        }
        mMyUser.setObjectId(mUser.getObjectId());
        mMyUser.setAvatarUrl(url);
        mMyUser.update(this, new UpdateListener() {
            @Override
            public void onSuccess() {
                Glide.with(UserDetailInfoActivity.this)
                        .load(mMyUser.getAvatarUrl())
                        .placeholder(R.drawable.default_avatar)
                        .crossFade()
                        .into(mUserAvatarView);
                Alerter.create(UserDetailInfoActivity.this)
                        .setTitle("提示")
                        .setText("头像已经修改")
                        .show();
            }

            @Override
            public void onFailure(int i, String s) {
                Alerter.create(UserDetailInfoActivity.this)
                        .setTitle("提示:头像修改失败")
                        .setText(s)
                        .show();
            }
        });
    }

    //更新用户的背景的url，并将用户原先的背景文件删除
    private void updateBackgroundUrl(String url){
        if(!TextUtils.isEmpty(mMyUser.getBackgroundUrl())){
            Log.d("UserDetailInfoActivity", "删除原背景" + mMyUser.getBackgroundUrl());
            BmobFile file = new BmobFile();
            file.setUrl(mMyUser.getBackgroundUrl());
            file.delete(this);
        }
        mMyUser.setObjectId(mUser.getObjectId());
        mMyUser.setBackgroundUrl(url);
        mMyUser.update(this, new UpdateListener() {
            @Override
            public void onSuccess() {
                Glide.with(UserDetailInfoActivity.this)
                        .load(mMyUser.getBackgroundUrl())
                        .placeholder(R.drawable.user_info_background)
                        .crossFade()
                        .into(mUserBackgroundView);
                Alerter.create(UserDetailInfoActivity.this)
                        .setTitle("提示")
                        .setText("背景已经修改")
                        .show();
            }

            @Override
            public void onFailure(int i, String s) {
                Alerter.create(UserDetailInfoActivity.this)
                        .setTitle("提示:背景修改失败")
                        .setText(s)
                        .show();
            }
        });
    }

    /**
     * 当显示的不是自己的信息时，需要对View做一些调整
     */
    private void shiftView() {
        mContactHimButton.setVisibility(View.VISIBLE);
        mContactHimButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BmobIMUserInfo userInfo = new BmobIMUserInfo();
                userInfo.setUserId(mUser.getObjectId());
                userInfo.setName(mUser.getUsername());
                userInfo.setAvatar(mMyUser.getAvatarUrl());

                BmobIM.getInstance().updateUserInfo(userInfo);
                BmobIM.getInstance().startPrivateConversation(userInfo, new ConversationListener() {
                    @Override
                    public void done(BmobIMConversation c, BmobException e) {
                        if (e == null) {
                            Intent intent = new Intent(UserDetailInfoActivity.this, ChatActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("BmobIMConversation",c);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        } else {
                            Log.d("UserDetailInfoActivity", e.getMessage() + "(" + e.getErrorCode() + ")");
                        }
                    }
                });
            }
        });
        for(ListItem item:mListItems){
            item.clickable = false;
        }
        mToolbarLayout.setClickable(false);
        mListItems.add(new ListItem("他的动态",""));
        mListItems.add(new ListItem("他的活动",""));
        mListItems.add(new ListItem("他的收藏",""));

        mUserAvatarView.setClickable(false);
    }

    /**
     * 该方法用户用户修改了自己的信息时，保存修改的信息到服务器
     * @param listener 保存的回调
     */
    private void saveData(UpdateListener listener){
        if(mListItems==null){
            return;
        }
        //如果不是自己的信息，那么无法修改信息，自然不用保存
        BmobUser currentUser = BmobUser.getCurrentUser(UserDetailInfoActivity.this);
        if(!mUser.getObjectId().equals(currentUser.getObjectId())){
            return;
        }
        MyUser user = new MyUser();
        user.setObjectId(mUser.getObjectId());
        user.setUsername(mUser.getUsername());
        user.setRealName(mListItems.get(0).value);
        user.setGender(mListItems.get(1).value.equals("男")?Boolean.TRUE:Boolean.FALSE);
        user.setCampu(mListItems.get(2).value);
        user.setMobilePhoneNumber(mListItems.get(3).value);
        user.setEmail(mListItems.get(4).value);
        user.setSignature(mListItems.get(5).value);
        if(listener==null){
            user.update(UserDetailInfoActivity.this);
        }else{
            user.update(UserDetailInfoActivity.this,listener);
        }
    }

    public void refreshData(){
        if(mListItems==null){
            mListItems = new ArrayList<>();
        }else{
            mListItems.clear();
        }

        BmobQuery<MyUser> query = new BmobQuery<MyUser>();
        query.getObject(this, mUser.getObjectId(), new GetListener<MyUser>() {
            @Override
            public void onSuccess(MyUser myUser) {
                mMyUser = myUser;
                mListItems.add(new ListItem("真实姓名",myUser.getRealName()==null?"":myUser.getRealName()));
                mListItems.add(new ListItem("性别",myUser.getGender()==null?"未知":(myUser.getGender()?"男":"女")));
                mListItems.add(new ListItem("学院",myUser.getCampu()==null?"":myUser.getCampu()));
                mListItems.add(new ListItem("电话号码",myUser.getMobilePhoneNumber()==null?"":myUser.getMobilePhoneNumber()));
                mListItems.add(new ListItem("电子邮箱",myUser.getEmail()==null?"":myUser.getEmail()));
                mListItems.add(new ListItem("个性签名",myUser.getSignature()==null?"":myUser.getSignature()));

                BmobUser currentUser = BmobUser.getCurrentUser(UserDetailInfoActivity.this);
                if(!mUser.getObjectId().equals(currentUser.getObjectId())){
                    //如果显示的不是自己的信息
                    shiftView();
                }
                mAdapter.notifyDataSetChanged();

                try{
                    //加载头像
                    Glide.with(UserDetailInfoActivity.this)
                            .load(myUser.getAvatarUrl())
                            .placeholder(R.drawable.default_avatar)
                            .error(R.drawable.default_avatar)
                            .crossFade()
                            .into(mUserAvatarView);
                    //加载背景
                    Glide.with(UserDetailInfoActivity.this)
                            .load(myUser.getBackgroundUrl())
                            .placeholder(R.drawable.user_info_background)
                            .error(R.drawable.user_info_background)
                            .crossFade()
                            .into(mUserBackgroundView);
                }catch (Exception e){
                    onFailure(-1,e.getMessage());
                }
            }

            @Override
            public void onFailure(int i, String s) {
                Alerter.create(UserDetailInfoActivity.this)
                        .setTitle("信息加载失败")
                        .setText(s)
                        .show();
            }
        });
    }

    private class ListItem {
        String key;
        String value;
        boolean clickable;
        ListItem(String key, String value) {
            this(key,value,true);
        }

        ListItem(String key, String value, boolean clickable) {
            this.key = key;
            this.value = value;
            this.clickable = clickable;
        }
    }

    class UserDetailInfoAdapter extends RecyclerView.Adapter<UserDetailInfoAdapter.MyViewHolder> implements View.OnClickListener {

        @Override
        public UserDetailInfoAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(
                    UserDetailInfoActivity.this).inflate(R.layout.user_detail_item,parent,
                    false);
            MyViewHolder holder = new MyViewHolder(view);
            view.setOnClickListener(this);
            return holder;
        }

        @Override
        public void onBindViewHolder(UserDetailInfoAdapter.MyViewHolder holder, int position) {
            holder.mType.setText(mListItems.get(position).key);
            holder.mTypeValue.setText(mListItems.get(position).value);
            holder.mRootView.setTag(position);
            holder.mRootView.setClickable(mListItems.get(position).clickable);
        }

        @Override
        public int getItemCount() {
            return mListItems.size();
        }

        /**
         * 修改用户信息，
         * @param which 用户点击的要修改的项的序号，即项在mListItems中的序号
         * @param listener 数据保存的回调
         */
        private void actionOnItemClick(final int which, final UpdateListener listener){
            if(which==0||which==2||which==3||which==5){
                new MaterialDialog.Builder(UserDetailInfoActivity.this)
                        .title("编辑"+mListItems.get(which).key)
                        .content("请输入你的"+mListItems.get(which).key+":")
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input("","",new MaterialDialog.InputCallback(){
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                if(TextUtils.isEmpty(input)){
                                    return;
                                }
                                mListItems.get(which).value = input.toString();
                                UserDetailInfoAdapter.this.notifyDataSetChanged();
                                saveData(listener);
                            }
                        }).show();
            }else if(which==1){
                String[] choice={"男","女"};
                AlertDialog.Builder builder = new AlertDialog.Builder(UserDetailInfoActivity.this);
                builder.setItems(choice,new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListItems.get(1).value = which==0?"男":"女";
                        UserDetailInfoAdapter.this.notifyDataSetChanged();
                        saveData(listener);
                    }
                });
                builder.show();
            }
            else if(which==4){
                Intent intent = new Intent(UserDetailInfoActivity.this,ChangeEmailActivity.class);
                startActivity(intent);
            }

            if(which<6){
                return;
            }
            /**
             *  一以下三项只有在查看他人信息时才出现，为：他的动态、他的活动、她的收藏
             */
            Bundle bundle = new Bundle();
            bundle.putSerializable("user",mUser);
            if(which==6){
                Intent intent = new Intent(UserDetailInfoActivity.this, MyDynamicsActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }else if(which==7){
                Intent intent = new Intent(UserDetailInfoActivity.this, MyActivityActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }else if(which==8){
                Intent intent = new Intent(UserDetailInfoActivity.this, MyCollectionActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }

        /**
         * 修改用户信息的策略是：
         * 1. 根据用户点击的条目弹出合适的对话框（点击姓名就弹输入对话框，性别就弹选择对话框，或者其他操作，启动Activity等）
         * 2. 用户输入相关信息之后更新mListItem, 并notifyDataSetChanged()跟新界面显示
         * 3. 将用户修改的信息保存到服务器
         *     （1）保存成功，弹出修改成功提示，完成
         *     （2）保存失败，弹出修改失败提示，并显示错误信息，最后将刷新界面的显示（改回修改前的），完成
         * @param view
         */
        @Override
        public void onClick(View view) {
            final int pos = (int) view.getTag();
            actionOnItemClick(pos, new UpdateListener() {
                @Override
                public void onSuccess() {
                    Alerter.create(UserDetailInfoActivity.this)
                            .setTitle("提示")
                            .setText(mListItems.get(pos).key+"已修改")
                            .show();
                }
                @Override
                public void onFailure(int i, String s) {
                    Alerter.create(UserDetailInfoActivity.this)
                            .setTitle("提示:"+mListItems.get(pos).key+"修改失败")
                            .setText(s)
                            .show();
                    refreshData();
                    UserDetailInfoAdapter.this.notifyDataSetChanged();
                }
            });
        }

        class MyViewHolder extends ViewHolder{
            View mRootView;
            TextView mType;
            TextView mTypeValue;
            MyViewHolder(View view) {
                super(view);
                mRootView = view;
                mType = (TextView)view.findViewById(R.id.item_type);
                mTypeValue = (TextView)view.findViewById(R.id.type_value);
            }
        }
    }
}
