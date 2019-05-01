package com.sdkj.dispatch.dao.vehicleTypeInfo;

import java.util.List;
import java.util.Map;

import com.sdkj.dispatch.domain.po.VehicleTypeInfo;

public interface VehicleTypeInfoMapper {
    VehicleTypeInfo findSingleVehicleTypeInfo(Map<String,Object> param);
    List<VehicleTypeInfo> findVehicleTypeInfoList(Map<String,Object> param);
}