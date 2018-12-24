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
import com.sdkj.dispatch.domain.po.OrderRoutePoint;
import com.sdkj.dispatch.domain.po.User;
import com.sdkj.dispatch.domain.vo.PushMessage;
import com.sdkj.dispatch.util.Constant;
import com.sdkj.dispatch.util.JsonUtil;
@Component(Constant.MQ_TAG_LEAVE_ROUTE_POINT)
public class RoutePointPlaceLeaveMessageListener implements MessageListener {
	
	Logger logger = LoggerFactory.getLogger(RoutePointPlaceLeaveMessageListener.class);
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
			String pointId = mqInfoNode.get("pointId").asText();
			String orderId = mqInfoNode.get("orderId").asText();
			Map<String,Object> queryMap = new HashMap<String,Object>();
			queryMap.put("orderId", orderId);
			List<OrderRoutePoint> pointList = orderRoutePointMapper.findRoutePointList(queryMap);
			queryMap.clear();
			queryMap.put("id", orderId);
			OrderInfo order = orderInfoMapper.findSingleOrder(queryMap);
			queryMap.clear();
			queryMap.put("id", order.getUserId());
			User user = userMapper.findSingleUser(queryMap);
			if(pointList!=null && pointList.size()>1) {
				String title = "";
				String content = "";
				List<String> registrionIdList = new ArrayList<String>();
				registrionIdList.add(user.getRegistrionId());
				PushMessage pushMessage = new PushMessage();
				pushMessage.addMessage("orderId", orderId);
				pushMessage.addMessage("pointId", pointId);
				OrderRoutePoint startPoint = pointList.get(0);
				OrderRoutePoint endPoint = pointList.get(pointList.size()-1);
				if(pointId.equals(startPoint.getId()+"")) {
					title="司机离开装货点";
					content = "司机已离开装货点"+startPoint.getPlaceName()+",准备前往目的地!";
					pushMessage.addMessage("placeName", startPoint.getPlaceName());
				}else if(pointId.equals(endPoint.getId()+"")) {
					title="司机离开目的地";
					content = "司机已离开目的地"+startPoint.getPlaceName()+",请确认货物已签收!";
					pushMessage.addMessage("placeName", endPoint.getPlaceName());
				}else {
					title="司机离开途经点";
					for(OrderRoutePoint point:pointList) {
						if(pointId.equals(point.getId()+"")) {
							content = "司机已离开途经点"+point.getPlaceName()+",正前往下一目的地!";
							pushMessage.addMessage("placeName", point.getPlaceName());
						}
					}
				}
				pushMessage.setMessageType(Constant.MQ_TAG_LEAVE_ROUTE_POINT);
				pushComponent.sentAndroidAndIosExtraInfoPushForCustomer(title, content, registrionIdList, pushMessage.toString());
			}
			return Action.CommitMessage;
		}catch(Exception e) {
			logger.error("推送司机离开途经点异常", e);
			if(message.getReconsumeTimes()>3){
				return Action.CommitMessage;
			}
			return Action.ReconsumeLater;
		}
	}

}
