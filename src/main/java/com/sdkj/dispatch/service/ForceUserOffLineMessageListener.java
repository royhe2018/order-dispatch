package com.sdkj.dispatch.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.fasterxml.jackson.databind.JsonNode;
import com.sdkj.dispatch.dao.user.UserMapper;
import com.sdkj.dispatch.domain.vo.PushMessage;
import com.sdkj.dispatch.util.Constant;
import com.sdkj.dispatch.util.JsonUtil;

@Component(Constant.MQ_TAG_FORCE_OFFLINE)
public class ForceUserOffLineMessageListener implements MessageListener{
	
	@Autowired
	private JPushComponent pushComponent;
	
	@Autowired
	private UserMapper userMapper;
	
	Logger logger = LoggerFactory.getLogger(ForceUserOffLineMessageListener.class);
	@Override
	public Action consume(Message message, ConsumeContext context) {
		logger.info("receive start");
    	try {
    		String mqInfo = new String(message.getBody(),"UTF-8");
			JsonNode mqInfoNode = JsonUtil.convertStrToJson(mqInfo);
			String userId = mqInfoNode.get("userId").asText();
			Integer userType = Integer.valueOf(mqInfoNode.get("userType").asText());
			String registrionId = mqInfoNode.get("registrionId").asText();
			String title = "下线通知";
			String content = "您的账号已在其他设备登录，您已下线";
			List<String> registrionIdList = new ArrayList<String>();
			registrionIdList.add(registrionId);
			PushMessage pushMessage = new PushMessage();
			pushMessage.setMessageType(Constant.MQ_TAG_FORCE_OFFLINE);
			pushMessage.addMessage("userId", userId);
			if(Constant.USER_TYPE_DRIVER==userType.intValue()) {
				if(pushComponent.isDriverOnline(registrionId)) {
					pushComponent.sentAndroidAndIosExtraInfoPush(title, content, registrionIdList, pushMessage.toString());
				}
			}else {
				if(pushComponent.isUserOnline(registrionId)) {
					pushComponent.sentAndroidAndIosExtraInfoPushForCustomer(title, content, registrionIdList, pushMessage.toString());
				}
			}
    	}catch(Exception e) {
    		logger.error("消息派发异常", e);
    	}
    	logger.info("receive end");
        return Action.CommitMessage; 
	}

}
