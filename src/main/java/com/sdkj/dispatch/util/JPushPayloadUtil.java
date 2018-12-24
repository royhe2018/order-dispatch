package com.sdkj.dispatch.util;

import java.util.List;

import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;

public class JPushPayloadUtil {
    public static PushPayload buildPushObjectWithExtra(String title,String content,List<String> registrationIdList,String extraInfo) {
        PushPayload payload = PushPayload.newBuilder()
                .setPlatform(Platform.android_ios())
                .setAudience(Audience.registrationId(registrationIdList))
                .setNotification(Notification.newBuilder()
                        .setAlert(content)
                        .addPlatformNotification(AndroidNotification.newBuilder()
                                .addExtra("extra", extraInfo)
                                .setTitle(title)
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
}
