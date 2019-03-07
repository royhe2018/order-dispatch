package com.sdkj.dispatch.service;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sdkj.dispatch.domain.po.NoticeRecord;
import com.sdkj.dispatch.domain.vo.PushMessage;
import com.sdkj.dispatch.util.Constant;
import com.sdkj.dispatch.util.DateUtilLH;
import com.sdkj.dispatch.util.JPushPayloadUtil;
import com.sdkj.dispatch.util.JsonUtil;

import cn.jpush.api.JPushClient;
import cn.jpush.api.device.OnlineStatus;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.PushPayload;

@Component
public class JPushComponent {
	Logger logger = LoggerFactory.getLogger(JPushComponent.class);
	@Value("${jpush.sdlh.appKey}")
	private String appKey;
	@Value("${jpush.sdlh.secret}")
	private String masterSecret;
	private static JPushClient jpushClient;
	
	
	@Value("${jpush.sdlh.customer.appKey}")
	private String customerAppKey;
	@Value("${jpush.sdlh.customer.secret}")
	private String customerMasterSecret;
	private static JPushClient customerJpushClient;
	
	@Autowired
	private NoticeRecordServiceImpl noticeRecordServiceImpl;
	
	@PostConstruct
	public void initPushComponent(){
		logger.info("appKey:"+appKey);
		logger.info("masterSecret:"+masterSecret);
		jpushClient = new JPushClient(masterSecret, appKey);
		customerJpushClient = new JPushClient(customerMasterSecret, customerAppKey);
	}
	
	public void sentAndroidAndIosExtraInfoPush(String title,String content,List<String> registrionIdList,PushMessage pushMessage,String notifyUserIds,String orderId,String queMessageId){
		try{
			String extraInfo = pushMessage.toString();
			logger.info("extraInfo is:"+extraInfo);
			logger.info("registrionIdList is:"+JsonUtil.convertObjectToJsonStr(registrionIdList));
			PushPayload pushPayload = JPushPayloadUtil.buildPushObjectWithExtraForDriver(title, content, registrionIdList, extraInfo);
			PushResult result =jpushClient.sendPush(pushPayload);
			//jpushClient.sendPush(pushPayload);
			logger.info("message is"+result.msg_id+":");
			if(result.error!=null){
				logger.info("error info:"+result.error.getMessage());
			}else{
				logger.info("success!");
			}
			NoticeRecord target = new NoticeRecord();
			target.setContent(content);
			target.setExtraMessage(extraInfo);
			target.setMessageType(pushMessage.getMessageType());
			target.setNoticeRegisterIds(JsonUtil.convertObjectToJsonStr(registrionIdList));
			target.setNoticeUserIds(notifyUserIds);
			target.setOrderId(Integer.valueOf(orderId));
			target.setMessageId(queMessageId);
			target.setCreateTime(DateUtilLH.getCurrentTime());
			target.setJpushMessageId(result.msg_id+"");
			noticeRecordServiceImpl.saveNoticeRecord(target);
			
		}catch(Exception e){
			logger.error("推送消息费常", e);
		}
	}
	
	public void sentAndroidAndIosExtraInfoPushForCustomer(String title,String content,List<String> registrionIdList,PushMessage pushMessage,String notifyUserIds,String orderId,String queMessageId){
		try{
			String extraInfo = pushMessage.toString();
			logger.info("extraInfo is:"+extraInfo);
			logger.info("registrionIdList is:"+JsonUtil.convertObjectToJsonStr(registrionIdList));
			PushPayload pushPayload = JPushPayloadUtil.buildPushObjectWithExtra(title, content, registrionIdList, extraInfo);
			PushResult result =customerJpushClient.sendPush(pushPayload);
			logger.info("message is"+result.msg_id+":");
			if(result.error!=null){
				logger.info("error info:"+result.error.getMessage());
			}else{
				logger.info("success!");
			}
			NoticeRecord target = new NoticeRecord();
			target.setContent(content);
			target.setExtraMessage(extraInfo);
			target.setMessageType(pushMessage.getMessageType());
			target.setNoticeRegisterIds(JsonUtil.convertObjectToJsonStr(registrionIdList));
			target.setNoticeUserIds(notifyUserIds);
			target.setOrderId(Integer.valueOf(orderId));
			target.setMessageId(queMessageId);
			target.setCreateTime(DateUtilLH.getCurrentTime());
			target.setJpushMessageId(result.msg_id+"");
			noticeRecordServiceImpl.saveNoticeRecord(target);
		}catch(Exception e){
			logger.error("推送消息费常", e);
		}
	}
	
	
	public void buildPushObjectSelfDefineMessageForDriver(List<String> registrionIdList,PushMessage pushMessage,String notifyUserIds,String orderId,String queMessageId){
		try{
			String extraInfo = pushMessage.toString();
			logger.info("extraInfo is:"+extraInfo);
			logger.info("registrionIdList is:"+JsonUtil.convertObjectToJsonStr(registrionIdList));
			PushPayload pushPayload = JPushPayloadUtil.buildPushObjectSelfDefineMessageWithExtras(registrionIdList, extraInfo);
			PushResult result =jpushClient.sendPush(pushPayload);
			logger.info("message is"+result.msg_id+":");
			if(result.error!=null){
				logger.info("error info:"+result.error.getMessage());
			}else{
				logger.info("success!");
			}
			NoticeRecord target = new NoticeRecord();
			target.setExtraMessage(extraInfo);
			target.setMessageType(pushMessage.getMessageType());
			target.setNoticeRegisterIds(JsonUtil.convertObjectToJsonStr(registrionIdList));
			target.setNoticeUserIds(notifyUserIds);
			target.setOrderId(Integer.valueOf(orderId));
			target.setMessageId(queMessageId);
			target.setCreateTime(DateUtilLH.getCurrentTime());
			target.setJpushMessageId(result.msg_id+"");
			noticeRecordServiceImpl.saveNoticeRecord(target);
		}catch(Exception e){
			logger.error("推送消息费常", e);
		}
	}
	
	public boolean isUserOnline(String registrionId) {
		try {
			Map<String, OnlineStatus> userOnline = customerJpushClient.getUserOnlineStatus(registrionId);
			if(userOnline!=null) {
				return userOnline.get(registrionId).getOnline();
			}
		}catch(Exception e) {
			logger.error("获取设备在线状态异常", e);
		}
		return false;
	}
	
	public boolean isDriverOnline(String registrionId) {
		try {
			Map<String, OnlineStatus> userOnline = jpushClient.getUserOnlineStatus(registrionId);
			if(userOnline!=null) {
				return userOnline.get(registrionId).getOnline();
			}
		}catch(Exception e) {
			logger.error("获取司机设备在线状态异常", e);
		}
		return false;
	}
}
