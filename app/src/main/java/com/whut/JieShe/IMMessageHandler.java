package com.whut.JieShe;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;

import com.whut.JieShe.bean.MyUser;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.event.MessageEvent;
import cn.bmob.newim.event.OfflineMessageEvent;
import cn.bmob.newim.listener.BmobIMMessageHandler;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.GetListener;

import static android.content.Context.NOTIFICATION_SERVICE;

public class IMMessageHandler extends BmobIMMessageHandler {

    private BmobIMMessageHandler handler = null;
    private Context mContext;

    //标志是否显示通知
    private boolean mNotification = true;

    private static final IMMessageHandler instance = new IMMessageHandler();

    private IMMessageHandler() {

    }

    @Override
    public void onMessageReceive(final MessageEvent event) {
        updateUserInfo(event);
        if(handler!=null){
            handler.onMessageReceive(event);
        }
        if(!mNotification){
            return;
        }
        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);

        Intent intent = new Intent(mContext, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("BmobIMConversation",event.getConversation());
        intent.putExtras(bundle);

        mBuilder.setContentTitle(event.getFromUserInfo().getName())//设置通知栏标题\
                .setAutoCancel(true)
                .setContentText(event.getMessage().getContent()) //设置通知栏显示内容
                .setContentIntent(PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)) //设置通知栏点击意图
                .setTicker(event.getFromUserInfo().getName()+":"+event.getMessage().getContent()) //通知首次出现在通知栏，带上升动画效果的
                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
                .setOngoing(false)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
                .setDefaults(Notification.DEFAULT_VIBRATE)//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合
                //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission
                .setSmallIcon(R.mipmap.ic_launcher);//设置通知小ICON

        mNotificationManager.notify(0, mBuilder.build());
    }

    public void updateUserInfo(MessageEvent event){
        final BmobIMConversation conversation = event.getConversation();
        final BmobIMUserInfo info = event.getFromUserInfo();
        String username =info.getName();
        String title = conversation.getConversationTitle();
        //sdk内部，将新会话的会话标题用objectId表示，因此需要比对用户名和会话标题--单聊，后续会根据会话类型进行判断
//        if(!username.equals(title)) {
            BmobQuery<MyUser> query = new BmobQuery<MyUser>();
            query.getObject(mContext, info.getUserId(), new GetListener<MyUser>() {
                @Override
                public void onSuccess(MyUser myUser) {
                    String name = myUser.getUsername();
                    String avatar = myUser.getAvatarUrl();
                    conversation.setConversationIcon(avatar);
                    conversation.setConversationTitle(name);
                    info.setName(name);
                    info.setAvatar(avatar);
//                    //更新用户资料
                    BmobIM.getInstance().updateUserInfo(info);
                }

                @Override
                public void onFailure(int i, String s) {

                }
            });
//        }
    }

    @Override
    public void onOfflineReceive(final OfflineMessageEvent offlineMessageEvent) {
        //每次调用connect方法时会查询一次离线消息，如果有，此方法会被调用
        Map<String,List<MessageEvent>> map = offlineMessageEvent.getEventMap();
        Iterator<String> iterator = map.keySet().iterator();
        while(iterator.hasNext()){
            List<MessageEvent> events = map.get(iterator.next());
            if(events!=null && events.size()>0){
                updateUserInfo(events.get(0));
                break;
            }
        }

        if(handler!=null){
            handler.onOfflineReceive(offlineMessageEvent);
        }
    }

    public void setHandler(BmobIMMessageHandler handler) {
        this.handler = handler;
    }

    public static IMMessageHandler getInstance() {
        return instance;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public boolean isNotification() {
        return mNotification;
    }

    public void setNotification(boolean mNotification) {
        this.mNotification = mNotification;
    }
}