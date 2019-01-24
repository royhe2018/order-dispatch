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
@Component(Constant.MQ_TAG_TAKED_ORDER)
public class OrderTakedMessageListener implements MessageListener {
	Logger logger = LoggerFactory.getLogger(OrderTakedMessageListener.class);
	@Autowired
	private OrderInfoMapper orderInfoMapper;
	@Autowired
	private OrderRoutePointMapper orderRoutePointMapper;
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
			if(orderInfo!=null && orderInfo.getStatus().intValue()==Constant.ORDER_STATUS_JIEDAN
					&& orderInfo.getDriverId()!=null) {
				queryMap.clear();
				queryMap.put("id", orderInfo.getDriverId());
				User driver = userMapper.findSingleUser(queryMap);
				
				queryMap.clear();
				queryMap.put("id", orderInfo.getUserId());
				User user = userMapper.findSingleUser(queryMap);
				if(driver!=null) {
					String title = "您的订单已被接单";
					String content = "您的订单已被接单,请准备装货!";
					List<String> registrionIdList = new ArrayList<String>();
					registrionIdList.add(user.getRegistrionId());
					PushMessage pushMessage = new PushMessage();
					pushMessage.setMessageType(Constant.MQ_TAG_TAKED_ORDER);
					pushMessage.addMessage("orderId", orderId);
					pushMessage.addMessage("userId", orderInfo.getUserId()+"");
					pushMessage.addMessage("driverId", orderInfo.getDriverId()+"");
					pushMessage.addMessage("driverName", driver.getNickName());
					pushMessage.addMessage("driverPhone", driver.getAccount());
					pushMessage.addMessage("mapTerminalId", driver.getMapTerminalId());
					pushMessage.addMessage("mapServiceId", "8914");
					pushComponent.sentAndroidAndIosExtraInfoPushForCustomer(title, content, registrionIdList, pushMessage.toString());
				}
			}
			return Action.CommitMessage;
		}catch(Exception e) {
			logger.error("接单消息推送异常", e);
			return Action.ReconsumeLater;
		}
	}

}
