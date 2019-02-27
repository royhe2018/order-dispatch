package com.sdkj.dispatch.service;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
	
	@PostConstruct
	public void initPushComponent(){
		logger.info("appKey:"+appKey);
		logger.info("masterSecret:"+masterSecret);
		jpushClient = new JPushClient(masterSecret, appKey);
		customerJpushClient = new JPushClient(customerMasterSecret, customerAppKey);
	}
	
	public void sentAndroidAndIosExtraInfoPush(String title,String content,List<String> registrionIdList,String extraInfo){
		try{
			logger.info("extraInfo is:"+extraInfo);
			logger.info("registrionIdList is:"+JsonUtil.convertObjectToJsonStr(registrionIdList));
			PushPayload pushPayload = JPushPayloadUtil.buildPushObjectWithExtra(title, content, registrionIdList, extraInfo);
			PushResult result =jpushClient.sendPush(pushPayload);
			logger.info("message is"+result.msg_id+":");
			if(result.error!=null){
				logger.info("error info:"+result.error.getMessage());
			}else{
				logger.info("success!");
			}
		}catch(Exception e){
			logger.error("推送消息费常", e);
		}
	}
	
	public void sentAndroidAndIosExtraInfoPushForCustomer(String title,String content,List<String> registrionIdList,String extraInfo){
		try{
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
		}catch(Exception e){
			logger.error("推送消息费常", e);
		}
	}
	
	
	public void buildPushObjectSelfDefineMessageForDriver(List<String> registrionIdList,String extraInfo){
		try{
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
