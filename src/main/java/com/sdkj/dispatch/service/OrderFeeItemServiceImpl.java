package com.sdkj.dispatch.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sdkj.dispatch.dao.distributionSetting.DistributionSettingMapper;
import com.sdkj.dispatch.dao.orderFeeItem.OrderFeeItemMapper;
import com.sdkj.dispatch.domain.po.DistributionSetting;
import com.sdkj.dispatch.domain.po.OrderFeeItem;
import com.sdkj.dispatch.domain.po.OrderInfo;

@Service
public class OrderFeeItemServiceImpl {

	@Autowired
	private OrderFeeItemMapper orderFeeItemMapper;

	@Autowired
	private DistributionSettingMapper distributionSettingMapper;

	public String caculateOrderFee(OrderInfo order, Integer driverType) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("orderId", order.getId());
		List<OrderFeeItem> feeItemList = orderFeeItemMapper.findOrderFeeItemList(param);
		Float driverFee =0f;
		if (feeItemList != null && feeItemList.size() > 0) {
			for (OrderFeeItem feeItem : feeItemList) {
				param.clear();
				param.put("city", order.getCityName());
				param.put("feeType", feeItem.getFeeType());
				param.put("driverType", driverType);
				param.put("vehicleType", order.getVehicleTypeId());
				List<DistributionSetting> feeDispatchSettingList = distributionSettingMapper
						.findDistributionSettingList(param);
				if (feeDispatchSettingList != null && feeDispatchSettingList.size() > 0) {
					Float distributeTotalFee = 0f;
					DistributionSetting distributionSetting = feeDispatchSettingList.get(0);
					Float clientRefereeRealAmount = distributionSetting.getClientRefereeAmount()
							* feeItem.getFeeAmount();
					BigDecimal clientRefereeBg = new BigDecimal(clientRefereeRealAmount).setScale(2, RoundingMode.UP);
					feeItem.setClientRefereeFee(clientRefereeBg.floatValue());
					distributeTotalFee += feeItem.getClientRefereeFee();

					Float driverRefereeRealAmount = distributionSetting.getDriverRefereeAmount()
							* feeItem.getFeeAmount();
					BigDecimal driverRefereeBg = new BigDecimal(driverRefereeRealAmount).setScale(2, RoundingMode.UP);
					feeItem.setDriverRefereeFee(driverRefereeBg.floatValue());
					distributeTotalFee += feeItem.getDriverRefereeFee();

					Float platformRealAmount = distributionSetting.getPlatformAmount() * feeItem.getFeeAmount();
					BigDecimal platformBg = new BigDecimal(platformRealAmount).setScale(2, RoundingMode.UP);
					feeItem.setPlatFormFee(platformBg.floatValue());
					distributeTotalFee += feeItem.getPlatFormFee();
					driverFee += feeItem.getFeeAmount() - distributeTotalFee;
				}
			}
		}
		BigDecimal driverTotalFeeBg = new BigDecimal(driverFee).setScale(2, RoundingMode.UP);
		return driverTotalFeeBg.floatValue()+"";
	}
}
