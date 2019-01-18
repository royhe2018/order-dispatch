package com.sdkj.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import com.sdkj.dispatch.domain.po.User;
import com.sdkj.dispatch.domain.vo.PushMessage;
import com.sdkj.dispatch.util.Constant;
import com.sdkj.dispatch.util.JsonUtil;

import cn.jpush.api.device.OnlineStatus;

@Component(Constant.MQ_TAG_DISPATCH_ORDER)
public class OrderDispatchMessageListener implements MessageListener{
	
	@Autowired
	private JPushComponent pushComponent;
	
	@Autowired
	private UserMapper userMapper;
	
	Logger logger = LoggerFactory.getLogger(OrderDispatchMessageListener.class);
	@Override
	public Action consume(Message message, ConsumeContext context) {
		logger.info("receive start");
    	try {
    		String orderInfo = new String(message.getBody(),"UTF-8");
    		logger.info("message: " +orderInfo);
    		Map<String,Object> param = new HashMap<String,Object>();
    		param.put("userType",Constant.USER_TYPE_DRIVER);
    		List<User> driverList = userMapper.findUserList(param);
    		List<String> registrionIdList=new ArrayList<String>();
    		if(driverList!=null && driverList.size()>0){
    			for(User item:driverList){
    				if(pushComponent.isDriverOnline(item.getRegistrionId())) {
    					registrionIdList.add(item.getRegistrionId());
    				}
    			}
    		}
    		Map<String,String> extraInfo = new HashMap<String,String>();
    		extraInfo.put("messageType", Constant.MQ_TAG_DISPATCH_ORDER);
    		extraInfo.put("message",orderInfo);
    		PushMessage pushMessage = new PushMessage();
			pushMessage.setMessageType(Constant.MQ_TAG_DISPATCH_ORDER);
			JsonNode node = JsonUtil.convertStrToJson(orderInfo);
			Iterator<String> keyIt = node.fieldNames();
			while(keyIt.hasNext()){
				String key = keyIt.next();
				pushMessage.addMessage(key, node.get(key).asText());
			}
    		pushComponent.sentAndroidAndIosExtraInfoPush("您有新订单", "请及时接单", registrionIdList, pushMessage.toString());
    	}catch(Exception e) {
    		logger.error("消息派发异常", e);
    	}
    	logger.info("receive end");
        return Action.CommitMessage; 
	}

}
