package com.sdkj.dispatch.service;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.aliyun.openservices.ons.api.Consumer;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.PropertyKeyConst;

@Component
public class MessageConsumerComponent {
	
	Logger logger = LoggerFactory.getLogger(MessageConsumerComponent.class);	
	@Value("${ali.mq.consumerId}")
	private String consumerId;
	@Value("${ali.mq.accessKey}")
	private String accessKey;
	@Value("${ali.mq.secretKey}")
	private String secretKey;
	@Value("${ali.mq.onsaddr}")
	private String onsaddr;
 
	@Autowired
	private TotalMessageListener totalMessageListener;
	@PostConstruct
	public void initConsumer(){
		Properties properties = new Properties();
        properties.put(PropertyKeyConst.ConsumerId, consumerId);
        properties.put(PropertyKeyConst.AccessKey, accessKey);
        properties.put(PropertyKeyConst.SecretKey, secretKey);
        properties.put(PropertyKeyConst.ONSAddr,onsaddr);
       // 集群订阅方式 (默认)
       // properties.put(PropertyKeyConst.MessageModel, PropertyValueConst.CLUSTERING);
       // 广播订阅方式
       // properties.put(PropertyKeyConst.MessageModel, PropertyValueConst.BROADCASTING);
       Consumer consumer = ONSFactory.createConsumer(properties);
       consumer.subscribe("Topic_SDLH_Dispatch_Vehicle", "OrderMessage",totalMessageListener);
       //consumer.subscribe("Topic_SDLH_Dispatch_Vehicle", Constant.MQ_TAG_CANCLE_ORDER,orderCancleMessageListener);
       //consumer.subscribe("Topic_SDLH_Dispatch_Vehicle", Constant.MQ_TAG_SIGN_RECEIVE_ORDER,orderSignReceiveMessageListener);
       //consumer.subscribe("Topic_SDLH_Dispatch_Vehicle", Constant.MQ_TAG_STOWAGE_ORDER,orderStowageMessageListener);
       //consumer.subscribe("Topic_SDLH_Dispatch_Vehicle", Constant.MQ_TAG_TAKED_ORDER,orderTakedMessageListener);
       //consumer.subscribe("Topic_SDLH_Dispatch_Vehicle", Constant.MQ_TAG_ARRIVE_ROUTE_POINT,routePointPlaceArrvieMessageListener);
       //consumer.subscribe("Topic_SDLH_Dispatch_Vehicle", Constant.MQ_TAG_LEAVE_ROUTE_POINT,routePointPlaceLeaveMessageListener);
       consumer.start();
       logger.info("consumer started");
	}
}