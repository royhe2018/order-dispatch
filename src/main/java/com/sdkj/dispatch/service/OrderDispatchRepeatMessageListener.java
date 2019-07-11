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
import com.sdkj.dispatch.dao.driverInfo.DriverInfoMapper;
import com.sdkj.dispatch.dao.orderInfo.OrderInfoMapper;
import com.sdkj.dispatch.dao.orderRoutePoint.OrderRoutePointMapper;
import com.sdkj.dispatch.dao.user.UserMapper;
import com.sdkj.dispatch.domain.po.DriverInfo;
import com.sdkj.dispatch.domain.po.OrderInfo;
import com.sdkj.dispatch.domain.po.OrderRoutePoint;
import com.sdkj.dispatch.domain.po.User;
import com.sdkj.dispatch.domain.vo.PushMessage;
import com.sdkj.dispatch.util.Constant;
import com.sdkj.dispatch.util.HttpRequestUtil;
import com.sdkj.dispatch.util.JsonUtil;

@Component(Constant.MQ_TAG_REPEAT_DISPATCH_ORDER)
public class OrderDispatchRepeatMessageListener implements MessageListener{
	
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
 
	
	Logger logger = LoggerFactory.getLogger(OrderDispatchRepeatMessageListener.class);
	@Override
	public Action consume(Message message, ConsumeContext context) {
		logger.info("dispatch repeat receive start");
    	try {
    		String orderInfo = new String(message.getBody(),"UTF-8");
    		logger.info("message: " +orderInfo);
    		JsonNode node = JsonUtil.convertStrToJson(orderInfo);
			String orderId = node.get("orderId").asText();
			Map<String,Object> param = new HashMap<String,Object>();
			param.put("id", orderId);
			OrderInfo order = orderInfoMapper.findSingleOrder(param);
			if(order.getCancleStatus()==null || order.getCancleStatus().intValue()==0){
				List<OrderRoutePoint> routePointList = orderRoutePointMapper.findRoutePointList(param);
				if(routePointList!=null && routePointList.size()>0){
					OrderRoutePoint startPoint = routePointList.get(0);
					String nearlyTerminalUrl = "http://www.shundaolahuo.com/map/find/nearly/terminal";
					param.clear();
					param.put("lon", startPoint.getLat());
					param.put("lat", startPoint.getLog());
					param.put("radius", 5000);
					JsonNode terminalResult = HttpRequestUtil.getForJsonResult(nearlyTerminalUrl, param);
					List<String> mapTerminalIdList = new ArrayList<String>();
					if(terminalResult!=null && terminalResult.has("data")&&terminalResult.get("data").size()>0){
						for(JsonNode item:terminalResult.get("data")){
							if(item.has("tid")){
								mapTerminalIdList.add(item.get("tid").asText());
							}
						}
					}
					if(mapTerminalIdList.size()>0){
						param.clear();
			    		param.put("status", 2);
			    		param.put("onDutyStatus", 2);
			    		param.put("mapTerminalIdList",mapTerminalIdList);
			    		List<Integer> vehicleTypeIdList = new ArrayList<Integer>();
		        		if(order.getVehicleTypeId().intValue()==5){
		        			vehicleTypeIdList.add(5);
		        			vehicleTypeIdList.add(9);
		        			vehicleTypeIdList.add(10);
		        		}else if(order.getVehicleTypeId().intValue()==6){
		        			vehicleTypeIdList.add(5);
		        			vehicleTypeIdList.add(6);
		        			vehicleTypeIdList.add(7);
		        			vehicleTypeIdList.add(8);
		        			vehicleTypeIdList.add(9);
		        			vehicleTypeIdList.add(10);
		        		}else if(order.getVehicleTypeId().intValue()==7){
		        			vehicleTypeIdList.add(5);
		        			vehicleTypeIdList.add(7);
		        			vehicleTypeIdList.add(8);
		        			vehicleTypeIdList.add(9);
		        			vehicleTypeIdList.add(10);
		        		}else if(order.getVehicleTypeId().intValue()==8){
		        			vehicleTypeIdList.add(5);
		        			vehicleTypeIdList.add(8);
		        			vehicleTypeIdList.add(9);
		        			vehicleTypeIdList.add(10);
		        		}else if(order.getVehicleTypeId().intValue()==9){
		        			vehicleTypeIdList.add(9);
		        		}else if(order.getVehicleTypeId().intValue()==10){
		        			vehicleTypeIdList.add(10);
		        		}
		        		param.put("vehicleTypeIdList", vehicleTypeIdList);
			    		logger.info("param:"+JsonUtil.convertObjectToJsonStr(param));
			    		List<DriverInfo> driverList = driverInfoMapper.findDriverInfoList(param);
						if(driverList!=null && driverList.size()>0){
							List<String> registrionIdListForDriver1=new ArrayList<String>();
				    		List<String> registrionIdListForDriver2=new ArrayList<String>();
				    		List<String> registrionIdListForDriver3=new ArrayList<String>();
				    		String notifyUserIdsForDriver1 = "";
				    		String notifyUserIdsForDriver2 = "";
				    		String notifyUserIdsForDriver3 = "";
			    			logger.info("driverList.size():"+driverList.size());
			    			for(DriverInfo item:driverList){
			    				param.clear();
			    				param.put("id", item.getUserId());
			    				User driverUser = userMapper.findSingleUser(param);
			    				if(pushComponent.isDriverOnline(driverUser.getRegistrionId())) {
		        					logger.info(driverUser.getNickName()+" : "+driverUser.getAccount()+" is online");
		        					if(item.getDriverType().equals("1")){
	        							notifyUserIdsForDriver1 +=driverUser.getId()+";";
	        							registrionIdListForDriver1.add(driverUser.getRegistrionId());
	        						}else if(item.getDriverType().equals("2")){
	        							notifyUserIdsForDriver2 +=driverUser.getId()+";";
	        							registrionIdListForDriver2.add(driverUser.getRegistrionId());
	        						}else if(item.getDriverType().equals("3")){
	        							notifyUserIdsForDriver3 +=driverUser.getId()+";";
	        							registrionIdListForDriver3.add(driverUser.getRegistrionId());
	        						} 
		        				}else{
		        					logger.info(driverUser.getNickName()+" : "+driverUser.getAccount()+" is not online");
		        				}
			    			}
			    			for(int driverType=1;driverType<4;driverType++){
			    				List<String> destRegistrationIdList = null;
			    				String userIds = null;
			    				if(driverType==1){
			    					destRegistrationIdList = registrionIdListForDriver1;
			    					userIds = notifyUserIdsForDriver1;
			    				}else if(driverType==2){
			    					destRegistrationIdList = registrionIdListForDriver2;
			    					userIds = notifyUserIdsForDriver2;
			    				}else if(driverType==3){
			    					destRegistrationIdList = registrionIdListForDriver3;
			    					userIds = notifyUserIdsForDriver3;
			    				}
			    				if(destRegistrationIdList.size()>0){
			        	    		PushMessage pushMessage = new PushMessage();
			        				pushMessage.setMessageType(Constant.MQ_TAG_DISPATCH_ORDER);
			        				logger.info("sendDispathOrderMessage start");
			        				pushMessage.addMessage("orderId", order.getId()+"");
			        				pushMessage.addMessage("useTime", order.getUseTruckTime());
			        				pushMessage.addMessage("totalDistance", order.getTotalDistance()+"");
			        				pushMessage.addMessage("remark", order.getRemark());
			        				param.clear();
			        				param.put("orderId", orderId);
			        				if(order!=null && routePointList!=null){
			        					OrderRoutePoint endPoint = routePointList.get(routePointList.size() - 1);
			        					String totalDriverFee = orderFeeItemServiceImpl.caculateOrderFee(order, driverType);
			        					String broadcastContent = "从"+startPoint.getPlaceName()+"至"+endPoint.getPlaceName()+",共"+order.getTotalDistance()+"公里,总费用："+totalDriverFee+"元";
			        					if(order.getServiceVehicleLevelId()!=null && order.getServiceVehicleLevelId().intValue()==2){
			        						broadcastContent = "返程车,"+broadcastContent;
			        					}
			        					pushMessage.addMessage("serviceLevel", order.getServiceVehicleLevelId()+"");
			        					pushMessage.addMessage("broadcastContent", broadcastContent);
			        					pushMessage.addMessage("startPointName", startPoint.getPlaceName());
			            				pushMessage.addMessage("startPointAddress", startPoint.getAddress());
			            				pushMessage.addMessage("startPointLocation", startPoint.getLat() + "," + startPoint.getLog());
			            				pushMessage.addMessage("endPointName", endPoint.getPlaceName());
			            				pushMessage.addMessage("endPointAddress", endPoint.getAddress());
			            				pushMessage.addMessage("endPointLocation", endPoint.getLat() + "," + endPoint.getLog());
			            				pushMessage.addMessage("totalFee", totalDriverFee);
			            				pushMessage.addMessage("startFee", totalDriverFee);
			            				pushMessage.addMessage("extraFee", totalDriverFee);
			            				pushMessage.addMessage("attachFee", totalDriverFee);
			            				pushMessage.addMessage("payStatus", "0");
			        				}
			                		pushComponent.sentAndroidAndIosExtraInfoPush("您有新订单", "请及时接单", registrionIdListForDriver1, pushMessage,userIds,orderId,message.getMsgID());
			        			}
			    			}
						}
					}
				}
			}
    	}catch(Exception e) {
    		logger.error("消息派发异常", e);
    	}
    	logger.info("receive end");
        return Action.CommitMessage; 
	}

}
