package com.sdkj.dispatch.dao.orderRoutePoint;

import java.util.List;
import java.util.Map;

import com.sdkj.dispatch.domain.po.OrderRoutePoint;

public interface OrderRoutePointMapper {
    int deleteByPrimaryKey(Long id);

    int insert(OrderRoutePoint record);

    OrderRoutePoint findSingleRoutePoint(Map<String,Object> param);
    
    List<OrderRoutePoint> findRoutePointList(Map<String,Object> param);
    
    int updateByPrimaryKeySelective(OrderRoutePoint record);

}