package com.sdkj.dispatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.fasterxml.jackson.databind.JsonNode;
import com.sdkj.dispatch.dao.noticeRecord.NoticeRecordMapper;
import com.sdkj.dispatch.dao.orderInfo.OrderInfoMapper;
import com.sdkj.dispatch.dao.orderRoutePoint.OrderRoutePointMapper;
import com.sdkj.dispatch.dao.user.UserMapper;
import com.sdkj.dispatch.domain.po.NoticeRecord;
import com.sdkj.dispatch.domain.po.OrderInfo;
import com.sdkj.dispatch.domain.po.OrderRoutePoint;
import com.sdkj.dispatch.domain.po.User;
import com.sdkj.dispatch.domain.vo.PushMessage;
import com.sdkj.dispatch.util.Constant;
import com.sdkj.dispatch.util.DateUtilLH;
import com.sdkj.dispatch.util.JsonUtil;
@Component(Constant.MQ_TAG_ARRIVE_ROUTE_POINT)
public class RoutePointPlaceArrvieMessageListener implements MessageListener {
	Logger logger = LoggerFactory.getLogger(RoutePointPlaceArrvieMessageListener.class);
	@Autowired
	private OrderInfoMapper orderInfoMapper;
	@Autowired
	private OrderRoutePointMapper orderRoutePointMapper;
	@Autowired
	private JPushComponent pushComponent;
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private NoticeRecordServiceImpl noticeRecordServiceImpl;
	
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
			queryMap.put("id", order.getDriverId());
			User driver = userMapper.findSingleUser(queryMap);
			if(pointList!=null && pointList.size()>1) {
				String title = "";
				String content = "";
				String userIds = "";
				Set<String> registrionIdSet = new HashSet<String>();
				String firsReceivUserId = "";
				queryMap.clear();
				queryMap.put("id", order.getUserId());
				User orderUser = userMapper.findSingleUser(queryMap);
				for(OrderRoutePoint point:pointList){
					logger.info("point id:"+point.getId()+";dealUserId:"+point.getDealUserId());
					if(point.getDealUserId()!=null){
						queryMap.clear();
						queryMap.put("id", point.getDealUserId());
						User dealUser = userMapper.findSingleUser(queryMap);
						registrionIdSet.add(dealUser.getRegistrionId());
						userIds += dealUser.getId()+";";
						if(StringUtils.isEmpty(firsReceivUserId)){
							firsReceivUserId = dealUser.getRegistrionId();
						}
					}else{
						registrionIdSet.add(orderUser.getRegistrionId());
						if(StringUtils.isEmpty(firsReceivUserId)){
							firsReceivUserId = orderUser.getRegistrionId();
						}
						userIds += orderUser.getId()+";";
					}
				}
				List<String> registrionIdList = new ArrayList<String>(registrionIdSet);
				PushMessage pushMessage = new PushMessage();
				pushMessage.addMessage("orderId", orderId);
				pushMessage.addMessage("pointId", pointId);
				pushMessage.addMessage("mapTerminalId", driver.getMapTerminalId());
				pushMessage.addMessage("mapServiceId", "8914");
				OrderRoutePoint startPoint = pointList.get(0);
				OrderRoutePoint endPoint = pointList.get(pointList.size()-1);
				
				if(pointId.equals(startPoint.getId()+"")) {
					title="司机到达装货点";
					content = "司机已到达装货点"+startPoint.getPlaceName()+",请准备装货!";
					pushMessage.addMessage("placeName", startPoint.getPlaceName());
					registrionIdList.clear();
					registrionIdList.add(firsReceivUserId);
				}else if(pointId.equals(endPoint.getId()+"")) {
					title="司机到达目的地";
					content = "司机已到达目的地"+endPoint.getPlaceName()+",请您安排好时间准备卸货!";
					pushMessage.addMessage("placeName", endPoint.getPlaceName());
				}else {
					title="司机到达途经点";
					for(OrderRoutePoint point:pointList) {
						if(pointId.equals(point.getId()+"")) {
							content = "司机已到达目的地"+point.getPlaceName()+",请您安排好时间准备卸货!";
							pushMessage.addMessage("placeName", point.getPlaceName());
						}
					}
				}
				pushMessage.setMessageType(Constant.MQ_TAG_ARRIVE_ROUTE_POINT);
				pushComponent.sentAndroidAndIosExtraInfoPushForCustomer(title, content, registrionIdList, pushMessage.toString());
				NoticeRecord target = new NoticeRecord();
				target.setContent(content);
				target.setExtraMessage(pushMessage.toString());
				target.setMessageType(Constant.MQ_TAG_ARRIVE_ROUTE_POINT);
				target.setNoticeRegisterIds(JsonUtil.convertObjectToJsonStr(registrionIdList));
				target.setNoticeUserIds(userIds);
				target.setOrderId(Integer.valueOf(orderId));
				target.setMessageId(message.getMsgID());
				target.setCreateTime(DateUtilLH.getCurrentTime());
				noticeRecordServiceImpl.saveNoticeRecord(target);
			}
			return Action.CommitMessage;
		}catch(Exception e) {
			logger.error("推送司机到达途经点异常", e);
			return Action.ReconsumeLater;
		}
	}

}
