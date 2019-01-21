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
import com.sdkj.dispatch.dao.orderInfo.OrderInfoMapper;
import com.sdkj.dispatch.dao.orderRoutePoint.OrderRoutePointMapper;
import com.sdkj.dispatch.dao.user.UserMapper;
import com.sdkj.dispatch.domain.po.OrderInfo;
import com.sdkj.dispatch.domain.po.OrderRoutePoint;
import com.sdkj.dispatch.domain.po.User;
import com.sdkj.dispatch.domain.vo.PushMessage;
import com.sdkj.dispatch.util.Constant;
import com.sdkj.dispatch.util.JsonUtil;

@Component(Constant.MQ_TAG_DISPATCH_ORDER)
public class OrderDispatchMessageListener implements MessageListener{
	
	@Autowired
	private JPushComponent pushComponent;
	
	@Autowired
	private UserMapper userMapper;
	
	@Autowired
	private OrderInfoMapper orderInfoMapper;
	
	@Autowired
	private OrderRoutePointMapper orderRoutePointMapper;
	
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
    					logger.info(item.getNickName()+" : "+item.getAccount()+" is online");
    					registrionIdList.add(item.getRegistrionId());
    				}else{
    					logger.info(item.getNickName()+" : "+item.getAccount()+" is not online");
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
			if(node!=null && node.has("orderId")){
				String orderId = node.get("orderId").asText();
				param.clear();
				param.put("id", orderId);
				OrderInfo order = orderInfoMapper.findSingleOrder(param);
				param.clear();
				param.put("orderId", orderId);
				List<OrderRoutePoint> routePointList = orderRoutePointMapper.findRoutePointList(param);
				if(order!=null && routePointList!=null){
					OrderRoutePoint startPoint = routePointList.get(0);
					OrderRoutePoint endPoint = routePointList.get(routePointList.size() - 1);
					String broadcastContent = "从"+startPoint.getPlaceName()+"至"+endPoint.getPlaceName()+",共"+order.getTotalDistance()+"公里,总费用："+node.get("totalFee").asText()+"元";
					pushMessage.addMessage("broadcastContent", broadcastContent);
				}
			}
    		pushComponent.sentAndroidAndIosExtraInfoPush("您有新订单", "请及时接单", registrionIdList, pushMessage.toString());
    	}catch(Exception e) {
    		logger.error("消息派发异常", e);
    	}
    	logger.info("receive end");
        return Action.CommitMessage; 
	}

}
