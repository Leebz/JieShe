package com.whut.JieShe;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.whut.JieShe.adapter.QuickMessageListAdapter;
import com.whut.JieShe.bean.BmobIMMessageWrapper;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMTextMessage;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.core.BmobIMClient;
import cn.bmob.newim.event.MessageEvent;
import cn.bmob.newim.listener.BmobIMMessageHandler;
import cn.bmob.newim.listener.MessageSendListener;
import cn.bmob.newim.listener.MessagesQueryListener;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener, BaseQuickAdapter.OnItemChildClickListener {

    private EditText mChatEdit;
    private BootstrapButton mSendButton;
    private BmobIMConversation mConversation;
    private RecyclerView mMessageList;
    private QuickMessageListAdapter mMessageAdapter;
    private BmobIMUserInfo mUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        BmobIMConversation c = (BmobIMConversation) bundle.getSerializable("BmobIMConversation");
        mConversation = BmobIMConversation.obtain(BmobIMClient.getInstance(),c);

        setContentView(R.layout.activity_chat);
        mUserInfo = BmobIM.getInstance().getUserInfo(mConversation.getConversationId());
        setTitle(mUserInfo.getName());

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mChatEdit = (EditText) findViewById(R.id.send_text_edit);
        mChatEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text = mChatEdit.getText().toString();
                if(TextUtils.isEmpty(text)){
                    mSendButton.setEnabled(false);
                    mSendButton.setShowOutline(true);
                }else{
                    mSendButton.setEnabled(true);
                    mSendButton.setShowOutline(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mChatEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
//                    mMessageList.scrollToPosition(mMessages.size()-1);
                }
            }
        });
        mSendButton = (BootstrapButton) findViewById(R.id.send);
        mSendButton.setEnabled(false);
        mSendButton.setShowOutline(true);
        mSendButton.setOnClickListener(this);

        mMessageList = (RecyclerView) findViewById(R.id.message_list);
        mMessageList.setLayoutManager(new LinearLayoutManager(this));
        mMessageAdapter = new QuickMessageListAdapter(this,new ArrayList<BmobIMMessageWrapper>());
        mMessageList.setAdapter(mMessageAdapter);
        mMessageAdapter.setOnItemChildClickListener(this);

        IMMessageHandler.getInstance().setHandler(new BmobIMMessageHandler(){
            @Override
            public void onMessageReceive(MessageEvent messageEvent) {
                super.onMessageReceive(messageEvent);
                if(messageEvent.getFromUserInfo().getUserId().equals(mConversation.getConversationId())){
                    mMessageAdapter.addData(new BmobIMMessageWrapper(BmobIMMessageWrapper.RECEIVE,messageEvent.getMessage()));
                    mMessageList.scrollToPosition(mMessageAdapter.getData().size()-1);
                    IMMessageHandler.getInstance().setNotification(false);
                } else {
                    IMMessageHandler.getInstance().setNotification(true);
                }
            }
        });
        mConversation.queryMessages(null, 50, new MessagesQueryListener() {
            @Override
            public void done(List<BmobIMMessage> list, BmobException e) {
                if (e == null) {
                    if (null != list && list.size() > 0) {
                        for(BmobIMMessage msg:list){
                            BmobIMMessageWrapper wrapper = new BmobIMMessageWrapper();
                            wrapper.setMessage(msg);
                            if (msg.getFromId().equals(BmobUser.getCurrentUser(ChatActivity.this).getObjectId())){
                                wrapper.setItemType(BmobIMMessageWrapper.SEND);
                            }else{
                                wrapper.setItemType(BmobIMMessageWrapper.RECEIVE);
                            }
                            mMessageAdapter.addData(wrapper);
                        }
                        mMessageList.scrollToPosition(mMessageAdapter.getData().size()-1);
                    }
                } else {
                    Log.d("ChatActivity", e.getMessage() + "(" + e.getErrorCode() + ")");
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        IMMessageHandler.getInstance().setNotification(true);
        mConversation.updateLocalCache();
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        String msgText = mChatEdit.getText().toString();
        if(TextUtils.isEmpty(msgText)){
            return;
        }
        BmobIMTextMessage message = new BmobIMTextMessage();
        message.setContent(msgText);
        mConversation.sendMessage(message, new MessageSendListener() {
            @Override
            public void onStart(BmobIMMessage bmobIMMessage) {
                super.onStart(bmobIMMessage);
                Log.d("ChatActivity:onStart", bmobIMMessage.getContent());
            }

            @Override
            public void done(BmobIMMessage bmobIMMessage, BmobException e) {
                if(e==null){
                    mMessageAdapter.addData(new BmobIMMessageWrapper(BmobIMMessageWrapper.SEND,bmobIMMessage));
                    mMessageList.scrollToPosition(mMessageAdapter.getData().size()-1);
                    mChatEdit.setText("");
                }else{
                    Log.e("ChatActivity", e.getMessage());
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        IMMessageHandler.getInstance().setNotification(false);
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        BmobIMMessageWrapper wrapper = mMessageAdapter.getData().get(position);
        BmobIMMessage message = wrapper.getMessage();

        Intent intent = new Intent(ChatActivity.this, UserDetailInfoActivity.class);
        BmobUser user = new BmobUser();
        user.setObjectId(message.getFromId());
        intent.putExtra("user",user);
        startActivity(intent);
    }
}
