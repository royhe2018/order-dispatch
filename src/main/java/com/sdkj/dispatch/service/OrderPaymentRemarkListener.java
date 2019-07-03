package com.sdkj.dispatch.service;

import java.io.UnsupportedEncodingException;
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
			sendPaymentMessage(message);
			sendDistributeFeeMessage(message);
			return Action.CommitMessage;
		}catch(Exception e){
			logger.error("订单已支付提醒异常", e);
			return Action.ReconsumeLater;
		}
	}

	private void sendPaymentMessage(Message message)
			throws UnsupportedEncodingException {
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
			String content = "您接的订单用户已支付,请注意接收新订单!";
			List<String> registrionIdList = new ArrayList<String>();
			registrionIdList.add(driver.getRegistrionId());
			PushMessage pushMessage = new PushMessage();
			pushMessage.setMessageType(Constant.MQ_TAG_PAYMENT_ORDER);
			pushMessage.addMessage("orderId", orderId);
			pushMessage.addMessage("userId", orderInfo.getUserId()+"");
			pushComponent.sentAndroidAndIosExtraInfoPush(title, content, registrionIdList, pushMessage,driver.getId()+"",orderId,message.getMsgID());
		}
	}
	
	private void sendDistributeFeeMessage(Message message) throws Exception{
		String mqInfo = new String(message.getBody(),"UTF-8");
		JsonNode mqInfoNode = JsonUtil.convertStrToJson(mqInfo);
		String orderId = mqInfoNode.get("orderId").asText();
		Map<String,Object> queryMap = new HashMap<String,Object>();
		queryMap.put("id", orderId);
		//OrderInfo orderInfo = orderInfoMapper.findSingleOrder(queryMap);
		queryMap.clear();
		queryMap.put("status", 1);//支付状态已付清
		queryMap.put("orderId", orderId);
		Map<String,Object> distributeFeeInfo = orderInfoMapper.findOrderFeeDistribute(queryMap);
		if(distributeFeeInfo.containsKey("driverId") && distributeFeeInfo.containsKey("driverFee")){
			queryMap.clear();
			queryMap.put("id", distributeFeeInfo.get("driverId"));
			User driver = userMapper.findSingleUser(queryMap);
			if(driver!=null) {
				String title = "您接的订单费用已入账";
				String content = "您接的订单已支付,费用合计："+distributeFeeInfo.get("driverFee")+"请注意查验!";
				List<String> registrionIdList = new ArrayList<String>();
				registrionIdList.add(driver.getRegistrionId());
				PushMessage pushMessage = new PushMessage();
				pushMessage.setMessageType(Constant.MQ_TAG_FEE_ACCOUNT);
				pushMessage.addMessage("orderFee", distributeFeeInfo.get("driverFee")+"");
				pushComponent.sentAndroidAndIosExtraInfoPush(title, content, registrionIdList, pushMessage,driver.getId()+"",orderId,message.getMsgID());
			}
		}
		
		if(distributeFeeInfo.containsKey("clientRefereeId") && distributeFeeInfo.containsKey("clientRefereeFee")){
			queryMap.clear();
			queryMap.put("id", distributeFeeInfo.get("clientRefereeId"));
			User clientReferee = userMapper.findSingleUser(queryMap);
			if(clientReferee!=null) {
				String accountMoney = distributeFeeInfo.get("clientRefereeFee")+"";
				Float money = Float.valueOf(accountMoney);
				if(money.floatValue()>0){
					String title = "您有新的推荐费入账";
					String content = "您推荐人订单已支付,本次入账合计："+accountMoney+"请注意查验!";
					List<String> registrionIdList = new ArrayList<String>();
					registrionIdList.add(clientReferee.getRegistrionId());
					PushMessage pushMessage = new PushMessage();
					pushMessage.setMessageType(Constant.MQ_TAG_FEE_ACCOUNT);
					pushMessage.addMessage("orderFee", distributeFeeInfo.get("clientRefereeFee")+"");
					if(clientReferee.getUserType().intValue()==2){
						pushComponent.sentAndroidAndIosExtraInfoPush(title, content, registrionIdList, pushMessage,clientReferee.getId()+"",orderId,message.getMsgID());
					}else{
						pushComponent.sentAndroidAndIosExtraInfoPushForCustomer(title, content, registrionIdList, pushMessage,clientReferee.getId()+"",orderId,message.getMsgID());
					}
				}
			}
		}
		
		if(distributeFeeInfo.containsKey("driverRefereeId") && distributeFeeInfo.containsKey("driverRefereeFee")){
			queryMap.clear();
			queryMap.put("id", distributeFeeInfo.get("driverRefereeId"));
			User driverReferee = userMapper.findSingleUser(queryMap);
			if(driverReferee!=null) {
				String accountMoney = distributeFeeInfo.get("driverRefereeFee")+"";
				Float money = Float.valueOf(accountMoney);
				if(money.floatValue()>0){
					String title = "您有新的推荐费入账";
					String content = "您推荐人订单已支付,本次入账合计："+accountMoney+"请注意查验!";
					List<String> registrionIdList = new ArrayList<String>();
					registrionIdList.add(driverReferee.getRegistrionId());
					PushMessage pushMessage = new PushMessage();
					pushMessage.setMessageType(Constant.MQ_TAG_FEE_ACCOUNT);
					pushMessage.addMessage("orderFee", distributeFeeInfo.get("driverRefereeFee")+"");
					if(driverReferee.getUserType().intValue()==2){
						pushComponent.sentAndroidAndIosExtraInfoPush(title, content, registrionIdList, pushMessage,driverReferee.getId()+"",orderId,message.getMsgID());
					}else{
						pushComponent.sentAndroidAndIosExtraInfoPushForCustomer(title, content, registrionIdList, pushMessage,driverReferee.getId()+"",orderId,message.getMsgID());
					}
				}
			}
		}
	}
}
