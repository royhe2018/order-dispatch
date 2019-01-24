package com.sdkj.dispatch.dao.orderInfo;

import java.util.List;
import java.util.Map;

import com.sdkj.dispatch.domain.po.OrderInfo;

public interface OrderInfoMapper {
    int deleteById(Long id);
    
    int insert(OrderInfo record);
    
    public OrderInfo findSingleOrder(Map<String,Object> param);

    public List<OrderInfo> findOrderList(Map<String,Object> param);
    
    List<Map<String,Object>> findOrderInfoListDisplay(Map<String,Object> param);
    
    int updateById(OrderInfo record);
    
    Map<String,Object> findOrderFeeDistribute(Map<String,Object> param);
    
    List<Map<String,Object>> findOrderFeeByPayStatus(Map<String,Object> param);
}