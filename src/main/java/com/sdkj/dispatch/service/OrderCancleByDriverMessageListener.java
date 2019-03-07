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
import com.sdkj.dispatch.dao.orderRoutePoint.OrderRoutePointMapper;
import com.sdkj.dispatch.dao.user.UserMapper;
import com.sdkj.dispatch.domain.po.OrderInfo;
import com.sdkj.dispatch.domain.po.User;
import com.sdkj.dispatch.domain.vo.PushMessage;
import com.sdkj.dispatch.util.Constant;
import com.sdkj.dispatch.util.JsonUtil;
@Component(Constant.MQ_TAG_CANCLE_ORDER_BY_DRIVER)
public class OrderCancleByDriverMessageListener implements MessageListener {
	
	Logger logger = LoggerFactory.getLogger(OrderCancleByDriverMessageListener.class);
	@Autowired
	private OrderInfoMapper orderInfoMapper;
 
	@Autowired
	private JPushComponent pushComponent;
	@Autowired
	private UserMapper userMapper;
	
	@Override
	public Action consume(Message message, ConsumeContext context) {
		try {
			String mqInfo = new String(message.getBody(),"UTF-8");
			JsonNode mqInfoNode = JsonUtil.convertStrToJson(mqInfo);
			String orderId = mqInfoNode.get("orderId").asText();
			Map<String,Object> queryMap = new HashMap<String,Object>();
			queryMap.put("id", orderId);
			OrderInfo orderInfo = orderInfoMapper.findSingleOrder(queryMap);
			if(orderInfo!=null && orderInfo.getCancleStatus().intValue()==Constant.ORDER_CANCLE_STATUS_CANCLE) {
				queryMap.clear();
				logger.info("orderId:"+orderId);
				logger.info("UserId:"+orderInfo.getUserId());
				queryMap.put("id", orderInfo.getUserId());
				User user = userMapper.findSingleUser(queryMap);
				if(user!=null) {
					logger.info("RegistrionId:"+user.getRegistrionId());
					String title = "您的订单已被取消";
					String content = "您的订单已取消,是否重新下单!";
					List<String> registrionIdList = new ArrayList<String>();
					registrionIdList.add(user.getRegistrionId());
					PushMessage pushMessage = new PushMessage();
					pushMessage.setMessageType(Constant.MQ_TAG_CANCLE_ORDER_BY_DRIVER);
					pushMessage.addMessage("orderId", orderId);
					pushMessage.addMessage("userId", orderInfo.getUserId()+"");
					queryMap.clear();
					queryMap.put("id", orderInfo.getDriverId());
					User driver = userMapper.findSingleUser(queryMap);
					pushMessage.addMessage("mapTerminalId", driver.getMapTerminalId());
					pushMessage.addMessage("mapServiceId", "8914");
					pushComponent.sentAndroidAndIosExtraInfoPushForCustomer(title, content, registrionIdList, pushMessage,user.getId()+"",orderId,message.getMsgID());
				}
			}
			return Action.CommitMessage;
		}catch(Exception e) {
			logger.error("订单取消异常", e);
			return Action.ReconsumeLater;
		}
	}

}
