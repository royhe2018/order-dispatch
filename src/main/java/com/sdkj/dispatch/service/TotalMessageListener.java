package com.sdkj.dispatch.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.fasterxml.jackson.databind.JsonNode;
import com.sdkj.dispatch.App;
import com.sdkj.dispatch.domain.po.MsgQueRecord;
import com.sdkj.dispatch.util.JsonUtil;

@Component
public class TotalMessageListener implements MessageListener {
	Logger logger = LoggerFactory.getLogger(TotalMessageListener.class);
	
	@Autowired
	private MsgQueRecordServiceImpl msgQueRecordServiceImpl;
	@Override
	public Action consume(Message message, ConsumeContext context) {
		try{
			String mqInfo = new String(message.getBody(),"UTF-8");
			JsonNode mqInfoNode = JsonUtil.convertStrToJson(mqInfo);
			String messageType = mqInfoNode.get("messageType").asText();
			logger.info("messageType:"+messageType);
			
			msgQueRecordServiceImpl.updateMsgQueRecord(message);
			MessageListener subListener = (MessageListener)App.getBean(messageType); 
			return subListener.consume(message, context);
		}catch(Exception e){
			logger.error("转换异常", e);
			return Action.ReconsumeLater;
		}
	}

}
