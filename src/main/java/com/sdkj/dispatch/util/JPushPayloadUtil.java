package com.sdkj.dispatch.util;

import java.util.List;

import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;

import com.google.gson.JsonObject;

public class JPushPayloadUtil {
    public static PushPayload buildPushObjectWithExtra(String title,String content,List<String> registrationIdList,String extraInfo) {
    	JsonObject intent = new JsonObject();
    	intent.addProperty("url", "intent:#Intent;component=com.jiguang.push/com.shundao.shundaolahuo.activity.OpenClickActivity;end");
    	PushPayload payload = PushPayload.newBuilder()
                .setPlatform(Platform.android_ios())
                .setAudience(Audience.registrationId(registrationIdList))
                .setNotification(Notification.newBuilder()
                        .setAlert(content)
                        .addPlatformNotification(AndroidNotification.newBuilder()
                                .addExtra("extra", extraInfo)
                                .setTitle(title)
                                .setUriActivity("com.shundao.shundaolahuo.activity.OpenClickActivity")
                                //.setIntent(intent)
                                .build())
                        .addPlatformNotification(IosNotification.newBuilder()
                                .incrBadge(1)
                                .addExtra("extra", extraInfo).build())
                        .build())
                 .setMessage(Message.newBuilder()
                         .setMsgContent(content)
                         .setTitle(title)
                         .addExtra("extra",extraInfo)
                         .build())       
                .build();
        
        
        
        return payload;
    }
    
 
    
    
    public static PushPayload buildPushObjectSelfDefineMessageWithExtras(List<String> registrationIdList,String extraInfo) {
        return PushPayload.newBuilder()
                .setPlatform(Platform.android_ios())
                .setAudience(Audience.registrationId(registrationIdList))
                .setMessage(Message.newBuilder()
                        .setMsgContent("self define message")
                        .addExtra("extra",extraInfo)
                        .build())
                .build();
    }
}
