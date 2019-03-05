package com.sdkj.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
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
import com.sdkj.dispatch.dao.orderInfo.OrderInfoMapper;
import com.sdkj.dispatch.dao.user.UserMapper;
import com.sdkj.dispatch.domain.po.OrderInfo;
import com.sdkj.dispatch.domain.po.User;
import com.sdkj.dispatch.domain.vo.PushMessage;
import com.sdkj.dispatch.util.Constant;
import com.sdkj.dispatch.util.JsonUtil;

@Component(Constant.MQ_TAG_PAYMENT_ORDER)
public class OrderPaymentRemarkListener implements MessageListener{
	Logger logger = LoggerFactory.getLogger(OrderPaymentRemarkListener.class);
	
	@Autowired
	private OrderInfoMapper orderInfoMapper;
	
	@Autowired
	private UserMapper userMapper;
	
	@Autowired
	private JPushComponent pushComponent;
	@Override
	public Action consume(Message message, ConsumeContext arg1) {
		try {
			String mqInfo = new String(message.getBody(),"UTF-8");
			JsonNode mqInfoNode = JsonUtil.convertStrToJson(mqInfo);
			String orderId = mqInfoNode.get("orderId").asText();
			Map<String,Object> queryMap = new HashMap<String,Object>();
			queryMap.put("id", orderId);
			OrderInfo orderInfo = orderInfoMapper.findSingleOrder(queryMap);
			 
			queryMap.clear();
			queryMap.put("id", orderInfo.getDriverId());
			User driver = userMapper.findSingleUser(queryMap);
			if(driver!=null) {
				String title = "您接的订单已支付";
				String content = "您接的订单用户已支付,请及时完成订单!";
				List<String> registrionIdList = new ArrayList<String>();
				registrionIdList.add(driver.getRegistrionId());
				PushMessage pushMessage = new PushMessage();
				pushMessage.setMessageType(Constant.MQ_TAG_PAYMENT_ORDER);
				pushMessage.addMessage("orderId", orderId);
				pushMessage.addMessage("userId", orderInfo.getUserId()+"");
				pushComponent.sentAndroidAndIosExtraInfoPush(title, content, registrionIdList, pushMessage.toString());
			}
			return Action.CommitMessage;
		}catch(Exception e){
			logger.error("订单已支付提醒异常", e);
			return Action.ReconsumeLater;
		}
	}
}
