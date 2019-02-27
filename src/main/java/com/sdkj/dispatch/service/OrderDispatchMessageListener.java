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
import com.sdkj.dispatch.dao.driverInfo.DriverInfoMapper;
import com.sdkj.dispatch.dao.orderInfo.OrderInfoMapper;
import com.sdkj.dispatch.dao.orderRoutePoint.OrderRoutePointMapper;
import com.sdkj.dispatch.dao.user.UserMapper;
import com.sdkj.dispatch.domain.po.DriverInfo;
import com.sdkj.dispatch.domain.po.NoticeRecord;
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
	
	@Autowired
	private DriverInfoMapper driverInfoMapper;
	
	@Autowired
	private OrderFeeItemServiceImpl orderFeeItemServiceImpl;
	
	@Autowired
	private NoticeRecordServiceImpl noticeRecordServiceImpl;
	
	Logger logger = LoggerFactory.getLogger(OrderDispatchMessageListener.class);
	@Override
	public Action consume(Message message, ConsumeContext context) {
		logger.info("receive start");
    	try {
    		String orderInfo = new String(message.getBody(),"UTF-8");
    		logger.info("message: " +orderInfo);
    		Map<String,Object> param = new HashMap<String,Object>();
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
			String orderId = node.get("orderId").asText();
			param.clear();
			param.put("id", orderId);
			OrderInfo order = orderInfoMapper.findSingleOrder(param);
			
    		for(int driverType=1;driverType<4;driverType++) {
    			String notifyUserIds = "";
    			param.clear();
        		param.put("status", 2);
        		param.put("driverType", driverType);
        		param.put("onDutyStatus", 2);
        		param.put("registerCity", order.getCityName());
        		logger.info("param:"+JsonUtil.convertObjectToJsonStr(param));
        		List<DriverInfo> driverList = driverInfoMapper.findDriverInfoList(param);
        		List<String> registrionIdList=new ArrayList<String>();
        		if(driverList!=null && driverList.size()>0){
        			logger.info("driverList.size():"+driverList.size());
        			for(DriverInfo item:driverList){
        				param.clear();
        				param.put("id", item.getUserId());
        				User driverUser = userMapper.findSingleUser(param);
        				if(pushComponent.isDriverOnline(driverUser.getRegistrionId())) {
        					logger.info(driverUser.getNickName()+" : "+driverUser.getAccount()+" is online");
        					notifyUserIds +=driverUser.getId()+";";
        					registrionIdList.add(driverUser.getRegistrionId());
        				}else{
        					logger.info(driverUser.getNickName()+" : "+driverUser.getAccount()+" is not online");
        				}
        			}
        			
        			param.clear();
    				param.put("orderId", orderId);
    				List<OrderRoutePoint> routePointList = orderRoutePointMapper.findRoutePointList(param);
    				if(order!=null && routePointList!=null){
    					OrderRoutePoint startPoint = routePointList.get(0);
    					OrderRoutePoint endPoint = routePointList.get(routePointList.size() - 1);
    					String totalDriverFee = orderFeeItemServiceImpl.caculateOrderFee(order, driverType);
    					String broadcastContent = "从"+startPoint.getPlaceName()+"至"+endPoint.getPlaceName()+",共"+order.getTotalDistance()+"公里,总费用："+totalDriverFee+"元";
    					pushMessage.addMessage("broadcastContent", broadcastContent);
    					pushMessage.addMessage("totalFee", totalDriverFee);
    				}
            		pushComponent.sentAndroidAndIosExtraInfoPush("您有新订单", "请及时接单", registrionIdList, pushMessage.toString());
            		NoticeRecord target = new NoticeRecord();
    				target.setContent("您有新订单请及时接单");
    				target.setExtraMessage(pushMessage.toString());
    				target.setMessageType(Constant.MQ_TAG_DISPATCH_ORDER);
    				target.setNoticeRegisterIds(JsonUtil.convertObjectToJsonStr(registrionIdList));
    				target.setNoticeUserIds(notifyUserIds);
    				target.setOrderId(Integer.valueOf(orderId));
    				noticeRecordServiceImpl.saveNoticeRecord(target);
    				Thread.sleep(2000);
        		}
    		}
    	}catch(Exception e) {
    		logger.error("消息派发异常", e);
    	}
    	logger.info("receive end");
        return Action.CommitMessage; 
	}

}
