package com.sdkj.dispatch.dao.driverInfo;

import java.util.Map;

import com.sdkj.dispatch.domain.po.DriverInfo;

public interface DriverInfoMapper {
    DriverInfo findSingleDriver(Map<String,Object> param);
}