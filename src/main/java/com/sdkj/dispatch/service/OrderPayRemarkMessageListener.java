package com.sdkj.dispatch.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.jiguang.common.utils.StringUtils;

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
import com.sdkj.dispatch.util.DateUtilLH;
import com.sdkj.dispatch.util.JsonUtil;

@Component(Constant.MQ_TAG_PAY_REMARK)
public class OrderPayRemarkMessageListener implements MessageListener{

	Logger logger = LoggerFactory.getLogger(OrderPayRemarkMessageListener.class);
	
	@Autowired
	private OrderInfoMapper orderInfoMapper;
	@Autowired
	private JPushComponent pushComponent;
	
	@Autowired
	private UserMapper userMapper;
	
	@Autowired
	private OrderRoutePointMapper orderRoutePointMapper;
	@Override
	public Action consume(Message message, ConsumeContext arg1) {
		try {
			String mqInfo = new String(message.getBody(),"UTF-8");
			JsonNode mqInfoNode = JsonUtil.convertStrToJson(mqInfo);
			String orderId = mqInfoNode.get("orderId").asText();
			String payFeeType = mqInfoNode.get("payFeeType").asText();
			Map<String,Object> queryMap = new HashMap<String,Object>();
			queryMap.put("id", orderId);
			OrderInfo orderInfo = orderInfoMapper.findSingleOrder(queryMap);
			queryMap.clear();
			queryMap.put("id", orderInfo.getUserId());
			User user = userMapper.findSingleUser(queryMap);
			if(user!=null) {
				String title = "费用支付提醒";
				String content = "您的订单已完成,是否去支付费用？";
				String preContent ="您的订单已完成:";
				if(StringUtils.isNotEmpty(payFeeType) && "2".equals(payFeeType)){
					content = "您的订单有新的费用补充,是否去支付费用？";
					preContent = "您的订单有新的费用补充:";
				}
				List<String> registrionIdList = new ArrayList<String>();
				registrionIdList.add(user.getRegistrionId());
				PushMessage pushMessage = new PushMessage();
				pushMessage.setMessageType(Constant.MQ_TAG_PAY_REMARK);
				pushMessage.addMessage("orderId", orderInfo.getId()+"");
				queryMap.clear();
				queryMap.put("orderId", orderInfo.getId());
				List<Map<String,Object>> feeStatusList = orderInfoMapper.findOrderFeeByPayStatus(queryMap);
				if(feeStatusList!=null && feeStatusList.size()>0){
					Float totalAmount =0f;
					Float totalPaidAmount =0f;
					Float totalForPayAmount = 0f;
					for(Map<String,Object> item:feeStatusList){
						Float amount = Float.valueOf(item.get("money").toString());
						if("0".equals(item.get("payStatus").toString())){
							totalForPayAmount += amount;
						}else{
							totalPaidAmount += amount;
						}
						totalAmount +=amount;
					}
					pushMessage.addMessage("totalAmount", totalAmount+"");
					pushMessage.addMessage("totalPaidAmount", totalPaidAmount+"");
					pushMessage.addMessage("totalForPayAmount", totalForPayAmount+"");
				}
				pushMessage.addMessage("totalDistance", orderInfo.getTotalDistance()+"");
				
				queryMap.clear();
				queryMap.put("orderId", orderInfo.getId());
				List<OrderRoutePoint> routePointList = orderRoutePointMapper.findRoutePointList(queryMap);
				Date startTime = new Date();
				Date endTime = new Date();
				if(routePointList!=null){
					String startTimeStr = routePointList.get(0).getArriveTime();
					if(StringUtils.isNotEmpty(startTimeStr)){
						startTime = DateUtilLH.convertStr2Date(startTimeStr, "yyyy-MM-dd HH:mm:ss");
					}
					String endTimeStr = routePointList.get(routePointList.size()-1).getArriveTime();
					if(StringUtils.isNotEmpty(endTimeStr)){
						endTime = DateUtilLH.convertStr2Date(endTimeStr, "yyyy-MM-dd HH:mm:ss");
					}
				}
				pushMessage.addMessage("totalTimes", (endTime.getTime()-startTime.getTime())/(1000*60)+"");
				pushMessage.addMessage("preContent", preContent);
				pushMessage.addMessage("afterContent", "是否去支付费用?");
				pushComponent.sentAndroidAndIosExtraInfoPushForCustomer(title, content, registrionIdList, pushMessage,user.getId()+"",orderId,message.getMsgID());
			}
			return Action.CommitMessage;
		}catch(Exception e) {
			logger.error("订单支付提醒异常", e);
			return Action.ReconsumeLater;
		}
	}

}
