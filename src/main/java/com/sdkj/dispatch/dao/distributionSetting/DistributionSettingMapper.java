package com.sdkj.dispatch.dao.distributionSetting;

import java.util.List;
import java.util.Map;

import com.sdkj.dispatch.domain.po.DistributionSetting;

public interface DistributionSettingMapper {
	//DistributionSetting findSingleDistributionSetting(Map<String,Object> param);
    List<DistributionSetting> findDistributionSettingList(Map<String,Object> param);

}