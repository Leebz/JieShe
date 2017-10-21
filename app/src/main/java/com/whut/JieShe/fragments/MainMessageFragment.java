package com.whut.JieShe.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.whut.JieShe.ChatActivity;
import com.whut.JieShe.R;
import com.whut.JieShe.adapter.QuickUserMessageAdapter;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.event.MessageEvent;
import cn.bmob.newim.event.OfflineMessageEvent;
import cn.bmob.newim.listener.ConversationListener;
import cn.bmob.v3.exception.BmobException;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainMessageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainMessageFragment extends Fragment implements BaseQuickAdapter.OnItemLongClickListener {

    private OnFragmentInteractionListener mListener;

    private RecyclerView mRecyclerView;
    private QuickUserMessageAdapter userMessageAdapter;


    public MainMessageFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static MainMessageFragment newInstance(OnFragmentInteractionListener listener) {
        MainMessageFragment fragment = new MainMessageFragment();
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
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main_message, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        mRecyclerView.addItemDecoration(new RecycleViewDivider(getContext(), LinearLayoutManager.VERTICAL, R.drawable.recyclerview_divider));


        userMessageAdapter = new QuickUserMessageAdapter(getContext(),new ArrayList<BmobIMConversation>());
        userMessageAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                BmobIMUserInfo userInfo = new BmobIMUserInfo();
                userInfo.setUserId(userMessageAdapter.getData().get(position).getConversationId());
                userInfo.setName(userMessageAdapter.getData().get(position).getConversationTitle());
                userInfo.setAvatar(userMessageAdapter.getData().get(position).getConversationIcon());
                BmobIM.getInstance().startPrivateConversation(userInfo, new ConversationListener() {
                    @Override
                    public void done(BmobIMConversation c, BmobException e) {
                        if (e == null) {
                            Intent intent = new Intent(getContext(), ChatActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("BmobIMConversation",c);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        } else {
                            Log.e("ChatActivity", e.getMessage() + "(" + e.getErrorCode() + ")");
                        }
                    }
                });
            }
        });
        userMessageAdapter.setOnItemLongClickListener(this);
        mRecyclerView.setAdapter(userMessageAdapter);

        List<BmobIMConversation> conversations = BmobIM.getInstance().loadAllConversation();
        if(conversations != null){
            userMessageAdapter.addData(conversations);
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        List<BmobIMConversation> conversations = BmobIM.getInstance().loadAllConversation();
        if(conversations==null){
            return;
        }
        userMessageAdapter.getData().clear();
        userMessageAdapter.addData(conversations);
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

    public void newMessage(OfflineMessageEvent offlineMessageEvent){
    }

    public void newMessage(MessageEvent messageEvent){
        if(userMessageAdapter==null){
            return;
        }
        List<BmobIMConversation> conversations = BmobIM.getInstance().loadAllConversation();
        if(conversations==null){
            return;
        }
        userMessageAdapter.getData().clear();
        userMessageAdapter.addData(conversations);
    }

    @Override
    public boolean onItemLongClick(BaseQuickAdapter adapter, View view, final int position) {
        DialogPlus dialog = DialogPlus.newDialog(getContext())
                .setContentHolder(new ViewHolder(R.layout.dialog_delete_this_conversation))
                .setGravity(Gravity.BOTTOM)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(DialogPlus dialog, View view) {
                        dialog.dismiss();
                        BmobIM.getInstance().deleteConversation(userMessageAdapter.getData().get(position));
                        userMessageAdapter.remove(position);
                    }
                })
                .create();
        dialog.show();
        return true;
    }
}
