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
import com.sdkj.dispatch.domain.po.NoticeRecord;
import com.sdkj.dispatch.domain.po.OrderInfo;
import com.sdkj.dispatch.domain.po.User;
import com.sdkj.dispatch.domain.vo.PushMessage;
import com.sdkj.dispatch.util.Constant;
import com.sdkj.dispatch.util.JsonUtil;

@Component(Constant.MQ_TAG_DISMISS_ORDER)
public class OrderDismissListener implements MessageListener{
	
	@Autowired
	private JPushComponent pushComponent;
	
	@Autowired
	private UserMapper userMapper;
	
	@Autowired
	private OrderInfoMapper orderInfoMapper;
	
	@Autowired
	private NoticeRecordServiceImpl noticeRecordServiceImpl;
	
	Logger logger = LoggerFactory.getLogger(OrderDismissListener.class);
	@Override
	public Action consume(Message message, ConsumeContext context) {
		logger.info("receive start");
    	try { 
    		String mqInfo = new String(message.getBody(),"UTF-8");
			JsonNode mqInfoNode = JsonUtil.convertStrToJson(mqInfo);
			String orderId = mqInfoNode.get("orderId").asText();
			Map<String,Object> queryMap = new HashMap<String,Object>();
			queryMap.put("id", orderId);
			OrderInfo orderInfo = orderInfoMapper.findSingleOrder(queryMap);
			queryMap.clear();
			queryMap.put("orderId", orderId);
			queryMap.put("messageType", Constant.MQ_TAG_DISPATCH_ORDER);
			List<NoticeRecord> noticeList = noticeRecordServiceImpl.findNoticeRecord(queryMap);
			
			if(noticeList!=null && noticeList.size()>0) {
				List<String> registrionIdList = new ArrayList<String>();
				String userIds="";
				String takedDriverRegisterId = "";
				String takedUserId = "";
				if(orderInfo.getDriverId()!=null) {
					queryMap.clear();
					queryMap.put("id", orderInfo.getDriverId());
					User driver = userMapper.findSingleUser(queryMap);
					takedDriverRegisterId = driver.getRegistrionId();
					takedUserId = driver.getId()+",";
				}
				for(NoticeRecord record:noticeList) {
					JsonNode noticeRegisterIdList = JsonUtil.convertStrToJson(record.getNoticeRegisterIds());
					if(noticeRegisterIdList!=null && noticeRegisterIdList.size()>0) {
						for(int i=0;i<noticeRegisterIdList.size();i++) {
							registrionIdList.add(noticeRegisterIdList.get(i).asText());
						}
					}
					userIds +=record.getNoticeUserIds();
				}
				registrionIdList.remove(takedDriverRegisterId);
				PushMessage pushMessage = new PushMessage();
				pushMessage.setMessageType(Constant.MQ_TAG_DISMISS_ORDER);
				pushMessage.addMessage("orderId", orderId);
				pushComponent.sentAndroidAndIosExtraInfoPushForCustomer("", "", registrionIdList, pushMessage.toString());
        		NoticeRecord target = new NoticeRecord();
				target.setContent("订单提醒销毁");
				target.setExtraMessage(pushMessage.toString());
				target.setMessageType(Constant.MQ_TAG_DISMISS_ORDER);
				target.setNoticeRegisterIds(JsonUtil.convertObjectToJsonStr(registrionIdList));
				target.setNoticeUserIds(takedUserId.replaceAll(takedUserId, ""));
				target.setOrderId(Integer.valueOf(orderId));
				noticeRecordServiceImpl.saveNoticeRecord(target);
			}
    	}catch(Exception e) {
    		logger.error("消息派发异常", e);
    	}
    	logger.info("receive end");
        return Action.CommitMessage; 
	}

}
