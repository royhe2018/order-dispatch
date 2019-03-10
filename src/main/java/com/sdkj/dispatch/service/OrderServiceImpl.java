package com.sdkj.dispatch.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sdkj.dispatch.dao.orderInfo.OrderInfoMapper;
import com.sdkj.dispatch.domain.po.OrderInfo;
import com.sdkj.dispatch.util.DateUtilLH;

@Service
@Transactional
public class OrderServiceImpl {
	Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
	@Autowired
	private OrderInfoMapper orderInfoMapper;
	
	public boolean isDriverHasOrderRunning(Long userId){
		boolean result = false;
		List<Integer> validStatusList = new ArrayList<Integer>();
		validStatusList.add(1);
		validStatusList.add(2);
		validStatusList.add(3);
		validStatusList.add(4);
		validStatusList.add(5);
		validStatusList.add(6);
		validStatusList.add(8);
		//validStatusList.add(9);
		Map<String,Object> param=new HashMap<String,Object>();
		param.put("driverId",userId );
		param.put("cancleStatus", 0);
		param.put("validStatusList", validStatusList);
		List<OrderInfo> orderList = orderInfoMapper.findOrderList(param);
		if(orderList!=null && orderList.size()>0){
			Calendar ca = Calendar.getInstance();
			ca.add(Calendar.HOUR_OF_DAY, 2);
			String limitTime = DateUtilLH.convertDate2Str(ca.getTime(), "yyyy-MM-dd HH:mm:ss");
			for(OrderInfo item:orderList){
				if(limitTime.compareTo(item.getUseTruckTime())>=0){
					logger.info("userId:"+userId+" have order "+item.getId()+" is running");
					result = true;
					break;
				}
			}
		}
		return result;
	}
	
}
