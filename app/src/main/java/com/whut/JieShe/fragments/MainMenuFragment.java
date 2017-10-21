package com.whut.JieShe.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.whut.JieShe.AdminActivity;
import com.whut.JieShe.BmobIMApplication;
import com.whut.JieShe.MyActivityActivity;
import com.whut.JieShe.MyCollectionActivity;
import com.whut.JieShe.MyDynamicsActivity;
import com.whut.JieShe.R;
import com.whut.JieShe.SystemConfigActivity;
import com.whut.JieShe.UserDetailInfoActivity;
import com.whut.JieShe.bean.MyUser;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.GetListener;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainMenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainMenuFragment extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;
    private View mUserInfoBar;
    private TextView mUserNameView;
    private TextView mUserEmailView;

    private View mMyDynamicBar;
    private View mMyActivityBar;
    private View mMyCollectionBar;
    private View mSystemConfigBar;
    private View mAdminBar;

    private ImageView mUserAvatarView;

    private BmobUser user;

    public MainMenuFragment() {
        // Required empty public constructor
    }

    public static MainMenuFragment newInstance(OnFragmentInteractionListener listener) {
        MainMenuFragment fragment = new MainMenuFragment();
        fragment.mListener = listener;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        user = BmobUser.getCurrentUser(getContext());

        View rootView = inflater.inflate(R.layout.fragment_main_menu, container, false);
        mUserInfoBar = rootView.findViewById(R.id.user_info_bar);
        mUserAvatarView = (ImageView) rootView.findViewById(R.id.menu_user_pic);
        mUserNameView = (TextView) rootView.findViewById(R.id.menu_user_name);
        mUserEmailView = (TextView) rootView.findViewById(R.id.menu_user_email);

        mUserInfoBar.setOnClickListener(this);
        mMyDynamicBar = rootView.findViewById(R.id.my_dynamic_bar);
        mMyActivityBar = rootView.findViewById(R.id.my_activity_bar);
        mMyCollectionBar = rootView.findViewById(R.id.my_collection_bar);
        mAdminBar = rootView.findViewById(R.id.admin_bar);
        mSystemConfigBar = rootView.findViewById(R.id.system_config_bar);

        mMyDynamicBar.setOnClickListener(this);
        mMyActivityBar.setOnClickListener(this);
        mMyCollectionBar.setOnClickListener(this);
        mSystemConfigBar.setOnClickListener(this);
        //如果当前用户不是管理员就隐藏管理员菜单项
        if(!BmobIMApplication.IS_MANAGER){
            mAdminBar.setVisibility(View.GONE);
        }else{
            mAdminBar.setOnClickListener(this);
        }

        refreshView();

        return rootView;
    }

    private void refreshView() {
        if(user==null){
            mUserNameView.setText("请登陆");
            mUserEmailView.setVisibility(View.GONE);
            return;
        }
        BmobQuery<MyUser> query = new BmobQuery<MyUser>();
        query.getObject(getContext(), user.getObjectId(), new GetListener<MyUser>() {
            @Override
            public void onSuccess(MyUser myUser) {
                mUserNameView.setText(myUser.getUsername());
                mUserEmailView.setVisibility(View.VISIBLE);
                mUserEmailView.setText(myUser.getEmail());
                Glide.with(getContext())
                        .load(myUser.getAvatarUrl())
                        .centerCrop()
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .crossFade()
                        .into(mUserAvatarView);
            }

            @Override
            public void onFailure(int i, String s) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.my_dynamic_bar:
                onMyDynamicBarClick();
                break;
            case R.id.my_activity_bar:
                onMyActivityBarClick();
                break;
            case R.id.my_collection_bar:
                onMyCollectionBarClick();
                break;
            case R.id.system_config_bar:
                onSystemConfigBarClick();
                break;
            case R.id.admin_bar:
                onAdminBarClick();
                break;
            case R.id.user_info_bar:
                onUserInfoBarClick();
            default:
                break;
        }
    }

    private void onUserInfoBarClick(){
        Intent intent = new Intent(getContext(), UserDetailInfoActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user",BmobUser.getCurrentUser(getContext()));
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void onAdminBarClick() {
        Intent intent = new Intent(getActivity(), AdminActivity.class);
        startActivity(intent);
    }

    private void onSystemConfigBarClick() {
        Intent intent = new Intent(getActivity(),SystemConfigActivity.class);
        startActivity(intent);
    }

    private void onMyCollectionBarClick() {
        Intent intent = new Intent(getContext(), MyCollectionActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user",BmobUser.getCurrentUser(getContext()));
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void onMyActivityBarClick() {
        Intent intent = new Intent(getContext(), MyActivityActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user",BmobUser.getCurrentUser(getContext()));
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void onMyDynamicBarClick() {
        Intent intent = new Intent(getContext(), MyDynamicsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user",BmobUser.getCurrentUser(getContext()));
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
