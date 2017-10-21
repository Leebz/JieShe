package com.whut.JieShe;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.soundcloud.android.crop.Crop;
import com.whut.JieShe.bean.Organization;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

/*
 * 社团信息的展示和修改
 */
public class GroupInformationActivity extends AppCompatActivity implements BaseQuickAdapter.OnItemClickListener {

    private static final int REQUEST_CODE_PICK_IMAGE_LOGO = 1;
    private static final int REQUEST_CODE_PICK_IMAGE_BACKGROUND = 2;
    private static final int REQUEST_CODE_ASK_WRITE_EXTERNAL_STORAGE = 0;

    private RecyclerView mRecyclerView;
    private GroupDetailInfoAdapter mAdapter;
    private Organization mOrganization;

    private ImageView mOrganizationLogoView;
    private MovingImageView mOrganizationBackgroundView;

    private int mPickType = -1; //记录了当前选择的是LOGO还是背景

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_information);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();
        mOrganization = (Organization) bundle.getSerializable("organization");
        if (mOrganization == null) {
            this.finish();
            return;
        }

        List<ListItem> items = new ArrayList<>();
        items.add(new ListItem("名称", mOrganization.getName()));
        items.add(new ListItem("简介", mOrganization.getIntroduction()));

        StringBuffer sb = new StringBuffer();
        if (mOrganization.getTags() != null) {
            List<String> list = mOrganization.getTags();
            for (int i = 0; i < list.size(); i++) {
                sb.append(list.get(i));
                if(i != list.size()-1){
                    sb.append(",");
                }
            }
        }
        items.add(new ListItem("标签", sb.toString()));
        items.add(new ListItem("社团参与人数", String.valueOf(mOrganization.getTotalJoinedUser())));

        mRecyclerView = (RecyclerView) findViewById(R.id.group_detail_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter = new GroupDetailInfoAdapter(items));
        mAdapter.setOnItemClickListener(this);

        CollapsingToolbarLayout toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolbarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickToolbar();
            }
        });

        mOrganizationLogoView = (ImageView) findViewById(R.id.organization_logo);
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

        //加载LOGO
        Glide.with(this)
                .load(mOrganization.getLogoUrl())
                .placeholder(R.drawable.default_logo)
                .error(R.drawable.default_logo)
                .crossFade()
                .into(mOrganizationLogoView);
        //加载背景
        Glide.with(this)
                .load(mOrganization.getBackgroundUrl())
                .placeholder(R.drawable.user_info_background)
                .error(R.drawable.user_info_background)
                .crossFade()
                .into(mOrganizationBackgroundView);
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode!=RESULT_OK){
            return;
        }
        //选择图片之后，裁剪
        if(requestCode== REQUEST_CODE_PICK_IMAGE_LOGO ||requestCode==REQUEST_CODE_PICK_IMAGE_BACKGROUND){
            mPickType = requestCode;
            List<Uri> mSelected = Matisse.obtainResult(data);
            Uri uri = mSelected.get(0);

            if(requestCode == REQUEST_CODE_PICK_IMAGE_LOGO){
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
    
    private void pickAImage(int type){
        Matisse.from(this)
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

    /**
     * @return 运行时检查是否具有WRITE_EXTERNAL_STORAGE和READ_EXTERNAL_STORAGE权限
     */
    private boolean checkPermission(){
        if (Build.VERSION.SDK_INT >= 23) {
            int checkWriteExternalStoragePermission = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            );
            int checkReadExternalStoragePermission = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            );
            if(checkWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED ||
                    checkReadExternalStoragePermission != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(
                        this,
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
     * 弹出一个菜单让用户选择更换LOGO or 更换背景
     */
    private void onClickToolbar(){
        DialogPlus dialog = DialogPlus.newDialog(this)
                .setContentHolder(new com.orhanobut.dialogplus.ViewHolder(R.layout.dialog_organization_detail_info_menu))
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
                            case R.id.change_logo_item:
                                if(checkPermission()){
                                    pickAImage(REQUEST_CODE_PICK_IMAGE_LOGO);
                                }
                                break;
                        }
                    }
                })
                .create();
        dialog.show();
    }

    private void uploadFile(File file){
        final BmobFile bmobFile = new BmobFile(file);
        bmobFile.uploadblock(this, new UploadFileListener() {
            @Override
            public void onSuccess() {
                if(mPickType==GroupInformationActivity.REQUEST_CODE_PICK_IMAGE_LOGO){
                    Log.d("UserDetailInfoActivity", "LOGO上传完成");
                    updateLogoUrl(bmobFile.getUrl());
                }else if(mPickType==GroupInformationActivity.REQUEST_CODE_PICK_IMAGE_BACKGROUND){
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

    //更新社团LOGO的url，并将社团原先的LOGO文件删除
    private void updateLogoUrl(String url){
        if(!TextUtils.isEmpty(mOrganization.getLogoUrl())){
            BmobFile file = new BmobFile();
            file.setUrl(mOrganization.getLogoUrl());
            file.delete(this);
        }
        mOrganization.setLogoUrl(url);
        mOrganization.update(this, new UpdateListener() {
            @Override
            public void onSuccess() {
                Glide.with(GroupInformationActivity.this)
                        .load(mOrganization.getLogoUrl())
                        .placeholder(R.drawable.default_avatar)
                        .crossFade()
                        .into(mOrganizationLogoView);
                Alerter.create(GroupInformationActivity.this)
                        .setTitle("提示")
                        .setText("LOGO已经修改")
                        .show();
            }

            @Override
            public void onFailure(int i, String s) {
                Alerter.create(GroupInformationActivity.this)
                        .setTitle("提示:LOGO修改失败")
                        .setText(s)
                        .show();
            }
        });
    }

    //更新社团的背景的url，并将社团原先的背景文件删除
    private void updateBackgroundUrl(String url){
        if(!TextUtils.isEmpty(mOrganization.getBackgroundUrl())){
            Log.d("UserDetailInfoActivity", "删除原背景" + mOrganization.getBackgroundUrl());
            BmobFile file = new BmobFile();
            file.setUrl(mOrganization.getBackgroundUrl());
            file.delete(this);
        }
        mOrganization.setBackgroundUrl(url);
        mOrganization.update(this, new UpdateListener() {
            @Override
            public void onSuccess() {
                Glide.with(GroupInformationActivity.this)
                        .load(mOrganization.getBackgroundUrl())
                        .placeholder(R.drawable.user_info_background)
                        .crossFade()
                        .into(mOrganizationBackgroundView);
                Alerter.create(GroupInformationActivity.this)
                        .setTitle("提示")
                        .setText("背景已经修改")
                        .show();
            }

            @Override
            public void onFailure(int i, String s) {
                Alerter.create(GroupInformationActivity.this)
                        .setTitle("提示:背景修改失败")
                        .setText(s)
                        .show();
            }
        });
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        ListItem item = mAdapter.getItem(position);
        if(item==null){
            return;
        }
        if("名称".equals(item.key)){
            changeOrganizationName(item);
        }else if("简介".equals(item.key)){
            changeOrganizationIntro(item);
        }else if("标签".equals(item.key)){
            changeOrganizationTag(item);
        }
    }

    private void saveData(UpdateListener listener) {
        List<ListItem> items = mAdapter.getData();
        if (items.size()==0) {
            return;
        }
        mOrganization.setName(items.get(0).value);
        mOrganization.setIntroduction(items.get(1).value);
        String[] tags = items.get(2).value.split(",");
        mOrganization.getTags().clear();
        mOrganization.getTags().addAll(Arrays.asList(tags));
        mOrganization.update(this,listener);
    }

    private void saveData() {
        saveData(new UpdateListener() {
            @Override
            public void onSuccess() {
                Alerter.create(GroupInformationActivity.this)
                        .setTitle("保存成功")
                        .show();
            }

            @Override
            public void onFailure(int i, String s) {
                Alerter.create(GroupInformationActivity.this)
                        .setTitle("保存失败")
                        .setText(s)
                        .show();
            }
        });
    }

    private void changeOrganizationTag(final ListItem item) {
        new MaterialDialog.Builder(this)
                .title("标签")
                .content("请输入新标签（逗号隔开）:")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(item.value, item.value, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (TextUtils.isEmpty(input)) {
                            return;
                        }
                        item.value = input.toString();
                        mAdapter.notifyDataSetChanged();
                        saveData();
                    }
                }).show();
    }

    private void changeOrganizationIntro(final ListItem item) {
        new MaterialDialog.Builder(this)
                .title("简介")
                .content("请输入新简介:")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(item.value, item.value, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (TextUtils.isEmpty(input)) {
                            return;
                        }
                        item.value = input.toString();
                        mAdapter.notifyDataSetChanged();
                        saveData();
                    }
                }).show();
    }

    private void changeOrganizationName(final ListItem item) {
        new MaterialDialog.Builder(this)
                .title("请谨慎编辑名称")
                .content("请输入新名称:")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(item.value, item.value, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (TextUtils.isEmpty(input)) {
                            return;
                        }
                        item.value = input.toString();
                        mAdapter.notifyDataSetChanged();
                        saveData();
                    }
                }).show();
    }

    private class ListItem {
        String key;
        String value;
        boolean clickable;

        ListItem(String key, String value) {
            this(key, value, true);
        }

        ListItem(String key, String value, boolean clickable) {
            this.key = key;
            this.value = value;
            this.clickable = clickable;
        }
    }

    private class GroupDetailInfoAdapter extends BaseQuickAdapter<ListItem, BaseViewHolder> {

        GroupDetailInfoAdapter(List<ListItem> items){
            super(R.layout.user_detail_item,items);
        }

        @Override
        protected void convert(BaseViewHolder helper, ListItem item) {
            helper.setText(R.id.item_type,item.key)
                    .setText(R.id.type_value,item.value);
        }
    }
}
