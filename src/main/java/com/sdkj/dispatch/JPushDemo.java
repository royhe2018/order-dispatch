package com.sdkj.dispatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jiguang.common.resp.DefaultResult;
import cn.jpush.api.JPushClient;
import cn.jpush.api.device.OnlineStatus;

import com.sdkj.dispatch.util.JPushPayloadUtil;
import com.sdkj.dispatch.util.JsonUtil;

public class JPushDemo {
	Logger logger = LoggerFactory.getLogger(JPushDemo.class);
	private static final String appKey = "6b03d0e0df24348828c821b9";
	private static final String masterSecret = "a71e13a30296e7dfde9df9e2";
	private static final String REGISTRATION_ID1="160a3797c85f58ca937";
	private static JPushClient jpushClient = new JPushClient(masterSecret, appKey);
	public static void main(String[] args) {
		try {
			//DefaultResult result =  jpushClient.bindMobile(REGISTRATION_ID1, "15129222933");
			//System.out.println("Got result " + result);
			Map<String, OnlineStatus> result =  jpushClient.getUserOnlineStatus(REGISTRATION_ID1);
			List<String> registList = new ArrayList<String>();
			registList.add("121c83f7600de048dba");
			Map<String,String> extraInfo = new HashMap<String,String>();
			extraInfo.put("test", "152");
			JPushPayloadUtil.buildPushObjectWithExtra("测试Title", "测试Content", registList, JsonUtil.convertObjectToJsonStr(extraInfo));
			System.out.println(result.toString());
		}catch(Exception e) {
			e.printStackTrace();
		}

		
	}
	
	public void bindMobile() {
		try {
			DefaultResult result =  jpushClient.bindMobile(REGISTRATION_ID1, "15129222933");
			System.out.println("Got result " + result);
		} catch (APIConnectionException e) {
			e.printStackTrace();
			logger.error("Connection error. Should retry later. ", e);
		} catch (APIRequestException e) {
			e.printStackTrace();
			logger.error("Error response from JPush server. Should review and fix it. ", e);
			logger.info("HTTP Status: " + e.getStatus());
			logger.info("Error Code: " + e.getErrorCode());
			logger.info("Error Message: " + e.getErrorMessage());
		}
	}
}
