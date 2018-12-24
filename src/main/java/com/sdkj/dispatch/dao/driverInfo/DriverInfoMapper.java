package com.sdkj.dispatch.dao.driverInfo;

import com.sdkj.dispatch.domain.po.DriverInfo;

public interface DriverInfoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(DriverInfo record);

    int insertSelective(DriverInfo record);

    DriverInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(DriverInfo record);

    int updateByPrimaryKey(DriverInfo record);
}