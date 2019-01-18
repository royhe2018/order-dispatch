package com.sdkj.dispatch.service;

import java.util.ArrayList;
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
import com.sdkj.dispatch.dao.user.UserMapper;
import com.sdkj.dispatch.domain.po.OrderInfo;
import com.sdkj.dispatch.domain.po.User;
import com.sdkj.dispatch.domain.vo.PushMessage;
import com.sdkj.dispatch.util.Constant;
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
				String content = "您接的订单已完成,是否去支付费用？";
				if(StringUtils.isNotEmpty(payFeeType) && "2".equals(payFeeType)){
					content = "您的订单有新的费用补充,是否去支付费用？";
				}
				List<String> registrionIdList = new ArrayList<String>();
				registrionIdList.add(user.getRegistrionId());
				PushMessage pushMessage = new PushMessage();
				pushMessage.setMessageType(Constant.MQ_TAG_PAY_REMARK);
				pushMessage.addMessage("orderId", orderInfo.getId()+"");
				pushComponent.sentAndroidAndIosExtraInfoPushForCustomer(title, content, registrionIdList, pushMessage.toString());
			}
			return Action.CommitMessage;
		}catch(Exception e) {
			logger.error("订单入账异常", e);
			return Action.ReconsumeLater;
		}
	}

}
